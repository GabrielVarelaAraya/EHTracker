package com.example.ehtracker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.ehtracker.data.repository.TrackerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

object ExportManager {

    suspend fun exportToJson(context: Context, repository: TrackerRepository): File? {
        return withContext(Dispatchers.IO) {
            try {
                val data = JSONObject()

                val accounts = repository.accounts().first()
                val accountsArray = JSONArray()
                for (account in accounts) {
                    val obj = JSONObject().apply {
                        put("id", account.id)
                        put("name", account.name)
                        put("icon", account.icon)
                        put("color", account.color)
                        put("initialBalance", account.initialBalance)
                        put("currency", account.currency.code)
                        put("createdAt", account.createdAt)
                    }
                    accountsArray.put(obj)
                }
                data.put("accounts", accountsArray)

                val expenses = repository.expenses().first()
                val expensesArray = JSONArray()
                for (expense in expenses) {
                    val obj = JSONObject().apply {
                        put("id", expense.id)
                        put("amount", expense.amount)
                        put("category", expense.category)
                        put("note", expense.note)
                        put("date", expense.date.toString())
                        put("accountId", expense.accountId ?: JSONObject.NULL)
                    }
                    expensesArray.put(obj)
                }
                data.put("expenses", expensesArray)

                val incomes = repository.incomes().first()
                val incomesArray = JSONArray()
                for (income in incomes) {
                    val obj = JSONObject().apply {
                        put("id", income.id)
                        put("amount", income.amount)
                        put("note", income.note)
                        put("date", income.date.toString())
                        put("accountId", income.accountId ?: JSONObject.NULL)
                    }
                    incomesArray.put(obj)
                }
                data.put("incomes", incomesArray)

                val habits = repository.habitsWithCompletions().first()
                val habitsArray = JSONArray()
                for (habit in habits) {
                    val obj = JSONObject().apply {
                        put("id", habit.id)
                        put("name", habit.name)
                        put("icon", habit.icon)
                        put("targetDaysPerWeek", habit.targetDaysPerWeek)
                        put("isNumeric", habit.isNumeric)
                        put("unit", habit.unit)
                        put("completedDates", JSONArray(habit.completedDates.map { it.toString() }))
                        val valuesObj = JSONObject()
                        habit.completionValues.forEach { (date, value) ->
                            valuesObj.put(date.toString(), value)
                        }
                        put("completionValues", valuesObj)
                    }
                    habitsArray.put(obj)
                }
                data.put("habits", habitsArray)

                val subscriptions = repository.subscriptions().first()
                val subsArray = JSONArray()
                for (sub in subscriptions) {
                    val obj = JSONObject().apply {
                        put("id", sub.id)
                        put("name", sub.name)
                        put("amount", sub.amount)
                        put("category", sub.category)
                        put("billingDay", sub.billingDay)
                        put("notes", sub.notes)
                        put("isActive", sub.isActive)
                        put("type", sub.type.name)
                        put("accountId", sub.accountId ?: JSONObject.NULL)
                    }
                    subsArray.put(obj)
                }
                data.put("subscriptions", subsArray)

                val goals = repository.savingsGoals().first()
                val goalsArray = JSONArray()
                for (goal in goals) {
                    val obj = JSONObject().apply {
                        put("id", goal.id)
                        put("name", goal.name)
                        put("targetAmount", goal.targetAmount)
                        put("currentAmount", goal.currentAmount)
                    }
                    goalsArray.put(obj)
                }
                data.put("savingsGoals", goalsArray)

                val categories = repository.categories().first()
                val catsArray = JSONArray()
                for (cat in categories) {
                    val obj = JSONObject().apply {
                        put("id", cat.id)
                        put("name", cat.name)
                        put("icon", cat.icon)
                        put("isBuiltIn", cat.isBuiltIn)
                    }
                    catsArray.put(obj)
                }
                data.put("categories", catsArray)

                data.put("exportDate", LocalDate.now().toString())
                data.put("version", "1.0")

                val fileName = "ehtracker_export_${LocalDate.now()}.json"
                val file = File(context.cacheDir, fileName)
                file.writeText(data.toString(2))
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun exportToCsv(context: Context, repository: TrackerRepository): File? {
        return withContext(Dispatchers.IO) {
            try {
                val sb = StringBuilder()
                sb.appendLine("Type,ID,Amount,Category,Note,Date,AccountID,Extra")

                val expenses = repository.expenses().first()
                for (e in expenses) {
                    sb.appendLine("EXPENSE,${e.id},${e.amount},${escapeCsv(e.category)},${escapeCsv(e.note)},${e.date},${e.accountId ?: ""},")
                }

                val incomes = repository.incomes().first()
                for (i in incomes) {
                    sb.appendLine("INCOME,${i.id},${i.amount},,${escapeCsv(i.note)},${i.date},${i.accountId ?: ""},")
                }

                val habits = repository.habitsWithCompletions().first()
                for (h in habits) {
                    for (date in h.completedDates) {
                        val value = h.completionValues[date]
                        sb.appendLine("HABIT,${h.id},,,${escapeCsv(h.name)},${date},${h.targetDaysPerWeek},${value ?: "true"}")
                    }
                }

                val fileName = "ehtracker_export_${LocalDate.now()}.csv"
                val file = File(context.cacheDir, fileName)
                file.writeText(sb.toString())
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String = "application/json") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export data"))
    }
}
