package com.heyanle.easybangumi4.plugin.source

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class SourceControllerSelectionTest {

    @Test
    fun selectHighestVersionCandidatesKeepsHighestVersionPerKey() {
        val selected = selectHighestVersionCandidates(
            listOf(
                SourceLoadCandidate(File("user.js"), "demo", 1),
                SourceLoadCandidate(File("asset.js"), "demo", 3),
                SourceLoadCandidate(File("other.js"), "other", 2),
                SourceLoadCandidate(File("invalid-a.js"), "", -1),
                SourceLoadCandidate(File("invalid-b.js"), "", -1),
            )
        )

        assertEquals(
            listOf("asset.js", "other.js", "invalid-a.js", "invalid-b.js"),
            selected.map { it.file.name },
        )
    }

    @Test
    fun selectHighestVersionCandidatesKeepsFirstWhenVersionsAreEqual() {
        val selected = selectHighestVersionCandidates(
            listOf(
                SourceLoadCandidate(File("user.js"), "demo", 3),
                SourceLoadCandidate(File("asset.js"), "demo", 3),
            )
        )

        assertEquals(listOf("user.js"), selected.map { it.file.name })
    }
}
