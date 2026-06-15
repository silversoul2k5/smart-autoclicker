/*
 * Copyright (C) 2025 Kevin Buzeau
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
package com.buzbuz.smartautoclicker.core.common.settings.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for sensitive data like API keys.
 */
object SecureStorage {
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_GEMINI_API = "gemini_api_key"

    private fun getSecurePreferences(context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun saveGeminiApiKey(context: Context, apiKey: String) {
        getSecurePreferences(context)
            .edit()
            .putString(KEY_GEMINI_API, apiKey)
            .apply()
    }

    fun getGeminiApiKey(context: Context): String? {
        return getSecurePreferences(context)
            .getString(KEY_GEMINI_API, null)
    }

    fun clearGeminiApiKey(context: Context) {
        getSecurePreferences(context)
            .edit()
            .remove(KEY_GEMINI_API)
            .apply()
    }

    fun hasGeminiApiKey(context: Context): Boolean {
        return getGeminiApiKey(context) != null
    }
}
