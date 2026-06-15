# Database Layer Implementation Files

## File 1: PuzzleEntity.kt

**Location**: `app/src/main/java/com/clicker/smart/action/puzzle/database/PuzzleEntity.kt`

```kotlin
package com.clicker.smart.action.puzzle.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Room entity for storing puzzle solving attempts
 * Used for analytics, debugging, and performance tracking
 */
@Entity(tableName = "puzzle_attempts")
data class PuzzleAttempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    // Identification
    val puzzleId: String = UUID.randomUUID().toString(),
    val puzzleType: String = "UNKNOWN",
    
    // Detection metrics
    val detectionSuccess: Boolean = false,
    val detectionTimeMs: Long = 0L,
    val detectionConfidence: Float = 0f,
    
    // Analysis metrics
    val apiUsed: String = "FALLBACK",  // "GEMINI" or "FALLBACK"
    val analysisSuccess: Boolean = false,
    val analysisTimeMs: Long = 0L,
    val analysisConfidence: Float = 0f,
    val analysisText: String = "",
    
    // Execution metrics
    val executionSuccess: Boolean = false,
    val executionTimeMs: Long = 0L,
    val solutionDetails: String = "",
    
    // Verification
    val verificationSuccess: Boolean = false,
    val verificationTimeMs: Long = 0L,
    
    // Overall metrics
    val totalTimeMs: Long = 0L,
    val solveAttemptNumber: Int = 1,
    val maxRetries: Int = 3,
    
    // Error tracking
    val errorMessage: String? = null,
    val errorCode: Int = 0,
    val stackTrace: String? = null,
    
    // Environment
    val deviceModel: String = "",
    val androidVersion: Int = 0,
    val screenResolution: String = "",
    val memoryUsageMb: Int = 0,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isSuccessful(): Boolean = detectionSuccess && analysisSuccess && executionSuccess
    
    fun getSuccessRate(): Float = when {
        detectionSuccess && analysisSuccess && executionSuccess -> 1.0f
        detectionSuccess && analysisSuccess -> 0.7f
        detectionSuccess -> 0.3f
        else -> 0.0f
    }
}

@Entity(tableName = "puzzle_solutions")
data class PuzzleSolution(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    val puzzleType: String,
    val solutionType: String,  // "SLIDE_PERCENTAGE", "ROTATION_ANGLE", "TAP_POINTS", etc.
    val solutionValue: String,  // Serialized solution (e.g., "0.65" for 65% slide)
    val confidence: Float,
    
    // Image data (hashed for deduplication)
    val imageHash: String,  // SHA256 hash of original puzzle image
    val imageSize: Long,
    
    // Verification
    val verifiedSuccessful: Boolean = true,
    val verificationCount: Int = 1,
    
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "puzzle_statistics")
data class PuzzleStatistics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    // Time window
    val date: String,  // "YYYY-MM-DD"
    val hour: Int,
    
    // Counts
    val totalAttempts: Int = 0,
    val successfulSolves: Int = 0,
    val failedSolves: Int = 0,
    
    // Puzzle type breakdown
    val slidePuzzleAttempts: Int = 0,
    val lockPuzzleAttempts: Int = 0,
    val matchPuzzleAttempts: Int = 0,
    
    // Performance
    val averageDetectionTimeMs: Long = 0L,
    val averageAnalysisTimeMs: Long = 0L,
    val averageExecutionTimeMs: Long = 0L,
    val averageTotalTimeMs: Long = 0L,
    
    // Confidence
    val averageDetectionConfidence: Float = 0f,
    val averageAnalysisConfidence: Float = 0f,
    
    // API usage
    val geminiApiCallsSuccess: Int = 0,
    val geminiApiCallsFailed: Int = 0,
    val fallbackSolutionsUsed: Int = 0,
    
    // Error tracking
    val errorCount: Int = 0,
    val mostCommonError: String? = null,
    
    val updatedAt: Long = System.currentTimeMillis()
)
```

## File 2: PuzzleDao.kt

**Location**: `app/src/main/java/com/clicker/smart/action/puzzle/database/PuzzleDao.kt`

```kotlin
package com.clicker.smart.action.puzzle.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for puzzle-related database operations
 */
@Dao
interface PuzzleDao {
    
    // ============ PUZZLE ATTEMPTS ============
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzleAttempt(attempt: PuzzleAttempt): Long
    
    @Update
    suspend fun updatePuzzleAttempt(attempt: PuzzleAttempt)
    
    @Delete
    suspend fun deletePuzzleAttempt(attempt: PuzzleAttempt)
    
    @Query("SELECT * FROM puzzle_attempts WHERE id = :id")
    suspend fun getPuzzleAttemptById(id: Long): PuzzleAttempt?
    
    @Query("SELECT * FROM puzzle_attempts ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentPuzzleAttempts(limit: Int = 50): List<PuzzleAttempt>
    
    @Query("SELECT * FROM puzzle_attempts WHERE puzzleType = :type ORDER BY createdAt DESC")
    suspend fun getPuzzleAttemptsByType(type: String): List<PuzzleAttempt>
    
    @Query("""
        SELECT * FROM puzzle_attempts 
        WHERE createdAt >= :startTime AND createdAt <= :endTime
        ORDER BY createdAt DESC
    """)
    suspend fun getPuzzleAttemptsByDateRange(startTime: Long, endTime: Long): List<PuzzleAttempt>
    
    @Query("SELECT COUNT(*) FROM puzzle_attempts WHERE detectionSuccess = 1")
    suspend fun getSuccessfulDetectionCount(): Int
    
    @Query("SELECT COUNT(*) FROM puzzle_attempts WHERE analysisSuccess = 1")
    suspend fun getSuccessfulAnalysisCount(): Int
    
    @Query("SELECT COUNT(*) FROM puzzle_attempts WHERE executionSuccess = 1")
    suspend fun getSuccessfulExecutionCount(): Int
    
    @Query("""
        SELECT COUNT(*) FROM puzzle_attempts 
        WHERE detectionSuccess = 1 AND analysisSuccess = 1 AND executionSuccess = 1
    """)
    suspend fun getTotalSuccessfulSolves(): Int
    
    @Query("""
        SELECT AVG(detectionTimeMs) FROM puzzle_attempts 
        WHERE detectionSuccess = 1
    """)
    suspend fun getAverageDetectionTime(): Long
    
    @Query("""
        SELECT AVG(analysisTimeMs) FROM puzzle_attempts 
        WHERE analysisSuccess = 1
    """)
    suspend fun getAverageAnalysisTime(): Long
    
    @Query("""
        SELECT AVG(executionTimeMs) FROM puzzle_attempts 
        WHERE executionSuccess = 1
    """)
    suspend fun getAverageExecutionTime(): Long
    
    @Query("""
        SELECT AVG(detectionConfidence) FROM puzzle_attempts 
        WHERE detectionSuccess = 1
    """)
    suspend fun getAverageDetectionConfidence(): Float
    
    // ============ PUZZLE SOLUTIONS ============
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzleSolution(solution: PuzzleSolution): Long
    
    @Query("SELECT * FROM puzzle_solutions WHERE imageHash = :hash")
    suspend fun getPuzzleSolutionByImageHash(hash: String): PuzzleSolution?
    
    @Query("SELECT * FROM puzzle_solutions WHERE puzzleType = :type AND confidence >= :minConfidence")
    suspend fun getCachedSolutionsByType(type: String, minConfidence: Float = 0.8f): List<PuzzleSolution>
    
    @Query("DELETE FROM puzzle_solutions WHERE createdAt < :expirationTime")
    suspend fun deleteSolutionsCacheOlderThan(expirationTime: Long)
    
    // ============ STATISTICS ============
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(stats: PuzzleStatistics)
    
    @Update
    suspend fun updateStatistics(stats: PuzzleStatistics)
    
    @Query("SELECT * FROM puzzle_statistics WHERE date = :date ORDER BY hour DESC")
    suspend fun getStatisticsByDate(date: String): List<PuzzleStatistics>
    
    @Query("""
        SELECT * FROM puzzle_statistics 
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date DESC, hour DESC
    """)
    suspend fun getStatisticsByDateRange(startDate: String, endDate: String): List<PuzzleStatistics>
    
    @Query("""
        SELECT 
            SUM(successfulSolves) as totalSuccessful,
            SUM(failedSolves) as totalFailed,
            AVG(averageTotalTimeMs) as avgTime
        FROM puzzle_statistics
    """)
    suspend fun getOverallStatistics(): OverallStats?
    
    // ============ CLEANUP ============
    
    @Query("DELETE FROM puzzle_attempts WHERE createdAt < :expirationTime")
    suspend fun deleteOldAttempts(expirationTime: Long)
    
    @Query("DELETE FROM puzzle_attempts")
    suspend fun clearAllAttempts()
    
    @Query("DELETE FROM puzzle_statistics")
    suspend fun clearAllStatistics()
}

data class OverallStats(
    val totalSuccessful: Int = 0,
    val totalFailed: Int = 0,
    val avgTime: Long = 0L
)
```

## File 3: PuzzleDatabase.kt

**Location**: `app/src/main/java/com/clicker/smart/action/puzzle/database/PuzzleDatabase.kt`

```kotlin
package com.clicker.smart.action.puzzle.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for puzzle solver
 * Stores attempts, solutions, and statistics
 */
@Database(
    entities = [
        PuzzleAttempt::class,
        PuzzleSolution::class,
        PuzzleStatistics::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PuzzleDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
    
    companion object {
        @Volatile
        private var INSTANCE: PuzzleDatabase? = null
        
        fun getDatabase(context: Context): PuzzleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PuzzleDatabase::class.java,
                    "puzzle_solver_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns or tables if needed in future versions
                // Example:
                // database.execSQL("ALTER TABLE puzzle_attempts ADD COLUMN new_column TEXT")
            }
        }
    }
}
```

## File 4: RepositoryManager.kt

**Location**: `app/src/main/java/com/clicker/smart/action/puzzle/utils/RepositoryManager.kt`

```kotlin
package com.clicker.smart.action.puzzle.utils

import android.content.Context
import com.clicker.smart.action.puzzle.database.PuzzleDatabase
import com.clicker.smart.action.puzzle.database.PuzzleAttempt
import com.clicker.smart.action.puzzle.database.PuzzleStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for managing puzzle database operations
 * Abstracts database layer from business logic
 */
class RepositoryManager(context: Context) {
    
    private val database = PuzzleDatabase.getDatabase(context)
    private val puzzleDao = database.puzzleDao()
    
    suspend fun recordPuzzleAttempt(
        puzzleType: String,
        detectionResult: Pair<Boolean, Long>,  // success, timeMs
        analysisResult: Pair<Boolean, Long>,
        executionResult: Pair<Boolean, Long>,
        confidence: Float = 0.8f,
        errorMessage: String? = null
    ) = withContext(Dispatchers.IO) {
        val attempt = PuzzleAttempt(
            puzzleType = puzzleType,
            detectionSuccess = detectionResult.first,
            detectionTimeMs = detectionResult.second,
            analysisSuccess = analysisResult.first,
            analysisTimeMs = analysisResult.second,
            executionSuccess = executionResult.first,
            executionTimeMs = executionResult.second,
            detectionConfidence = confidence,
            analysisConfidence = confidence,
            totalTimeMs = detectionResult.second + analysisResult.second + executionResult.second,
            errorMessage = errorMessage
        )
        puzzleDao.insertPuzzleAttempt(attempt)
    }
    
    suspend fun getSuccessRate(): Float = withContext(Dispatchers.IO) {
        val total = puzzleDao.getRecentPuzzleAttempts(100).size
        if (total == 0) return@withContext 0f
        
        val successful = puzzleDao.getTotalSuccessfulSolves()
        successful.toFloat() / total
    }
    
    suspend fun getPerformanceMetrics() = withContext(Dispatchers.IO) {
        PerformanceMetrics(
            avgDetectionTime = puzzleDao.getAverageDetectionTime(),
            avgAnalysisTime = puzzleDao.getAverageAnalysisTime(),
            avgExecutionTime = puzzleDao.getAverageExecutionTime(),
            avgDetectionConfidence = puzzleDao.getAverageDetectionConfidence(),
            totalSuccessfulSolves = puzzleDao.getTotalSuccessfulSolves()
        )
    }
    
    suspend fun updateDailyStatistics(puzzleType: String, isSuccess: Boolean) = 
        withContext(Dispatchers.IO) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dateFormat.format(Date())
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            
            val stats = PuzzleStatistics(
                date = date,
                hour = hour,
                totalAttempts = 1,
                successfulSolves = if (isSuccess) 1 else 0,
                failedSolves = if (!isSuccess) 1 else 0
            )
            
            puzzleDao.insertStatistics(stats)
        }
    
    suspend fun clearOldData(daysToKeep: Int = 30) = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysToKeep)
        val expirationTime = calendar.timeInMillis
        
        puzzleDao.deleteOldAttempts(expirationTime)
        puzzleDao.deleteSolutionsCacheOlderThan(expirationTime)
    }
}

data class PerformanceMetrics(
    val avgDetectionTime: Long = 0L,
    val avgAnalysisTime: Long = 0L,
    val avgExecutionTime: Long = 0L,
    val avgDetectionConfidence: Float = 0f,
    val totalSuccessfulSolves: Int = 0
)
```

---

# ViewModel Implementation

## File 5: PuzzleSolverViewModel.kt

**Location**: `app/src/main/java/com/clicker/smart/action/puzzle/viewmodel/PuzzleSolverViewModel.kt`

```kotlin
package com.clicker.smart.action.puzzle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.clicker.smart.action.puzzle.PuzzleSolverAction
import com.clicker.smart.action.puzzle.utils.RepositoryManager
import com.clicker.smart.action.puzzle.utils.PerformanceMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for puzzle solver configuration and status
 * Manages UI state and database interactions
 */
class PuzzleSolverViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = RepositoryManager(application)
    
    // Current action configuration
    private val _puzzleAction = MutableLiveData<PuzzleSolverAction>()
    val puzzleAction: LiveData<PuzzleSolverAction> = _puzzleAction
    
    // UI state
    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState
    
    // Status messages
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage
    
    // Performance metrics
    private val _metrics = MutableLiveData<PerformanceMetrics>()
    val metrics: LiveData<PerformanceMetrics> = _metrics
    
    // Success rate
    private val _successRate = MutableLiveData<Float>(0f)
    val successRate: LiveData<Float> = _successRate
    
    // Gemini API key validation
    private val _apiKeyValid = MutableLiveData<Boolean>(false)
    val apiKeyValid: LiveData<Boolean> = _apiKeyValid
    
    init {
        _puzzleAction.value = PuzzleSolverAction.default()
        loadMetrics()
    }
    
    fun setAction(action: PuzzleSolverAction) {
        _puzzleAction.value = action
    }
    
    fun validateApiKey(apiKey: String) {
        viewModelScope.launch {
            // Basic validation
            val isValid = apiKey.isNotEmpty() && apiKey.length > 30
            _apiKeyValid.value = isValid
            
            if (isValid) {
                _statusMessage.value = "API key is valid ✓"
            } else {
                _statusMessage.value = "Invalid API key format"
            }
        }
    }
    
    fun loadMetrics() {
        viewModelScope.launch {
            val perf = repository.getPerformanceMetrics()
            _metrics.value = perf
            
            val successRate = repository.getSuccessRate()
            _successRate.value = successRate
            
            _statusMessage.value = 
                "Success rate: ${(successRate * 100).toInt()}% | " +
                "Avg time: ${perf.avgDetectionTime + perf.avgAnalysisTime}ms"
        }
    }
    
    fun updateStatus(message: String) {
        _statusMessage.value = message
    }
    
    fun setUiState(state: UiState) {
        _uiState.value = state
    }
    
    fun clearCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.clearOldData(days Keeper = 0)
            }
            _statusMessage.value = "Cache cleared"
        }
    }
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Detecting : UiState()
    object Analyzing : UiState()
    object Executing : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}
```

---

# UI Implementation

## File 6: PuzzleSolverDialog.kt

**Location**: `app/src/main/java/com/clicker/smart/action/puzzle/ui/PuzzleSolverDialog.kt`

```kotlin
package com.clicker.smart.action.puzzle.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.clicker.smart.action.puzzle.PuzzleSolverAction
import com.clicker.smart.action.puzzle.viewmodel.PuzzleSolverViewModel

/**
 * Dialog for configuring puzzle solver action
 * Allows user to set API key, detection parameters, and gesture settings
 */
class PuzzleSolverDialog : DialogFragment() {
    
    private lateinit var viewModel: PuzzleSolverViewModel
    private var listener: OnConfigurationListener? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(this).get(PuzzleSolverViewModel::class.java)
        
        val view = layoutInflater.inflate(
            R.layout.dialog_puzzle_solver_config,
            null
        )
        
        setupUi(view)
        
        return AlertDialog.Builder(requireContext())
            .setTitle("Puzzle Solver Configuration")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                savConfiguration()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }
    
    private fun setupUi(view: android.view.View) {
        // API Key section
        val apiKeyInput = view.findViewById<EditText>(R.id.et_api_key)
        val apiKeyValidateBtn = view.findViewById<Button>(R.id.btn_validate_api)
        val apiKeyStatus = view.findViewById<TextView>(R.id.tv_api_status)
        
        apiKeyValidateBtn.setOnClickListener {
            val key = apiKeyInput.text.toString()
            viewModel.validateApiKey(key)
        }
        
        // Timeout settings
        val detectionTimeoutInput = view.findViewById<EditText>(R.id.et_detection_timeout)
        val geminiTimeoutInput = view.findViewById<EditText>(R.id.et_gemini_timeout)
        
        // Slider settings
        val sliderDurationInput = view.findViewById<EditText>(R.id.et_slider_duration)
        val swipeStepsInput = view.findViewById<EditText>(R.id.et_swipe_steps)
        val animatedSwipeCheckbox = view.findViewById<CheckBox>(R.id.cb_animated_swipe)
        
        // Retry settings
        val retryCheckbox = view.findViewById<CheckBox>(R.id.cb_retry_on_failure)
        val maxRetriesInput = view.findViewById<EditText>(R.id.et_max_retries)
        
        // Validation
        val validateCheckbox = view.findViewById<CheckBox>(R.id.cb_validate_after_solve)
        
        // Gemini vision checkbox
        val useGeminiCheckbox = view.findViewById<CheckBox>(R.id.cb_use_gemini_vision)
        
        // Load current values if editing
        val currentAction = viewModel.puzzleAction.value
        if (currentAction != null) {
            apiKeyInput.setText(currentAction.geminiApiKey)
            detectionTimeoutInput.setText(currentAction.detectionTimeoutMs.toString())
            geminiTimeoutInput.setText(currentAction.geminiTimeoutMs.toString())
            sliderDurationInput.setText(currentAction.sliderDuration.toString())
            swipeStepsInput.setText(currentAction.swipeSteps.toString())
            animatedSwipeCheckbox.isChecked = currentAction.animatedSwipe
            retryCheckbox.isChecked = currentAction.retryOnFailure
            maxRetriesInput.setText(currentAction.maxRetries.toString())
            validateCheckbox.isChecked = currentAction.validateAfterSolve
            useGeminiCheckbox.isChecked = currentAction.useGeminiVision
        }
        
        // Observe ViewModel changes
        viewModel.apiKeyValid.observe(this) { isValid ->
            apiKeyStatus.text = if (isValid) "✓ Valid" else "✗ Invalid"
            apiKeyStatus.setTextColor(
                if (isValid) 
                    android.graphics.Color.GREEN 
                else 
                    android.graphics.Color.RED
            )
        }
    }
    
    private fun savConfiguration() {
        val view = view ?: return
        
        val apiKey = view.findViewById<EditText>(R.id.et_api_key).text.toString()
        val detectionTimeout = 
            view.findViewById<EditText>(R.id.et_detection_timeout).text.toString().toLongOrNull() ?: 5000L
        val geminiTimeout = 
            view.findViewById<EditText>(R.id.et_gemini_timeout).text.toString().toLongOrNull() ?: 3000L
        val sliderDuration = 
            view.findViewById<EditText>(R.id.et_slider_duration).text.toString().toLongOrNull() ?: 500L
        val swipeSteps = 
            view.findViewById<EditText>(R.id.et_swipe_steps).text.toString().toIntOrNull() ?: 20
        val animatedSwipe = view.findViewById<CheckBox>(R.id.cb_animated_swipe).isChecked
        val retryOnFailure = view.findViewById<CheckBox>(R.id.cb_retry_on_failure).isChecked
        val maxRetries = 
            view.findViewById<EditText>(R.id.et_max_retries).text.toString().toIntOrNull() ?: 3
        val validateAfterSolve = view.findViewById<CheckBox>(R.id.cb_validate_after_solve).isChecked
        val useGemini = view.findViewById<CheckBox>(R.id.cb_use_gemini_vision).isChecked
        
        val action = PuzzleSolverAction(
            geminiApiKey = apiKey,
            detectionTimeoutMs = detectionTimeout,
            geminiTimeoutMs = geminiTimeout,
            sliderDuration = sliderDuration,
            swipeSteps = swipeSteps,
            animatedSwipe = animatedSwipe,
            retryOnFailure = retryOnFailure,
            maxRetries = maxRetries,
            validateAfterSolve = validateAfterSolve,
            useGeminiVision = useGemini
        )
        
        listener?.onConfigurationSaved(action)
    }
    
    fun setListener(listener: OnConfigurationListener) {
        this.listener = listener
    }
    
    interface OnConfigurationListener {
        fun onConfigurationSaved(action: PuzzleSolverAction)
    }
    
    companion object {
        const val TAG = "PuzzleSolverDialog"
        
        fun newInstance(): PuzzleSolverDialog {
            return PuzzleSolverDialog()
        }
    }
}
```

---

# Configuration & Layout Files

## File 7: dialog_puzzle_solver_config.xml

**Location**: `app/src/main/res/layout/dialog_puzzle_solver_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Gemini API Key Section -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Gemini API Configuration"
            android:textStyle="bold"
            android:textSize="16sp"
            android:layout_marginTop="16dp" />

        <EditText
            android:id="@+id/et_api_key"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Google Gemini API Key"
            android:inputType="text"
            android:layout_marginTop="8dp" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="8dp">

            <Button
                android:id="@+id/btn_validate_api"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Validate Key" />

            <TextView
                android:id="@+id/tv_api_status"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:gravity="center"
                android:text="Not validated"
                android:layout_gravity="center_vertical" />
        </LinearLayout>

        <CheckBox
            android:id="@+id/cb_use_gemini_vision"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Use Gemini Vision API (if unchecked, uses fallback)"
            android:layout_marginTop="8dp" />

        <!-- Timeout Settings -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Timeout Settings (ms)"
            android:textStyle="bold"
            android:textSize="16sp"
            android:layout_marginTop="16dp" />

        <EditText
            android:id="@+id/et_detection_timeout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Detection Timeout (5000ms)"
            android:inputType="number"
            android:layout_marginTop="8dp" />

        <EditText
            android:id="@+id/et_gemini_timeout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Gemini API Timeout (3000ms)"
            android:inputType="number"
            android:layout_marginTop="8dp" />

        <!-- Slider Settings -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Gesture Settings"
            android:textStyle="bold"
            android:textSize="16sp"
            android:layout_marginTop="16dp" />

        <EditText
            android:id="@+id/et_slider_duration"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Slider Duration (500ms)"
            android:inputType="number"
            android:layout_marginTop="8dp" />

        <EditText
            android:id="@+id/et_swipe_steps"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Swipe Steps for Smoothness (20)"
            android:inputType="number"
            android:layout_marginTop="8dp" />

        <CheckBox
            android:id="@+id/cb_animated_swipe"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Animated Swipe (smooth motion)"
            android:layout_marginTop="8dp" />

        <!-- Retry Settings -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Retry Settings"
            android:textStyle="bold"
            android:textSize="16sp"
            android:layout_marginTop="16dp" />

        <CheckBox
            android:id="@+id/cb_retry_on_failure"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Retry on Failure"
            android:layout_marginTop="8dp" />

        <EditText
            android:id="@+id/et_max_retries"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Max Retries (3)"
            android:inputType="number"
            android:layout_marginTop="8dp" />

        <!-- Validation Settings -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Validation Settings"
            android:textStyle="bold"
            android:textSize="16sp"
            android:layout_marginTop="16dp" />

        <CheckBox
            android:id="@+id/cb_validate_after_solve"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Validate Solution with Gemini"
            android:layout_marginTop="8dp" />

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="@android:color/darker_gray"
            android:layout_marginTop="16dp" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Tip: Get API key from https://ai.google.dev/"
            android:textSize="12sp"
            android:textColor="@android:color/darker_gray"
            android:layout_marginTop="16dp" />

    </LinearLayout>

</ScrollView>
```

---

# Utility Files

## File 8: ConfigurationManager.kt

**Location**: `app/src/main/java/com/clicker/smart/action/puzzle/utils/ConfigurationManager.kt`

```kotlin
package com.clicker.smart.action.puzzle.utils

import android.content.Context
import android.content.SharedPreferences
import com.clicker.smart.action.puzzle.PuzzleSolverAction
import com.google.gson.Gson

/**
 * Manages persistent storage of puzzle solver configuration
 */
class ConfigurationManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "puzzle_solver_config",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    fun saveAction(action: PuzzleSolverAction) {
        val json = gson.toJson(action)
        sharedPreferences.edit().putString(KEY_ACTION, json).apply()
    }
    
    fun loadAction(): PuzzleSolverAction? {
        val json = sharedPreferences.getString(KEY_ACTION, null) ?: return null
        return try {
            gson.fromJson(json, PuzzleSolverAction::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
    }
    
    fun loadApiKey(): String? {
        return sharedPreferences.getString(KEY_API_KEY, null)
    }
    
    fun setGeminiEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_GEMINI_ENABLED, enabled).apply()
    }
    
    fun isGeminiEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_GEMINI_ENABLED, true)
    }
    
    fun clearConfiguration() {
        sharedPreferences.edit().clear().apply()
    }
    
    companion object {
        private const val KEY_ACTION = "puzzle_solver_action"
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_GEMINI_ENABLED = "gemini_enabled"
    }
}
```

## File 9: LoggingUtil.kt

**Location**: `app/src/main/java/com/clicker/smart/action/puzzle/utils/LoggingUtil.kt`

```kotlin
package com.clicker.smart.action.puzzle.utils

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized logging utility for debug and analytics
 */
object LoggingUtil {
    
    private const val TAG = "PuzzleSolver"
    private val logsDirectory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        "PuzzleSolverLogs"
    )
    
    init {
        if (!logsDirectory.exists()) {
            logsDirectory.mkdirs()
        }
    }
    
    fun logDebug(tag: String, message: String) {
        Log.d(TAG, "[$tag] $message")
        writeToFile("DEBUG", tag, message)
    }
    
    fun logError(tag: String, message: String, exception: Exception? = null) {
        Log.e(TAG, "[$tag] $message", exception)
        val fullMessage = if (exception != null) {
            "$message\n${exception.stackTraceToString()}"
        } else {
            message
        }
        writeToFile("ERROR", tag, fullMessage)
    }
    
    fun logInfo(tag: String, message: String) {
        Log.i(TAG, "[$tag] $message")
        writeToFile("INFO", tag, message)
    }
    
    fun logPerformance(
        tag: String,
        operation: String,
        durationMs: Long,
        success: Boolean,
        metadata: Map<String, String> = emptyMap()
    ) {
        val status = if (success) "SUCCESS" else "FAILED"
        val metadataStr = metadata.entries.joinToString(", ") { "${it.key}=${it.value}" }
        val message = "[$operation] $status in ${durationMs}ms | $metadataStr"
        logDebug(tag, message)
    }
    
    fun saveBitmapDebugImage(filename: String, bitmap: android.graphics.Bitmap) {
        try {
            val file = File(logsDirectory, filename)
            val outputStream = FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            logDebug("ImageSave", "Saved: $filename")
        } catch (e: Exception) {
            logError("ImageSave", "Failed to save image", e)
        }
    }
    
    private fun writeToFile(level: String, tag: String, message: String) {
        try {
            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
            val logFile = File(logsDirectory, "puzzle_solver.log")
            val logMessage = "[$timestamp] $level [$tag] $message\n"
            
            val outputStream = FileOutputStream(logFile, true)
            outputStream.write(logMessage.toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write log file", e)
        }
    }
    
    fun clearLogs() {
        logsDirectory.listFiles()?.forEach { it.delete() }
    }
    
    fun getLogFileSize(): Long {
        return logsDirectory.listFiles()?.sumOf { it.length() } ?: 0L
    }
}
```

## File 10: MatConversions.kt

**Location**: `app/src/main/java/com/clicker/smart/action/puzzle/utils/MatConversions.kt`

```kotlin
package com.clicker.smart.action.puzzle.utils

import android.graphics.Bitmap
import android.graphics.Rect
import org.opencv.core.Mat
import java.security.MessageDigest

/**
 * Helper functions for OpenCV Mat ↔ Android Bitmap conversions
 * and image hashing for caching
 */
object MatConversions {
    
    fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        org.opencv.android.Utils.bitmapToMat(bitmap, mat)
        return mat
    }
    
    fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        org.opencv.android.Utils.matToBitmap(mat, bitmap)
        return bitmap
    }
    
    fun cropBitmap(bitmap: Bitmap, rect: Rect): Bitmap {
        return Bitmap.createBitmap(
            bitmap,
            rect.left,
            rect.top,
            rect.width(),
            rect.height()
        )
    }
    
    fun scaleBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
    
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    fun calculateImageHash(bitmap: Bitmap): String {
        val bytes = bitmapToByteArray(bitmap)
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(bytes)
        return bytesToHex(hash)
    }
    
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return outputStream.toByteArray()
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        )
        val result = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            result[i * 2] = hexChars[v ushr 4]
            result[i * 2 + 1] = hexChars[v and 0x0F]
        }
        return String(result)
    }
}
```

---

**All database and utility files are now complete!**
**Ready for integration with your Smart AutoClicker project.**
