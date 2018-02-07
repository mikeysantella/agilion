#!/bin/bash
set -e

: ${CONTAINER_NAME:=`basename $PWD`-neo4j}
: ${DATADIR:=$PWD/dataio}
[ -e "$DATADIR" ] || mkdir -vp "$DATADIR/neo4j"

# All new files created in the directory will have the group set to the group of the directory.
chmod g+s "$DATADIR"
# All new directories will have the group rw permission
setfacl -Rdm g:`stat -c "%G" "$DATADIR"`:rw "$DATADIR"

# https://neo4j.com/docs/operations-manual/current/configuration/ports/
: ${HOST_BROWSER_PORT:=7474}  # Used by the Neo4j Browser
: ${HOST_BOLT_PORT:=7687}  # Used by Cypher Shell and by the Neo4j Browser
: ${NEO_ALLARGS:=""} 

[ "" ] && cat <<EOF
NOTES:
You can also run the whole script at once using bin/neo4j-shell (DEPRECATED!) -path northwind.db -file import_csv.cypher.
In the docker: cypher-shell (https://neo4j.com/docs/operations-manual/current/tools/cypher-shell/)
	https://github.com/jexp/neo4j-shell-tools#cypher-import

neo4j-shell can be used to create the graph but I cannot use its output for CSV file
cypher-shell needs Neo4j server to work
Use embedded Neo4j within Java: https://neo4j.com/docs/java-reference/current/#tutorials-java-embedded
	Neo4j Embedded can expose a Bolt connector for Neo4j Browser and Neo4j client Drivers (and cypher-shell?)
EOF

echo "########  Starting Neo4j Server container '$CONTAINER_NAME' on http port $HOST_BROWSER_PORT and Bolt port $HOST_BOLT_PORT with args='$NEO_ALLARGS'"
docker run --name "$CONTAINER_NAME" -d --rm -p $HOST_BROWSER_PORT:7474 -p $HOST_BOLT_PORT:7687 \
  -v $DATADIR/neo4j/data:/data \
  -v $DATADIR/neo4j/import:/var/lib/neo4j/import \
  neo4j $NEO_ALLARGS

echo "########  Dockers running:"
docker ps
echo "########  To stop Neo4j Server, run 'docker stop $CONTAINER_NAME'"
sleep 10
echo "########  Startup log is in $PWD/$CONTAINER_NAME.log"
{ 
  docker logs $CONTAINER_NAME 
  echo "######## Docker info:"
  # Get IP of container to access port 3306
  docker inspect $CONTAINER_NAME 
} &> $CONTAINER_NAME.log

