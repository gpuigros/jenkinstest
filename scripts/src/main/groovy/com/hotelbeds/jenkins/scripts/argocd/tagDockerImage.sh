PROJECT_FOLDER=$1
ENVIRONMENT=$2
REPOSITORY=$3
ECR_URL=$4
ECR_REGION=$5

cd $PROJECT_FOLDER

echo "Retrieving authentication token..."
$(/usr/local/bin/aws ecr get-login --no-include-email --region ${ECR_REGION})
echo "Tagging image..."
docker tag ${ECR_URL}/${REPOSITORY}:${RPM_VERSION}-${RPM_RELEASE} ${ECR_URL}/${REPOSITORY}:${ENVIRONMENT}
docker push ${ECR_URL}/${REPOSITORY}:${ENVIRONMENT}
