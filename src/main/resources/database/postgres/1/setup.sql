create table krile.repository
(
    id         serial
        constraint repository_pk
            primary key,
    url        text not null,
    identifier text not null
);

create unique index repository_identifier_uindex
    on krile.repository (identifier);


create table krile.guild_repository
(
    guild_id      bigint  not null,
    repository_id integer not null
        constraint guild_repository_repository_id_fk
            references krile.repository
            on delete cascade
);

alter table krile.guild_repository
    add prio integer default 1 not null;

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
    tag           text    not null,
    content       text    not null
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
SELECT tag.id, tag, 1 as prio
FROM krile.tag
UNION
SELECT a.tag_id, alias, 2 as prio
FROM krile.tag_alias a
ORDER BY prio;

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

create table krile.author
(
    id   serial not null
        constraint author_pk
            primary key,
    name text   not null,
    mail text   not null
);

create unique index author_name_mail_uindex
    on krile.author (name, mail);

create table krile.tag_author
(
    tag_id    integer
        constraint tag_author_tag_id_fk
            references krile.tag (id),
    author_id integer
        constraint auth
            references krile.author
);

create unique index tag_author_tag_id_author_id_uindex
    on krile.tag_author (tag_id, author_id);

create table krile.repository_meta
(
    repository_id integer            not null
        constraint repository_meta_pk
            primary key
        constraint repository_meta_repository_id_fk
            references krile.repository,
    name          text               not null,
    description   text,
    public_repo   bool default false not null,
    language      text,
    public        boolean GENERATED ALWAYS AS ( public_repo and description != '' and language != '' ) STORED
);

create table krile.repository_category
(
    repository_id integer not null
        constraint repository_category_repository_id_fk
            references krile.repository,
    category_id   integer not null
        constraint repository_category_category_id_fk
            references krile.category (id),
    constraint repository_category_pk
        primary key (repository_id, category_id)
);

create table krile.repository_data
(
    repository_id integer not null
        constraint repository_data_pk
            primary key
        constraint repository_data_repository_id_fk
            references krile.repository,
    updated       integer not null,
    checked       integer not null,
    commit        integer not null
);

create table krile.tag_meta
(
    tag_id      integer                 not null
        constraint tag_meta_pk
            primary key
        constraint tag_meta_tag_id_fk
            references krile.tag (id),
    image       text                    not null,
    created     timestamp default now() not null,
    created_by  integer                 not null,
    modified    timestamp default now() not null,
    modified_by integer                 not null
);

