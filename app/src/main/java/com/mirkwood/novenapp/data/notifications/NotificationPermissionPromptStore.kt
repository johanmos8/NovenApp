package com.mirkwood.novenapp.data.notifications

import android.content.Context

/**
 * Remembers whether Home has already asked for `POST_NOTIFICATIONS` on its own (i.e.
 * because a devotional was on *by default*, not because the user long-pressed a card) -
 * so that automatic prompt happens once, not on every Home visit (R1 / Q8 option (a)
 * in specs/code-audit-v3).
 *
 * Plain SharedPreferences, read synchronously, in its own file on purpose: the backup
 * rules are an opt-in `<include>` list that only names the two DataStore files, so this
 * file is never backed up. A restored/transferred install therefore starts with a
 * fresh "not asked yet" flag and gets asked again - which matters, because runtime
 * permissions themselves are *not* restored with a backup.
 */
class NotificationPermissionPromptStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasAutoPrompted(): Boolean = prefs.getBoolean(KEY_AUTO_PROMPTED, false)

    fun markAutoPrompted() {
        prefs.edit().putBoolean(KEY_AUTO_PROMPTED, true).apply()
    }

    private companion object {
        const val PREFS_NAME = "notification_permission_prompt"
        const val KEY_AUTO_PROMPTED = "auto_prompted"
    }
}
