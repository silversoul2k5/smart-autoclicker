/*
 * Copyright (C) 2024 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.core.database.migrations

import android.os.Build

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry

import com.buzbuz.smartautoclicker.core.database.ClickDatabase

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Tests for [Migration19to20]. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class Migration19to20Tests {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ClickDatabase::class.java,
    )

    @Test
    fun migrate19to20_conditionTableHasDetectionCounterColumns() {
        val db = helper.createDatabase("test_db", 19)
        db.execSQL(
            """INSERT INTO condition_table
               (id, eventId, name, type, priority, repeat_count)
               VALUES (1, 1, 'test', 'ON_IMAGE_DETECTED', 0, 5)"""
        )
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            "test_db",
            20,
            true,
            Migration19to20,
        )

        val cursor = migratedDb.query(
            """SELECT detection_interval_ms, required_detection_count, reset_after_trigger
               FROM condition_table WHERE id = 1"""
        )
        cursor.moveToFirst()

        assertEquals(500L, cursor.getLong(cursor.getColumnIndexOrThrow("detection_interval_ms")))
        assertEquals(5, cursor.getInt(cursor.getColumnIndexOrThrow("required_detection_count")))
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("reset_after_trigger")))

        cursor.close()
        migratedDb.close()
    }
}
