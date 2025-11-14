CREATE EXTENSION if NOT EXISTS pgcrypto;

CREATE TYPE desired_path AS ENUM('DATA', 'IOS');

CREATE TYPE exercise_type AS ENUM('CLOZE', 'ANALYZE', 'TRIVIA', 'INFO');

CREATE TYPE exercise_type_enum AS ENUM('CLOZE', 'INFO', 'TRIVIA', 'ANALYZE');

CREATE TYPE language_type AS ENUM('python', 'web');

CREATE TABLE ludo_user (
       id uuid DEFAULT gen_random_uuid () NOT NULL PRIMARY KEY,
       first_name TEXT NOT NULL,
       last_name TEXT NOT NULL,
       pfp_src TEXT,
       email TEXT NOT NULL,
       created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
       time_zone TEXT DEFAULT 'UTC'::TEXT NOT NULL
);

CREATE TABLE course (
        id uuid DEFAULT gen_random_uuid () NOT NULL PRIMARY KEY,
        title TEXT NOT NULL UNIQUE,
        img_src TEXT
);

CREATE TABLE module (
        id uuid DEFAULT gen_random_uuid () NOT NULL PRIMARY KEY,
        title TEXT NOT NULL,
        order_index INTEGER NOT NULL CONSTRAINT module_order_index_check CHECK (order_index > 0),
        is_deleted BOOLEAN NOT NULL,
        course_id uuid NOT NULL REFERENCES course
);

CREATE TABLE exercise (
      id uuid DEFAULT gen_random_uuid () NOT NULL,
      version_number INTEGER DEFAULT 1 NOT NULL,
      exercise_type exercise_type_enum DEFAULT 'CLOZE'::exercise_type_enum NOT NULL,
      title TEXT NOT NULL,
      subtitle TEXT,
      prompt TEXT,
      is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
      exercise_media TEXT,
      PRIMARY KEY (id, version_number)
);

CREATE TABLE lesson (
    id uuid DEFAULT gen_random_uuid () NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    is_deleted BOOLEAN NOT NULL
);

CREATE TABLE module_lessons (
    module_id uuid NOT NULL REFERENCES module ON DELETE CASCADE,
    lesson_id uuid NOT NULL CONSTRAINT uq_lesson_single_module UNIQUE REFERENCES lesson ON DELETE RESTRICT,
    order_index INTEGER NOT NULL CONSTRAINT module_lessons_order_index_check CHECK (order_index > 0),
    PRIMARY KEY (module_id, order_index),
    UNIQUE (module_id, lesson_id)
);

CREATE TABLE lesson_completion (
   id uuid NOT NULL PRIMARY KEY,
   user_id uuid NOT NULL REFERENCES ludo_user,
   lesson_id uuid NOT NULL REFERENCES lesson,
   course_id uuid NOT NULL REFERENCES course,
   score INTEGER DEFAULT 0 NOT NULL,
   accuracy NUMERIC(4, 2),
   completed_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
   is_deleted BOOLEAN NOT NULL
);

CREATE TABLE lesson_exercises (
  lesson_id uuid NOT NULL REFERENCES lesson ON DELETE CASCADE,
  exercise_id uuid NOT NULL,
  exercise_version INTEGER NOT NULL,
  order_index INTEGER NOT NULL CONSTRAINT lesson_exercises_order_index_check CHECK (order_index > 0),
  PRIMARY KEY (lesson_id, order_index),
  UNIQUE (lesson_id, exercise_id, exercise_version),
  FOREIGN key (exercise_id, exercise_version) REFERENCES exercise ON DELETE RESTRICT
);

CREATE TABLE option_content (
    id uuid DEFAULT gen_random_uuid () NOT NULL PRIMARY KEY,
    content TEXT NOT NULL UNIQUE
);

CREATE TABLE course_progress (
     user_id uuid NOT NULL REFERENCES ludo_user,
     course_id uuid NOT NULL REFERENCES course,
     current_lesson_id uuid NOT NULL REFERENCES lesson,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
     is_complete BOOLEAN NOT NULL,
     PRIMARY KEY (user_id, course_id)
);

CREATE TABLE user_project (
      id uuid DEFAULT gen_random_uuid () NOT NULL PRIMARY KEY,
      name TEXT DEFAULT 'Untitled Project'::TEXT NOT NULL,
      user_id uuid REFERENCES ludo_user,
      project_language language_type NOT NULL,
      created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
      updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
      request_hash uuid unique not null
);

CREATE TABLE project_file (
      id uuid DEFAULT gen_random_uuid () NOT NULL PRIMARY KEY,
      project_id uuid REFERENCES user_project,
      content_url TEXT DEFAULT ''::TEXT,
      file_path TEXT NOT NULL,
      file_language language_type NOT NULL,
      content_hash TEXT DEFAULT ''::TEXT NOT NULL,
      CONSTRAINT project_file_unique_path UNIQUE (project_id, file_path) DEFERRABLE INITIALLY deferred
);

CREATE TABLE user_daily_goal (
     user_id uuid NOT NULL,
     local_date date NOT NULL,
     PRIMARY KEY (user_id, local_date)
);

CREATE TABLE user_stats (
    user_id uuid NOT NULL PRIMARY KEY REFERENCES ludo_user ON DELETE CASCADE,
    coins INTEGER DEFAULT 0 NOT NULL,
    streak INTEGER DEFAULT 0 NOT NULL
);

CREATE TABLE user_streak (
     user_id uuid NOT NULL PRIMARY KEY,
     current_streak_days INTEGER DEFAULT 0 NOT NULL,
     best_streak_days INTEGER DEFAULT 0 NOT NULL,
     last_met_local_date date,
     last_met_goal_utc TIMESTAMP WITH TIME ZONE
);

CREATE TABLE user_preferences (
      user_id uuid NOT NULL PRIMARY KEY REFERENCES ludo_user,
      has_experience BOOLEAN NOT NULL,
      chosen_path desired_path NOT NULL
);

CREATE TABLE exercise_option (
     id uuid DEFAULT gen_random_uuid () NOT NULL PRIMARY KEY,
     exercise_id uuid NOT NULL,
     exercise_version INTEGER NOT NULL,
     option_id uuid NOT NULL REFERENCES option_content,
     answer_order INTEGER,
     UNIQUE (exercise_id, exercise_version, option_id),
     FOREIGN key (exercise_id, exercise_version) REFERENCES exercise ON DELETE CASCADE
);

CREATE TABLE external_account (
      id uuid DEFAULT gen_random_uuid () NOT NULL PRIMARY KEY,
      user_id uuid NOT NULL REFERENCES ludo_user ON DELETE CASCADE,
      provider TEXT NOT NULL,
      provider_user_id TEXT NOT NULL,
      created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
      UNIQUE (provider, provider_user_id)
);

CREATE TABLE exercise_attempt (
      id uuid NOT NULL PRIMARY KEY,
      exercise_id uuid NOT NULL,
      exercise_version INTEGER NOT NULL,
      user_id uuid NOT NULL REFERENCES ludo_user,
      FOREIGN key (exercise_id, exercise_version) REFERENCES exercise ON DELETE RESTRICT
);

CREATE TABLE attempt_option (
        attempt_id uuid NOT NULL REFERENCES exercise_attempt ON DELETE CASCADE,
        exercise_option_id uuid NOT NULL REFERENCES exercise_option ON DELETE RESTRICT,
        PRIMARY KEY (attempt_id, exercise_option_id)
);