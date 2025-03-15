CREATE TABLE my_schema.ast_env_variables
(
    ast_environment_id BIGINT       NOT NULL,
    variable_key       VARCHAR(255) NOT NULL,
    variable_value     VARCHAR,
    PRIMARY KEY (ast_environment_id, variable_key),
    FOREIGN KEY (ast_environment_id) REFERENCES my_schema.ast_environment (id) ON DELETE CASCADE
);
