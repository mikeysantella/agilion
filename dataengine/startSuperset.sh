#!/bin/bash

cat <<EOF
NOTES:

# Install Superset
https://hub.docker.com/r/amancevice/superset/
docker run --detach --name superset amancevice/superset
docker exec -it superset superset-init

http://172.17.0.4:8088/login/
admin please

http://www.mybogdanhelp.ro/mysql/doc/en/InnoDB_transaction_model.html
show variables like "%isola%";

EOF

