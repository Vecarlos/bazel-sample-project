#!/bin/bash

set -eu

OUTPUT=

function parse_option() {
    local -r opt="$1"
    if [[ "${OUTPUT}" = "1" ]]; then
        OUTPUT=$opt
    elif [[ "$opt" = "-o" ]]; then
        # output is coming
        OUTPUT=1
    fi
}

# let parse the option list
for i in "$@"; do
    if [[ "$i" = @* && -r "${i:1}" ]]; then
        while IFS= read -r opt
        do
            parse_option "$opt"
        done < "${i:1}" || exit 1
    else
        parse_option "$i"
    fi
done

# Set-up the environment


# Call the C++ compiler
/usr/lib/llvm-14/bin/clang "$@"

# Generate an empty file if header processing succeeded.
if [[ "${OUTPUT}" == *.h.processed ]]; then
  echo -n > "${OUTPUT}"
fi