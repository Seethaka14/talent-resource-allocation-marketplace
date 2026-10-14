package com.talentmarketplace.migration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.EmployeeUnavailability;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.ResourceAllocation;
import com.talentmarketplace.entity.Skill;

class FlywayMigrationIntegrationTest {

    private static final String DB_USER = "root";
    private static final String DB_PASS = "password123";
    private static final String CLEAN_DB_NAME = "talent_migration_test";
    private static final String CLEAN_JDBC_URL = "jdbc:mysql://localhost:3306/" + CLEAN_DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String ADOPT_DB_NAME = "talent_existing_adoption_test";
    private static final String ADOPT_JDBC_URL = "jdbc:mysql://localhost:3306/" + ADOPT_DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true";

    @BeforeEach
    void ensureSafetyAndResetDatabases() throws Exception {
        // Assert we never ever target talent_marketplace
        assertThat(CLEAN_JDBC_URL).doesNotContain("talent_marketplace?");
        assertThat(ADOPT_JDBC_URL).doesNotContain("talent_marketplace?");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true", DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP DATABASE IF EXISTS " + CLEAN_DB_NAME);
            stmt.execute("CREATE DATABASE " + CLEAN_DB_NAME);

            stmt.execute("DROP DATABASE IF EXISTS " + ADOPT_DB_NAME);
            stmt.execute("CREATE DATABASE " + ADOPT_DB_NAME);
        }
    }

    @Test
    @DisplayName("Clean Database: Flyway applies V1 migration cleanly and matches Hibernate schema validation")
    void cleanDatabase_FlywayMigrate_AndHibernateValidationPasses() throws Exception {
        // 1. Run Flyway migration on clean database
        Flyway flyway = Flyway.configure()
                .dataSource(CLEAN_JDBC_URL, DB_USER, DB_PASS)
                .locations("classpath:db/migration")
                .cleanDisabled(true)
                .load();

        MigrateResult result = flyway.migrate();

        assertThat(result.success).isTrue();
        assertThat(result.migrationsExecuted).isEqualTo(1);
        assertThat(result.targetSchemaVersion).isEqualTo("1");

        // Verify flyway_schema_history
        MigrationInfo current = flyway.info().current();
        assertThat(current).isNotNull();
        assertThat(current.getVersion().getVersion()).isEqualTo("1");
        assertThat(current.getDescription()).isEqualTo("initial schema");
        assertThat(current.getChecksum()).isNotNull();

        // 2. Verify all 7 domain tables and schema_history exist in MySQL
        Set<String> tables = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(CLEAN_JDBC_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
            while (rs.next()) {
                tables.add(rs.getString(1).toLowerCase());
            }
        }

        assertThat(tables).contains(
                "flyway_schema_history",
                "employees",
                "skills",
                "employee_skills",
                "employee_unavailabilities",
                "project_demands",
                "demand_skills",
                "resource_allocations"
        );

        // 3. Verify Hibernate schema validation against the Flyway-generated schema
        Map<String, Object> settings = new HashMap<>();
        settings.put("jakarta.persistence.jdbc.url", CLEAN_JDBC_URL);
        settings.put("jakarta.persistence.jdbc.user", DB_USER);
        settings.put("jakarta.persistence.jdbc.password", DB_PASS);
        settings.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        settings.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        settings.put("hibernate.hbm2ddl.auto", "validate");

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        try {
            MetadataSources metadataSources = new MetadataSources(registry);
            metadataSources.addAnnotatedClass(Employee.class);
            metadataSources.addAnnotatedClass(Skill.class);
            metadataSources.addAnnotatedClass(EmployeeSkill.class);
            metadataSources.addAnnotatedClass(EmployeeUnavailability.class);
            metadataSources.addAnnotatedClass(ProjectDemand.class);
            metadataSources.addAnnotatedClass(DemandSkill.class);
            metadataSources.addAnnotatedClass(ResourceAllocation.class);

            // buildSessionFactory() runs Hibernate's schema validator on all entities
            try (var sessionFactory = metadataSources.buildMetadata().buildSessionFactory()) {
                assertThat(sessionFactory).isNotNull();
                assertThat(sessionFactory.isOpen()).isTrue();
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    @DisplayName("Existing Database Adoption: Non-empty schema requires explicit baseline before migration")
    void existingDatabase_RequiresBaselineBeforeMigrating() throws Exception {
        // 1. Simulate an existing database created by Hibernate or previous scripts
        try (Connection conn = DriverManager.getConnection(ADOPT_JDBC_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE existing_table (id BIGINT PRIMARY KEY, name VARCHAR(50))");
        }

        // 2. Attempting to run flyway.migrate() on a non-empty schema without baseline fails safely
        Flyway flywayWithoutBaseline = Flyway.configure()
                .dataSource(ADOPT_JDBC_URL, DB_USER, DB_PASS)
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .load();

        assertThatThrownBy(flywayWithoutBaseline::migrate)
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Found non-empty schema(s)");

        // 3. Adopting via baseline establishes version 1 history without altering existing tables
        Flyway flywayWithBaseline = Flyway.configure()
                .dataSource(ADOPT_JDBC_URL, DB_USER, DB_PASS)
                .locations("classpath:db/migration")
                .baselineVersion("1")
                .baselineDescription("Adopt existing schema")
                .load();

        flywayWithBaseline.baseline();

        MigrationInfo current = flywayWithBaseline.info().current();
        assertThat(current).isNotNull();
        assertThat(current.getVersion().getVersion()).isEqualTo("1");
        assertThat(current.getDescription()).isEqualTo("Adopt existing schema");
    }
}
