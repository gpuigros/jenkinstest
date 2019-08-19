echo "******** Adding pipeline.yml to ${GIT_REPO_URL} repository"

cd project
git config user.email "atlasdeploy@hotelbeds.com"
git config user.name "atlasdeploy"
git config push.default simple
  

if [ "$RELEASEMENT_STRATEGY" = "CONTINUOUS_DEPLOYMENT" ]; then
  git checkout master

  git add pipeline.yml
  if [[ $(git diff --stat origin/master) ]]; then
    git commit -m "Add ${PROJECT_NAME} pipeline configuration"
	echo "Push changes"
    eval $(ssh-agent -s)
    ssh-add ~/.ssh/jenkins
    git push origin master
  fi
fi

if [ "$RELEASEMENT_STRATEGY" = "RELEASES" ]; then

  if [[ $PROFILES == *"ON_PREMISE"* ]]; then
     # STAGE
     git checkout ${STAGE_BRANCH}

     cp ${WORKSPACE}/environments/STAGE/pipeline-STAGE.yml .
     cp ${WORKSPACE}/environments/DEV/pipeline-DEV.yml .
     cp ${WORKSPACE}/environments/TEST/pipeline-TEST.yml .

     git add pipeline-DEV.yml
     git add pipeline-TEST.yml
     git add pipeline-STAGE.yml
     if [[ $(git diff --stat origin/${STAGE_BRANCH}) ]]; then
        git commit -m "Add ${PROJECT_NAME} pipeline configuration"
        echo "Push changes"
        eval $(ssh-agent -s)
        ssh-add ~/.ssh/jenkins
        git push -f origin ${STAGE_BRANCH}
     fi
  fi

  # DEV
  git checkout ${DEV_BRANCH}

  cp ${WORKSPACE}/environments/DEV/pipeline-DEV.yml .
  cp ${WORKSPACE}/environments/TEST/pipeline-TEST.yml .

  git add pipeline-DEV.yml
  git add pipeline-TEST.yml

  if [[ $PROFILES == *"ON_PREMISE"* ]]; then
    cp ${WORKSPACE}/environments/STAGE/pipeline-STAGE.yml .
    git add pipeline-STAGE.yml
  fi

  if [[ $(git diff --stat origin/${DEV_BRANCH}) ]]; then
      git commit -m "Add ${PROJECT_NAME} pipeline configuration"
      echo "Push changes"
      eval $(ssh-agent -s)
      ssh-add ~/.ssh/jenkins
      git push -f origin ${DEV_BRANCH}
  fi

  # TEST
  git checkout ${TEST_BRANCH}

  cp ${WORKSPACE}/environments/TEST/pipeline-TEST.yml .
  cp ${WORKSPACE}/environments/DEV/pipeline-DEV.yml .

  git add pipeline-TEST.yml
  git add pipeline-DEV.yml

  if [[ $PROFILES == *"ON_PREMISE"* ]]; then
    cp ${WORKSPACE}/environments/STAGE/pipeline-STAGE.yml .
    git add pipeline-STAGE.yml
  fi

  if [[ $(git diff --stat origin/${TEST_BRANCH}) ]]; then
      git commit -m "Add ${PROJECT_NAME} pipeline configuration"
      echo "Push changes"
      eval $(ssh-agent -s)
      ssh-add ~/.ssh/jenkins
      git push -f origin ${TEST_BRANCH}
  fi

fi