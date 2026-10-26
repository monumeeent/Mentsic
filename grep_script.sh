#!/bin/bash
cd /d/Mentsic || exit 1
OUT=/d/Mentsic/grep_out.txt
: > "$OUT"
echo "=== Material3IconButton ===" >> "$OUT"
grep -rn "Material3IconButton(" app/src/main/kotlin/ >> "$OUT" 2>&1 || true
echo "=== MaterialIconButton ===" >> "$OUT"
grep -rn "MaterialIconButton(" app/src/main/kotlin/ >> "$OUT" 2>&1 || true
echo "=== LCImport count ===" >> "$OUT"
grep -rn "import com.metrolist.music.ui.component.LongClickIconButton" app/src/main/kotlin/ >> "$OUT" 2>&1 || true
echo "DONE" >> "$OUT"
