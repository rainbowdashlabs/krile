CREATE TABLE krile.repository (
    id         SERIAL
        CONSTRAINT repository_pk
            PRIMARY KEY,
    url        TEXT NOT NULL,
    platform   TEXT NOT NULL,
    name       TEXT NOT NULL,
    repo       TEXT NOT NULL,
    path       TEXT,
    directory  TEXT,
    identifier TEXT GENERATED ALWAYS AS (
                   CASE
                       WHEN path IS NOT NULL THEN
                           platform || ':' || name || '/' || repo || '/' || path
                                             ELSE platform || ':' || name || '/' || repo
                   END) STORED
);

CREATE UNIQUE INDEX repository_platform_name_repo_path_uindex
    ON krile.repository (platform, name, repo, coalesce(path, ''::TEXT));

CREATE TABLE krile.guild_repository (
    guild_id      BIGINT  NOT NULL,
    repository_id INTEGER NOT NULL
        CONSTRAINT guild_repository_repository_id_fk
            REFERENCES krile.repository
            ON DELETE CASCADE,
    priority      INTEGER DEFAULT 1 NOTNULL
);

CREATE UNIQUE INDEX guild_repository_guild_id_repository_id_uindex
    ON krile.guild_repository (guild_id, repository_id);

CREATE TABLE krile.tag (
    repository_id INTEGER NOT NULL
        CONSTRAINT tag_repository_id_fk
            REFERENCES krile.repository
            ON DELETE CASCADE,
    id            SERIAL,
    tag_id        TEXT    NOT NULL,
    tag           TEXT    NOT NULL,
    content       TEXT    NOT NULL
);

CREATE UNIQUE INDEX tag_id_uindex
    ON krile.tag (id);

CREATE UNIQUE INDEX tag_repository_id_tag_id_uindex
    ON krile.tag (repository_id, tag_id);

CREATE UNIQUE INDEX tag_repository_id_tag_uindex
    ON krile.tag (repository_id, tag);


CREATE TABLE krile.tag_alias (
    tag_id INTEGER NOT NULL
        CONSTRAINT tag_alias_tag_id_fk
            REFERENCES krile.tag (id)
            ON DELETE CASCADE,
    alias  TEXT    NOT NULL
);

CREATE UNIQUE INDEX tag_alias_tag_id_alias_uindex
    ON krile.tag_alias (tag_id, alias);

CREATE VIEW krile.repo_tags AS
    SELECT
        rank() OVER (PARTITION BY tag ORDER BY prio) AS global_prio,
        repository_id,
        id,
        tag,
        prio
    FROM
        (
            SELECT
                repository_id,
                tag.id,
                tag,
                1 AS prio
            FROM
                krile.tag
            UNION
            SELECT
                repository_id,
                a.tag_id,
                alias,
                2 AS prio
            FROM
                krile.tag_alias a
                    LEFT JOIN krile.tag t
                    ON a.tag_id = t.id
            ORDER BY prio
        ) tags;

CREATE TABLE krile.category (
    id       SERIAL NOT NULL,
    category TEXT   NOT NULL
);

CREATE UNIQUE INDEX category_lower_category_uindex
    ON krile.category (lower(category));

CREATE UNIQUE INDEX category_id_uindex
    ON krile.category (id);

CREATE TABLE krile.tag_category (
    tag_id      INTEGER NOT NULL
        CONSTRAINT tag_category_tag_id_fk
            REFERENCES krile.tag (id)
            ON DELETE CASCADE,
    category_id INTEGER NOT NULL
        CONSTRAINT tag_category_category_id_fk
            REFERENCES krile.category (id)
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX tag_category_tag_id_category_id_uindex
    ON krile.tag_category (tag_id, category_id);

CREATE TABLE krile.author (
    id   SERIAL NOT NULL
        CONSTRAINT author_pk
            PRIMARY KEY,
    name TEXT   NOT NULL,
    mail TEXT   NOT NULL
);

CREATE UNIQUE INDEX author_name_mail_uindex
    ON krile.author (name, mail);

CREATE TABLE krile.tag_author (
    tag_id    INTEGER
        CONSTRAINT tag_author_tag_id_fk
            REFERENCES krile.tag (id)
            ON DELETE CASCADE,
    author_id INTEGER
        CONSTRAINT auth
            REFERENCES krile.author
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX tag_author_tag_id_author_id_uindex
    ON krile.tag_author (tag_id, author_id);

CREATE TABLE krile.repository_meta (
    repository_id INTEGER            NOT NULL
        CONSTRAINT repository_meta_pk
            PRIMARY KEY
        CONSTRAINT repository_meta_repository_id_fk
            REFERENCES krile.repository
            ON DELETE CASCADE,
    name          TEXT,
    description   TEXT,
    public_repo   BOOL DEFAULT FALSE NOT NULL,
    language      TEXT,
    public        BOOLEAN GENERATED ALWAYS AS ( public_repo AND description != '' AND language != '' ) STORED
);

CREATE TABLE krile.repository_category (
    repository_id INTEGER NOT NULL
        CONSTRAINT repository_category_repository_id_fk
            REFERENCES krile.repository
            ON DELETE CASCADE,
    category_id   INTEGER NOT NULL
        CONSTRAINT repository_category_category_id_fk
            REFERENCES krile.category (id)
            ON DELETE CASCADE,
    CONSTRAINT repository_category_pk
        PRIMARY KEY (repository_id, category_id)
);

CREATE TABLE krile.repository_data (
    repository_id INTEGER   NOT NULL
        CONSTRAINT repository_data_pk
            PRIMARY KEY
        CONSTRAINT repository_data_repository_id_fk
            REFERENCES krile.repository
            ON DELETE CASCADE,
    updated       TIMESTAMP NOT NULL,
    checked       TIMESTAMP NOT NULL,
    commit        TEXT      NOT NULL,
    branch        TEXT      NOT NULL
);

CREATE TABLE krile.tag_meta (
    tag_id      INTEGER                 NOT NULL
        CONSTRAINT tag_meta_pk
            PRIMARY KEY
        CONSTRAINT tag_meta_tag_id_fk
            REFERENCES krile.tag (id)
            ON DELETE CASCADE,
    file_name   TEXT                    NOT NULL,
    image       TEXT,
    created     TIMESTAMP DEFAULT now() NOT NULL,
    created_by  INTEGER                 NOT NULL,
    modified    TIMESTAMP DEFAULT now() NOT NULL,
    modified_by INTEGER                 NOT NULL
);

CREATE TABLE krile.tag_stat (
    guild_id BIGINT            NOT NULL,
    tag_id   INTEGER           NOT NULL
        CONSTRAINT tag_stat_tag_id_fk
            REFERENCES krile.tag (id),
    views    INTEGER DEFAULT 1 NOT NULL
);

CREATE UNIQUE INDEX tag_stat_guild_id_tag_id_uindex
    ON krile.tag_stat (guild_id, tag_id);

CREATE VIEW krile.repo_stats AS
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
        authors
            AS (
            SELECT
                repository_id,
                count(1)
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
        views,
        tags
    FROM
        tag_count c
            LEFT JOIN tag_views v
            ON c.repository_id = v.repository_id
            LEFT JOIN authors a
            ON c.repository_id = a.repository_id;
