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

/** Tests for [Migration18to19]. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class Migration18to19Tests {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ClickDatabase::class.java,
    )

    @Test
    fun migrate18to19_conditionTableHasRepeatCountColumn() {
        // Create the database at version 18
        val db = helper.createDatabase("test_db", 18)

        // Insert a condition row using v18 schema (no repeat_count column)
        db.execSQL(
            """INSERT INTO condition_table
               (id, eventId, name, type, priority)
               VALUES (1, 1, 'test', 'ON_IMAGE_DETECTED', 0)"""
        )
        db.close()

        // Run migration to v19
        val migratedDb = helper.runMigrationsAndValidate(
            "test_db",
            19,
            true,
            Migration18to19,
        )

        // Verify the column exists and defaulted to 1
        val cursor = migratedDb.query("SELECT repeat_count FROM condition_table WHERE id = 1")
        cursor.moveToFirst()
        val repeatCount = cursor.getInt(cursor.getColumnIndexOrThrow("repeat_count"))
        assertEquals("Migrated row should default repeat_count to 1", 1, repeatCount)
        cursor.close()
        migratedDb.close()
    }
}
