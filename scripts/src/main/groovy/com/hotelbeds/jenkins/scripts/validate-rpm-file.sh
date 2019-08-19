PIPELINE_NAME="$1"
RPM_FILE="$2"
RPM_DATA=`rpm -qip $RPM_FILE | egrep "Name|Version|Release" | cut -d ':' -f2 | cut -d ' ' -f2`
RPM_VERSION=`echo "$RPM_DATA" | head -n2 | tail -n1`
RPM_RELEASE=`echo "$RPM_DATA" | tail -n1`
RPM_NAME=`echo "$RPM_DATA" | head -n1`

if [[ "$RPM_NAME" == "$PIPELINE_NAME" && "$RPM_VERSION-$RPM_RELEASE" == "${BUILD_VERSION}" ]]; then
  echo 'Valid RPM file'
  exit 0
else
  echo 'No valid RPM file'
  exit 1
fi