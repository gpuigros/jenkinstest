#!/usr/bin/env bash
SERVICE_NAME="$1"
ENV="$2"
ACCEPT_HEADER="Accept: application/json"
CONTENT_TYPE_HEADER="Content-Type: application/json"
AUTHORIZATION_HEADER="Authorization: Basic $JIRA_HBDDEVOPS_BASE64_CREDENTIALS"
JIRA_URL="https://agile.hotelbeds.com/jira/rest/api/latest/issue"
NOW=`date +%Y-%m-%dT%H:%M:%S.%3N%z`

PIPELINE_URL="${BUILD_URL%/job/*}/view/$SERVICE_NAME/"

REQUEST_BODY="\
{\
    \"fields\": {\
       \"project\":\
       {\
          \"key\": \"RDM\"\
       },\
       \"summary\": \"Deploy $ENV for $SERVICE_NAME $BUILD_VERSION\",\
       \"description\": \"Deploy $ENV for $SERVICE_NAME $BUILD_VERSION\",\
       \"issuetype\": {\
          \"id\": \"12202\"\
       },\
       \"components\" : [{\"name\" : \"$SERVICE_NAME\"}],\
     \"labels\": [\"jenkins-pipeline\"],\
     \"customfield_21300\" : {\"id\": \"25204\"},\
     \"customfield_21004\" : {\"id\": \"24426\"},\
     \"customfield_14105\" : {\"id\": \"13310\"},\
     \"customfield_11601\" : \"$NOW\",\
     \"customfield_11602\" : \"$NOW\",\
     \"customfield_17002\" : \"Please go to: $PIPELINE_URL to approve the deploy for $SERVICE_NAME $BUILD_VERSION \",\
     \"customfield_16813\" : \"Install previous version\",\
     \"customfield_14204\" : \"$SERVICE_NAME $BUILD_VERSION\",\
     \"customfield_14502\" : {\"id\": \"14120\"}\
   }\
}\
"

# store the whole response with the status at the end
HTTP_RESPONSE=$(curl --silent --write-out "HTTPSTATUS:%{http_code}" -X POST -H "$ACCEPT_HEADER" -H "$CONTENT_TYPE_HEADER" -H "$AUTHORIZATION_HEADER" --url "$JIRA_URL" -d "$REQUEST_BODY")

# extract the body
HTTP_BODY=$(echo "$HTTP_RESPONSE" | sed -e 's/HTTPSTATUS\:.*//g')

# extract the status
HTTP_STATUS=$(echo "$HTTP_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')

echo 'Jira create rdm task http_status:' ${HTTP_STATUS}

if [[ ${HTTP_STATUS}  == "201" ]]; then
  echo 'Jira RDM task has been created'
  echo 'Response: ' ${HTTP_BODY}
  exit 0
else
  echo 'Jira RDM task has NOT been created'
  exit 1
fi