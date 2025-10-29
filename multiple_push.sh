#!/bin/bash

POLLUTER_BRANCH="buildbuddy_kt_cov"
VICTIM_BRANCH="buildbuddy_kt_cov_2"

REMOTE_NAME="origin"
CI_WAIT_TIME_SECONDS=1700

TOTAL_RUNS=6

for i in $(seq 1 $TOTAL_RUNS)
do
  echo -e "\n================ Cycle $i / $TOTAL_RUNS ==================="

  echo "Running $POLLUTER_BRANCH"
  git checkout $POLLUTER_BRANCH
  git commit --allow-empty -m "$POLLUTER_BRANCH $i"
  git push $REMOTE_NAME $POLLUTER_BRANCH

  echo "Running $VICTIM_BRANCH"
  git checkout $VICTIM_BRANCH
  git commit --allow-empty -m "$VICTIM_BRANCH $i"
  git push $REMOTE_NAME $VICTIM_BRANCH

  echo -e "\n~ Waiting $CI_WAIT_TIME_SECONDS seconds..."
  sleep $CI_WAIT_TIME_SECONDS

done

echo -e "\n==========================================================="
echo "Done"
