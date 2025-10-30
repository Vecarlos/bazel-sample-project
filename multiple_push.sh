#!/bin/bash

POLLUTER_BRANCH="buildbuddy_kt_cov"
VICTIM_BRANCH="buildbuddy_kt_cov_2"

REMOTE_NAME="origin"
POLL_SECONDS=900
REFRESH_SECONDS=15
TOTAL_RUNS=6


wait_for_workflow_completion() {
  sleep ${REFRESH_SECONDS}
  local branch_to_check="$1"
  local active_runs=$(gh run list --json status,headBranch --jq 'map(select(.headBranch == "'${branch_to_check}'" and (.status == "in_progress" or .status == "queued"))) | length')

  while [[ "$active_runs" -gt 0 ]]; do
    echo "--- Running '$branch_to_check'. Waiting ${POLL_SECONDS}s..."
    sleep ${POLL_SECONDS}
    active_runs=$(gh run list --json status,headBranch --jq 'map(select(.headBranch == "'${branch_to_check}'" and (.status == "in_progress" or .status == "queued"))) | length')
  done
}

for i in $(seq 1 $TOTAL_RUNS)
do
  echo -e "\n================ Cycle $i / $TOTAL_RUNS ==================="

  echo "Running $POLLUTER_BRANCH"
  git checkout $POLLUTER_BRANCH
  git commit --allow-empty -m "$POLLUTER_BRANCH $i"
  git push $REMOTE_NAME $POLLUTER_BRANCH
  wait_for_workflow_completion $POLLUTER_BRANCH

  echo "Running $VICTIM_BRANCH"
  git checkout $VICTIM_BRANCH
  git commit --allow-empty -m "$VICTIM_BRANCH $i"
  git push $REMOTE_NAME $VICTIM_BRANCH
  wait_for_workflow_completion $VICTIM_BRANCH
done

echo -e "\n==========================================================="
echo "Done"
