#!/bin/bash

mkdir src/main/resources

git_describe=$(git describe --tags)

if [[ -z "${CI_COMMIT_REF_NAME}" ]]; then
	#undefined
	branch_name=$(git rev-parse --abbrev-ref HEAD)
	if [[ "$branch_name" != "master" ]]; then
		echo version=${branch_name} > src/main/resources/version.properties
	fi
elif [[ -z "${CI_COMMIT_TAG}" ]]; then
	echo version=${git_describe}-${CI_COMMIT_REF_NAME} > src/main/resources/version.properties
elif [[ "${CI_COMMIT_TAG}" ]]; then
	echo version=${CI_COMMIT_TAG} > src/main/resources/version.properties
fi
