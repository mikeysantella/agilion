
DLDIR="$HOME/NOBACKUP/Download"

rm -rf tmp
unzip -d tmp "$DLDIR/java-client-generated.zip"
rsync -av --exclude=pom.xml tmp .
rm -fv "$DLDIR/java-client-generated.zip"
gradle clean install
