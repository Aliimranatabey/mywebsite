#!/usr/bin/env bash
# Sahibi olmayan bütün yazıları verilen hesaba bağlar (sitede kayıt olduktan sonra):
#   bash deploy/sahiplen.sh senin@epostan.com
set -euo pipefail
cd "$(dirname "$0")/.."

EPOSTA="${1:?kullanım: bash deploy/sahiplen.sh senin@epostan.com}"
EPOSTA="$(printf '%s' "$EPOSTA" | tr '[:upper:]' '[:lower:]' | sed "s/'//g")"

docker compose exec -T postgres psql -U devuser -d personal_site -v ON_ERROR_STOP=1 -c "
do \$\$
declare uid bigint; n int;
begin
  select id into uid from users where email = '$EPOSTA';
  if uid is null then raise exception 'Bu e-postayla kayıtlı hesap yok: $EPOSTA'; end if;
  update blog_posts set author_id = uid where author_id is null;
  get diagnostics n = row_count;
  raise notice '% yazı sana bağlandı', n;
end \$\$;"
