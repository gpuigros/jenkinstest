cd ##DEFAULT_PROJECT_DIR##
VERSION=`cat setup.py | sed -n '/version *=/s/[^0-9\.]//gp'`
echo "BUILD_VERSION=$VERSION-$BUILD_NUMBER" > version.properties
echo "RPM_VERSION=$VERSION" >> version.properties
echo "RPM_RELEASE=$BUILD_NUMBER" >> version.properties
echo "VERSION=$VERSION" >> version.properties
