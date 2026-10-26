#!/bin/bash
# Audit IconButton import/call consistency across app sources.
cd /d/Mentsic || exit 1
for f in $(grep -rl "IconButton(" app/src/main/kotlin/ 2>/dev/null); do
  m3imp=$(grep -c "^import androidx.compose.material3.IconButton$" "$f")
  aliasimp=$(grep -c "^import androidx.compose.material3.IconButton as Material3IconButton$" "$f")
  lcimp=$(grep -c "component.LongClickIconButton$" "$f")
  badimp=$(grep -c "component.IconButton$" "$f")
  clc=$(grep -c "LongClickIconButton(" "$f")
  calias=$(grep -c "Material3IconButton(" "$f")
  cbare=$(grep -oE "(^|[^.[:alnum:]_])IconButton\(" "$f" | wc -l)
  issues=""
  [ "$badimp" -gt 0 ] && issues="$issues [BAD_component_IconButton_import]"
  [ "$cbare" -gt 0 ] && [ "$m3imp" -eq 0 ] && issues="$issues [BARE_CALLS_$cbare_NO_M3_import]"
  [ "$calias" -gt 0 ] && [ "$aliasimp" -eq 0 ] && issues="$issues [ALIAS_CALLS_$calias_no_alias_import]"
  [ "$clc" -gt 0 ] && [ "$lcimp" -eq 0 ] && [ "$(basename $f)" != "IconButton.kt" ] && issues="$issues [LC_CALLS_$clc_no_import]"
  [ -n "$issues" ] && echo "$f ::$issues"
done
echo "AUDIT_DONE"
