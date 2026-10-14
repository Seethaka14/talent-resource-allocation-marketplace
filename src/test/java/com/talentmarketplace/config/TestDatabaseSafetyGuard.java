package com.talentmarketplace.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@TestConfiguration
public class TestDatabaseSafetyGuard {

    @EventListener(ContextRefreshedEvent.class)
    public void verifyTestDatabase(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        if (!ctx.containsBean("dataSource")) {
            return;
        }

        DataSource ds = ctx.getBean(DataSource.class);
        try (Connection conn = ds.getConnection()) {
            String catalog = conn.getCatalog();
            if (catalog != null && catalog.equalsIgnoreCase("talent_marketplace")) {
                throw new IllegalStateException(
                        "CRITICAL SAFETY GUARD VIOLATION: Tests are connected to the live demo database 'talent_marketplace'! " +
                        "Destructive operations are strictly forbidden on the demo database. " +
                        "Configure tests to use 'talent_marketplace_test'."
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to verify test database safety guard", e);
        }
    }

    public static void assertSafeTestDatabase(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            String catalog = conn.getCatalog();
            if (catalog != null && catalog.equalsIgnoreCase("talent_marketplace")) {
                throw new IllegalStateException(
                        "CRITICAL SAFETY GUARD VIOLATION: Test cleanup attempted on live demo database 'talent_marketplace'!"
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to verify test database safety guard", e);
        }
    }
}
