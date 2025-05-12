bazel_python="$1"
ninja_bin="$2"
pytype_binary="$3"
proto_gen_root="$4"

python_bin_dir=$(dirname "$bazel_python")
ninja_bin_dir=$(dirname "$ninja_bin")
export PATH="${python_bin_dir}:${ninja_bin_dir}:$PATH"
python_paths=$(find "${BUILD_WORKING_DIRECTORY}/src/main/python" -type d | paste -sd:)
python_paths_test=$(find "${BUILD_WORKING_DIRECTORY}/src/test/python" -type d | paste -sd:)


python_files_to_analyze=$(find "${BUILD_WORKING_DIRECTORY}/src/main/python" "${BUILD_WORKING_DIRECTORY}/src/test/python" -name '*.py' ! -name '__init__.py') 
for file in ${python_files_to_analyze}
do
  echo -e "\n${file}"
  ${pytype_binary} --pythonpath "${python_paths}:${python_paths_test}:${proto_gen_root}" input ${file}
done
