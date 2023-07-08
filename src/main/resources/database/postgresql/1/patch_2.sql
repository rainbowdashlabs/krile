alter table krile.tag
    alter column content type text[] using content::text[];

alter table krile.repository_data
    alter column updated set default (now() AT TIME ZONE 'UTC'::text);

alter table krile.repository_data
    alter column checked set default (now() AT TIME ZONE 'UTC'::text);

alter table krile.tag_stat
    drop constraint tag_stat_tag_id_fk;

alter table krile.tag_stat
    add constraint tag_stat_tag_id_fk
        foreign key (tag_id) references krile.tag (id)
            on delete cascade;

