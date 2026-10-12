insert into blog_posts (slug, title, content)
values
('vue3-ile-baslangic','Vue 3 ile Başlangıç','Vue 3 dünyasına giriş...'),
('spring-boot-giris','Spring Boot Giriş','Spring Boot ile REST API...');

insert into tags (name) values ('vue'), ('frontend'), ('spring'), ('backend')
on conflict do nothing;

insert into post_tags (post_id, tag_id)
select p.id, t.id from blog_posts p join tags t on
  (p.slug='vue3-ile-baslangic' and t.name in ('vue','frontend'))
union all
select p.id, t.id from blog_posts p join tags t on
  (p.slug='spring-boot-giris' and t.name in ('spring','backend'));
