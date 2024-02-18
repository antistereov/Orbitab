package io.github.antistereov.start.widgets.nextcloud.model

import java.time.ZonedDateTime

data class RRuleModel(
    val freq: String?,
    val until: ZonedDateTime?,
    val count: Int?,
    val interval: Int?,
    val byDay: List<String>?,
    val byMonthDay: List<Int>?,
    val byYearDay: List<Int>?,
    val byWeekNo: List<Int>?,
    val byMonth: List<Int>?,
    val bySetPos: List<Int>?,
    val wkst: String?
)