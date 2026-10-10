package com.platform.app.presentation.bills.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.platform.app.presentation.common.AppStrings

@Composable
fun QuickContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newContactName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.Dialogs.NEW_CONTACT_SUPPLIER_TITLE) },
        text = {
            OutlinedTextField(
                value = newContactName,
                onValueChange = { newContactName = it },
                label = { Text(AppStrings.Dialogs.CONTACT_NAME_LABEL) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newContactName.isNotBlank()) {
                        onConfirm(newContactName)
                    }
                },
                enabled = newContactName.isNotBlank()
            ) {
                Text(AppStrings.Actions.SAVE)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.Actions.CANCEL)
            }
        }
    )
}
