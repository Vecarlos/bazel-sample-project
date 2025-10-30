#!/bin/bash


SLEEP_SECONDS=30 
COMMIT_BASE_NAME="Fluctuation test commit"
BRANCH_NAME="releases/fluctuation_test_$(date +%Y_%m_%d_%H_%M_%S)"
REMOTE_NAME="origin"

git checkout -b ${BRANCH_NAME}


wait_for_workflow_completion() {
  local active_runs=$(gh run list --json status,headBranch --jq 'map(select(.headBranch == "'${BRANCH_NAME}'" and (.status == "in_progress" or .status == "queued"))) | length'
)

  echo "Checking if active workflows"
  while [[ "$active_runs" -gt 0 ]]; do
    echo -e "\n--- Waiting ${SLEEP_SECONDS} seconds"
    sleep ${SLEEP_SECONDS}
    active_runs=$(gh run list --json status,headBranch --jq 'map(select(.headBranch == "'${BRANCH_NAME}'" and (.status == "in_progress" or .status == "queued"))) | length'
)
  done
}

for i in {1..6}
do
  wait_for_workflow_completion()
  echo ">>> $i. Empty commit: (p$i)"
  git commit --allow-empty -m "${COMMIT_BASE_NAME} p$i" && git push ${REMOTE_NAME} ${BRANCH_NAME}
done
echo -e "\nAll process completed"
