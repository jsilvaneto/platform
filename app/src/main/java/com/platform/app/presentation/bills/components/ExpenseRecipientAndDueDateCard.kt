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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformShapes
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

    val nowCal = Calendar.getInstance()
    val dueCal = Calendar.getInstance().apply { timeInMillis = uiState.dueDate }
    val isToday = nowCal.get(Calendar.YEAR) == dueCal.get(Calendar.YEAR) &&
            nowCal.get(Calendar.DAY_OF_YEAR) == dueCal.get(Calendar.DAY_OF_YEAR)

    val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val isTomorrow = tomorrowCal.get(Calendar.YEAR) == dueCal.get(Calendar.YEAR) &&
            tomorrowCal.get(Calendar.DAY_OF_YEAR) == dueCal.get(Calendar.DAY_OF_YEAR)

    val isDay5 = dueCal.get(Calendar.DAY_OF_MONTH) == 5

    PlatformCard(
        shape = PlatformShapes.large,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "2. Vencimento & Contato",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // VENCIMENTO COM ATALHOS RÁPIDOS
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vencimento *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatDate(uiState.dueDate),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = isToday,
                        onClick = { onDueDateChange(System.currentTimeMillis()) },
                        label = { Text(AppStrings.DateShortcuts.TODAY) },
                        shape = PlatformShapes.small,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = isTomorrow,
                        onClick = { onDueDateChange(DateUtils.addDays(System.currentTimeMillis(), 1)) },
                        label = { Text(AppStrings.DateShortcuts.TOMORROW) },
                        shape = PlatformShapes.small,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = isDay5 && !isToday && !isTomorrow,
                        onClick = {
                            val cal = Calendar.getInstance()
                            if (cal.get(Calendar.DAY_OF_MONTH) >= 5) {
                                cal.add(Calendar.MONTH, 1)
                            }
                            cal.set(Calendar.DAY_OF_MONTH, 5)
                            onDueDateChange(cal.timeInMillis)
                        },
                        label = { Text(AppStrings.DateShortcuts.DAY_5) },
                        shape = PlatformShapes.small,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = !isToday && !isTomorrow && (!isDay5 || isToday || isTomorrow),
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
                        label = { Text(AppStrings.DateShortcuts.OTHER) },
                        shape = PlatformShapes.small,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // CONTATO / FORNECEDOR (OPCIONAL)
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
                        label = { Text(AppStrings.ExpenseForm.CONTACT_OPTIONAL) },
                        placeholder = { Text("Selecione o favorecido") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactDropdownExpanded) },
                        shape = PlatformShapes.medium,
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
        }
    }
}
