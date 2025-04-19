-- Insert users
INSERT INTO my_schema.ast_user (id, email, created_on, active)
VALUES (1, 'dario.mauri9@gmail.com', '2024-02-23T10:00:00Z', true);

-- folders
INSERT INTO my_schema.ast_request_folder (id, name, created_on, user_id)
VALUES (1, 'folder 1', '2025-04-16 12:00:00', 1);

-- requests
INSERT INTO my_schema.ast_request (id, user_id, name, created_on, modified_on, uri, method, request_body, num_requests,
                                   thread_pool_size, stop_on_error, request_type, active, folder_id)
VALUES (1, 1, 'Request 1', '2024-02-23T12:00:00Z', '2024-02-23T12:00:00Z', 'https://www.google.{domain}/', 'GET',
        '{ "key": "{{bodyValue}}" }', 1, 1, true, 'REQUEST', true, 1),
       (2, 1, 'Request 2', '2024-02-23T13:00:00Z', '2024-02-23T13:00:00Z', 'https://example.com/api/test2', 'GET',
        NULL, 50, 5, true, 'REQUEST', true, null),
       (3, 1, 'Request 3', '2024-02-24T13:00:00Z', '2024-02-24T13:00:00Z', 'https://example.com/api/test3', 'GET',
        NULL, 50, 5, true, 'REQUEST', true, 1),
       (4, 1, 'Pre-request 1', '2024-02-23T12:00:00Z', '2024-02-23T12:00:00Z', 'https://www.example.com/', 'GET',
        '{ "key": "body pre-request 1" }', 1, 1, true, 'PRE_REQUEST', true, null),
       (5, 1, 'AuthzToken', '2024-02-23T12:00:00Z', '2024-02-23T12:00:00Z',
        'http://localhost:8099/v1/authz-token?env={{env}}', 'GET', '{ "key": "body AuthzToken" }', 1, 1, true,
        'PRE_REQUEST', true, null),
       (6, 1, 'Pre-request 3', '2024-02-23T13:00:00Z', '2024-02-23T13:00:00Z', 'https://example.com/api/test2', 'GET',
        NULL, 1, 1, true, 'PRE_REQUEST', false, null);

-- Insert headers for ast_request 1
INSERT INTO my_schema.ast_request_headers (ast_request_id, header_key, header_value, created_on)
VALUES (1, 'Content-Type', 'application/json', '2025-03-02 12:00:00'),
       (1, 'Authorization', 'Bearer {{AuthzToken}}', '2025-03-02 13:00:00');

-- Insert URI variables for ast_request 1
INSERT INTO my_schema.ast_request_uri_variables (ast_request_id, variable_key, variable_value, created_on)
VALUES (1, 'domain', 'com', '2025-03-02 12:00:00'),
       (1, 'orderId', '456', '2025-03-02 13:00:00');

-- Insert query parameters for ast_request 1
INSERT INTO my_schema.ast_request_query_params (ast_request_id, param_key, param_value, created_on)
VALUES (1, 'limit', '100', '2025-03-02 12:00:00'),
       (1, 'sort', 'desc', '2025-03-02 13:00:00');

-- Insert headers for ast_request 2
INSERT INTO my_schema.ast_request_headers (ast_request_id, header_key, header_value, created_on)
VALUES (2, 'Accept', 'application/xml', '2025-03-02 12:00:00'),
       (2, 'Cache-Control', 'no-cache', '2025-03-02 13:00:00');

-- Insert URI variables for ast_request 2
INSERT INTO my_schema.ast_request_uri_variables (ast_request_id, variable_key, variable_value, created_on)
VALUES (2, 'productId', '789', '2025-03-02 12:00:00');

-- Insert query parameters for ast_request 2
INSERT INTO my_schema.ast_request_query_params (ast_request_id, param_key, param_value, created_on)
VALUES (2, 'page', '2', '2025-03-02 12:00:00'),
       (2, 'size', '20', '2025-03-02 13:00:00');

-- Insert URI variables for ast_request (pre-request) 3
INSERT INTO my_schema.ast_request_uri_variables (ast_request_id, variable_key, variable_value, created_on)
VALUES (3, 'pre-request 1 uriVar', '789', '2025-03-02 12:00:00');

-- Insert query parameters for ast_request (pre-request) 3
INSERT INTO my_schema.ast_request_query_params (ast_request_id, param_key, param_value, created_on)
VALUES (3, 'pre-request 1 queryParam', '2', '2025-03-02 12:00:00');

-- Insert headers for ast_request (pre-request) 4
INSERT INTO my_schema.ast_request_headers (ast_request_id, header_key, header_value, created_on)
VALUES (4, 'Accept', 'application/json', '2025-03-02 12:00:00');

-- Insert URI variables for ast_request (pre-request) 4
INSERT INTO my_schema.ast_request_uri_variables (ast_request_id, variable_key, variable_value, created_on)
VALUES (4, 'pre-request 2 uriVar', '789', '2025-03-02 12:00:00');

-- Insert query parameters for ast_request (pre-request) 4
INSERT INTO my_schema.ast_request_query_params (ast_request_id, param_key, param_value, created_on)
VALUES (4, 'pre-request 2 queryParam', '2', '2025-03-02 12:00:00');

-- ast_environment
INSERT INTO my_schema.ast_environment (id, name, created_on, user_id)
VALUES (1, 'LOCAL', '2025-03-02 12:00:00', 1),
       (2, 'INT', '2025-03-02 14:00:00', 1),
       (3, 'QA', '2025-03-02 15:30:00', 1);

-- Insert environment variables for ast_environment 1
INSERT INTO my_schema.ast_env_variables (ast_environment_id, variable_key, variable_value, created_on)
VALUES (1, 'env', 'LOCAL', '2025-03-02 12:00:00'),
       (1, 'token', 'eyJhbGci...', '2025-03-02 13:00:00'),
       (1, 'bodyValue', 'I am the body', '2025-03-02 14:00:00'),
       (1, 'AuthzToken', '<not assigned yet>', '2025-03-02 15:00:00');
-- Insert environment variables for ast_environment 2
INSERT INTO my_schema.ast_env_variables (ast_environment_id, variable_key, variable_value, created_on)
VALUES (2, 'var1', 'int value 1', '2025-03-02 12:00:00'),
       (2, 'var2', 'int value 2', '2025-03-02 13:00:00');
-- Insert environment variables for ast_environment 3
INSERT INTO my_schema.ast_env_variables (ast_environment_id, variable_key, variable_value, created_on)
VALUES (3, 'var1', 'QA value 1', '2025-03-02 12:00:00'),
       (3, 'var2', 'QA value 2', '2025-03-02 13:00:00');

-- Adjust the auto-increment value to avoid PK constraint violation
ALTER TABLE my_schema.ast_user
    ALTER COLUMN id RESTART WITH 3;
ALTER TABLE my_schema.ast_request
    ALTER COLUMN id RESTART WITH 8;
ALTER TABLE my_schema.ast_environment
    ALTER COLUMN id RESTART WITH 4;
ALTER TABLE my_schema.ast_request_folder
    ALTER COLUMN id RESTART WITH 2;
