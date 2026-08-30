package com.fracorbas.motivationapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AtomicHabitsQuotesTest {

    @Test
    fun `quote list is not empty`() {
        assertTrue(AtomicHabitsQuotes.quotes.isNotEmpty())
    }

    @Test
    fun `forDay returns a quote from the list`() {
        assertTrue(AtomicHabitsQuotes.forDay(0) in AtomicHabitsQuotes.quotes)
        assertTrue(AtomicHabitsQuotes.forDay(9999) in AtomicHabitsQuotes.quotes)
    }

    @Test
    fun `forDay is stable for the same day`() {
        val q1 = AtomicHabitsQuotes.forDay(42)
        val q2 = AtomicHabitsQuotes.forDay(42)
        assertEquals(q1, q2)
    }

    @Test
    fun `forDay wraps around the list size`() {
        val size = AtomicHabitsQuotes.quotes.size
        assertEquals(AtomicHabitsQuotes.forDay(0), AtomicHabitsQuotes.forDay(size))
    }

    @Test
    fun `no quote is blank`() {
        AtomicHabitsQuotes.quotes.forEach { assertTrue(it.isNotBlank()) }
    }
}
