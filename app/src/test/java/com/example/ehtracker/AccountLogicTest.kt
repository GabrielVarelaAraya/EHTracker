package com.example.ehtracker

import com.example.ehtracker.data.repository.computeAccountBalance
import com.example.ehtracker.data.repository.filterByAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountLogicTest {

    private data class Item(val id: String, val accountId: String?)

    private val items = listOf(
        Item("a", "acc1"),
        Item("b", "acc1"),
        Item("c", "acc2"),
        Item("d", null)
    )

    @Test
    fun filterByAccount_returnsAll_whenNoAccountSelected() {
        val result = filterByAccount(items, null) { it.accountId }
        assertEquals(4, result.size)
    }

    @Test
    fun filterByAccount_returnsOnlyMatchingAccount() {
        val result = filterByAccount(items, "acc1") { it.accountId }
        assertEquals(listOf("a", "b"), result.map { it.id })
    }

    @Test
    fun filterByAccount_returnsEmpty_whenNoMatch() {
        val result = filterByAccount(items, "acc99") { it.accountId }
        assertTrue(result.isEmpty())
    }

    @Test
    fun filterByAccount_excludesUnassigned_whenAccountSelected() {
        val result = filterByAccount(items, "acc2") { it.accountId }
        assertEquals(listOf("c"), result.map { it.id })
    }

    @Test
    fun computeAccountBalance_includesInitialAndIncomeMinusExpenses() {
        assertEquals(150.0, computeAccountBalance(100.0, listOf(25.0, 25.0), listOf(100.0)), 0.0001)
    }

    @Test
    fun computeAccountBalance_negativeResult() {
        assertEquals(-50.0, computeAccountBalance(0.0, listOf(100.0), listOf(50.0)), 0.0001)
    }

    @Test
    fun computeAccountBalance_zeroWhenEmpty() {
        assertEquals(0.0, computeAccountBalance(0.0, emptyList(), emptyList()), 0.0001)
    }
}
