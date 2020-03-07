#!/bin/bash

echo $CI_COMMIT_TAG
echo ""

curl_upload_result=$(curl -v --header "PRIVATE-TOKEN: ${PRIVATE_TOKEN}" --form "file=@build/libs/micro-task.jar" https://gitlab.com/api/v4/projects/12882469/uploads)

echo $curl_upload_result
echo ""
echo ""

#url=$("${curl_upload_result}" | jq --raw-output '.markdown')
markdown=$(jq '.markdown' <(echo $curl_upload_result))

echo ""
echo $markdown
echo ""

curl_release_result=$(curl -v --request POST --header "PRIVATE-TOKEN: ${PRIVATE_TOKEN}" --header "Content-Type: application/json" --data '{"description": '$markdown'}' https://gitlab.com/api/v4/projects/12882469/repository/tags/${CI_COMMIT_TAG}/release)

echo $curl_release_result
echo ""

# if the results contain error then we failed to upload the file

if [[ $curl_release_result == *"error"* ]]; then
    exit 1
fi

exit 0