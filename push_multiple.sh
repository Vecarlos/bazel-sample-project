#!/bin/bash


POLL_SECONDS=5 
COMMIT_BASE_NAME="Fluctuation test commit"
BRANCH_NAME="releases/fluctuation_test_$(date +%Y_%m_%d_%H_%M_%S)"
REMOTE_NAME="origin"
REFRESH_SECONDS=10 

git checkout -b ${BRANCH_NAME}

wait_for_workflow_completion() {
  sleep ${REFRESH_SECONDS}  
  local branch_to_check="$1"
  local active_runs=$(gh run list --json status,headBranch --jq 'map(select(.headBranch == "'${branch_to_check}'" and (.status == "in_progress" or .status == "queued"))) | length')

  while [[ "$active_runs" -gt 0 ]]; do
    echo "--- Workflow activo en '$branch_to_check'. Esperando ${POLL_SECONDS}s..."
    sleep ${POLL_SECONDS}
    active_runs=$(gh run list --json status,headBranch --jq 'map(select(.headBranch == "'${branch_to_check}'" and (.status == "in_progress" or .status == "queued"))) | length')
  done
}

for i in {1..6}
do
  wait_for_workflow_completion $BRANCH_NAME
  echo ">>> $i. Empty commit: (p$i)"
  git commit --allow-empty -m "${COMMIT_BASE_NAME} p$i" && git push ${REMOTE_NAME} ${BRANCH_NAME}
done
echo -e "\nAll process completed"
