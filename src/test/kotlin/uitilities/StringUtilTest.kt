package me.bossm0n5t3r.uitilities

import me.bossm0n5t3r.uitilities.StringUtil.toSlug
import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilTest {
    @Test
    fun testToSlug_withSimpleString() {
        // Given a simple string
        val input = "Hello World"

        // When converting to slug
        val slug = input.toSlug()

        // Then the result should be a properly formatted slug
        assertEquals("hello-world", slug)
    }

    @Test
    fun testToSlug_withSpecialCharacters() {
        // Given a string with special characters
        val input = "Hello, World! This is a test."

        // When converting to slug
        val slug = input.toSlug()

        // Then the result should be a properly formatted slug with special characters removed
        assertEquals("hello-world-this-is-a-test", slug)
    }

    @Test
    fun testToSlug_withNewlines() {
        // Given a string with newlines
        val input = "Hello\nWorld\nTest"

        // When converting to slug
        val slug = input.toSlug()

        // Then the result should be a properly formatted slug with newlines replaced
        assertEquals("hello-world-test", slug)
    }

    @Test
    fun testToSlug_withMultipleSpaces() {
        // Given a string with multiple spaces
        val input = "Hello   World    Test"

        // When converting to slug
        val slug = input.toSlug()

        // Then the result should be a properly formatted slug with multiple spaces collapsed
        assertEquals("hello-world-test", slug)
    }

    @Test
    fun testToSlug_withMixedCase() {
        // Given a string with mixed case
        val input = "HeLLo WoRlD"

        // When converting to slug
        val slug = input.toSlug()

        // Then the result should be a lowercase slug
        assertEquals("hello-world", slug)
    }

    @Test
    fun testToSlug_withNumbers() {
        // Given a string with numbers
        val input = "Hello World 123"

        // When converting to slug
        val slug = input.toSlug()

        // Then the result should keep the numbers
        assertEquals("hello-world-123", slug)
    }

    @Test
    fun testToSlug_withConsecutiveSpecialCharacters() {
        // Given a string with consecutive special characters
        val input = "Hello!@#$%^&*()World"

        // When converting to slug
        val slug = input.toSlug()

        // Then the result should replace all special characters with a single hyphen
        assertEquals("hello-world", slug)
    }

    @Test
    fun testToSlug_withEmptyString() {
        // Given an empty string
        val input = ""

        // When converting to slug
        val slug = input.toSlug()

        // Then the result should be an empty string
        assertEquals("", slug)
    }
}
