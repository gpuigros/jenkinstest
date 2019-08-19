RPM_DESTINATION=$1
rm -f /opt/ace/packages/RPMS/x86_64/ace-hotel-daemon*.rpm

cd /opt/ace/project/build
make dist-daemon BUILD_NUMBER=$RPM_RELEASE;
cd /opt/ace/packages/RPMS/x86_64


RPM_FILE=$(find ace-hotel-daemon*.rpm)

cp /opt/ace/packages/RPMS/x86_64/$RPM_FILE ${WORKSPACE}/$RPM_DESTINATION/ace-hotel-daemon-$BUILD_VERSION.x86_64.rpm

exit 0;