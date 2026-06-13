# Gemini Pro 3 Detailed Instructions: Add Detection Control Toggle Switches
## Smart AutoClicker - Advanced Detection Configuration

---

## 📋 OBJECTIVE
Add on/off toggle switches for:
1. **Detection Interval Control** - Enable/disable custom detection intervals with toggles OFF by default
2. **Required Detections Control** - Enable/disable number of detections requirement with toggles OFF by default

Both features should have toggle switches that enable advanced configuration when turned on, while defaulting to disabled state.

---

## 🎯 CONTEXT & BACKGROUND

### What We're Working With
- **Project**: Smart AutoClicker (Android, Kotlin, Gradle)
- **Architecture**: Multi-module (feature modules, core modules)
- **Key Files to Modify**:
  - Event Configuration Dialog: `feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/ui/event/EventDialog.kt`
  - Event ViewModel: `feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/ui/event/EventDialogViewModel.kt`
  - UI Models: `feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/ui/common/model/event/`

### Current Architecture
- **Pattern**: MVVM with Kotlin Coroutines
- **Dependency Injection**: Hilt
- **UI Framework**: Android Material Design
- **State Management**: ViewModel with LiveData/StateFlow

### What Needs to Change
1. Add boolean fields for toggle states (default: false/off)
2. Add UI components (toggle switches) in EventDialog
3. Add toggle state management in EventDialogViewModel
4. Add visibility logic to show/hide advanced controls based on toggle state
5. Ensure data persistence through configuration changes

---

## 📁 FILE STRUCTURE TO UNDERSTAND

```
feature/smart-config/
└── src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/
    ├── data/
    │   ├── EventEditor.kt          ← Manages event data
    │   └── ...
    ├── ui/
    │   ├── event/
    │   │   ├── EventDialog.kt       ← MAIN UI FILE TO MODIFY
    │   │   ├── EventDialogViewModel.kt  ← STATE MANAGEMENT FILE
    │   │   └── ...
    │   ├── common/
    │   │   └── model/
    │   │       ├── event/
    │   │       │   ├── UiEvent.kt   ← UI DATA MODEL
    │   │       │   └── ...
    │   │       └── ...
    │   └── ...
    └── ...

core/database/
└── src/main/java/com/buzbuz/smartautoclicker/core/database/
    └── entity/
        ├── EventEntity.kt          ← DATABASE MODEL
        └── ...
```

---

## 🔄 STEP-BY-STEP IMPLEMENTATION GUIDE FOR GEMINI PRO 3

### STEP 1: UNDERSTAND THE DATA MODEL

**Gemini Prompt:**
```
Analyze the Smart AutoClicker codebase and provide:

1. The complete data model for Event entity in:
   - feature/smart-config/src/main/java/.../ui/common/model/event/UiEvent.kt
   - core/database/src/main/java/.../entity/EventEntity.kt

2. Current fields related to:
   - Detection interval
   - Required detections
   - Any existing toggle/flag patterns in the data model

3. Identify:
   - Data types used (Boolean, Int, etc.)
   - Nullable vs non-nullable fields
   - Default values
   - Relationships to other entities

4. Show me the complete class definitions for both files

Provide the actual code from these files.
```

**What You'll Get**: Complete understanding of current data structures

---

### STEP 2: IDENTIFY UI COMPONENTS

**Gemini Prompt:**
```
Examine the EventDialog.kt file and provide:

1. The complete source code of:
   feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/ui/event/EventDialog.kt

2. Identify:
   - Current UI controls (EditText, Spinner, SeekBar, etc.)
   - How detection interval is currently displayed
   - How required detections is currently displayed
   - The layout structure (which container holds what)
   - Existing toggle/switch patterns in the UI

3. Show the compose/XML structure being used (if Jetpack Compose or XML layouts)

4. Identify where I should add new toggle switches:
   - Detection Interval toggle switch
   - Required Detections toggle switch

Provide the complete EventDialog.kt code.
```

**What You'll Get**: Complete UI code showing current implementation

---

### STEP 3: EXAMINE VIEW MODEL STATE MANAGEMENT

**Gemini Prompt:**
```
Provide the complete source code and analysis of:

File: feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/ui/event/EventDialogViewModel.kt

Analyze:

1. Current state properties:
   - How is detection interval managed?
   - How is required detections managed?
   - What are the types (Int, Long, Boolean, LiveData, StateFlow, etc.)?

2. Observable patterns used:
   - LiveData
   - StateFlow
   - MutableLiveData
   - Kotlin Flows
   
3. Update methods:
   - How does the view model update detection values?
   - Are there validation methods?
   - How is data saved?

4. For adding toggles, show me:
   - How to add new observable state properties for toggle states
   - How other boolean properties are managed
   - The pattern for updating state

5. Show all LiveData/StateFlow properties and their corresponding update methods

Provide complete ViewModel code.
```

**What You'll Get**: Complete state management code and patterns

---

### STEP 4: GET TOGGLE IMPLEMENTATION CODE

**Gemini Prompt:**
```
Based on the EventDialog.kt and EventDialogViewModel.kt from Smart AutoClicker project:

1. Generate the code to add TWO toggle switches:
   
   Toggle 1: "Detection Interval"
   - Label: "Use Custom Detection Interval"
   - Default state: OFF (false)
   - When ON: Show the detection interval editor
   - When OFF: Hide the detection interval editor
   - Store state in: enableDetectionIntervalControl: LiveData<Boolean>

   Toggle 2: "Required Detections"  
   - Label: "Require Specific Number of Detections"
   - Default state: OFF (false)
   - When ON: Show the required detections editor
   - When OFF: Hide the required detections editor
   - Store state in: enableRequiredDetectionsControl: LiveData<Boolean>

2. For the UI layer (EventDialog.kt):
   - Generate Material Design toggle switch Composables (if using Jetpack Compose)
   - OR generate Switch XML views (if using traditional layouts)
   - Include proper spacing and styling
   - Add click listeners that call ViewModel methods

3. For the ViewModel layer (EventDialogViewModel.kt):
   - Add two new MutableLiveData<Boolean> properties
   - Add update methods: setDetectionIntervalControlEnabled(enabled: Boolean)
   - Add update methods: setRequiredDetectionsControlEnabled(enabled: Boolean)
   - Ensure these integrate with existing code
   - Show how to save these toggle states when event is saved

4. Include:
   - Proper null safety
   - Type-safe implementations
   - Integration with existing patterns in the codebase
   - Proper lifecycle management

Provide complete, production-ready code for both files that can be directly integrated.
```

**What You'll Get**: Complete toggle implementation code for both UI and ViewModel

---

### STEP 5: GET CONDITIONAL VISIBILITY LOGIC

**Gemini Prompt:**
```
Generate the conditional visibility logic for EventDialog.kt:

1. When "Detection Interval" toggle is OFF:
   - Hide: Detection interval editor/input field
   - Hide: Detection interval label
   - Hide: Any detection interval related controls

2. When "Detection Interval" toggle is ON:
   - Show: Detection interval editor/input field
   - Show: Detection interval label  
   - Show: All detection interval controls

3. When "Required Detections" toggle is OFF:
   - Hide: Required detections editor/input field
   - Hide: Required detections label
   - Hide: Any required detections related controls

4. When "Required Detections" toggle is ON:
   - Show: Required detections editor/input field
   - Show: Required detections label
   - Show: All required detections controls

Provide:
- Compose-based visibility modifiers (if using Jetpack Compose)
- XML-based visibility logic (if using traditional layouts)
- Complete code that can be added to EventDialog.kt

Make the visibility logic smooth with proper state management.
```

**What You'll Get**: Visibility control logic for conditional UI rendering

---

### STEP 6: GET DATA PERSISTENCE CODE

**Gemini Prompt:**
```
Generate the data persistence logic for saving toggle states:

1. When the user saves an event:
   - Save the "Detection Interval Control Enabled" toggle state
   - Save the "Required Detections Control Enabled" toggle state

2. When the user opens an existing event:
   - Load the "Detection Interval Control Enabled" toggle state
   - Load the "Required Detections Control Enabled" toggle state
   - Initialize the UI with correct toggle state

3. When creating a new event:
   - Default both toggles to OFF (false)
   - No data persistence needed (they're new)

Provide:
- The update logic in EventDialogViewModel
- How to integrate with existing event saving mechanism
- Show the complete flow: UI → ViewModel → EventEditor → Database

Reference the current file:
feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/data/EventEditor.kt

Show how to add two new fields:
- detectionIntervalControlEnabled: Boolean (default: false)
- requiredDetectionsControlEnabled: Boolean (default: false)
```

**What You'll Get**: Complete data persistence and loading logic

---

### STEP 7: GET LAYOUT MODIFICATIONS

**Gemini Prompt:**
```
I need to modify the event configuration dialog layout to add toggle switches.

Current files to modify:
- feature/smart-config/src/main/java/com/buzbuz/smartautoclicker/feature/smart/config/ui/event/EventDialog.kt

Generate:
1. The layout structure showing:
   - Where to place the "Detection Interval" toggle
   - Where to place the "Required Detections" toggle
   - Proper spacing between toggles and their associated controls
   - Visual hierarchy

2. Layout hierarchy should be:
   ```
   Dialog Container
   ├── Event Name Section
   ├── Event Conditions Section
   ├── Event Actions Section
   ├── [NEW] Detection Interval Toggle Section
   │   ├── Toggle Switch: "Use Custom Detection Interval"
   │   ├── Detection Interval Editor (hidden when OFF)
   │   └── Help text
   ├── [NEW] Required Detections Toggle Section
   │   ├── Toggle Switch: "Require Specific Detections"
   │   ├── Required Detections Editor (hidden when OFF)
   │   └── Help text
   └── Save/Cancel Buttons
   ```

3. For each toggle section, generate:
   - The layout code
   - Proper Material Design styling
   - Responsive design that works on different screen sizes
   - Padding and margins according to Material Design guidelines

4. Provide code in:
   - Jetpack Compose format (if EventDialog uses Compose)
   - XML layout format (if it uses traditional layouts)

Show complete, production-ready layout code.
```

**What You'll Get**: Complete layout code for the new toggle sections

---

### STEP 8: GET INTEGRATION CODE

**Gemini Prompt:**
```
Generate the integration code to connect everything:

1. Update EventDialog.kt to:
   - Collect the toggle state LiveData
   - Update the UI when toggle state changes
   - Call ViewModel methods when user clicks toggles
   - Update visibility of controls based on toggle state

2. Update EventDialogViewModel.kt to:
   - Initialize toggle states from loaded event data
   - Update toggle states when user clicks UI
   - Save toggle states when event is saved
   - Provide LiveData for UI to observe

3. Update EventEditor.kt (data layer) to:
   - Add new fields for toggle states
   - Handle reading/writing toggle states
   - Initialize default values (both false)

4. Provide the complete integration:
   - All file modifications in order
   - The exact lines to add/modify/remove
   - Show how they interact together

5. Include:
   - Error handling
   - Null safety
   - Proper lifecycle management

Provide complete code patches that can be directly applied.
```

**What You'll Get**: Complete integration code showing all connections

---

### STEP 9: GET TESTING CODE

**Gemini Prompt:**
```
Generate unit and integration tests for the toggle functionality:

1. Unit Tests for EventDialogViewModel:
   - Test toggle initialization with default values (OFF)
   - Test toggle state changes
   - Test that changing toggle doesn't clear associated data
   - Test saving and loading toggle states

2. UI Tests for EventDialog:
   - Test toggle switches are visible
   - Test toggling switches shows/hides associated controls
   - Test default toggle state (OFF)
   - Test persistence after dialog is closed and reopened

3. Integration Tests:
   - Test complete workflow: create event, toggle on/off, save, reload
   - Test that detection interval data is ignored when toggle is OFF
   - Test that required detections data is ignored when toggle is OFF

Provide complete test code using:
- JUnit 4
- Mockito or similar mocking framework
- Android testing libraries
- LiveData testing utils

Show how to run the tests.
```

**What You'll Get**: Complete test suite code

---

### STEP 10: GET FINAL VERIFICATION CHECKLIST

**Gemini Prompt:**
```
Create a complete verification checklist to ensure the implementation is correct:

Checklist should verify:

1. Data Model Changes
   [ ] EventEntity has two new boolean fields
   [ ] UiEvent has two new boolean fields
   [ ] Default values are FALSE (toggles off)
   [ ] Fields are properly typed and nullable

2. ViewModel Changes
   [ ] Two new MutableLiveData<Boolean> properties added
   [ ] Update methods created and work correctly
   [ ] Toggle states initialize from event data
   [ ] Toggle states are saved with event

3. UI Changes
   [ ] Two toggle switches visible in EventDialog
   [ ] Toggles are Material Design compliant
   [ ] Default state is OFF (unchecked)
   [ ] Toggles can be clicked and change state
   [ ] Click listeners call ViewModel methods

4. Visibility Logic
   [ ] When "Detection Interval" toggle is OFF: editor hidden
   [ ] When "Detection Interval" toggle is ON: editor visible
   [ ] When "Required Detections" toggle is OFF: editor hidden
   [ ] When "Required Detections" toggle is ON: editor visible
   [ ] Visibility changes are smooth and immediate

5. Data Flow
   [ ] Toggling switch → ViewModel method called
   [ ] ViewModel updates state → UI updates
   [ ] Saving event → toggle states saved to database
   [ ] Loading event → toggle states loaded from database
   [ ] New events → toggles default to OFF

6. Edge Cases
   [ ] Changing toggle doesn't clear associated data
   [ ] Toggling OFF then ON preserves data
   [ ] Works on screen rotation
   [ ] Works with configuration changes
   [ ] Proper null safety throughout

7. Testing
   [ ] All unit tests pass
   [ ] All UI tests pass
   [ ] All integration tests pass
   [ ] No crashes when toggling
   [ ] No crashes when saving/loading

8. Documentation
   [ ] Code comments explain toggle behavior
   [ ] README updated with new feature
   [ ] ViewModel methods documented
   [ ] UI components documented

Provide detailed checklist with expected results for each item.
```

**What You'll Get**: Complete verification checklist

---

## 📝 PROMPT EXECUTION ORDER

Follow this exact order when prompting Gemini Pro 3:

1. **STEP 1** → STEP 2 → STEP 3 (Understand current code)
2. **STEP 4** → STEP 5 → STEP 6 (Get implementation code)
3. **STEP 7** → STEP 8 (Get layout and integration)
4. **STEP 9** → STEP 10 (Test and verify)

---

## 🎯 KEY POINTS FOR GEMINI

When asking Gemini, emphasize:

1. **"Provide complete source code"** - Don't summarize, give full code
2. **"Production-ready"** - Code should be directly usable
3. **"Follow existing patterns"** - Match the codebase style
4. **"Default OFF"** - Both toggles must default to disabled
5. **"Material Design"** - Use Material Design components
6. **"MVVM pattern"** - Follow ViewModel pattern
7. **"Kotlin coroutines"** - Use Kotlin coroutines if async needed
8. **"LiveData"** - Use LiveData for state management
9. **"Type-safe"** - No unchecked casts or suppressions
10. **"Null-safe"** - Proper null safety with Kotlin

---

## 🔍 EXPECTED RESULTS FROM GEMINI

After following all 10 steps, you should have:

✅ Complete understanding of current codebase
✅ Two new toggle switches in UI
✅ Toggle state management in ViewModel
✅ Conditional visibility logic
✅ Data persistence for toggle states
✅ Layout modifications
✅ Integration code ready to apply
✅ Complete test suite
✅ Verification checklist
✅ Production-ready implementation

---

## 💾 FILE MODIFICATIONS SUMMARY

Files you'll need to modify:

1. **EventEntity.kt** - Add 2 boolean fields
2. **UiEvent.kt** - Add 2 boolean fields  
3. **EventDialog.kt** - Add 2 toggles + visibility logic
4. **EventDialogViewModel.kt** - Add state management
5. **EventEditor.kt** - Add data persistence
6. **Tests** - Add test coverage

---

## 🚀 AFTER GETTING GEMINI RESPONSES

1. Save each response to separate files
2. Review code for completeness
3. Apply changes in order (data model → ViewModel → UI)
4. Test after each change
5. Run full test suite
6. Build and test on device
7. Verify all checklist items pass

---

## ⚠️ IMPORTANT NOTES

- **Don't ask Gemini to modify files directly** - It will give you code you copy manually
- **Ask for complete code** - Not snippets or explanations
- **Verify each response** - Make sure code is complete and compilable
- **Test incrementally** - Don't apply all changes at once
- **Keep backups** - Before modifying files
- **Follow the order** - Don't skip steps

---

## 📞 IF GEMINI GIVES INCOMPLETE RESPONSES

Follow up with:

```
"That's helpful, but I need the complete code. Please provide:
1. The complete file content
2. Every line that needs to be added/modified
3. Where exactly in the file it goes
4. Complete function/method bodies

Don't summarize or explain - just provide the full, complete code I can copy directly into my IDE."
```

---

This guide gives you everything needed to get Gemini Pro 3 to generate complete, production-ready code for adding detection control toggles with proper on/off state management.
