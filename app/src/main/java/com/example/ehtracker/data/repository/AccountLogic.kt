package com.example.ehtracker.data.repository

fun <T> filterByAccount(items: List<T>, currentAccountId: String?, accountIdOf: (T) -> String?): List<T> {
    return if (currentAccountId == null) items else items.filter { accountIdOf(it) == currentAccountId }
}

fun computeAccountBalance(initialBalance: Double, expenseAmounts: List<Double>, incomeAmounts: List<Double>): Double {
    return initialBalance + incomeAmounts.sum() - expenseAmounts.sum()
}
