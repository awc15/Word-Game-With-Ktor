package com.awcology.extensions

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate


fun Activity.disableDarkTheme() {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
}
