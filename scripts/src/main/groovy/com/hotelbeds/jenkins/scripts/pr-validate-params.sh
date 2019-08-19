echo "validating that PULL_REQUEST_URL parameter is not empty"
if [[ -z "${PULL_REQUEST_URL// }" ]]; then
        echo "PULL_REQUEST_URL is empty"
        exit 1
fi
echo "validating that PULL_REQUEST_ID parameter is not empty"
if [[ -z "${PULL_REQUEST_ID// }" ]]; then
        echo "PULL_REQUEST_ID is empty"
        exit 1
fi
