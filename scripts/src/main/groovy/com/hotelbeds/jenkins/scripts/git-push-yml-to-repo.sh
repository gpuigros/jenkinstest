echo "******** Adding pipeline.yml to ${GIT_REPO_URL} repository"

cd project
git config user.email "atlasdeploy@hotelbeds.com"
git config user.name "atlasdeploy"
git config push.default simple
  
git checkout master

git add -A
if [[ $(git diff --stat origin/master) ]]; then    
    git commit -m "Add ${PROJECT_NAME} pipeline configuration"
	echo "Push changes"    
    eval $(ssh-agent -s)
    ssh-add ~/.ssh/jenkins
	git push origin master
fi