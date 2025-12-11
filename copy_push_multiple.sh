#!/bin/bash
set -euo pipefail


comment_bazel_target() {
    local target_name="$1"
    local file_path="$2"  

    echo "🔧 Comentando target '$target_name' en '$file_path'..."

    awk -v name="$target_name" -v mode='comment' '
      function cnt_paren(s,   tmp,o,c){ tmp=s; o=gsub(/\(/,"(",tmp); c=gsub(/\)/,")",tmp); return o-c }
      {
        line=$0
        if (!in_block && line ~ /^[[:space:]]*#?[[:space:]]*[a-zA-Z0-9_]+_test[[:space:]]*\(/) {
          in_block=1; n=0; depth = cnt_paren(line)
          buf[++n]=line; next
        }
        if (in_block) {
          buf[++n]=line
          depth += cnt_paren(line)
          
          if (depth==0) {
            block_has_name=0
            for(i=1;i<=n;i++) if (buf[i] ~ "name[[:space:]]*=[[:space:]]*\"" name "\"") block_has_name=1
            
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
      ' "$file_path" > "${file_path}.tmp" && mv "${file_path}.tmp" "$file_path"
}

uncomment_bazel_target() {
    local target_name="$1"
    local file_path="$2"


    echo "🔓 UNcomment target '$target_name' in '$file_path'..."

    awk -v name="$target_name" -v mode='uncomment' '
      function cnt_paren(s,   tmp,o,c){ tmp=s; o=gsub(/\(/,"(",tmp); c=gsub(/\)/,")",tmp); return o-c }
      {
        line=$0
        # Detectar inicio (puede estar comentado con # o no)
        if (!in_block && line ~ /^[[:space:]]*#?[[:space:]]*[a-zA-Z0-9_]+_test[[:space:]]*\(/) {
          in_block=1; n=0; depth = cnt_paren(line)
          buf[++n]=line; next
        }
        if (in_block) {
          buf[++n]=line
          depth += cnt_paren(line)
          
          if (depth==0) {
            block_has_name=0
            for(i=1;i<=n;i++) {
              # Crear copia temporal limpia de # para buscar el nombre
              tmp=buf[i]
              gsub(/^[[:space:]]*#/,"",tmp) 
              # Regex flexible para encontrar name = "TARGET"
              if (tmp ~ "name[[:space:]]*=[[:space:]]*\"" name "\"") block_has_name=1
            }
            
            if (block_has_name) {
              for(i=1;i<=n;i++) {
                line=buf[i]
                # Quitar el # inicial y espacios precedentes al #
                sub(/^[[:space:]]*#/,"",line)   
                print line
              }
            } else {
              # Si no es el target buscado, imprimir tal cual estaba
              for(i=1;i<=n;i++) print buf[i]
            }
            in_block=0; n=0; next
          }
          next
        }
        print
      }
      ' "$file_path" > "${file_path}.tmp" && mv "${file_path}.tmp" "$file_path"
}



TEST_BUILD_CONTENT="test_build.txt"
CODE_1_CONTENT="FillableTemplateTest_file.txt"
CODE_2_CONTENT="SortedListsTest_file.txt"

DEST_TEST_DIR="src/test/kotlin/org/wfanet/measurement/common"

CODE_1_CONTENT_FILE="$DEST_TEST_DIR/FillableTemplateTest.kt"
CODE_2_CONTENT_FILE="$DEST_TEST_DIR/SortedListsTest.kt"
TEST_BUILD_CONTENT_FILE="$DEST_TEST_DIR/BUILD.bazel"


BUILD_FILE_1="src/test/kotlin/org/wfanet/measurement/edpaggregator/service/v1alpha/BUILD.bazel"
TARGET_1="RequisitionMetadataServiceTest"

BUILD_FILE_2="src/test/kotlin/org/wfanet/measurement/eventdataprovider/requisition/v2alpha/common/BUILD.bazel"
TARGET_2="FrequencyVectorBuilderTest"

BUILD_FILE_3="src/test/kotlin/org/wfanet/measurement/kingdom/service/api/v2alpha/BUILD.bazel"
TARGET_3="EventGroupMetadataDescriptorsServiceTest"

# DORMAND_BRANCH="releases/fluctuation_test_$(date +%Y_%m_%d_%H_%M_%S)_dormand"
# ACTIVE_BRANCH="releases/fluctuation_test_$(date +%Y_%m_%d_%H_%M_%S)_active"
ACTIVE_BRANCH="buildbuddy_kt_cov_2_2"

# git checkout -b ${DORMAND_BRANCH}
# git checkout -b ${ACTIVE_BRANCH}


REMOTE_NAME="origin"
POLL_SECONDS=15
REFRESH_SECONDS=15
TOTAL_RUNS=10



# BUILD_FILE="src/test/kotlin/org/wfanet/measurement/common/grpc/BUILD.bazel"
# VICTIM_FILE_1="src/main/kotlin/org/wfanet/measurement/edpaggregator/service/internal/Errors.kt"
VICTIM_FILE_1="src/main/kotlin/org/wfanet/measurement/edpaggregator/eventgroups/EventGroupSync.kt"
VICTIM_FILE_2="src/main/kotlin/org/wfanet/measurement/eventdataprovider/requisition/v2alpha/common/FrequencyVectorBuilder.kt"
VICTIM_FILE_3="src/main/kotlin/org/wfanet/measurement/kingdom/service/api/v2alpha/EventGroupMetadataDescriptorsService.kt"
VICTIM_FILE_4="src/main/kotlin/org/wfanet/measurement/reporting/service/api/CelEnvProvider.kt"


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
    println("Injected function 4 executed")
}
fun injectedFunction5() {
    println("Injected function 5 executed")
}
fun injectedFunction6() {
    println("Injected function 6 executed")
}
fun injectedFunction7() {
    println("Injected function 7 executed")
}
fun injectedFunction8() {
    println("Injected function 8 executed")
}
fun injectedFunction9() {
    println("Injected function 9 executed")
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
  # echo "Running $DORMAND_BRANCH"
  # git checkout $DORMAND_BRANCH
  # git commit --allow-empty -m "Dormand $i"
  # git push $REMOTE_NAME $DORMAND_BRANCH

  echo "Running $ACTIVE_BRANCH"
  git checkout $ACTIVE_BRANCH
  commit_msg=""

  sed -i '/\/\/ --- INJECTED FOR CACHE TEST ---/,/\/\/ --- END INJECTED ---/d' "$VICTIM_FILE_1" || true
  sed -i '/\/\/ --- INJECTED FOR CACHE TEST ---/,/\/\/ --- END INJECTED ---/d' "$VICTIM_FILE_2" || true
  sed -i '/\/\/ --- INJECTED FOR CACHE TEST ---/,/\/\/ --- END INJECTED ---/d' "$VICTIM_FILE_3" || true
  sed -i '/\/\/ --- INJECTED FOR CACHE TEST ---/,/\/\/ --- END INJECTED ---/d' "$VICTIM_FILE_4" || true
  if [ $(($i % 2)) -eq 0 ]; then
    echo "Comment RequisitionMetadataServiceTest and delete functions"
    commit_msg="ACTIVE $i: Comment test and add empty functions"
  else
    echo "Uncomment RequisitionMetadataServiceTest and add functions"
    echo "$INJECTED_CONTENT" >> "$VICTIM_FILE_1"
     echo "$INJECTED_CONTENT" >> "$VICTIM_FILE_2"
     echo "$INJECTED_CONTENT" >> "$VICTIM_FILE_3"
     echo "$INJECTED_CONTENT" >> "$VICTIM_FILE_4"
    commit_msg="ACTIVE $i: Uncomment test and delete empty functions"
  fi

  git add .
  git commit --allow-empty -m "$commit_msg"
  git push $REMOTE_NAME $ACTIVE_BRANCH
  # wait_for_workflow_completion $DORMAND_BRANCH
  wait_for_workflow_completion $ACTIVE_BRANCH

done

echo -e "\n==========================================================="
echo "Done"

