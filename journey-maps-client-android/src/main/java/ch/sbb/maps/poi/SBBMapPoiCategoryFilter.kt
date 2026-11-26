// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.poi

import com.google.gson.Gson

/**
 * Applies a POI sub-category filter to a layer so only the selected categories
 * (e.g. "park_rail") are shown. An existing category clause is replaced; otherwise
 * a new one is added.
 */
internal object SBBMapPoiCategoryFilter {
    private val gson = Gson()

    fun apply(
        filter: List<Any?>,
        categories: List<String>,
    ): List<Any?> =
        if (gson.toJson(filter).contains("[\"in\",[\"get\",\"subCategory\"]")) {
            replace(filter, categories)
        } else {
            insert(filter, categories)
        }

    private fun insert(
        filter: List<Any?>,
        categories: List<String>,
    ): List<Any?> {
        val clause = listOf("in", listOf("get", "subCategory"), listOf("literal", categories))
        if (filter.isEmpty()) return clause

        return if ((filter.firstOrNull() as? String)?.lowercase() == "all") {
            listOf(filter.first()) + listOf(clause) + filter.drop(1)
        } else {
            listOf("all", clause, filter)
        }
    }

    private fun replace(
        filter: List<Any?>,
        categories: List<String>,
    ): List<Any?> {
        val operator = (filter.firstOrNull() as? String)?.lowercase() ?: return filter
        return when (operator) {
            "all", "any" ->
                listOf(filter.first()) +
                    filter.drop(1).map { child ->
                        asList(child)?.let { replace(it, categories) } ?: child
                    }

            "in" ->
                if (filter.size == 3 && gson.toJson(filter[1]) == "[\"get\",\"subCategory\"]") {
                    listOf(filter[0], filter[1], listOf("literal", categories))
                } else {
                    filter
                }

            else -> filter
        }
    }

    private fun asList(value: Any?): List<Any?>? =
        when (value) {
            is List<*> -> value
            is Array<*> -> value.toList()
            else -> null
        }
}
