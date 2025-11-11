#!/bin/bash
set -euo pipefail

DORMAND_BRANCH="releases/fluctuation_test_$(date +%Y_%m_%d_%H_%M_%S)_dormand"
ACTIVE_BRANCH="releases/fluctuation_test_$(date +%Y_%m_%d_%H_%M_%S)_active"

git checkout -b ${DORMAND_BRANCH}
git checkout -b ${ACTIVE_BRANCH}


REMOTE_NAME="origin"
POLL_SECONDS=5
REFRESH_SECONDS=15
TOTAL_RUNS=10


TEST_BUILD_CONTENT="test_build.txt"
CODE_1_CONTENT="FillableTemplateTest_file.txt"
CODE_2_CONTENT="SortedListsTest_file.txt"

DEST_TEST_DIR="src/test/kotlin/org/wfanet/measurement/common"

CODE_1_CONTENT_FILE="$DEST_TEST_DIR/FillableTemplateTest.kt"
CODE_2_CONTENT_FILE="$DEST_TEST_DIR/SortedListsTest.kt"
TEST_BUILD_CONTENT_FILE="$DEST_TEST_DIR/BUILD.bazel"

BUILD_FILE="src/test/kotlin/org/wfanet/measurement/common/grpc/BUILD.bazel"
VICTIM_FILE="src/main/kotlin/org/wfanet/measurement/common/grpc/Interceptors.kt"

INJECTED_CONTENT=$(cat <<EOF
// --- INJECTED FOR CACHE TEST ---
fun injectedFunction1() {
    println("Injected function 1 executed")
}
fun injectedFunction2() {
    println("Injected function 2 executed")
}
fun injectedFunction3() {
    println("Injected function 3 executed")
}
// --- END INJECTED ---
EOF
)


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
  echo "Running $DORMAND_BRANCH"
  git checkout $DORMAND_BRANCH
  git commit --allow-empty -m "Dormand $i"
  git push $REMOTE_NAME $DORMAND_BRANCH

  echo "Running $ACTIVE_BRANCH"
  git checkout $ACTIVE_BRANCH
  commit_msg=""

  sed -i '/\/\/ --- INJECTED FOR CACHE TEST ---/,/\/\/ --- END INJECTED ---/d' "$VICTIM_FILE" || true
  if [ $(($i % 2)) -eq 0 ]; then
    echo "Comment RateLimiterProviderTest and delete functions"
    awk -v name='RateLimiterProviderTest' -v mode='comment' '
      function cnt_paren(s,   tmp,o,c){ tmp=s; o=gsub(/\(/,"(",tmp); c=gsub(/\)/,")",tmp); return o-c }
      {
        line=$0
        # detectar inicio (puede estar comentado ya con #)
        if (!in_block && line ~ /^[[:space:]]*#?[[:space:]]*kt_jvm_test[[:space:]]*\(/) {
          in_block=1; n=0; depth = cnt_paren(line)
          buf[++n]=line; next
        }
        if (in_block) {
          buf[++n]=line
          depth += cnt_paren(line)
          if (depth==0) {
            # unir y chequear si el bloque tiene name
            block_has_name=0
            for(i=1;i<=n;i++) if (buf[i] ~ "name[[:space:]]*=.*\"" name "\"") block_has_name=1
            if (block_has_name) {
              for(i=1;i<=n;i++) {
                # si ya estaba comentada, no duplicar #
                if (buf[i] ~ /^[[:space:]]*#/) print buf[i]
                else print "#" buf[i]
              }
            } else {
              for(i=1;i<=n;i++) print buf[i]
            }
            in_block=0; n=0; next
          }
          next
        }
        print
      }
      ' "$BUILD_FILE" > "$BUILD_FILE".tmp && mv "$BUILD_FILE".tmp "$BUILD_FILE"

    
    echo "Cycle $i (ODD): Deleting victim targets..."
    rm -rf "$CODE_1_CONTENT_FILE"
    rm -rf "$CODE_2_CONTENT_FILE"
    rm -rf "$TEST_BUILD_CONTENT_FILE"
    commit_msg="ACTIVE $i: Deleted test targets, comment test and delete empty functions"
  else
    echo "Discomment RateLimiterProviderTest and add functions"
    echo "$INJECTED_CONTENT" >> "$VICTIM_FILE"
    awk -v name='RateLimiterProviderTest' -v mode='uncomment' '
      function cnt_paren(s,   tmp,o,c){ tmp=s; o=gsub(/\(/,"(",tmp); c=gsub(/\)/,")",tmp); return o-c }
      {
        line=$0
        if (!in_block && line ~ /^[[:space:]]*#?[[:space:]]*kt_jvm_test[[:space:]]*\(/) {
          in_block=1; n=0; depth = cnt_paren(line)
          buf[++n]=line; next
        }
        if (in_block) {
          buf[++n]=line
          depth += cnt_paren(line)
          if (depth==0) {
            block_has_name=0
            for(i=1;i<=n;i++) {
              # chequear con/ sin # si contiene name
              tmp=buf[i]; gsub(/^[[:space:]]*#/,"",tmp)
              if (tmp ~ "name[[:space:]]*=.*\"" name "\"") block_has_name=1
            }
            if (block_has_name) {
              for(i=1;i<=n;i++) {
                line=buf[i]
                sub(/^[[:space:]]*#/,"",line)   # quitar un solo '#' inicial si existe
                print line
              }
            } else {
              for(i=1;i<=n;i++) print buf[i]
            }
            in_block=0; n=0; next
          }
          next
        }
        print
      }
      ' "$BUILD_FILE" > "$BUILD_FILE".tmp && mv "$BUILD_FILE".tmp "$BUILD_FILE"


    echo "Cycle $i (EVEN): Creating victim targets..."
    mkdir -p "$DEST_TEST_DIR"
    cp "$CODE_1_CONTENT" "$CODE_1_CONTENT_FILE"
    cp "$CODE_2_CONTENT" "$CODE_2_CONTENT_FILE"
    cp "$TEST_BUILD_CONTENT" "$TEST_BUILD_CONTENT_FILE"
    commit_msg="ACTIVE $i: Created test targets, discomment test and add empty functions"
  fi

  git add .
  git commit --allow-empty -m "$commit_msg"
  git push $REMOTE_NAME $ACTIVE_BRANCH
  wait_for_workflow_completion $DORMAND_BRANCH
  wait_for_workflow_completion $ACTIVE_BRANCH

done

echo -e "\n==========================================================="
echo "Done"

