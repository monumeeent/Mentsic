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
* When a build error indicates missing files, layouts, or resources, **never run exploratory shell commands** (such as `git `, recursive file searches, or deep directory listings) to investigate or locate them.
* Do not Run Git Command Ask The User Instead. 
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
* Capture any encountered errors, build failures, or debugging context into `tests/errornote.md` for future reference and troubleshooting.

## 7. Skill Routing & Activation Matrix
* Activate specific skills based on the exact nature of the task and user request:

Core Engineering & System Maintenance
- gravitas: Trigger for repository-changing engineering tasks (implementation, complex refactoring, bug fixes, code reviews). Enforces scoped reconnaissance, adaptive planning, requirement tracking, and evidence-backed verification.

- ponytail: Trigger for concise, minimal-diff coding tasks. Use when asked to "be lazy", "keep it simple", "avoid over-engineering", or when implementing surgical single-file fixes with zero bloat.

- reverse-engineer-anything: Trigger when analyzing compiled binaries, native artifacts, Electron/JS bundles, firmware, or browser runtimes to decompile code or trace behavior. Skip for standard source-code analysis.

Design, UI/UX & Art Direction
- design-taste-frontend: Trigger when building, reviewing, or restyling public-facing web pages, landing pages, marketing sites, or portfolios. Use when an interface looks generic, "AI-made", or sloppy, and requires high-end gallery-level art direction.

- impeccable: Trigger when critique, auditing, or designing component-level UI/UX across web and mobile (dashboards, forms, settings screens, empty states, onboarding). Handles layout hierarchy, typography, motion, micro-copy, and DESIGN.md documentation.

- ui-ux-pro-max: Trigger when needing design system intelligence, stack-specific layout rules, dark-mode palettes, color profiles, font pairings, chart choices, accessibility guidelines, or GSAP animation presets.

## 8 Read Files
- Read Only Necessary Files 

## 9 Full Project in Simple 
- Read `rea.md`