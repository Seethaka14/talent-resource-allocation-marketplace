package com.talentmarketplace.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestDatabaseConfigurationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Verify that test datasource is connected to talent_marketplace_test and NOT talent_marketplace")
    void testActiveDatasourceIsIsolatedTestDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            String catalog = conn.getCatalog();
            assertThat(catalog).isEqualToIgnoringCase("talent_marketplace_test");
            assertThat(catalog).isNotEqualToIgnoringCase("talent_marketplace");
        }
    }
}
