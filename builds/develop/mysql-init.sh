#!/usr/bin/env bash
set -euo pipefail

databases=(
  dodam-auth
  dodam-user
  dodam-wakeup-song
  dodam-inapp
  dodam-oauth
  dodam-neis
  dodam-out-sleeping
  dodam-nightstudy
  dodam-file
  dodam-notification
)

escaped_user=${MYSQL_USER//\`/\`\`}

for database in "${databases[@]}"; do
  escaped_database=${database//\`/\`\`}
  mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE DATABASE IF NOT EXISTS \`${escaped_database}\`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON \`${escaped_database}\`.* TO \`${escaped_user}\`@'%';
SQL
done

mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" -e 'FLUSH PRIVILEGES;'
