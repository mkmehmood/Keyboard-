package com.tobaccoscanner.app

import android.content.Context

/** Keeps the user's API key and model name on this phone only. */
object Prefs {
    const val DEFAULT_MODEL = "claude-sonnet-5-5"
    private const val FILE = "settings"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun key(ctx: Context): String = sp(ctx).getString("api_key", "") ?: ""
    fun model(ctx: Context): String =
        (sp(ctx).getString("model", DEFAULT_MODEL) ?: DEFAULT_MODEL).ifBlank { DEFAULT_MODEL }

    fun save(ctx: Context, key: String, model: String) {
        sp(ctx).edit()
            .putString("api_key", key.trim())
            .putString("model", model.trim().ifBlank { DEFAULT_MODEL })
            .apply()
    }
}
