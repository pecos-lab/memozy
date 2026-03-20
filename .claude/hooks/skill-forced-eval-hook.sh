#!/bin/bash
UserPromptSubmit hook that forces explicit skill evaluation
cat <<'EOF'
INSTRUCTION: MANDATORY SKILL ACTIVATION SEQUENCE

ALWAYS output your skill evaluation visibly. Never skip or internalize this process.

Step 1 - EVALUATE:
Review all available skills and identify relevant ones.
List ONLY the relevant skills:
[skill-name]: [brief reason why it's relevant]

If no skills are relevant, state "No skills needed" and skip to Step 3.

Step 2 - ACTIVATE:
For EACH relevant skill from Step 1, immediately call:
Skill(skill-name)

Step 3 - IMPLEMENT:
Proceed with implementation.

CRITICAL:
Do NOT list all skills with YES/NO (too verbose)
Do NOT skip Step 2 - you MUST call Skill() tool
Evaluation without activation is useless

Example of correct sequence:
Step 1 - EVALUATE:
Relevant skills:
implementing-mvi: Need ViewModel with MVI pattern
creating-activities: Creating new Activity with navigation

Step 2 - ACTIVATE:
Skill(implementing-mvi)
Skill(creating-activities)

Step 3 - IMPLEMENT:
[Start implementation]
EOF
