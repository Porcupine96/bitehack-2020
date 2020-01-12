#!/bin/bash


if [[  "$1" != "--no-up" ]];
then
    CURRENT_VERSION=`sed -n 's/.*version := "\([0-9\.]*\)"/\1/p' build.sbt`

    VERSION_STRIPPED=${CURRENT_VERSION#*.}
    NEXT_VERSION="0.$((VERSION_STRIPPED+1))"

    VERSION=$NEXT_VERSION bash -c './update-version.sh'

    echo "Version updated to: ${NEXT_VERSION}"
fi


sbt clean compile docker:publishLocal
docker-compose up -d --no-deps backend

