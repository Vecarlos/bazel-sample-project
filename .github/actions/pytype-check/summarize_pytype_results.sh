echo "Total errors:"
count=$(grep -c "error:" pytype_output.txt || echo 0)
count=$(echo "$count" | head -n 1)
echo "$count"

echo ""
echo "Error type:"
grep -o "https://google.github.io/pytype/errors.html#[^ ]*" pytype_output.txt | sed 's/.*#//' | sort | uniq -c | sed 's/^[[:space:]]*//' || echo "None"

echo ""
echo "Files affected:"
grep "error:" pytype_output.txt | cut -d':' -f1 | sort | uniq || echo "None"

if [ "$count" -gt 0 ]; then
    echo "::error ::Pytype found $count errors"
    exit 1
fi