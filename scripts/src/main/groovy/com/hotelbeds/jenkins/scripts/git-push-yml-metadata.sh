echo "******** Adding metadata to jenkins pipelines repository"

cd pipelines
git config user.email "atlasdeploy@hotelbeds.com"
git config user.name "atlasdeploy"
git config push.default simple

git checkout -b master

git add -A
if [[ $(git diff --stat origin/master) ]]; then
    git commit -m "Add ${PROJECT_NAME} pipeline configuration"
    echo "Push changes"
    eval $(ssh-agent -s)
    ssh-add ~/.ssh/jenkins
    git pull origin master
    git push origin master
fi