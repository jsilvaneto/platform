package com.platform.app.presentation.bills.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.DateUtils
import com.platform.app.presentation.bills.NewExpenseUiState
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.Dimens
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseRecipientAndDueDateCard(
    uiState: NewExpenseUiState,
    onContactSelect: (String) -> Unit,
    onDueDateChange: (Long) -> Unit,
    onNewQuickContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var contactDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    PlatformCard(
        shape = RoundedCornerShape(Dimens.cardCornerRadius),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "2. Destinatário & Vencimento *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // SELETOR DE CONTATO / FORNECEDOR (OBRIGATÓRIO)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = contactDropdownExpanded,
                    onExpandedChange = { contactDropdownExpanded = !contactDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.selectedContact?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Contato / Fornecedor *") },
                        placeholder = { Text("Selecione o favorecido") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactDropdownExpanded) },
                        isError = uiState.selectedContactId == null && uiState.amountCents > 0L,
                        supportingText = if (uiState.selectedContactId == null && uiState.amountCents > 0L) {
                            { Text("Contato é obrigatório no lançamento", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = contactDropdownExpanded,
                        onDismissRequest = { contactDropdownExpanded = false }
                    ) {
                        if (uiState.contacts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Nenhum contato cadastrado") },
                                onClick = { contactDropdownExpanded = false }
                            )
                        }
                        for (contact in uiState.contacts) {
                            DropdownMenuItem(
                                text = { Text(contact.name, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    onContactSelect(contact.id)
                                    contactDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onNewQuickContactClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Novo Contato Rápido",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Vencimento e Data
            val dateFormatted = DateUtils.formatDate(uiState.dueDate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vencimento Inicial *",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = uiState.dueDate }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(year, month, day, 12, 0, 0)
                                }
                                onDueDateChange(selectedCal.timeInMillis)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(Dimens.buttonCornerRadius)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Alterar Data")
                }
            }
        }
    }
}
