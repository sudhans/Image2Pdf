package com.msd.image2pdf

import android.content.Context

// For Page Size
enum class PageSize {
    A4,
    A4_SCALE_DOWN,
    A4_NO_SCALING,
    IMAGE_SIZE,
    A4_GRID
}

// For Page Numbers
enum class HorizontalPageNumberAlignment {
    START, CENTER, END
}

enum class VerticalPageNumberAlignment {
    TOP, BOTTOM
}

data class PageNumberSettings(
    val showPageNumbers: Boolean = true,
    val startPageNumber: Int = 1,
    val horizontalAlignment: HorizontalPageNumberAlignment = HorizontalPageNumberAlignment.END,
    val verticalAlignment: VerticalPageNumberAlignment = VerticalPageNumberAlignment.BOTTOM,
    val prefixText: String = ""
)


object AppSettings {
    private const val PREFS_NAME = "Image2PdfPrefs"

    // Page Size Keys
    private const val KEY_PAGE_SIZE = "pageSize"

    // Page Number Keys
    private const val KEY_SHOW_PAGE_NUMBERS = "showPageNumbers"
    private const val KEY_START_PAGE_NUMBER = "startPageNumber"
    private const val KEY_HORIZONTAL_ALIGNMENT = "horizontalAlignment"
    private const val KEY_VERTICAL_ALIGNMENT = "verticalAlignment"
    private const val KEY_PREFIX_TEXT = "prefixText"

    // Page Size Get/Set
    fun getPageSize(context: Context): PageSize {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return PageSize.valueOf(prefs.getString(KEY_PAGE_SIZE, PageSize.A4.name) ?: PageSize.A4.name)
    }

    fun setPageSize(context: Context, pageSize: PageSize) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PAGE_SIZE, pageSize.name).apply()
    }

    // Page Number Get/Set
    fun getPageNumberSettings(context: Context): PageNumberSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return PageNumberSettings(
            showPageNumbers = prefs.getBoolean(KEY_SHOW_PAGE_NUMBERS, true),
            startPageNumber = prefs.getInt(KEY_START_PAGE_NUMBER, 1),
            horizontalAlignment = HorizontalPageNumberAlignment.valueOf(prefs.getString(KEY_HORIZONTAL_ALIGNMENT, HorizontalPageNumberAlignment.END.name) ?: HorizontalPageNumberAlignment.END.name),
            verticalAlignment = VerticalPageNumberAlignment.valueOf(prefs.getString(KEY_VERTICAL_ALIGNMENT, VerticalPageNumberAlignment.BOTTOM.name) ?: VerticalPageNumberAlignment.BOTTOM.name),
            prefixText = prefs.getString(KEY_PREFIX_TEXT, "") ?: ""
        )
    }

    fun savePageNumberSettings(context: Context, settings: PageNumberSettings) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_SHOW_PAGE_NUMBERS, settings.showPageNumbers)
            putInt(KEY_START_PAGE_NUMBER, settings.startPageNumber)
            putString(KEY_HORIZONTAL_ALIGNMENT, settings.horizontalAlignment.name)
            putString(KEY_VERTICAL_ALIGNMENT, settings.verticalAlignment.name)
            putString(KEY_PREFIX_TEXT, settings.prefixText)
            apply()
        }
    }
}
