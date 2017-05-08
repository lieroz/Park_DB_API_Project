DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS threads CASCADE;
DROP TABLE IF EXISTS forums CASCADE;
DROP TABLE IF EXISTS users CASCADE;

DROP INDEX IF EXISTS forums_user_id_idx;
DROP INDEX IF EXISTS threads_user_id_idx;
DROP INDEX IF EXISTS threads_forum_id_idx;
DROP INDEX IF EXISTS posts_user_id_idx;
DROP INDEX IF EXISTS posts_forum_id_idx;
DROP INDEX IF EXISTS posts_thread_id_idx;

CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE IF NOT EXISTS users (
  id       SERIAL PRIMARY KEY,
  about    TEXT DEFAULT NULL,
  email    CITEXT UNIQUE,
  fullname TEXT DEFAULT NULL,
  nickname CITEXT COLLATE ucs_basic UNIQUE
);

CREATE TABLE IF NOT EXISTS forums (
  id      SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users (id) ON DELETE CASCADE NOT NULL,
  posts   INTEGER DEFAULT 0,
  threads INTEGER DEFAULT 0,
  slug    CITEXT UNIQUE                                   NOT NULL,
  title   TEXT                                            NOT NULL
);

CREATE INDEX IF NOT EXISTS forums_user_id_idx
  ON forums (user_id);

CREATE TABLE IF NOT EXISTS threads (
  user_id  INTEGER REFERENCES users (id) ON DELETE CASCADE  NOT NULL,
  created  TIMESTAMPTZ DEFAULT NOW(),
  forum_id INTEGER REFERENCES forums (id) ON DELETE CASCADE NOT NULL,
  id       SERIAL PRIMARY KEY,
  message  TEXT        DEFAULT NULL,
  slug     CITEXT UNIQUE,
  title    TEXT                                             NOT NULL,
  votes    INTEGER     DEFAULT 0
);

CREATE INDEX IF NOT EXISTS threads_user_id_idx
  ON threads (user_id);
CREATE INDEX IF NOT EXISTS threads_forum_id_idx
  ON threads (forum_id);

CREATE TABLE IF NOT EXISTS posts (
  user_id   INTEGER REFERENCES users (id) ON DELETE CASCADE   NOT NULL,
  created   TIMESTAMPTZ DEFAULT NOW(),
  forum_id  INTEGER REFERENCES forums (id) ON DELETE CASCADE  NOT NULL,
  id        SERIAL PRIMARY KEY,
  is_edited BOOLEAN     DEFAULT FALSE,
  message   TEXT        DEFAULT NULL,
  parent    INTEGER     DEFAULT 0,
  thread_id INTEGER REFERENCES threads (id) ON DELETE CASCADE NOT NULL
);

CREATE INDEX IF NOT EXISTS posts_user_id_idx
  ON posts (user_id);
CREATE INDEX IF NOT EXISTS posts_forum_id_idx
  ON posts (forum_id);
CREATE INDEX IF NOT EXISTS posts_thread_id_idx
  ON posts (thread_id);

ALTER TABLE users
  ADD thread_id INTEGER REFERENCES threads (id) ON DELETE CASCADE;
ALTER TABLE users
  ADD voice INTEGER DEFAULT 0;