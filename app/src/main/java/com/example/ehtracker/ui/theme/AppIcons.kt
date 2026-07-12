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
