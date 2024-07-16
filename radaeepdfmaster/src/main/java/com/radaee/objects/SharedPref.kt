package com.radaee.objects

import android.content.Context
import android.content.SharedPreferences

object SharedPref {
    private const val PREF_NAME = "my_shared_prefs"
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    private fun getEditor(context: Context): SharedPreferences.Editor {
        return getSharedPreferences(context).edit()
    }

    fun saveString(context: Context, key: String, value: String) {
        val editor = getEditor(context)
        editor.putString(key, value)
        editor.apply()
    }
    fun getString(context: Context, key: String, defaultValue: String?): String? {
        return getSharedPreferences(context).getString(key, defaultValue)
    }

    fun saveInt(context: Context, key: String, value: Int) {
        val editor = getEditor(context)
        editor.putInt(key, value)
        editor.apply()
    }
    fun getInt(context: Context, key: String, defaultValue: Int): Int {
        return getSharedPreferences(context).getInt(key, defaultValue)
    }
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        val editor = getEditor(context)
        editor.putBoolean(key, value)
        editor.apply()
    }
    fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        return getSharedPreferences(context).getBoolean(key, defaultValue)
    }
    fun remove(context: Context, key: String) {
        val editor = getEditor(context)
        editor.remove(key)
        editor.apply()
    }
    fun clear(context: Context) {
        val editor = getEditor(context)
        editor.clear()
        editor.apply()
    }
}
