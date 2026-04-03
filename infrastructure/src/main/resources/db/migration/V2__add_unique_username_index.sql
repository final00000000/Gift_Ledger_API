UPDATE users u
JOIN (
    SELECT id
    FROM (
        SELECT
            id,
            username,
            ROW_NUMBER() OVER (PARTITION BY username ORDER BY created_at, id) AS rn
        FROM users
    ) ranked
    WHERE ranked.rn > 1
) duplicates ON duplicates.id = u.id
SET u.username = CONCAT(u.username, '_', LOWER(LEFT(REPLACE(u.id, '-', ''), 6)));

CREATE UNIQUE INDEX ux_users_username ON users (username);
