package com.example.playlistmaker.presentation.ui

/**
 * Помогает предотвратить повторные клики за короткий промежуток времени.
 *
 * @param debounceMillis — минимальный интервал между кликами в миллисекундах.
 */
class ClickDebounceHelper(private val debounceMillis: Long = 1000L) {
    private var lastClickTime = 0L

    /**
     * Возвращает true, если с момента последнего клика прошло больше debounceMillis
     * и обновляет время последнего клика.
     */
    fun canClick(): Boolean {
        val now = System.currentTimeMillis()
        return if (now - lastClickTime >= debounceMillis) {
            lastClickTime = now
            true
        } else {
            false
        }
    }
}
