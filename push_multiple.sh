#!/bin/bash

DELAY_SECONDS=100 #Change accordingly to build time

COMMIT_BASE_NAME="Fluctuation test commit"
BRANCH_NAME="fluctuation_test_$(date +%Y_%m_%d_%H:%M:%S)"
REMOTE_NAME="origin"
git checkout -b ${BRANCH_NAME}

for i in {1..6}
do
  echo ">>> $i. Empty commit: (p$i)"
  git commit --allow-empty -m "${COMMIT_BASE_NAME} p$i" && git push ${REMOTE_NAME} 
${BRANCH_NAME}
  echo -e "\n--- Waiting ${DELAY_SECONDS} seconds"
  sleep ${DELAY_SECONDS}
done
echo -e "\nAll process completed"
