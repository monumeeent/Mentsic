#!/bin/bash
cd /d/Mentsic || exit 1
OUT=/d/Mentsic/iconbutton_report.txt
: > "$OUT"
FILES=$(grep -rl "IconButton(" app/src/main/kotlin/ 2>/dev/null)
for f in $FILES; do
  {
    echo "### $f"
    grep -nE "^import .*(IconButton|LongClickIconButton)" "$f" | sed 's/^/  IMPORT /'
    grep -nE "(^|[^.[:alnum:]_])IconButton\(" "$f" | sed 's/^/  BARE /'
    grep -nE "Material3IconButton\(" "$f" | sed 's/^/  ALIAS /'
    grep -nE "LongClickIconButton\(" "$f" | sed 's/^/  LONG /'
  } >> "$OUT"
done
echo "DONE" >> "$OUT"
