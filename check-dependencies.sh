#!/bin/bash

# check if any dependencies are out of date
./gradlew dependencyUpdates -DoutputFormatter=json

# I don't expect anything to be exceeded or unresolved, mainly checking outdated and gradle
exceeded=$(cat build/dependencyUpdates/report.json | jq '.exceeded.count')
outdated=$(cat build/dependencyUpdates/report.json | jq '.outdated.count')
unresolved=$(cat build/dependencyUpdates/report.json | jq '.unresolved.count')
gradle=$(cat build/dependencyUpdates/report.json | jq '.gradle.current.isUpdateAvailable')

# fail is any exceeded current versions
if [ $exceeded -gt 0 ]
then
  echo "Some exceeded dependencies found"
  exit 1
fi

# fail if any are out of date
if [ $outdated -gt 0 ]
then
  echo "Some outdated dependencies found"
  exit 1
fi

# fail if any were unresolved
if [ $unresolved -gt 0 ]
then
  echo "Some unresolved dependencies found"
  exit 1
fi

# fail if gradle can be updated
if [ $gradle = "true" ]
then
  echo "Gradle requires update"
  exit 1
fi
