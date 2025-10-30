#!/bin/bash


SLEEP_SECONDS=30 
COMMIT_BASE_NAME="Fluctuation test commit"
BRANCH_NAME="buildbuddy_kt_cov_2"
REMOTE_NAME="origin"
REFRESH_SECONDS=10 

# git checkout -b ${BRANCH_NAME}

wait_for_workflow_completion() {
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
  wait_for_workflow_completion
  echo ">>> $i. Empty commit: (p$i)"
  git commit --allow-empty -m "${COMMIT_BASE_NAME} p$i" && git push ${REMOTE_NAME} ${BRANCH_NAME}
done
echo -e "\nAll process completed"
