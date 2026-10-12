-- Yazının kapak görseli. Sadece /api/uploads/ altına yüklenmiş dosyalara işaret eder.
-- Boşsa frontend yazının içindeki ilk resmi, o da yoksa üretilmiş bir kapağı kullanır.
alter table blog_posts add column if not exists cover_image_url text;
