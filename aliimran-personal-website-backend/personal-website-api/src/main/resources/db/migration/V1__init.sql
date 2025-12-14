create table if not exists blog_posts (
  id bigserial primary key,
  slug text not null unique,
  title text not null,
  content text not null,
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);

create table if not exists tags (
  id bigserial primary key,
  name text not null unique
);

create table if not exists post_tags (
  post_id bigint not null references blog_posts(id) on delete cascade,
  tag_id  bigint not null references tags(id) on delete cascade,
  primary key (post_id, tag_id)
);

create table if not exists contact_messages (
  id bigserial primary key,
  name varchar(100) not null,
  email varchar(200) not null,
  message text not null,
  created_at timestamp not null default now()
);
