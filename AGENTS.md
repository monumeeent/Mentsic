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


## 3. Missing File & Resource Handling Rule
* When a build error indicates missing files, layouts, or resources, **never run exploratory shell commands** (recursive file searches, or deep directory listings) to investigate or locate them.

* Always assume the file is missing, and **immediately ask the user to provide the correct file path or directory** instead of guessing or searching through git history.


## 4. Preferred CI/CD Workflow
* When local compilation is requested, **DO NOT** execute it. Output the exact command for the user to copy-paste manually, then suggest pushing to GitHub instead:
  > *"I will not run Gradle locally on this low-spec machine. If you wish to run it manually: `./gradlew assembleDebug`. Otherwise, please push to GitHub to trigger the CI build."*

* Instead Follow add , commit , push > **Verification Path:** Code edits $\rightarrow$ User commits/pushes $\rightarrow$ GitHub Actions workflow builds.

## 5. Design System & UI Rules
* Refer to `ui_rulebook.md` for all color tokens, typography scales, spacing rules, and reusable component patterns.


## 6. Constraints 
- No local Gradle/Android build commands; verification via static analysis + CI on push
- Git Bash compatible terminal commands only
- Always tell me the **Next Step:** `What to do next after Build Compilation`  .
- How to Test the new features