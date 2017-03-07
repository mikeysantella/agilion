
DLDIR="$HOME/NOBACKUP/Download"

rm -rf tmp
unzip -d tmp "$DLDIR/jaxrs-server-generated.zip"
rsync -av --exclude=pom.xml --exclude=OffsetDateTimeProvider.java --exclude=LocalDateProvider.java --exclude=Bootstrap.java --exclude=StringUtil.java --exclude=ApiOriginFilter.java tmp/ .
rm -fv "$DLDIR/jaxrs-server-generated.zip"
read -p "Press Enter after accepting/rejecting changes"
gradle clean install
