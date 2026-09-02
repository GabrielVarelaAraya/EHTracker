package com.example.ehtracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.ehtracker.R

fun habitDrawableId(emoji: String): Int? {
    return when (emoji) {
        "\uD83D\uDCD6" -> R.drawable.menu_book
        "\uD83D\uDCAA" -> R.drawable.fitness_center
        "\uD83E\uDDD8" -> R.drawable.self_improvement
        "☕" -> R.drawable.coffee
        "\uD83C\uDFC3" -> R.drawable.directions_run
        "\uD83D\uDCA7" -> R.drawable.opacity
        "\uD83D\uDE34" -> R.drawable.bed_time
        "\uD83D\uDCF5" -> R.drawable.phone_link_off
        "✍\uFE0F" -> R.drawable.edit_note
        "\uD83C\uDFB5" -> R.drawable.music_note
        "\uD83E\uDDF9" -> R.drawable.cleaning_services
        "\uD83E\uDD57" -> R.drawable.lunch_dining
        "\uD83D\uDC8A" -> R.drawable.local_pharmacy
        else -> null
    }
}

fun categoryDrawableId(emoji: String): Int? {
    return when (emoji) {
        "\uD83D\uDE97" -> R.drawable.directions_car
        "\uD83D\uDECD" -> R.drawable.shopping_cart
        "\uD83D\uDCC4" -> R.drawable.receipt
        "\uD83D\uDC8A" -> R.drawable.local_pharmacy
        "\uD83C\uDFAC" -> R.drawable.movie
        "\uD83D\uDCE6" -> R.drawable.inventory2
        "\uD83C\uDF5C" -> R.drawable.food
        "\uD83D\uDCB3" -> R.drawable.card
        else -> null
    }
}

@Composable
fun HabitIcon(emoji: String, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val id = habitDrawableId(emoji)
    if (id != null) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = id),
            contentDescription = contentDescription,
            modifier = modifier
        )
    } else {
        androidx.compose.material3.Text(text = emoji, modifier = modifier)
    }
}

@Composable
fun CategoryIcon(emoji: String, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val id = categoryDrawableId(emoji)
    if (id != null) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = id),
            contentDescription = contentDescription,
            modifier = modifier
        )
    } else {
        androidx.compose.material3.Text(text = emoji, modifier = modifier)
    }
}

fun accountDrawableId(icon: String): Int? {
    return when (icon) {
        "credit_card" -> R.drawable.credit_card
        "card" -> R.drawable.card
        "assignment" -> R.drawable.assignment
        "inventory2" -> R.drawable.inventory2
        "receipt" -> R.drawable.receipt
        "directions_car" -> R.drawable.directions_car
        "food" -> R.drawable.food
        "shopping_cart" -> R.drawable.shopping_cart
        "coffee" -> R.drawable.coffee
        "fitness_center" -> R.drawable.fitness_center
        "menu_book" -> R.drawable.menu_book
        "music_note" -> R.drawable.music_note
        "movie" -> R.drawable.movie
        "local_pharmacy" -> R.drawable.local_pharmacy
        "lunch_dining" -> R.drawable.lunch_dining
        "waving_hand" -> R.drawable.waving_hand
        else -> null
    }
}

@Composable
fun AccountIcon(
    icon: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.material3.LocalContentColor.current
) {
    val id = accountDrawableId(icon) ?: categoryDrawableId(icon) ?: habitDrawableId(icon)
    if (id != null) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = id),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    } else {
        androidx.compose.material3.Text(text = icon, modifier = modifier)
    }
}
