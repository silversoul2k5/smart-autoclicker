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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.buzbuz.smartautoclicker.core.database.CONDITION_TABLE

/** Migration from database v19 to v20. */
object Migration19to20 : Migration(19, 20) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `${CONDITION_TABLE}_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `eventId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `priority` INTEGER NOT NULL DEFAULT 0,
                `path` TEXT,
                `area_left` INTEGER,
                `area_top` INTEGER,
                `area_right` INTEGER,
                `area_bottom` INTEGER,
                `threshold` INTEGER,
                `detection_type` INTEGER,
                `shouldBeDetected` INTEGER,
                `detection_area_left` INTEGER,
                `detection_area_top` INTEGER,
                `detection_area_right` INTEGER,
                `detection_area_bottom` INTEGER,
                `detection_interval_ms` INTEGER NOT NULL DEFAULT 500,
                `required_detection_count` INTEGER NOT NULL DEFAULT 1,
                `reset_after_trigger` INTEGER NOT NULL DEFAULT 1,
                `broadcast_action` TEXT,
                `counter_name` TEXT,
                `counter_comparison_operation` TEXT,
                `counter_operation_value_type` TEXT,
                `counter_value` INTEGER,
                `counter_value_counter_name` TEXT,
                `timer_value_ms` INTEGER,
                `timer_restart_when_reached` INTEGER,
                FOREIGN KEY(`eventId`) REFERENCES `event_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )"""
        )
        db.execSQL(
            """INSERT INTO `${CONDITION_TABLE}_new` (
                `id`,
                `eventId`,
                `name`,
                `type`,
                `priority`,
                `path`,
                `area_left`,
                `area_top`,
                `area_right`,
                `area_bottom`,
                `threshold`,
                `detection_type`,
                `shouldBeDetected`,
                `detection_area_left`,
                `detection_area_top`,
                `detection_area_right`,
                `detection_area_bottom`,
                `detection_interval_ms`,
                `required_detection_count`,
                `reset_after_trigger`,
                `broadcast_action`,
                `counter_name`,
                `counter_comparison_operation`,
                `counter_operation_value_type`,
                `counter_value`,
                `counter_value_counter_name`,
                `timer_value_ms`,
                `timer_restart_when_reached`
            )
            SELECT
                `id`,
                `eventId`,
                `name`,
                `type`,
                `priority`,
                `path`,
                `area_left`,
                `area_top`,
                `area_right`,
                `area_bottom`,
                `threshold`,
                `detection_type`,
                `shouldBeDetected`,
                `detection_area_left`,
                `detection_area_top`,
                `detection_area_right`,
                `detection_area_bottom`,
                500,
                MAX(`repeat_count`, 1),
                1,
                `broadcast_action`,
                `counter_name`,
                `counter_comparison_operation`,
                `counter_operation_value_type`,
                `counter_value`,
                `counter_value_counter_name`,
                `timer_value_ms`,
                `timer_restart_when_reached`
            FROM `$CONDITION_TABLE`"""
        )
        db.execSQL("DROP TABLE `$CONDITION_TABLE`")
        db.execSQL("ALTER TABLE `${CONDITION_TABLE}_new` RENAME TO `$CONDITION_TABLE`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_condition_table_eventId` ON `$CONDITION_TABLE` (`eventId`)")
    }
}
