CREATE OR REPLACE VIEW krile.repo_stats AS
    WITH
        tag_views
            AS (
            SELECT
                repository_id,
                sum(views) AS views
            FROM
                krile.tag_stat s
                    LEFT JOIN krile.tag t
                    ON s.tag_id = t.id
            GROUP BY repository_id
        ),
        tag_count
            AS (
            SELECT
                repository_id,
                count(1) AS tags
            FROM
                krile.tag
            GROUP BY repository_id
        ),
        tag_authors
            AS (
            SELECT
                repository_id,
                count(1) as authors
            FROM
                (
                    SELECT DISTINCT
                        (repository_id, author_id),
                        repository_id,
                        author_id
                    FROM
                        krile.tag_author a
                            LEFT JOIN krile.tag t
                            ON a.tag_id = t.id
                ) a
            GROUP BY repository_id
        )
    SELECT
        c.repository_id AS id,
        coalesce(views,0) AS views,
        tags,
        authors
    FROM
        tag_count c
            LEFT JOIN tag_views v
            ON c.repository_id = v.repository_id
            LEFT JOIN tag_authors a
            ON c.repository_id = a.repository_id;
