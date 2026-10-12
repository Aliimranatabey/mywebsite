-- Kullanıcı hesapları
create table if not exists users (
  id            bigserial primary key,
  email         text not null unique,
  display_name  text not null,
  password_hash text not null,
  created_at    timestamp not null default now()
);

-- Blog yazılarına sahiplik.
-- Nullable, çünkü V2__seed.sql'den gelen mevcut yazıların sahibi yok.
-- Sahipsiz yazılar API üzerinden düzenlenemez/silinemez (403 döner).
alter table blog_posts add column if not exists author_id bigint references users(id);
create index if not exists idx_blog_posts_author on blog_posts(author_id);
