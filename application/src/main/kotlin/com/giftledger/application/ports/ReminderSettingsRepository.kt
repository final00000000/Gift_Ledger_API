package com.giftledger.application.ports

import com.giftledger.domain.ReminderSettings
import com.giftledger.domain.UserId

interface ReminderSettingsRepository {
    fun get(userId: UserId): ReminderSettings
    fun update(userId: UserId, dueDays: Int, warnDays: Int, maxRemindCount: Int, template: String): ReminderSettings
}
