# Agent Instructions & Guidelines

## 1. Execution & Hardware Safety Rules
* **HARD RESTRICTION:** Never execute local Gradle or Android build commands (e.g., `./gradlew`, `gradle build`, `assembleDebug`, `compileFossDebugKotlin`, `test`, `lint`).
* Never run background polling, sleep timers, or wait loops (e.g., `sleep`, `timeout`).
* Avoid CPU/RAM-heavy commands. Rely on static code analysis, precise file inspections, and remote CI validation.
* Use native file tools or Git Bash compatible commands (`bash`).
* Do **NOT** hallucinate unavailable tool names (e.g., `shell`).
* All output must be written in English.

---

## 2. Terminal & Shell Command Rule
* **DEFAULT TERMINAL:** All terminal command snippets, scripts, or examples provided must target **Git Bash** compatibility as long as possible (using standard Unix-like utilities available in Git Bash on Windows).

---

## 3. Missing File & Resource Handling Rule
* When a build error indicates missing files, layouts, or resources, **never run exploratory shell commands** (such as `git log`, recursive file searches, or deep directory listings) to investigate or locate them.
* Always assume the file is missing, and **immediately ask the user to provide the correct file path or directory** instead of guessing or searching through git history.

---

## 4. Preferred CI/CD Workflow
* **Verification Path:** Code edits $\rightarrow$ User commits/pushes $\rightarrow$ GitHub Actions workflow builds.
* When local compilation is requested, **DO NOT** execute it. Output the exact command for the user to copy-paste manually, then suggest pushing to GitHub instead:
  > *"I will not run Gradle locally on this low-spec machine. If you wish to run it manually: `./gradlew assembleDebug`. Otherwise, please push to GitHub to trigger the CI build."*

---

## 5. Design System & UI Rules
* Refer to `ui_rulebook.md` for all color tokens, typography scales, spacing rules, and reusable component patterns.
* Do **NOT** hardcode colors or raw font sizes in new Composables; always use `MaterialTheme.colorScheme` and `MaterialTheme.typography`.

---

## 6. Error Logging
* Capture any encountered errors, build failures, or debugging context into `errornote.md` for future reference and troubleshooting.