# Execution & Hardware Safety Rules
- HARD RESTRICTION: Never execute local Gradle or Android build commands (e.g., `./gradlew`, `gradle build`, `assembleDebug`, `compileFossDebugKotlin`, `test`, `lint`).
- Never run background polling, sleep timers, or wait loops (e.g., `sleep`, `timeout`).
- Avoid CPU/RAM-heavy commands. Rely on static code analysis, precise file inspections, and remote CI validation.
Use native file tools or PowerShell equivalents.
- Do NOT hallucinate unavailable tool names (e.g., `shell`). Only use valid registered tools (`run_commands`).
- Output Should be in English . 

# Preferred CI/CD Workflow
- Path for verification: Code edits -> User commits/pushes -> GitHub Actions workflow builds.
- When local compilation is requested, DO NOT execute it. Output the exact command for the user to copy-paste manually, then suggest pushing to GitHub instead:
  > "I will not run Gradle locally on this low-spec machine. If you wish to run it manually: `./gradlew assembleDebug`. Otherwise, please push to GitHub to trigger the CI build."

## Design System & UI Rules
- Refer to `ui_rulebook.md` for all color tokens, typography scales, spacing rules, and reusable component patterns.
- Do NOT hardcode colors or raw font sizes in new Composables; always use `MaterialTheme.colorScheme` and `MaterialTheme.typography`.

## Error Logging
- Capture any encountered errors or build failures into `errornote.md` for future reference and troubleshooting.