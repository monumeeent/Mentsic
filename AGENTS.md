# Agent Instructions & Guidelines

## 1. Execution & Hardware Safety Rules
* **HARD RESTRICTION:** Never execute local Gradle or Android build commands (e.g., `./gradlew`, `gradle build`, `assembleDebug`, `compileFossDebugKotlin`, `test`, `lint`).
* Never run background polling, sleep timers, or wait loops (e.g., `sleep`, `timeout`).
* All output must be written in English.

## 2. Git Command Restrictions 
- Trigger this only if user : 
    the user asks to run `git acp` (Add,Commit,Push)
**Only Allowed** To Run This 3 Git Commands : Add, Commit, Push. 
- Git Add : add the changed Files
- Git Commit : Proper Short Message
- Git Push : Just Push it 
* Run The Git Command To Push to Workflow and Let The App Be Built. 

## 3. Design System & UI Rules
* Refer to `ui_rulebook.md` for all color tokens, typography scales, spacing rules, and reusable component patterns.
* Ask The User if anything need to confirmed then continue work .

## 4. Constraints 
- No local Gradle/Android build commands; verification via static analysis + CI on push
- Git Bash compatible terminal commands only
- Always tell me the **Next Step:** `How to Test the new features`.