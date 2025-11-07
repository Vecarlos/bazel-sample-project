#!/bin/bash
set -euo pipefail

BRANCH_A_BRANCH="buildbuddy_kt_cov"
BRANCH_B_BRANCH="buildbuddy_kt_cov_3"
BRANCH_C_BRANCH="buildbuddy_kt_cov_2"

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
VICTIM_FILE="src/main/kotlin/org/wfanet/measurement/common/grpc/Interceptors.kt"

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
fun injectedFunction4() {
    println("Injected function 1 executed")
}
fun injectedFunction5() {
    println("Injected function 2 executed")
}
fun injectedFunction6() {
    println("Injected function 3 executed")
}
fun injectedFunction7() {
    println("Injected function 1 executed")
}
fun injectedFunction8() {
    println("Injected function 2 executed")
}
fun injectedFunction9() {
    println("Injected function 3 executed")
}
// --- END INJECTED ---
EOF
)



for i in $(seq 1 $TOTAL_RUNS)
do
  echo -e "\n================ Cycle $i / $TOTAL_RUNS ==================="
  echo "Running $BRANCH_A_BRANCH"
  git checkout $BRANCH_A_BRANCH
  commit_msg="empty"

  sed -i '/\/\/ --- INJECTED FOR CACHE TEST ---/,/\/\/ --- END INJECTED ---/d' "$VICTIM_FILE" || true
  if [ $(($i % 2)) -eq 0 ]; then
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

    commit_msg="Discomment RateLimiterProviderTest and add functions"
  else
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
    commit_msg="Comment RateLimiterProviderTest and delete functions"
  fi

  if [ $(($i % 2)) -eq 0 ]; then
    echo "Cycle $i (EVEN): Creating victim targets..."
    mkdir -p "$DEST_TEST_DIR"
    cp "$CODE_1_CONTENT" "$CODE_1_CONTENT_FILE"
    cp "$CODE_2_CONTENT" "$CODE_2_CONTENT_FILE"
    cp "$TEST_BUILD_CONTENT" "$TEST_BUILD_CONTENT_FILE"
    commit_msg="A $i: Created test targets"
  else
    echo "Cycle $i (ODD): Deleting victim targets..."
    rm -rf "$CODE_1_CONTENT_FILE"
    rm -rf "$CODE_2_CONTENT_FILE"
    rm -rf "$TEST_BUILD_CONTENT_FILE"
    commit_msg="A $i: Deleted test targets"
  fi


  git add .
  git commit --allow-empty -m "$commit_msg - $i"
  git push $REMOTE_NAME $BRANCH_A_BRANCH
  # wait_for_workflow_completion $BRANCH_A_BRANCH

  echo "Running $BRANCH_B_BRANCH"
  git checkout $BRANCH_B_BRANCH

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
    commit_msg="Comment RateLimiterProviderTest and delete functions"
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

    commit_msg="Discomment RateLimiterProviderTest and add functions"
  fi

  if [ $(($i % 2)) -eq 0 ]; then
    echo "Cycle $i (ODD): Deleting victim targets..."
    rm -rf "$CODE_1_CONTENT_FILE"
    rm -rf "$CODE_2_CONTENT_FILE"
    rm -rf "$TEST_BUILD_CONTENT_FILE"
    commit_msg="B $i: Deleted test targets"
  else
    echo "Cycle $i (EVEN): Creating victim targets..."
    mkdir -p "$DEST_TEST_DIR"
    
    cp "$CODE_1_CONTENT" "$CODE_1_CONTENT_FILE"
    cp "$CODE_2_CONTENT" "$CODE_2_CONTENT_FILE"
    cp "$TEST_BUILD_CONTENT" "$TEST_BUILD_CONTENT_FILE"
    commit_msg="B $i: Created test targets"
  fi

  git add .
  git commit --allow-empty -m "$commit_msg"
  git push $REMOTE_NAME $BRANCH_B_BRANCH

 
  wait_for_workflow_completion $BRANCH_A_BRANCH
  wait_for_workflow_completion $BRANCH_B_BRANCH
done

echo -e "\n==========================================================="
echo "Done"