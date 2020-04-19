#!/bin/bash

echo "$CI_COMMIT_TAG"
echo ""

# upload release jar
curl_upload_result=$(curl -v --header "PRIVATE-TOKEN: ${PRIVATE_TOKEN}" --form "file=@build/libs/micro-task.jar" https://gitlab.com/api/v4/projects/12882469/uploads)

echo "$curl_upload_result"
echo ""

# if the results contain error then we failed to upload the file

if [[ $curl_release_result == *"error"* ]]; then
    exit 1
fi

#url=$("${curl_upload_result}" | jq --raw-output '.markdown')
markdown=$(jq '.markdown' <(echo "$curl_upload_result"))

echo ""
echo "$markdown"
echo ""

# create release
curl_release_result=$(curl -v --request POST --header "PRIVATE-TOKEN: ${PRIVATE_TOKEN}" --header "Content-Type: application/json" --data '{"name": "'"$CI_COMMIT_TAG"'", "tag_name": "'"$CI_COMMIT_TAG"'", "description": '"$markdown"', "milestones": ["'"$CI_COMMIT_TAG"'"]}' https://gitlab.com/api/v4/projects/12882469/releases)
echo "$curl_release_result"
echo ""

# if the results contain error then we failed to upload the file

if [[ $curl_release_result == *"error"* ]]; then
    exit 1
fi

exit 0