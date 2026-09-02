package com.example.ehtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ehtracker.data.model.Category

private val CATEGORY_EMOJIS = listOf(
    "\uD83C\uDF5C", "\uD83C\uDF55", "\uD83C\uDF54", "\uD83C\uDF2E", "\uD83C\uDF63", "\uD83E\uDD57",
    "\u2615", "\uD83E\uDDCB", "\uD83C\uDF69", "\uD83C\uDF70", "\uD83C\uDF6D", "\uD83C\uDF7A",
    "\uD83D\uDE97", "\uD83D\uDE8C", "\uD83D\uDE95", "\uD83D\uDEF5", "\u26FD", "\u2708\uFE0F",
    "\uD83D\uDE9F", "\uD83D\uDE82", "\uD83D\uDED2", "\uD83D\uDECD", "\uD83D\uDC55", "\uD83D\uDC84",
    "\uD83C\uDFAE", "\uD83D\uDCF1", "\uD83D\uDCBB", "\uD83C\uDFA7", "\uD83C\uDFE0", "\uD83D\uDCC4",
    "\uD83D\uDCA1", "\uD83D\uDCA7", "\uD83D\uDCF6", "\uD83D\uDD11", "\uD83D\uDEE0", "\uD83D\uDC8A",
    "\uD83C\uDFE5", "\uD83E\uDEB7", "\uD83D\uDC41", "\uD83D\uDCAA", "\uD83E\uDDD8", "\uD83C\uDFAC",
    "\uD83C\uDFAD", "\uD83C\uDFAB", "\uD83C\uDFB8", "\u26BD", "\uD83C\uDFC0", "\uD83C\uDFAF",
    "\uD83D\uDC36", "\uD83D\uDC31", "\uD83C\uDF81", "\uD83D\uDC76", "\uD83C\uDF93", "\uD83D\uDCB0",
    "\uD83D\uDCE6", "\u2B50", "\u2764\uFE0F", "\uD83C\uDF31"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPicker(
    categories: List<Category>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onAddCategory: (String, String) -> Unit = { _, _ -> },
    onUpdateCategory: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteCategory: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = cat.id == selectedId
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .combinedClickable(
                        onClick = { onSelect(cat.id) },
                        onLongClick = if (!cat.isBuiltIn && !cat.isSavings) {
                            { editingCategory = cat }
                        } else null
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(text = cat.icon, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = cat.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showCreateDialog = true }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "New",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showCreateDialog) {
        CategoryDialog(
            title = "New category",
            initial = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, icon ->
                onAddCategory(name, icon)
                showCreateDialog = false
            }
        )
    }

    editingCategory?.let { cat ->
        CategoryDialog(
            title = "Edit category",
            initial = cat,
            onDismiss = { editingCategory = null },
            onSave = { name, icon ->
                onUpdateCategory(cat.id, name, icon)
                editingCategory = null
            },
            onDelete = {
                onDeleteCategory(cat.id)
                editingCategory = null
            }
        )
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initial: Category?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: CATEGORY_EMOJIS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(20) },
                    label = { Text("Name") },
                    prefix = { Text(icon) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    items(CATEGORY_EMOJIS) { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (emoji == icon) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .clickable { icon = emoji }
                        ) {
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name, icon) },
                enabled = name.isNotBlank()
            ) {
                Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}
