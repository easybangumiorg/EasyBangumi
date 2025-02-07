package com.heyanle.easy_bangumi_cm.model.meida.local.entities

class StackedFileRule(
    val token: String,
) {
    private var _tokenRegex: Regex

    init {
        require(token.isNotEmpty()) { "Token must not be empty" }
        _tokenRegex = Regex(token, RegexOption.IGNORE_CASE)
    }

    fun match(input: String): StackedFileRuleResult? {
        val match = _tokenRegex.matchEntire(input)
        if (match == null) {
            return null
        }
        val partType = match.groups["parttype"]?.value ?: "Unknown"
        return StackedFileRuleResult(
            stackName = match.groups["filename"]!!.value,
            partType = partType,
            partNumber = match.groups["number"]!!.value,
        )
    }

    data class StackedFileRuleResult(
        val stackName: String,
        val partType: String,
        val partNumber: String,
    )
}