cd ##DEFAULT_PROJECT_DIR##
VERSION=`cat version.properties | sed s/-SNAPSHOT//`
echo "BUILD_VERSION=$VERSION-$BUILD_NUMBER" > version.properties
echo "RPM_VERSION=$VERSION" >> version.properties
echo "RPM_RELEASE=$BUILD_NUMBER" >> version.properties
echo "VERSION=$VERSION" >> version.properties