-- V3: Create demand_code_sequences table for concurrency-safe unique demand code generation
CREATE TABLE IF NOT EXISTS `demand_code_sequences` (
    `sequence_name` VARCHAR(30) NOT NULL,
    `current_value` BIGINT NOT NULL,
    PRIMARY KEY (`sequence_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `demand_code_sequences` (`sequence_name`, `current_value`)
VALUES ('DEM-2026', 0)
ON DUPLICATE KEY UPDATE `sequence_name` = `sequence_name`;
