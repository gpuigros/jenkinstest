PROJECT_FOLDER=$1
PIPELINE_NAME=$2
ECR_URL=$3
ECR_REGION=$4


cd $PROJECT_FOLDER
echo 'Building new image...'
docker build -t ${PIPELINE_NAME}:${BUILD_VERSION} --label commit=${BUILD_GIT_COMMIT} .
echo 'Retrieving authentication token...'
$(/usr/local/bin/aws ecr get-login --no-include-email --region ${ECR_REGION})
#docker login -u AWS -p
echo 'Tagging image...'
docker tag ${PIPELINE_NAME}:${BUILD_VERSION} ${ECR_URL}/${PIPELINE_NAME}:${BUILD_VERSION}
echo 'Uploading image...'
docker push ${ECR_URL}/${PIPELINE_NAME}:${BUILD_VERSION}
echo 'Removing previous images...'
docker images -f before=${PIPELINE_NAME}:${BUILD_VERSION} -f label=building_block=${PIPELINE_NAME} --format '{{.Repository}}:{{.Tag}}'
