-- Insert users
INSERT INTO my_schema.ast_user (id, email, created_on, active)
VALUES (1, 'dario.mauri9@gmail.com', '2024-02-23T10:00:00Z', true),
       (2, 'user2@example.com', '2024-02-23T11:00:00Z', false);

-- Insert ast_requests
INSERT INTO my_schema.ast_request (id, user_id, name, created_on, modified_on, uri, method, request_body, num_requests,
                                   thread_pool_size, stop_on_error)
VALUES (1, 1, 'Request 1', '2024-02-23T12:00:00Z', '2024-02-23T12:00:00Z', 'https://example.com/api/test1', 'POST',
        '{"key": "value"}', 100, 10, false),
       (2, 1, 'Request 2', '2024-02-23T13:00:00Z', '2024-02-23T13:00:00Z', 'https://example.com/api/test2', 'GET',
        NULL, 50, 5, true),
       (3, 2, 'Request 3', '2024-02-23T14:00:00Z', '2024-02-23T14:00:00Z', 'https://example.com/api/test3', 'PUT',
        '{"update": "data"}', 200, 20, false);

-- Insert headers for ast_request 1
INSERT INTO my_schema.ast_request_headers (ast_request_id, header_key, header_value)
VALUES (1, 'Content-Type', 'application/json'),
       (1, 'Authorization', 'Bearer sampletoken');

-- Insert URI variables for ast_request 1
INSERT INTO my_schema.ast_request_uri_variables (ast_request_id, variable_key, variable_value)
VALUES (1, 'userId', '123'),
       (1, 'orderId', '456');

-- Insert query parameters for ast_request 1
INSERT INTO my_schema.ast_request_query_params (ast_request_id, param_key, param_value)
VALUES (1, 'limit', '10'),
       (1, 'sort', 'desc');

-- Insert headers for ast_request 2
INSERT INTO my_schema.ast_request_headers (ast_request_id, header_key, header_value)
VALUES (2, 'Accept', 'application/xml'),
       (2, 'Cache-Control', 'no-cache');

-- Insert URI variables for ast_request 2
INSERT INTO my_schema.ast_request_uri_variables (ast_request_id, variable_key, variable_value)
VALUES (2, 'productId', '789');

-- Insert query parameters for ast_request 2
INSERT INTO my_schema.ast_request_query_params (ast_request_id, param_key, param_value)
VALUES (2, 'page', '2'),
       (2, 'size', '20');


-- Adjust the auto-increment value to avoid PK constraint violation
ALTER TABLE my_schema.ast_user ALTER COLUMN id RESTART WITH 3;
ALTER TABLE my_schema.ast_request ALTER COLUMN id RESTART WITH 4;
