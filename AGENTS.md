# Agent Instructions & Guidelines

## 1. Execution & Hardware Safety Rules
* **HARD RESTRICTION:** Never execute local Gradle or Android build commands (e.g., `./gradlew`, `gradle build`, `assembleDebug`, `compileFossDebugKotlin`, `test`, `lint`).
* Never run background polling, sleep timers, or wait loops (e.g., `sleep`, `timeout`).
* All output must be written in English.

## 2. Preferred CI/CD Workflow
* When local compilation is requested, **DO NOT** execute it. Output the exact command for the user to copy-paste manually, then push to GitHub instead:
  > *"I will not run Gradle locally on this low-spec machine. If you wish to run it manually: `./gradlew assembleDebug`. Otherwise, please push to GitHub to trigger the CI build."*

* add , commit , push > **Verification Path:** Code edits $\rightarrow$ User commits/pushes $\rightarrow$ GitHub Actions workflow builds.

## 5. Design System & UI Rules
* Refer to `ui_rulebook.md` for all color tokens, typography scales, spacing rules, and reusable component patterns.


## 6. Constraints 
- No local Gradle/Android build commands; verification via static analysis + CI on push
- Git Bash compatible terminal commands only
- Always tell me the **Next Step:** `How to Test the new features`.