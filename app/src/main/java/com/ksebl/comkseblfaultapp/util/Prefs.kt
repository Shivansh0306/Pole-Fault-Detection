package com.ksebl.comkseblfaultapp.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)


