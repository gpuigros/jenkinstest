#!/usr/bin/env bash
cd $1
git config user.email "atlasdeploy@hotelbeds.com"
git config user.name "atlasdeploy"
git config push.default simple

tag=git tag -l $BUILD_VERSION

if [ "$BUILD_VERSION" == tag ]; then
    echo "tag $BUILD_VERSION already exists"
else
    echo "Create tag $BUILD_VERSION"
    git tag -a $BUILD_VERSION -m "Create tag $BUILD_VERSION"
    eval $(ssh-agent -s)
    ssh-add ~/.ssh/jenkins
    git push --tags
fi

