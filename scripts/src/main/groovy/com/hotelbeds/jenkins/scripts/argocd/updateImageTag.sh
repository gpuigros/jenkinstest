PROJECT_FOLDER=$1
ENVIRONMENT=$2

cd $PROJECT_FOLDER

git config --global user.email "atlasdeploy@hotelbeds.com"
git config --global user.name "atlasdeploy"
git config --global push.default simple

eval $(ssh-agent -s)
ssh-add ~/.ssh/jenkins
git pull origin master

HELM_VALUES_FILE="helm/values-${ENVIRONMENT}.yaml"
PREV_VERSION=`sed -n -e '/^  tag:/p' ${HELM_VALUES_FILE}`
sed -i.bak "s/${PREV_VERSION}/  tag: ${RPM_VERSION}-${RPM_RELEASE}/g" ${HELM_VALUES_FILE}

git add ${HELM_VALUES_FILE}
git commit -m "Update container image tag for ${ENVIRONMENT} environment"

git push origin HEAD:master
