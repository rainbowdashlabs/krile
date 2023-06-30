create table krile.repository
(
    id  serial not null
        constraint repository_pk
            primary key,
    url text   not null
);

create unique index repository_url_uindex
    on krile.repository (url);

create table krile.guild_repository
(
    guild_id      bigint  not null,
    repository_id integer not null
        constraint guild_repository_repository_id_fk
            references krile.repository
            on delete cascade
);

create unique index guild_repository_guild_id_repository_id_uindex
    on krile.guild_repository (guild_id, repository_id);

create table krile.tag
(
    repository_id integer not null
        constraint tag_repository_id_fk
            references krile.repository
            on delete cascade,
    id            serial,
    tag_id        text    not null,
    tag           text    not null
);

create unique index tag_id_uindex
    on krile.tag (id);

create unique index tag_repository_id_tag_id_uindex
    on krile.tag (repository_id, tag_id);

create unique index tag_repository_id_tag_uindex
    on krile.tag (repository_id, tag);


create table krile.tag_alias
(
    tag_id integer not null
        constraint tag_alias_tag_id_fk
            references krile.tag (id)
            on delete cascade,
    alias  text    not null
);

create unique Index tag_alias_tag_id_alias_uindex
    on krile.tag_alias (tag_id, alias);

CREATE VIEW krile.repo_tags AS
with tags as (SELECT tag.repository_id, tag.id, tag
              FROM krile.tag
                       LEFT JOIN krile.repository r on r.id = tag.repository_id),
     aliase as (SELECT t.repository_id, a.tag_id, alias
                FROM krile.tag_alias a
                         LEFT JOIN krile.tag t ON t.id = a.tag_id
                         LEFT JOIN krile.repository r on t.repository_id = r.id
                WHERE alias NOT IN (SELECT tag from tags where tags.repository_id = t.repository_id))
SELECT repository_id, id, tag
FROM tags
UNION
SELECT repository_id, tag_id, alias as tag
FROM aliase;

create table krile.category
(
    id       serial not null,
    category text   not null
);

create unique index category_lower_category_uindex
    on krile.category (lower(category));

create unique index category_id_uindex
    on krile.category (id);

create table krile.tag_category
(
    tag_id      integer not null
        constraint tag_category_tag_id_fk
            references krile.tag (id),
    category_id integer not null
        constraint tag_category_category_id_fk
            references krile.category (id)
            on delete cascade
);

create unique index tag_category_tag_id_category_id_uindex
    on krile.tag_category (tag_id, category_id);






