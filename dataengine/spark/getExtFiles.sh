#!/bin/bash

RSYNC_SRC="someserver:/path/to/extLibs"
RSYNC_HOST="${RSYNC_SRC%%:*}"
ping -c 1 "$RSYNC_HOST" || unset RSYNC_HOST

cat extFileList | while read FILE URL OTHER; do 
	[ "$FILE" ] || continue #skip blanks
	[ "$FILE" == "#" ] && continue
	if [ -e "$FILE" ]; then
		echo "Exists: $FILE"
	else
		if [ "$RSYNC_HOST" ]; then
			echo "Retrieving $FILE from $RSYNC_SRC"
			rsync -av "$RSYNC_SRC"/"$FILE" .
		else
			echo "$RSYNC_HOST not available.  Will attempt using URL=$URL"
		fi
		if ! [ -e "$FILE" ]; then
			echo "Retrieving $FILE from $RSYNC_SRC"
			wget "$URL"
		fi
		if ! [ -e "$FILE" ]; then
			echo "ERROR: Could not get $FILE"
		fi
	fi
done


