create extension if not exists pgcrypto;

create type desired_path as enum ('DATA', 'IOS');
create type exercise_type as enum ('CLOZE', 'ANALYZE', 'TRIVIA', 'INFO');
create type exercise_type_enum as enum ('CLOZE', 'INFO', 'TRIVIA', 'ANALYZE');
create type language_type as enum ('python', 'web');



create table ludo_user
(
    id         uuid                     default gen_random_uuid() not null
        primary key,
    first_name text                                               not null,
    last_name  text                                               not null,
    pfp_src    text,
    email      text                                               not null,
    created_at timestamp with time zone default now()             not null,
    time_zone  text                     default 'UTC'::text       not null
);

create table course
(
    id      uuid default gen_random_uuid() not null
        primary key,
    title   text                           not null
        unique,
    img_src text
);

create table module
(
    id          uuid default gen_random_uuid() not null
        primary key,
    title       text                           not null,
    order_index integer                        not null
        constraint module_order_index_check
            check (order_index > 0),
    is_deleted  boolean                        not null,
    course_id   uuid                           not null
        references course
);



create table exercise
(
    id             uuid               default gen_random_uuid()           not null,
    version_number integer            default 1                           not null,
    exercise_type  exercise_type_enum default 'CLOZE'::exercise_type_enum not null,
    title          text                                                   not null,
    subtitle       text,
    prompt         text,
    is_deleted     boolean            default false                       not null,
    exercise_media text,
    primary key (id, version_number)
);

create table lesson
(
    id         uuid default gen_random_uuid() not null
        primary key,
    title      text                           not null,
    is_deleted boolean                        not null
);

create table module_lessons
(
    module_id   uuid    not null
        references module
            on delete cascade,
    lesson_id   uuid    not null
        constraint uq_lesson_single_module
            unique
        references lesson
            on delete restrict,
    order_index integer not null
        constraint module_lessons_order_index_check
            check (order_index > 0),
    primary key (module_id, order_index),
    unique (module_id, lesson_id)
);

create table lesson_completion
(
    id           uuid                                   not null
        primary key,
    user_id      uuid                                   not null
        references ludo_user,
    lesson_id    uuid                                   not null
        references lesson,
    course_id    uuid                                   not null
        references course,
    score        integer                  default 0     not null,
    accuracy     numeric(4, 2),
    completed_at timestamp with time zone default now() not null,
    is_deleted   boolean                                not null
);

create table lesson_exercises
(
    lesson_id        uuid    not null
        references lesson
            on delete cascade,
    exercise_id      uuid    not null,
    exercise_version integer not null,
    order_index      integer not null
        constraint lesson_exercises_order_index_check
            check (order_index > 0),
    primary key (lesson_id, order_index),
    unique (lesson_id, exercise_id, exercise_version),
    foreign key (exercise_id, exercise_version) references exercise
        on delete restrict
);

create table option_content
(
    id      uuid default gen_random_uuid() not null
        primary key,
    content text                           not null
        unique
);


create table course_progress
(
    user_id           uuid                                   not null
        references ludo_user,
    course_id         uuid                                   not null
        references course,
    current_lesson_id uuid                                   not null
        references lesson,
    created_at        timestamp with time zone default now() not null,
    updated_at        timestamp with time zone default now() not null,
    is_complete       boolean                                not null,
    primary key (user_id, course_id)
);

create table user_project
(
    id               uuid                     default gen_random_uuid()        not null
        primary key,
    name             text                     default 'Untitled Project'::text not null,
    user_id          uuid
        references ludo_user,
    project_language language_type                                             not null,
    created_at       timestamp with time zone default now(),
    updated_at       timestamp with time zone default now()
);

create table project_file
(
    id            uuid default gen_random_uuid() not null
        primary key,
    project_id    uuid
        references user_project,
    content_url   text default ''::text,
    file_path     text                           not null,
    file_language language_type                  not null,
    content_hash  text default ''::text          not null,
    constraint project_file_unique_path
        unique (project_id, file_path)
            deferrable initially deferred
);

create table user_daily_goal
(
    user_id    uuid not null,
    local_date date not null,
    primary key (user_id, local_date)
);

create table user_stats
(
    user_id uuid              not null
        primary key
        references ludo_user
            on delete cascade,
    coins   integer default 0 not null,
    streak  integer default 0 not null
);

create table user_streak
(
    user_id             uuid              not null
        primary key,
    current_streak_days integer default 0 not null,
    best_streak_days    integer default 0 not null,
    last_met_local_date date,
    last_met_goal_utc   timestamp with time zone
);

create table user_preferences
(
    user_id        uuid         not null
        primary key
        references ludo_user,
    has_experience boolean      not null,
    chosen_path    desired_path not null
);



create table exercise_option
(
    id               uuid default gen_random_uuid() not null
        primary key,
    exercise_id      uuid                           not null,
    exercise_version integer                        not null,
    option_id        uuid                           not null
        references option_content,
    answer_order     integer,
    unique (exercise_id, exercise_version, option_id),
    foreign key (exercise_id, exercise_version) references exercise
        on delete cascade
);

create table external_account
(
    id               uuid                     default gen_random_uuid() not null
        primary key,
    user_id          uuid                                               not null
        references ludo_user
            on delete cascade,
    provider         text                                               not null,
    provider_user_id text                                               not null,
    created_at       timestamp with time zone default now()             not null,
    unique (provider, provider_user_id)
);

create table exercise_attempt
(
    id               uuid    not null
        primary key,
    exercise_id      uuid    not null,
    exercise_version integer not null,
    user_id          uuid    not null
        references ludo_user,
    foreign key (exercise_id, exercise_version) references exercise
        on delete restrict
);

create table attempt_option
(
    attempt_id         uuid not null
        references exercise_attempt
            on delete cascade,
    exercise_option_id uuid not null
        references exercise_option
            on delete restrict,
    primary key (attempt_id, exercise_option_id)
);
