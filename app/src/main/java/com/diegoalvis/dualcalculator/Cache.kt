package com.diegoalvis.dualcalculator

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface Cache {

    suspend fun writeExpressions(expressions: List<String>)

    suspend fun readExpressions(): List<String>?
}

internal class CacheImpl(
    context: Context,
    preferencesName: String
) : Cache {

    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    private val editor = preferences.edit()

    companion object {
        const val EXPRESSIONS_LIST = "EXPRESSIONS_LIST"
    }

    override suspend fun writeExpressions(expressions: List<String>) {
        withContext(Dispatchers.IO) {
            val listString = expressions.joinToString(separator = ",")
            editor.putString(EXPRESSIONS_LIST, listString)
            editor.commit()
        }
    }

    override suspend fun readExpressions(): List<String>? {
        return withContext(Dispatchers.IO) {
            preferences.getString(EXPRESSIONS_LIST, "")?.split(",")
        }
    }
}