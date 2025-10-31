#!/bin/bash
set -euo pipefail

BRANCH_A_BRANCH="buildbuddy_kt_cov"
BRANCH_B_BRANCH="buildbuddy_kt_cov_2"

REMOTE_NAME="origin"
POLL_SECONDS=15
REFRESH_SECONDS=15
TOTAL_RUNS=6


TEST_BUILD_CONTENT="test_build.txt"
CODE_1_CONTENT="FillableTemplateTest_file.txt"
CODE_2_CONTENT="SortedListsTest_file.txt"

DEST_TEST_DIR="src/test/kotlin/org/wfanet/measurement/common"

CODE_1_CONTENT_FILE="$DEST_TEST_DIR/FillableTemplateTest.kt"
CODE_2_CONTENT_FILE="$DEST_TEST_DIR/SortedListsTest.kt"
TEST_BUILD_CONTENT_FILE="$DEST_TEST_DIR/BUILD.bazel"


BUILD_FILE="src/test/kotlin/org/wfanet/measurement/common/grpc/BUILD.bazel"


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
  echo "Running $BRANCH_A_BRANCH"
  git checkout $BRANCH_A_BRANCH
  commit_msg=""

  if [ $(($i % 2)) -eq 1 ]; then
      echo "Cycle $i (ODD): Commenting RateLimiterProviderTest in $BUILD_FILE..."
      sed -i '/name = "RateLimiterProviderTest"/,/^)$/{s/^/#/}' "$BUILD_FILE" || true
      
      commit_msg="BRANCH A $i: Commented RateLimiterProviderTest"
  else
      echo "Cycle $i (EVEN): Uncommenting RateLimiterProviderTest in $BUILD_FILE..."
      sed -i '/# *name = "RateLimiterProviderTest"/,/^#)$/{s/^#//}' "$BUILD_FILE" || true
      commit_msg="BRANCH A $i: Uncommented RateLimiterProviderTest"
  fi

  git add .
  git commit --allow-empty -m "$commit_msg"
  git push $REMOTE_NAME $BRANCH_A_BRANCH
  wait_for_workflow_completion $BRANCH_A_BRANCH

  echo "Running $BRANCH_B_BRANCH"
  git checkout $BRANCH_B_BRANCH

  if [ $(($i % 2)) -eq 1 ]; then
    echo "Cycle $i (ODD): Deleting victim targets..."
    rm -rf "$CODE_1_CONTENT_FILE"
    rm -rf "$CODE_2_CONTENT_FILE"
    rm -rf "$TEST_BUILD_CONTENT_FILE"
    commit_msg="VICTIM $i: Deleted test targets"
  else
    echo "Cycle $i (EVEN): Creating victim targets..."
    mkdir -p "$DEST_TEST_DIR"
    
    cp "$CODE_1_CONTENT" "$CODE_1_CONTENT_FILE"
    cp "$CODE_2_CONTENT" "$CODE_2_CONTENT_FILE"
    cp "$TEST_BUILD_CONTENT" "$TEST_BUILD_CONTENT_FILE"
    commit_msg="VICTIM $i: Created test targets"
  fi

  git add .
  git commit --allow-empty -m "$commit_msg"
  git push $REMOTE_NAME $BRANCH_B_BRANCH
  wait_for_workflow_completion $BRANCH_B_BRANCH
done

echo -e "\n==========================================================="
echo "Done"