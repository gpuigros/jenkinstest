PIPELINE_SCRIPTS_VERSION=`sed 's/version:[[:space:]]*//g' scripts/pipelines.metadata`
grep -q '^builtByVersion' pipelines/$PIPELINE && sed -i "s/builtByVersion: .*/builtByVersion: ${PIPELINE_SCRIPTS_VERSION}/g" pipelines/$PIPELINE || echo "builtByVersion: ${PIPELINE_SCRIPTS_VERSION}" >> pipelines/$PIPELINE

echo "******** Update the scripts pipeline version"

cd pipelines
git config user.email "atlasdeploy@hotelbeds.com"
git config user.name "atlasdeploy"
git config push.default simple

git add $PIPELINE
if [[ $(git diff --stat origin/master) ]]; then
    git commit -m "Update ${PROJECT_NAME} pipeline configuration"
	echo "Push changes"
    eval $(ssh-agent -s)
    ssh-add ~/.ssh/jenkins
    git pull origin master
	git push origin master
fi