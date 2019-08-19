PIPELINE_NAME=$1
PIPELINE_SCRIPTS_VERSION=`sed 's/version:[[:space:]]*//g' scripts/pipelines.metadata`
grep -q '^builtByVersion' pipelines/${PIPELINE_NAME}.yml && sed -i "s/builtByVersion: .*/builtByVersion: ${PIPELINE_SCRIPTS_VERSION}/g" pipelines/${PIPELINE_NAME}.yml || echo "builtByVersion: ${PIPELINE_SCRIPTS_VERSION}" >> pipelines/${PIPELINE_NAME}.yml

echo "******** Update the scripts pipeline version"

cd pipelines
git config user.email "atlasdeploy@hotelbeds.com"
git config user.name "atlasdeploy"
git config push.default simple

git add ${PIPELINE_NAME}.yml
if [[ $(git diff --stat origin/master) ]]; then
    git commit -m "Update ${PROJECT_NAME} pipeline configuration"
	echo "Push changes"
    eval $(ssh-agent -s)
    ssh-add ~/.ssh/jenkins
    git pull origin master
	git push origin master
fi