#!/usr/bin/env bash

environment=##environment##
nexusRepositoryId=##nexusRepositoryId##
artifactName=##artifactName##

url="https://nexus-repository.${environment}.hotelbeds.com/nexus/content/repositories/${nexusRepositoryId}/com/hotelbeds/${artifactName}/${BUILD_VERSION}/"
echo ${url}

http_status=$(curl -I ${url} 2>/dev/null | head -n 1 | cut -d ' ' -f 2)
echo ${http_status}

if [[ ${http_status}  == "200" ]]; then
  echo 'RPM file already uploaded'
  exit 1
else
  echo 'RPM file NOT uploaded'
  exit 0
fi