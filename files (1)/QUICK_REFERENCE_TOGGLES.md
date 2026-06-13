# QUICK REFERENCE CARD
## Detection Control Toggles Implementation

---

## 🎯 WHAT YOU'RE IMPLEMENTING

Two toggle switches that control advanced detection features:

1. **Detection Interval Toggle**
   - Label: "Use Custom Detection Interval"
   - When ON: Show detection interval editor
   - When OFF: Hide detection interval editor
   - Default: OFF (disabled)

2. **Required Detections Toggle**
   - Label: "Require Specific Number of Detections"
   - When ON: Show required detections editor
   - When OFF: Hide required detections editor
   - Default: OFF (disabled)

---

## 📁 FILES TO MODIFY

```
✏️  EventEntity.kt (Database)
   └─ Add: detectionIntervalControlEnabled: Boolean = false
   └─ Add: requiredDetectionsControlEnabled: Boolean = false

✏️  UiEvent.kt (UI Model)
   └─ Add: detectionIntervalControlEnabled: Boolean = false
   └─ Add: requiredDetectionsControlEnabled: Boolean = false

✏️  EventEditor.kt (Data Layer)
   └─ Add: var detectionIntervalControlEnabled: Boolean = false
   └─ Add: var requiredDetectionsControlEnabled: Boolean = false

✏️  EventDialogViewModel.kt (State Management)
   └─ Add: private val _detectionIntervalControlEnabled = MutableLiveData<Boolean>()
   └─ Add: val detectionIntervalControlEnabled: LiveData<Boolean>
   └─ Add: fun setDetectionIntervalControlEnabled(enabled: Boolean)
   └─ Add: Initialization code in init/load
   └─ Add: Save code in onEventConfigured/save

✏️  EventDialog.kt (UI)
   └─ Add: Switch/Toggle UI component for Detection Interval
   └─ Add: Switch/Toggle UI component for Required Detections
   └─ Add: Visibility logic for controls
   └─ Add: LiveData observers
   └─ Add: Click listeners
```

---

## 🔄 DATA FLOW

```
User clicks toggle switch
        ↓
EventDialog.onCheckedChange() called
        ↓
viewModel.setDetectionIntervalControlEnabled(isChecked)
        ↓
_detectionIntervalControlEnabled.value = isChecked
        ↓
LiveData emits new value
        ↓
EventDialog observes change
        ↓
UI visibility updated (show/hide controls)
        ↓
User saves event
        ↓
EventDialogViewModel.onEventConfigured()
        ↓
eventEditor.detectionIntervalControlEnabled = value
        ↓
Saved to database
```

---

## 📋 KEY IMPLEMENTATIONS

### 1️⃣ Add Data Fields

**EventEntity.kt**:
```kotlin
@ColumnInfo(name = "detection_interval_control_enabled")
val detectionIntervalControlEnabled: Boolean = false,

@ColumnInfo(name = "required_detections_control_enabled")
val requiredDetectionsControlEnabled: Boolean = false,
```

**UiEvent.kt**:
```kotlin
val detectionIntervalControlEnabled: Boolean = false,
val requiredDetectionsControlEnabled: Boolean = false,
```

**EventEditor.kt**:
```kotlin
var detectionIntervalControlEnabled: Boolean = false
var requiredDetectionsControlEnabled: Boolean = false
```

---

### 2️⃣ Add ViewModel State

**EventDialogViewModel.kt**:
```kotlin
// In properties section:
private val _detectionIntervalControlEnabled = MutableLiveData<Boolean>()
val detectionIntervalControlEnabled: LiveData<Boolean> = _detectionIntervalControlEnabled

private val _requiredDetectionsControlEnabled = MutableLiveData<Boolean>()
val requiredDetectionsControlEnabled: LiveData<Boolean> = _requiredDetectionsControlEnabled

// Setter methods:
fun setDetectionIntervalControlEnabled(enabled: Boolean) {
    _detectionIntervalControlEnabled.value = enabled
}

fun setRequiredDetectionsControlEnabled(enabled: Boolean) {
    _requiredDetectionsControlEnabled.value = enabled
}

// In init/load method:
_detectionIntervalControlEnabled.value = event.detectionIntervalControlEnabled
_requiredDetectionsControlEnabled.value = event.requiredDetectionsControlEnabled

// In save method:
eventEditor.detectionIntervalControlEnabled = _detectionIntervalControlEnabled.value ?: false
eventEditor.requiredDetectionsControlEnabled = _requiredDetectionsControlEnabled.value ?: false
```

---

### 3️⃣ Add UI Components

**EventDialog.kt (Jetpack Compose)**:
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text("Use Custom Detection Interval")
    Spacer(modifier = Modifier.weight(1f))
    Switch(
        checked = detectionIntervalControlEnabled,
        onCheckedChange = { viewModel.setDetectionIntervalControlEnabled(it) }
    )
}

Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text("Require Specific Number of Detections")
    Spacer(modifier = Modifier.weight(1f))
    Switch(
        checked = requiredDetectionsControlEnabled,
        onCheckedChange = { viewModel.setRequiredDetectionsControlEnabled(it) }
    )
}
```

---

### 4️⃣ Add Visibility Logic

**EventDialog.kt (Jetpack Compose)**:
```kotlin
if (detectionIntervalControlEnabled) {
    // Show detection interval editor
    DetectionIntervalEditor(...)
}

if (requiredDetectionsControlEnabled) {
    // Show required detections editor
    RequiredDetectionsEditor(...)
}
```

---

### 5️⃣ Add LiveData Observers

**EventDialog.kt**:
```kotlin
val detectionIntervalControlEnabled by viewModel.detectionIntervalControlEnabled.collectAsState()
val requiredDetectionsControlEnabled by viewModel.requiredDetectionsControlEnabled.collectAsState()
```

---

## ✅ VERIFICATION STEPS

After implementing:

1. **Build**:
   ```bash
   ./gradlew build
   ```

2. **Install**:
   ```bash
   ./gradlew installDebug
   ```

3. **Test**:
   - Open event configuration
   - Look for two new toggle switches
   - Toggle ON → controls should appear
   - Toggle OFF → controls should disappear
   - Save event
   - Reopen event
   - Verify toggle state persisted

4. **Check Logcat**:
   ```bash
   adb logcat | grep -E "error|exception"
   ```

---

## 🔍 COMMON ISSUES & FIXES

| Issue | Cause | Fix |
|-------|-------|-----|
| Toggle not appearing | Missing UI code in EventDialog | Add Switch component |
| Controls not hiding | Missing visibility logic | Add if condition with LiveData |
| State not persisting | Missing save code | Add to onEventConfigured() |
| Crash when toggling | Null safety issue | Add ?: false defaults |
| Build errors | Field name mismatch | Verify all names consistent |

---

## 📊 IMPLEMENTATION ORDER

1. Add data fields (EventEntity, UiEvent)
2. Build and verify (no errors)
3. Add ViewModel state (LiveData, setters)
4. Build and verify (no errors)
5. Add EventEditor fields
6. Build and verify (no errors)
7. Add UI components (switches)
8. Build and verify (no errors)
9. Add visibility logic
10. Add observers and listeners
11. Build and install
12. Test on device
13. Verify all checklist items

**Total Time**: ~2.5-3 hours

---

## 🎯 DEFAULT STATES

| Toggle | Default | Meaning |
|--------|---------|---------|
| Detection Interval | OFF (false) | User must enable to use custom interval |
| Required Detections | OFF (false) | User must enable to require count |

---

## 💾 SAVE LOCATIONS

The toggle states are saved in:
- **Database**: EventEntity table, columns `detection_interval_control_enabled` and `required_detections_control_enabled`
- **UI Memory**: EventDialogViewModel LiveData properties
- **Temporary**: EventEditor instance variables

---

## 🧪 TEST CASES

```
✓ New event: toggles default to OFF
✓ Toggle ON: controls appear
✓ Toggle OFF: controls hide
✓ Save with toggle OFF: controls ignored
✓ Save with toggle ON: values saved
✓ Load event: toggle state restored
✓ Screen rotation: state preserved
✓ Config change: state preserved
```

---

## 📞 GEMINI PROMPTS TO USE

Get responses in this order:

1. **PROMPT 1**: Understand current data model
2. **PROMPT 2**: Review Event Dialog UI
3. **PROMPT 3**: Review Event ViewModel
4. **PROMPT 4**: Review Event Editor
5. **PROMPT 5**: Generate toggle implementation
6. **PROMPT 6**: Generate visibility logic
7. **PROMPT 7**: Generate data persistence
8. **PROMPT 8**: Generate layout modifications
9. **PROMPT 9**: Generate integration code
10. **PROMPT 10**: Generate verification checklist

---

## 🚀 AFTER IMPLEMENTATION

- Build release APK
- Test on multiple devices
- Deploy to users
- Monitor for issues
- Gather feedback
- Iterate if needed

---

## 📖 FULL DOCUMENTATION

For detailed step-by-step guide, see: `IMPLEMENTATION_AFTER_GEMINI.md`
For Gemini prompts, see: `GEMINI_QUICK_PROMPTS.md`
For detailed Gemini instructions, see: `GEMINI_PRO_3_DETAILED_INSTRUCTIONS.md`

---

**That's everything you need to implement detection control toggles!**
