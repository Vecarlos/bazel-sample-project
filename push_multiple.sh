#!/bin/bash


COMMIT_BASE_NAME="no array put"
BRANCH_NAME="buildbuddy"
REMOTE_NAME="origin"
DELAY_SECONDS=80
WORKFLOW_NAME=".github/workflows/build-test.yml"

echo ">>> 1. Add and commit"
git add .
git commit -m "$COMMIT_BASE_NAME" && git push $REMOTE_NAME $BRANCH_NAME

for i in {2..6}
do
  echo -e "\n--- Waiting $DELAY_SECONDS seconds"
  sleep $DELAY_SECONDS

  echo ">>> $i. Empty commit: (p$i)"
  # git commit --allow-empty -m "$COMMIT_BASE_NAME p$i" && git push $REMOTE_NAME $BRANCH_NAME
  gh workflow run "$WORKFLOW_NAME" --ref "$BRANCH_NAME"
done
echo -e "\nAll process completed"


