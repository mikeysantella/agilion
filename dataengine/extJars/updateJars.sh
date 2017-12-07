#!/bin/bash

copyToFlat(){
	for D in net/deelam/activemq-rpc/0.1.0/ net/deelam/coord-workers/0.1.0/ net/deelam/graph/0.1.0/ net/deelam/utils-zero/0.1.0/ net/deelam/zkbasedinit/0.1.0/ ; do
		cp -vulf ~/.m2/repository/$D/{*[0-9].jar,*-sources.jar,*.pom} .
	done
}

installFlatToLocalMaven(){
	for J in *.jar; do
		echo "Installing $J"
		case "$J" in
			*-javadoc.jar) ;;
			*-sources.jar)
				F=`basename "$J" -sources.jar`
				mvn install:install-file -Dpackaging=java-source -DgeneratePom=false -Dfile="$J" -DpomFile="$F.pom" ;;
			*.jar)
				F=`basename "$J" .jar`
				mvn install:install-file -Dfile="$J" -DpomFile="$F.pom" ;;
		esac
	done
}

updateToTree(){
	echo "Updating from Maven repo to current directory..."
	mkdir -p net/deelam
	cp -vruf ~/.m2/repository/net/deelam/* net/deelam/
	echo "Done"
}

copyTreeToLocalMaven(){
	rsync -av net/deelam ~/.m2/repository/net
}

case "$1" in
	"") updateToTree;;
	"install") copyTreeToLocalMaven;;
	*) "$@";;
esac

