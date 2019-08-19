cd ##DEFAULT_PROJECT_DIR##
VERSION=`cat specs/ace-hotel-daemon.spec | grep "%define version" | head -1 | tr -s [:space:] | cut -d ' ' -f 3`
echo "BUILD_VERSION=$VERSION-$BUILD_NUMBER" > version.properties
echo "RPM_VERSION=$VERSION" >> version.properties
echo "RPM_RELEASE=$BUILD_NUMBER" >> version.properties
echo "VERSION=$VERSION" >> version.properties