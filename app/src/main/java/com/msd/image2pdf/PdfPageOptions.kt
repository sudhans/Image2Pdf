package com.msd.image2pdf

import android.content.Context

enum class PageSize {
    A4,
    A4_SCALE_DOWN,
    A4_NO_SCALING,
    IMAGE_SIZE,
    A4_GRID
}

object AppSettings {
    private const val PREFS_NAME = "Image2PdfPrefs"
    private const val KEY_PAGE_SIZE = "pageSize"

    fun getPageSize(context: Context): PageSize {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return PageSize.valueOf(prefs.getString(KEY_PAGE_SIZE, PageSize.A4.name) ?: PageSize.A4.name)
    }

    fun setPageSize(context: Context, pageSize: PageSize) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PAGE_SIZE, pageSize.name).apply()
    }
}
