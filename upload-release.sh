#!/bin/bash

echo $CI_COMMIT_TAG
echo ""

curl_upload_result=$(curl --header "PRIVATE-TOKEN: ${PRIVATE_TOKEN}" --form "file=@target/todo-app-${CI_COMMIT_TAG}.one-jar.jar" https://gitlab.com/api/v4/projects/12882469/uploads)

echo $curl_upload_result
echo ""
echo ""

#url=$("${curl_upload_result}" | jq --raw-output '.markdown')
markdown=$(jq '.markdown' <(echo $curl_upload_result))

echo ""
echo $markdown
echo ""

curl_release_result=$(curl --request POST --header "PRIVATE-TOKEN: ${PRIVATE_TOKEN}" --header "Content-Type: application/json" --data '{"description": "'$markdown'"}' https://gitlab.com/api/v4/projects/12882469/repository/tags/${CI_COMMIT_TAG}/release)

echo $curl_release_result
echo ""
