package com.example.ehtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> AnimatedList(
    items: List<T>,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    animationDelay: Int = 50,
    content: @Composable (T) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(
            items = items,
            key = key?.let { { it(it) } } ?: { it.hashCode() }
        ) { item ->
            val index = items.indexOf(item)
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300, delayMillis = index * animationDelay)) +
                        slideInVertically(animationSpec = tween(300, delayMillis = index * animationDelay)) { it / 4 }
            ) {
                content(item)
            }
        }
    }
}

fun <T> LazyListScope.animatedItems(
    items: List<T>,
    key: ((T) -> Any)? = null,
    animationDelay: Int = 50,
    itemContent: @Composable (T) -> Unit
) {
    items(
        items = items,
        key = key?.let { { it(it) } } ?: { it.hashCode() }
    ) { item ->
        val index = items.indexOf(item)
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(300, delayMillis = index * animationDelay)) +
                    slideInVertically(animationSpec = tween(300, delayMillis = index * animationDelay)) { it / 4 }
        ) {
            itemContent(item)
        }
    }
}
