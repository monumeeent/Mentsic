#!/bin/bash
# Final audit of all IconButton import/call consistency across app sources.
cd /d/Mentsic || exit 1
OUT=/d/Mentsic/iconbutton_audit_final.txt
: > "$OUT"

for f in $(grep -rl "IconButton(" app/src/main/kotlin/ 2>/dev/null); do
  m3imp=$(grep -c "^import androidx.compose.material3.IconButton$" "$f")
  aliasimp=$(grep -c "^import androidx.compose.material3.IconButton as Material3IconButton$" "$f")
  lcimp=$(grep -c "component.LongClickIconButton$" "$f")
  badimp=$(grep -c "component.IconButton$" "$f")
  clc=$(grep -c "LongClickIconButton(" "$f")
  calias=$(grep -c "Material3IconButton(" "$f")
  cqual=$(grep -c "androidx\.compose\.material3\.IconButton(" "$f")
  cbare=$(grep -oE "(^|[^.[:alnum:]_])IconButton\(" "$f" | wc -l)
  cresize=$(grep -c "ResizableIconButton(" "$f")
  cbare=$((cbare - cresize - clc - calias))
  issues=""
  [ "$badimp" -gt 0 ] && issues="$issues BAD_COMPONENT_ICONBUTTON_IMPORT"
  [ "$cbare" -gt 0 ] && [ "$m3imp" -eq 0 ] && [ "$cqual" -eq 0 ] && issues="$issues BARE_CALL_NO_M3_IMP:$cbare"
  [ "$calias" -gt 0 ] && [ "$aliasimp" -eq 0 ] && issues="$issues ALIAS_CALL_NO_IMPORT:$calias"
  [ "$clc" -gt 0 ] && [ "$lcimp" -eq 0 ] && [ "$(basename $f)" != "IconButton.kt" ] && issues="$issues LC_CALL_NO_IMPORT:$clc"
  if [ -n "$issues" ]; then
    echo "ISSUE:$f:$issues" >> "$OUT"
    echo "  IMPORTS: m3=$m3imp alias=$aliasimp lcimp=$lcimp bad=$badimp" >> "$OUT"
    echo "  CALLS: bare=$cbare alias=$calias lc=$clc qual=$cqual" >> "$OUT"
  fi
done
echo "AUDIT_DONE" >> "$OUT"
