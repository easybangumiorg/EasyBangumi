package com.heyanle.easybangumi4.plugin.source.json

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import org.jsoup.select.NodeTraversor
import java.util.Locale

object XPathUtils {

    fun select(element: Element, xpath: String): Elements {
        if (xpath.isBlank()) return Elements()
        val parts = xpath.trim().removePrefix(".").split("/")
            .filter { it.isNotBlank() }
        var current = Elements(element)
        var descendant = xpath.startsWith("//") || xpath.startsWith(".//")
        for (part in parts) {
            if (part.isBlank()) continue
            if (part == "") {
                descendant = true
                continue
            }
            val step = XPathStep.parse(part)
            val next = Elements()
            current.forEach { base ->
                val candidates = if (descendant) base.getAllElements().drop(1) else base.children()
                val matched = candidates.filter { step.matches(it) }
                next.addAll(matched)
            }
            current = next
            descendant = false
        }
        return current
    }

    fun parse(html: String, baseUrl: String? = null): Document {
        return if (baseUrl.isNullOrBlank()) Jsoup.parse(html) else Jsoup.parse(html, baseUrl)
    }

    private data class XPathStep(
        val tag: String,
        val attrName: String? = null,
        val attrValue: String? = null,
        val className: String? = null,
        val index: Int? = null,
    ) {
        fun matches(element: Element): Boolean {
            if (tag != "*" && element.tagName().lowercase(Locale.ROOT) != tag) return false
            if (attrName != null) {
                if (!element.hasAttr(attrName)) return false
                if (attrValue != null && element.attr(attrName) != attrValue) return false
            }
            if (className != null && !element.classNames().contains(className)) return false
            if (index != null && element.sameTagSiblingIndex() != index) return false
            return true
        }

        private fun Element.sameTagSiblingIndex(): Int {
            val parent = parent() ?: return 1
            var position = 0
            parent.children().forEach { sibling ->
                if (sibling.tagName().equals(tagName(), ignoreCase = true)) {
                    position++
                }
                if (sibling === this) {
                    return position
                }
            }
            return 1
        }

        companion object {
            private val attrRegex = Regex("""@([A-Za-z0-9_:-]+)\s*=\s*['"]([^'"]+)['"]""")
            private val classRegex = Regex("""contains\(\s*@class\s*,\s*['"]([^'"]+)['"]\s*\)""")
            private val indexRegex = Regex("""^\d+$""")

            fun parse(raw: String): XPathStep {
                val tag = raw.substringBefore("[")
                    .takeIf { it.isNotBlank() }
                    ?.lowercase(Locale.ROOT)
                    ?: "*"
                val predicates = Regex("""\[([^\]]+)]""").findAll(raw).map { it.groupValues[1] }.toList()
                var attrName: String? = null
                var attrValue: String? = null
                var className: String? = null
                var index: Int? = null
                predicates.forEach { predicate ->
                    when {
                        indexRegex.matches(predicate.trim()) -> index = predicate.trim().toIntOrNull()
                        attrRegex.containsMatchIn(predicate) -> {
                            val match = attrRegex.find(predicate)
                            attrName = match?.groupValues?.getOrNull(1)
                            attrValue = match?.groupValues?.getOrNull(2)
                        }
                        classRegex.containsMatchIn(predicate) -> {
                            className = classRegex.find(predicate)?.groupValues?.getOrNull(1)
                        }
                    }
                }
                return XPathStep(tag, attrName, attrValue, className, index)
            }
        }
    }
}

fun Element.selectBy(rule: SelectorRule): Elements {
    if (rule.isBlank()) return Elements()
    return when (rule.type) {
        SelectorType.CSS -> select(rule.query)
        SelectorType.XPATH -> XPathUtils.select(this, rule.query)
    }
}

fun Element.extract(rule: SelectorRule?): String? {
    rule ?: return null
    val element = if (rule.isBlank()) this else selectBy(rule).getOrNull(rule.index)
        ?: return rule.default
    val raw = when (rule.attr?.lowercase(Locale.ROOT)) {
        null, "", "text" -> element.text()
        "html" -> element.html()
        "outerhtml" -> element.outerHtml()
        else -> element.attr(rule.attr)
    }.trim()
    val value = if (rule.regex.isNullOrBlank()) {
        raw
    } else {
        Regex(rule.regex).find(raw)?.let { match ->
            var result = rule.replacement
            match.groupValues.forEachIndexed { index, group ->
                result = result.replace("$$index", group)
            }
            result
        }.orEmpty()
    }
    return value.takeIf { it.isNotBlank() } ?: rule.default
}
