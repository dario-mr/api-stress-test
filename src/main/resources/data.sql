-- Insert users
INSERT INTO my_schema.ast_user (id, email, created_on, active)
VALUES (1, 'dario.mauri9@gmail.com', '2024-02-23T10:00:00Z', true),
       (2, 'user2@example.com', '2024-02-23T11:00:00Z', false);

-- Insert ast_requests
INSERT INTO my_schema.ast_request (id, user_id, name, created_on, modified_on, uri, method, request_body, num_requests,
                                   thread_pool_size, stop_on_error)
VALUES (1, 1, 'Request 1', '2024-02-23T12:00:00Z', '2024-02-23T12:00:00Z', 'https://example.com/api/test1/{userId}', 'POST',
        '{"key": "value"}', 100, 10, false),
       (2, 1, 'Request 2', '2024-02-23T13:00:00Z', '2024-02-23T13:00:00Z', 'https://example.com/api/test2', 'GET',
        NULL, 50, 5, true);

-- Insert headers for ast_request 1
INSERT INTO my_schema.ast_request_headers (ast_request_id, header_key, header_value, created_on)
VALUES (1, 'Content-Type', 'application/json', '2025-03-02 12:00:00'),
       (1, 'Authorization', 'Bearer sampletoken', '2025-03-02 13:00:00');

-- Insert URI variables for ast_request 1
INSERT INTO my_schema.ast_request_uri_variables (ast_request_id, variable_key, variable_value, created_on)
VALUES (1, 'userId', '123', '2025-03-02 12:00:00'),
       (1, 'orderId', '456', '2025-03-02 13:00:00');

-- Insert query parameters for ast_request 1
INSERT INTO my_schema.ast_request_query_params (ast_request_id, param_key, param_value, created_on)
VALUES (1, 'limit', '10', '2025-03-02 12:00:00'),
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

-- ast_environment
INSERT INTO my_schema.ast_environment (id, name, created_on, user_id)
VALUES (1, 'LOCAL', '2025-03-02 12:00:00', 1),
       (2, 'INT', '2025-03-02 14:00:00', 1),
       (3, 'QA', '2025-03-02 15:30:00', 1);

-- Insert environment variables for ast_environment 1
INSERT INTO my_schema.ast_env_variables (ast_environment_id, variable_key, variable_value, created_on)
VALUES (1, 'var1', 'local value 1', '2025-03-02 12:00:00'),
       (1, 'var2', 'local value 2', '2025-03-02 13:00:00');
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
    ALTER COLUMN id RESTART WITH 4;
ALTER TABLE my_schema.ast_environment
    ALTER COLUMN id RESTART WITH 4;
