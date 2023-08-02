CREATE OR REPLACE VIEW krile.repository_updates AS
with repository_ids
         AS (SELECT id, url, num
             FROM (SELECT id,
                          url,
                          row_number() over (PARTITION BY url ORDER BY path NULLS FIRST ) as num
                   FROM krile.repository) a
             where num = 1),
     last_updated as (SELECT url, min(checked) as checked
                      FROM krile.repository_data m
                               LEFT JOIN krile.repository r on r.id = m.repository_id
                      GROUP BY url)
SELECT r.url, checked
FROM last_updated m
         LEFT JOIN repository_ids r on r.url = m.url;


ALTER TABLE krile.repository
    DROP COLUMN identifier;

ALTER TABLE krile.repository
    ADD COLUMN identifier text GENERATED ALWAYS AS (
        CASE
            WHEN path IS NOT NULL THEN
                platform || ':' || name || '/' || repo || '//' || path
            ELSE platform || ':' || name || '/' || repo
            END) STORED
