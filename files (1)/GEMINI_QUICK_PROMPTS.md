# GEMINI PRO 3 - COPY & PASTE PROMPTS
## Quick Reference Guide for Detection Toggle Implementation

---

## 📋 INTRODUCTION

This document contains **actual prompts you can copy and paste directly into Gemini Pro 3**.

For each prompt:
1. Copy the entire prompt
2. Paste into Gemini Pro 3 chat
3. Wait for complete response
4. Save the response to a text file
5. Move to next prompt

**IMPORTANT**: Follow the prompts in numerical order (1 → 2 → 3 → etc.)

---

## PROMPT 1: UNDERSTAND CURRENT DATA MODEL

```
Analyze the Smart AutoClicker codebase and provide the complete source code and analysis:

File 1: feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/ui/common/model/event/UiEvent.kt

File 2: core/database/src/main/java/com/buzbuz/smartautoclicker/core/database/entity/EventEntity.kt

For each file, provide:

1. The COMPLETE source code (don't summarize, show every line)
2. All current properties, especially:
   - Detection interval related fields
   - Required detections related fields
   - Any existing boolean toggle fields
3. Data types for each field
4. Default values used
5. Documentation/comments explaining each field

Also identify:
- Is this using data classes?
- Are there any builder patterns?
- How are defaults set?
- What are the nullable vs non-nullable fields?

Provide the complete, unmodified code from both files.
```

**Save response as**: `01_current_data_model.txt`

---

## PROMPT 2: ANALYZE EVENT DIALOG UI

```
Provide the COMPLETE source code of this file:

feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/ui/event/EventDialog.kt

Include:
1. Every single line of the file
2. All imports
3. All class definitions
4. All functions and methods
5. All UI components

Also analyze:
1. What UI framework is used? (Jetpack Compose or XML layouts?)
2. Where is detection interval currently displayed?
3. Where is required detections currently displayed?
4. How are other UI controls implemented? (EditText, Spinner, etc.)
5. Where would be the best place to add new toggle switches?
6. What styling/theme is used for UI components?
7. Are there existing toggle switches? If yes, show how they're implemented
8. What's the general layout structure?

Provide the complete EventDialog.kt code exactly as it appears in the file.
```

**Save response as**: `02_event_dialog_code.txt`

---

## PROMPT 3: ANALYZE EVENT VIEW MODEL

```
Provide the COMPLETE source code of this file:

feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/ui/event/EventDialogViewModel.kt

Include:
1. Every single line of the file
2. All imports
3. The complete class definition
4. Every property
5. Every method and function

Also analyze:
1. What state management pattern is used? (LiveData, StateFlow, etc.)
2. How is detection interval currently managed?
3. How is required detections currently managed?
4. Show all the properties (their types, whether they're mutable, etc.)
5. Show how state is updated - what methods/functions do this?
6. Is there validation? Show validation methods
7. How is data saved? Show the save/complete method
8. Are there other boolean properties? Show how they're managed
9. How are LiveData properties initialized?
10. What's the pattern for updating state from UI?

Provide the COMPLETE ViewModel code as it appears in the file.
```

**Save response as**: `03_event_view_model_code.txt`

---

## PROMPT 4: ANALYZE EVENT EDITOR (DATA LAYER)

```
Provide the COMPLETE source code of this file:

feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/data/EventEditor.kt

Include:
1. Every line of code
2. All imports
3. All class definitions
4. All properties and methods
5. All functions

Also analyze:
1. How does this class handle event data?
2. How are properties read and written?
3. Are there setters and getters?
4. How are default values set?
5. How does data get saved to database?
6. How does data get loaded from database?
7. What properties exist for detection-related settings?
8. Are there any boolean properties? Show how they're handled
9. How is this class initialized?
10. What's the pattern for adding new fields?

Provide the COMPLETE EventEditor.kt code.
```

**Save response as**: `04_event_editor_code.txt`

---

## PROMPT 5: GENERATE TOGGLE IMPLEMENTATION

```
Based on the code from the Smart AutoClicker project (which uses: Kotlin, MVVM, LiveData, Hilt, Material Design), generate production-ready code to add two toggle switches.

Requirements:
1. Add toggle switches to EventDialog.kt for:
   - "Use Custom Detection Interval" (stores state in: enableDetectionIntervalControl)
   - "Require Specific Number of Detections" (stores state in: enableRequiredDetectionsControl)

2. Default state: Both toggles OFF (false)

3. For EventDialog.kt, provide:
   - The exact UI code for the two toggle switches
   - Using Material Design 3 toggle switch components
   - If using Jetpack Compose: @Composable functions for toggles
   - If using XML: Switch or ToggleButton XML definitions
   - Include proper spacing, padding, labels
   - Include click listeners that call ViewModel methods

4. For EventDialogViewModel.kt, provide:
   - Two new properties:
     * val enableDetectionIntervalControl: LiveData<Boolean>
     * val enableRequiredDetectionsControl: LiveData<Boolean>
   - The setter methods:
     * fun setDetectionIntervalControlEnabled(enabled: Boolean)
     * fun setRequiredDetectionsControlEnabled(enabled: Boolean)
   - Initialization code that loads values from event data
   - Code to save these values when event is saved

5. For EventEditor.kt, provide:
   - Two new fields:
     * detectionIntervalControlEnabled: Boolean (default false)
     * requiredDetectionsControlEnabled: Boolean (default false)
   - Methods to read/write these fields
   - Integration with save/load methods

6. Follow these patterns from the codebase:
   - Use LiveData for state management
   - Use MutableLiveData internally, expose immutable LiveData
   - Follow existing naming conventions
   - Include proper null safety
   - Include Hilt annotations if needed
   - Match the code style of existing files

7. Provide COMPLETE, production-ready code for all three files:
   - Every method body
   - Every line needed
   - Ready to copy directly into the files

Don't explain or summarize - provide complete code only.
```

**Save response as**: `05_toggle_implementation_code.txt`

---

## PROMPT 6: GENERATE VISIBILITY LOGIC

```
Generate the conditional visibility logic for showing/hiding detection controls based on toggle state.

Requirements:

1. When "Detection Interval" toggle is OFF (false):
   - Hide the detection interval input field/editor
   - Hide the detection interval label
   - Hide all detection interval related controls
   - They should be invisible but not removed from layout (don't change layout)

2. When "Detection Interval" toggle is ON (true):
   - Show the detection interval input field/editor
   - Show the detection interval label
   - Show all detection interval related controls

3. When "Required Detections" toggle is OFF (false):
   - Hide the required detections input field/editor
   - Hide the required detections label
   - Hide all required detections related controls

4. When "Required Detections" toggle is ON (true):
   - Show the required detections input field/editor
   - Show the required detections label
   - Show all required detections related controls

5. Provide:
   - Complete code for EventDialog.kt
   - If Jetpack Compose: use visibility modifiers (Modifier.alpha, Modifier.size)
   - If XML: use visibility (View.VISIBLE, View.GONE)
   - Show how to observe the toggle LiveData
   - Show how to trigger visibility changes

6. The visibility logic should be:
   - Reactive (updates immediately when toggle changes)
   - Smooth (no flickering)
   - Maintains data (hiding doesn't clear the field value)

Provide complete, working visibility code for all controls.
```

**Save response as**: `06_visibility_logic_code.txt`

---

## PROMPT 7: GENERATE DATA PERSISTENCE CODE

```
Generate the complete data persistence code for saving and loading toggle states.

Requirements:

1. When user SAVES an event:
   - The toggle state for "Detection Interval Control" must be saved
   - The toggle state for "Required Detections Control" must be saved
   - These values should persist in the database

2. When user OPENS an existing event:
   - Load the "Detection Interval Control" toggle state from database
   - Load the "Required Detections Control" toggle state from database
   - Initialize the UI toggles to match saved state
   - Initialize the toggle states in ViewModel

3. When user creates a NEW event:
   - Both toggles default to OFF (false)
   - No database persistence needed yet

4. The data flow should be:
   - UI (toggle click) → ViewModel (update state) → EventEditor (persist data) → Database

5. Provide code for:
   - EventDialogViewModel.kt: How to initialize toggle states from loaded event
   - EventDialogViewModel.kt: How to save toggle states to EventEditor
   - EventEditor.kt: How to handle reading/writing toggle states
   - Show the complete flow

6. Include:
   - The exact methods needed
   - Complete method bodies
   - Proper null safety
   - Default values (false)
   - Integration with existing save/load code

Provide complete data persistence code.
```

**Save response as**: `07_data_persistence_code.txt`

---

## PROMPT 8: GENERATE LAYOUT MODIFICATIONS

```
Generate the complete layout code for adding the two toggle switch sections to the event configuration dialog.

Current state:
The EventDialog shows various event configuration options. We need to add two new sections for the toggle switches.

Requirements:

1. Add a "Detection Interval Settings" section containing:
   - Toggle switch: "Use Custom Detection Interval"
   - Label above toggle
   - Help text below toggle (if space available)
   - When toggle is OFF: hide the detection interval editor below it
   - When toggle is ON: show the detection interval editor

2. Add a "Required Detections Settings" section containing:
   - Toggle switch: "Require Specific Number of Detections"
   - Label above toggle
   - Help text below toggle (if space available)
   - When toggle is OFF: hide the required detections editor below it
   - When toggle is ON: show the required detections editor

3. Layout hierarchy should be:
   Dialog Container
   ├── Existing sections (name, conditions, actions, etc.)
   ├── [NEW] Detection Interval Settings Section
   │   ├── Label text
   │   ├── Toggle Switch
   │   ├── Help text (optional)
   │   └── Detection Interval Editor (conditionally visible)
   ├── [NEW] Required Detections Settings Section
   │   ├── Label text
   │   ├── Toggle Switch
   │   ├── Help text (optional)
   │   └── Required Detections Editor (conditionally visible)
   └── Save/Cancel buttons
   ```

4. Design requirements:
   - Follow Material Design 3 guidelines
   - Use proper spacing (8dp, 16dp increments)
   - Responsive design for all screen sizes
   - Should work on phones, tablets, landscape, portrait
   - Consistent with existing UI style

5. Provide code in:
   - If Jetpack Compose: @Composable code
   - If XML layouts: Complete XML layout definitions
   - Include proper padding, margins, styling
   - Include Material Design components

6. Show:
   - Where to place these new sections relative to existing sections
   - Complete layout code
   - Styling/theming applied
   - All necessary attributes

Provide complete, production-ready layout code.
```

**Save response as**: `08_layout_modifications_code.txt`

---

## PROMPT 9: GENERATE INTEGRATION CODE

```
Generate the complete integration code to connect all the pieces together.

Context:
We have:
- New data model fields (boolean flags for toggles)
- New ViewModel state properties (LiveData for toggle states)
- New UI components (toggle switches)
- New visibility logic
- New persistence code

Now we need to integrate everything.

Requirements:

1. Provide complete EventDialog.kt modifications:
   - Import statements needed
   - Collect the toggle state LiveData and observe it
   - Handle toggle switch clicks
   - Call ViewModel methods when toggle is clicked
   - Update visibility of associated controls

2. Provide complete EventDialogViewModel.kt modifications:
   - Initialize toggle states from loaded event
   - Save toggle states when event is saved
   - Full method implementations
   - Complete property definitions

3. Provide complete EventEditor.kt modifications:
   - Add new toggle state fields
   - Handle reading/writing toggle states
   - Initialize default values
   - Integration with existing save/load methods

4. For each modification, provide:
   - Exact file name
   - The exact lines to add/modify
   - Where in the file (before/after which function/property)
   - The complete code to insert
   - Any lines to remove (if applicable)

5. Show the complete data flow:
   - User clicks toggle → EventDialog calls ViewModel method
   - ViewModel method updates LiveData → UI observes and updates visibility
   - User saves event → ViewModel calls EventEditor
   - EventEditor saves toggle states to database

6. Include:
   - Complete integration steps in order
   - No missing pieces
   - Production-ready code
   - Proper error handling
   - Null safety throughout

Provide complete integration code for all three files with exact line-by-line modifications.
```

**Save response as**: `09_integration_code.txt`

---

## PROMPT 10: VERIFY IMPLEMENTATION CHECKLIST

```
Generate a detailed implementation verification checklist.

This checklist will be used to verify that the toggle switch implementation is complete and correct.

For each item, include:
- What to check
- Where to check it (which file/class)
- What the expected result should be
- How to test it

Checklist sections:

1. DATA MODEL VERIFICATION (5-7 items)
   - Verify new fields in EventEntity
   - Verify new fields in UiEvent
   - Verify default values are false
   - Verify field types are Boolean
   - Verify nullable/non-nullable status
   
2. VIEW MODEL VERIFICATION (6-8 items)
   - Verify new LiveData properties exist
   - Verify update methods created
   - Verify proper initialization
   - Verify state persistence
   - Verify LiveData updates
   - Verify coroutine scope usage
   
3. UI VERIFICATION (7-10 items)
   - Verify toggle switches are visible
   - Verify toggles are Material Design compliant
   - Verify default state is OFF
   - Verify toggles are clickable
   - Verify click listeners work
   - Verify proper styling applied
   - Verify responsive on different screen sizes
   
4. VISIBILITY LOGIC VERIFICATION (4-6 items)
   - Verify controls hidden when toggle OFF
   - Verify controls shown when toggle ON
   - Verify smooth visibility changes
   - Verify no layout jumping
   - Verify responsive to toggle changes
   
5. DATA FLOW VERIFICATION (5-7 items)
   - Verify toggle click → ViewModel method
   - Verify ViewModel update → UI update
   - Verify event save → toggle states saved
   - Verify event load → toggle states loaded
   - Verify new events → toggles default OFF
   
6. EDGE CASES VERIFICATION (6-8 items)
   - Verify toggle OFF then ON preserves data
   - Verify screen rotation preserves state
   - Verify config changes preserve state
   - Verify null safety throughout
   - Verify no crashes when toggling
   - Verify no crashes when saving
   
7. TESTING VERIFICATION (3-5 items)
   - Verify unit tests pass
   - Verify UI tests pass
   - Verify integration tests exist
   - Verify test coverage for toggles
   
8. DOCUMENTATION VERIFICATION (3-4 items)
   - Verify code comments explain toggle behavior
   - Verify method documentation added
   - Verify UI component documentation
   - Verify README updated (if applicable)

For each item, provide:
1. The exact verification step
2. Where to find it in the code
3. What the expected result is
4. How to test it
5. Where to look for evidence (logcat, UI, database, etc.)

Provide a complete, detailed checklist with 40+ verification items.
```

**Save response as**: `10_verification_checklist.txt`

---

## 🚀 EXECUTION WORKFLOW

1. **Ask PROMPT 1** → Get and save response
2. **Ask PROMPT 2** → Get and save response
3. **Ask PROMPT 3** → Get and save response
4. **Ask PROMPT 4** → Get and save response
5. **Ask PROMPT 5** → Get and save response
6. **Ask PROMPT 6** → Get and save response
7. **Ask PROMPT 7** → Get and save response
8. **Ask PROMPT 8** → Get and save response
9. **Ask PROMPT 9** → Get and save response
10. **Ask PROMPT 10** → Get and save response

---

## 📋 HOW TO USE THESE PROMPTS

### In Gemini Pro 3:

1. **Create new chat**
2. **Copy PROMPT 1 entirely**
3. **Paste into chat**
4. **Click Send**
5. **Wait for complete response**
6. **Copy entire response**
7. **Save to `01_current_data_model.txt`**
8. **Repeat for PROMPT 2-10**

### Important Notes:

- **Copy the ENTIRE prompt** (don't modify it)
- **Don't ask follow-ups** - use the next prompt instead
- **Save each response** to numbered text files
- **Follow the order** - don't skip ahead
- **Review each response** for completeness
- **If incomplete**, use the follow-up prompt below

---

## ❓ IF GEMINI'S RESPONSE IS INCOMPLETE

Use this follow-up prompt:

```
I need the COMPLETE code, not a summary. Please provide:

1. Every single line of code
2. All function bodies (not "... implementation ...")
3. All method parameters and return types
4. All imports and declarations
5. Complete code ready to copy directly

Don't explain or summarize. Just provide the complete, full code.
```

---

## 💾 SAVING RESPONSES

After each Gemini response:
1. Select all text
2. Copy to clipboard
3. Paste into text file
4. Name file: `01_current_data_model.txt`, `02_event_dialog_code.txt`, etc.
5. Save in your project root

---

## ✅ FINAL STEP

After getting all 10 responses:
1. Review each response for completeness
2. Start with PROMPT 1-4 responses (understand current code)
3. Then apply PROMPT 5-9 responses (implement features)
4. Use PROMPT 10 response to verify everything works

---

That's it! Follow these prompts in order and you'll get complete, production-ready code for implementing the detection control toggles.
