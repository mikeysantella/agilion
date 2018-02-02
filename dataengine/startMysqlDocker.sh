#!/bin/bash
set -e
which docker || { echo "Docker must be installed!"; exit 1; }

# https://hub.docker.com/_/mysql/
# Superset is not compatible with MySQL 8
SQL_VERSION=5.7

: ${DATADIR:=$PWD/dataio}
[ -e "$DATADIR" ] || mkdir -vp "$DATADIR"

# All new files created in the directory will have the group set to the group of the directory.
chmod g+s "$DATADIR"
# All new directories will have the group rw permission
setfacl -Rdm g:`stat -c "%G" "$DATADIR"`:rw "$DATADIR"
# Let container's non-root user write to DATADIR
chmod o+w "$DATADIR"

if ! [ -e "$DATADIR/mysql-conf.d/mysql.conf" ]; then
	echo "########  Downloading MySQL $SQL_VERSION"
	docker pull mysql:$SQL_VERSION

	[ -e "$DATADIR/mysql-conf.d" ] || mkdir -v "$DATADIR/mysql-conf.d"
	cat <<EOF > "$DATADIR/mysql-conf.d/mysql.conf"
[mysqld]
local-infile=1

[mysql]
local-infile=1
EOF
	chmod -R a+rX "$DATADIR/mysql-conf.d"
fi

: ${CONTAINER_NAME:=`basename $PWD`-mysql}
: ${MYSQL_ROOT_PW:="$CONTAINER_NAME"}
: ${MYSQL_ARGS:=""}
: ${MYSQL_ALLARGS:="$MYSQL_ARGS --local-infile --secure-file-priv=$DATADIR --explicit_defaults_for_timestamp"}
#To check, SHOW VARIABLES LIKE "secure_file_priv";
: ${HOST_PORT:=3306}
echo "########  Starting MySQL $SQL_VERSION container '$CONTAINER_NAME' on port $HOST_PORT"
echo "########     with args='$MYSQL_ALLARGS'"
docker run --name "$CONTAINER_NAME" -d --rm=true \
  -v "$DATADIR/mysql-conf.d":/etc/mysql/conf.d \
  -v "$DATADIR":"$DATADIR" \
  -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PW" \
  -e MYSQL_DATABASE="tempdb" \
  -p $HOST_PORT:3306 \
  mysql:$SQL_VERSION $MYSQL_ALLARGS

echo "########  Dockers running:"
docker ps
echo "########  To stop this MySQL service, run 'docker stop $CONTAINER_NAME'"
sleep 10
echo "########  Startup log is in $PWD/$CONTAINER_NAME.log"
{ 
  docker logs $CONTAINER_NAME 
  echo "######## Docker info:"
  # Get IP of container to access port 3306
  docker inspect $CONTAINER_NAME 
} &> $CONTAINER_NAME.log

if ! docker inspect -f {{.State.Running}} $CONTAINER_NAME; then
  echo "FAILED:  Container '$CONTAINER_NAME' is not running!"
  docker ps
fi

# # To diagnose server
# docker exec -ti $CONTAINER_NAME-mysql bash
# THEN: mysqld --verbose --help | grep infile
# 
# OR ONE-LINER: docker run -it --rm mysql:$SQL_VERSION --verbose --help | grep infile
# THIS WORKED: docker run -it --rm mysql:$SQL_VERSION --local-infile --verbose --help | grep infile
# 
# # Run client
# docker run -it --link $CONTAINER_NAME-mysql:mysql --rm mysql sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD" --local-infile'
# # Or MySQL Workbench
# mysql-workbench-community-6.3.9-1ubuntu16.04-amd64.deb at https://dev.mysql.com/downloads/file/?id=468285


