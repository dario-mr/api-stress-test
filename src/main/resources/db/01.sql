CREATE TABLE my_schema.ast_user
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_on TIMESTAMPTZ  NOT NULL,
    active     BOOLEAN      NOT NULL
);

CREATE TABLE my_schema.ast_request
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(500)  NOT NULL,
    created_on       TIMESTAMPTZ   NOT NULL,
    modified_on      TIMESTAMPTZ   NOT NULL,
    uri              VARCHAR(2048) NOT NULL,
    method           VARCHAR(10)   NOT NULL,
    request_type     VARCHAR(64)   NOT NULL,
    active           BOOLEAN       NOT NULL,
    request_body     TEXT,
    num_requests     INT           NOT NULL,
    thread_pool_size INT           NOT NULL,
    stop_on_error    BOOLEAN       NOT NULL,
    user_id          BIGINT        NOT NULL,
    folder_id        BIGINT,
    CONSTRAINT fk_ast_request_user FOREIGN KEY (user_id) REFERENCES my_schema.ast_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_ast_request_folder FOREIGN KEY (folder_id) REFERENCES my_schema.ast_request_folder (id) ON DELETE CASCADE
);

CREATE TABLE my_schema.ast_request_headers
(
    ast_request_id BIGINT       NOT NULL,
    header_key     VARCHAR(255) NOT NULL,
    header_value   VARCHAR(2048),
    created_on     TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (ast_request_id, header_key),
    FOREIGN KEY (ast_request_id) REFERENCES my_schema.ast_request (id) ON DELETE CASCADE
);

CREATE TABLE my_schema.ast_request_uri_variables
(
    ast_request_id BIGINT       NOT NULL,
    variable_key   VARCHAR(255) NOT NULL,
    variable_value TEXT,
    created_on     TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (ast_request_id, variable_key),
    FOREIGN KEY (ast_request_id) REFERENCES my_schema.ast_request (id) ON DELETE CASCADE
);

CREATE TABLE my_schema.ast_request_query_params
(
    ast_request_id BIGINT       NOT NULL,
    param_key      VARCHAR(255) NOT NULL,
    param_value    VARCHAR(2048),
    created_on     TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (ast_request_id, param_key),
    FOREIGN KEY (ast_request_id) REFERENCES my_schema.ast_request (id) ON DELETE CASCADE
);

CREATE TABLE my_schema.ast_environment
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_on TIMESTAMP    NOT NULL,
    user_id    BIGINT       NOT NULL,
    CONSTRAINT fk_ast_environment_user FOREIGN KEY (user_id) REFERENCES my_schema.ast_user (id) ON DELETE CASCADE
);

CREATE TABLE my_schema.ast_env_variables
(
    ast_environment_id BIGINT       NOT NULL,
    variable_key       VARCHAR(255) NOT NULL,
    variable_value     TEXT,
    created_on         TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (ast_environment_id, variable_key),
    FOREIGN KEY (ast_environment_id) REFERENCES my_schema.ast_environment (id) ON DELETE CASCADE
);

CREATE TABLE my_schema.oauth_token
(
    user_id       TEXT PRIMARY KEY,
    refresh_token TEXT,
    expires_at    TIMESTAMPTZ,
    last_updated  TIMESTAMPTZ,
    provider      VARCHAR(255) NOT NULL
);

CREATE TABLE my_schema.ast_request_folder
(
    id               BIGSERIAL PRIMARY KEY,
    name             TEXT        NOT NULL,
    created_on       TIMESTAMPTZ NOT NULL,
    user_id          BIGINT      NOT NULL,
    parent_folder_id BIGINT,
    CONSTRAINT fk_ast_request_folder_user FOREIGN KEY (user_id) REFERENCES my_schema.ast_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_ast_request_folder_folder FOREIGN KEY (parent_folder_id) REFERENCES my_schema.ast_request_folder (id) ON DELETE CASCADE
);
CREATE INDEX idx_ast_request_folder_parent_folder_id ON my_schema.ast_request_folder (parent_folder_id);
