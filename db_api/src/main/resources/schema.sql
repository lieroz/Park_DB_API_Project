DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS threads CASCADE;
DROP TABLE IF EXISTS forums CASCADE;
DROP TABLE IF EXISTS users CASCADE;

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

ALTER TABLE users
  ADD thread_id INTEGER REFERENCES threads (id) ON DELETE CASCADE;
ALTER TABLE users
  ADD voice INTEGER DEFAULT 0;

CREATE OR REPLACE FUNCTION check_post_parent(post_id INTEGER)
  RETURNS INTEGER AS
'
DECLARE
  result INTEGER DEFAULT NULL;
BEGIN
  WITH RECURSIVE tuple AS (
    (SELECT
       *,
       ARRAY [id] AS path
     FROM posts
     WHERE id = post_id
    )
    UNION (
      SELECT
        posts.*,
        array_append(tuple.path, posts.id) AS path
      FROM tuple
        JOIN posts ON posts.id = tuple.parent
      WHERE posts.thread_id = tuple.thread_id
    )
  ) SELECT parent
    FROM tuple
    ORDER BY parent
    LIMIT 1
    INTO result;

  IF result = 0
  THEN
    RETURN result;
  END IF;

  RETURN NULL;
END;
'
LANGUAGE 'plpgsql';