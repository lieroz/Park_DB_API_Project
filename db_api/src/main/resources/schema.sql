DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS threads CASCADE;
DROP TABLE IF EXISTS forums CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS forum_users CASCADE;
DROP TABLE IF EXISTS votes CASCADE;

DROP INDEX IF EXISTS forums_user_id_idx;
DROP INDEX IF EXISTS threads_user_id_idx;
DROP INDEX IF EXISTS threads_forum_id_idx;
DROP INDEX IF EXISTS posts_user_id_idx;
DROP INDEX IF EXISTS posts_forum_id_idx;
DROP INDEX IF EXISTS posts_thread_id_idx;
DROP INDEX IF EXISTS posts_path_thread_id_idx;
DROP INDEX IF EXISTS posts_path_help_idx;
DROP INDEX IF EXISTS posts_multi_idx;
DROP INDEX IF EXISTS forum_users_user_id_idx;
DROP INDEX IF EXISTS forum_users_forum_id_idx;

DROP FUNCTION IF EXISTS thread_insert( CITEXT, TIMESTAMPTZ, CITEXT, TEXT, CITEXT, TEXT );
DROP FUNCTION IF EXISTS post_insert( CITEXT, TIMESTAMPTZ, INTEGER, INTEGER, TEXT, INTEGER, INTEGER );
DROP FUNCTION IF EXISTS update_or_insert_votes( INTEGER, INTEGER, INTEGER );

CREATE EXTENSION IF NOT EXISTS CITEXT;

SET SYNCHRONOUS_COMMIT = 'off';

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
  thread_id INTEGER REFERENCES threads (id) ON DELETE CASCADE NOT NULL,
  path      INTEGER []                                        NOT NULL
);

CREATE INDEX IF NOT EXISTS posts_user_id_idx
  ON posts (user_id);
CREATE INDEX IF NOT EXISTS posts_forum_id_idx
  ON posts (forum_id);
CREATE INDEX IF NOT EXISTS posts_thread_id_idx
  ON posts (thread_id);
CREATE INDEX IF NOT EXISTS posts_path_thread_id_idx
  ON posts (thread_id, path);
CREATE INDEX IF NOT EXISTS posts_path_help_idx
  ON posts ((path [1]), path);
CREATE INDEX IF NOT EXISTS posts_multi_idx
  ON posts (thread_id, parent, id);

CREATE TABLE IF NOT EXISTS forum_users (
  user_id  INTEGER REFERENCES users (id) ON DELETE CASCADE,
  forum_id INTEGER REFERENCES forums (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS forum_users_user_id_idx
  ON forum_users (user_id);
CREATE INDEX IF NOT EXISTS forum_users_forum_id_idx
  ON forum_users (forum_id);

CREATE TABLE IF NOT EXISTS votes (
  user_id   INTEGER REFERENCES users (id) ON DELETE CASCADE,
  thread_id INTEGER REFERENCES threads (id) ON DELETE CASCADE,
  voice     INTEGER DEFAULT 0
);

CREATE OR REPLACE FUNCTION thread_insert(thread_author  CITEXT, thread_created TIMESTAMPTZ, forum_slug CITEXT,
                                         thread_message TEXT, thread_slug CITEXT, thread_title TEXT)
  RETURNS INTEGER AS '
DECLARE
  thread_id       INTEGER;
  thread_user_id  INTEGER;
  thread_forum_id INTEGER;
BEGIN
  SELECT id
  FROM users
  WHERE nickname = thread_author
  INTO thread_user_id;
  --
  SELECT id
  FROM forums
  WHERE slug = forum_slug
  INTO thread_forum_id;
  --
  IF thread_created IS NULL
  THEN
    INSERT INTO threads (user_id, forum_id, message, slug, title)
    VALUES (thread_user_id, thread_forum_id, thread_message, thread_slug, thread_title)
    RETURNING id
      INTO thread_id;
  ELSE
    INSERT INTO threads (user_id, created, forum_id, message, slug, title)
    VALUES (thread_user_id, thread_created, thread_forum_id, thread_message, thread_slug, thread_title)
    RETURNING id
      INTO thread_id;
  END IF;
  --
  IF NOT EXISTS(
      SELECT *
      FROM forum_users
      WHERE forum_id = thread_forum_id AND user_id = thread_user_id)
  THEN
    INSERT INTO forum_users (user_id, forum_id) VALUES (thread_user_id, thread_forum_id);
  END IF;
  --
  RETURN thread_id;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION post_insert(post_author  CITEXT, post_created TIMESTAMPTZ, post_forum_id INTEGER,
                                       post_id INTEGER, post_message TEXT, post_parent INTEGER, post_thread_id INTEGER)
  RETURNS VOID AS '
DECLARE
  post_user_id INTEGER;
BEGIN
  SELECT id
  FROM users
  WHERE nickname = post_author
  INTO post_user_id;
  --
  INSERT INTO posts (user_id, created, forum_id, id, message, parent, thread_id, path)
  VALUES (post_user_id, post_created, post_forum_id, post_id, post_message, post_parent, post_thread_id,
          array_append((SELECT path
                        FROM posts
                        WHERE id = post_parent), post_id));
  --
  IF NOT EXISTS(
      SELECT *
      FROM forum_users
      WHERE forum_id = post_forum_id AND user_id = post_user_id)
  THEN
    INSERT INTO forum_users (user_id, forum_id) VALUES (post_user_id, post_forum_id);
  END IF;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_or_insert_votes(vote_user_id INTEGER, vote_thread_it INTEGER, vote_value INTEGER)
  RETURNS VOID AS '
DECLARE
  count INTEGER;
BEGIN
  SELECT COUNT(*)
  FROM votes
  WHERE user_id = vote_user_id AND thread_id = vote_thread_it
  INTO count;
  IF count > 0
  THEN
    UPDATE votes
    SET voice = vote_value
    WHERE user_id = vote_user_id AND thread_id = vote_thread_it;
  ELSE
    INSERT INTO votes (user_id, thread_id, voice) VALUES (vote_user_id, vote_thread_it, vote_value);
  END IF;
  UPDATE threads
  SET votes = (SELECT SUM(voice)
               FROM votes
               WHERE thread_id = vote_thread_it)
  WHERE id = vote_thread_it;
END;
' LANGUAGE plpgsql