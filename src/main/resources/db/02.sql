CREATE TABLE my_schema.ast_environment
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_on TIMESTAMP    NOT NULL,
    user_id    BIGINT       NOT NULL,
    CONSTRAINT fk_ast_environment_user FOREIGN KEY (user_id) REFERENCES my_schema.ast_user (id) ON DELETE CASCADE
);
