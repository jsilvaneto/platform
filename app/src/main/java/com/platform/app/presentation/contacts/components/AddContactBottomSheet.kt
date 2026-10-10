package com.platform.app.presentation.contacts.components

import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import com.platform.app.presentation.contacts.getContactTypeColors
import com.platform.app.presentation.contacts.getContactTypeIcon
import androidx.compose.ui.unit.dp
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.ContactType
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.SuccessGreen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactBottomSheet(
    contact: Contact?,
    sheetState: androidx.compose.material3.SheetState,
    onLookupCep: suspend (String) -> com.platform.app.domain.repository.AddressInfo?,
    onDismiss: () -> Unit,
    onSave: (Contact) -> Unit
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var name by remember { mutableStateOf(contact?.name ?: "") }
    var type by remember { mutableStateOf(contact?.type ?: ContactType.FORNECEDOR) }
    var phone by remember { mutableStateOf(contact?.phone ?: "") }
    var email by remember { mutableStateOf(contact?.email ?: "") }
    var zipCode by remember { mutableStateOf(contact?.zipCode ?: "") }
    var street by remember { mutableStateOf(contact?.street ?: "") }
    var number by remember { mutableStateOf(contact?.number ?: "") }
    var complement by remember { mutableStateOf(contact?.complement ?: "") }
    var neighborhood by remember { mutableStateOf(contact?.neighborhood ?: "") }
    var city by remember { mutableStateOf(contact?.city ?: "") }
    var state by remember { mutableStateOf(contact?.state ?: "") }
    var country by remember { mutableStateOf(contact?.country ?: "Brasil") }

    var isCepLoading by remember { mutableStateOf(false) }
    var cepError by remember { mutableStateOf<String?>(null) }

    fun triggerCepLookup(cepToSearch: String) {
        val clean = cepToSearch.replace(Regex("[^0-9]"), "")
        if (clean.length == 8) {
            isCepLoading = true
            cepError = null
            coroutineScope.launch {
                val address = onLookupCep(clean)
                isCepLoading = false
                if (address != null) {
                    if (address.street.isNotBlank()) street = address.street
                    if (address.neighborhood.isNotBlank()) neighborhood = address.neighborhood
                    if (address.city.isNotBlank()) city = address.city
                    if (address.state.isNotBlank()) state = address.state
                } else {
                    cepError = "CEP não localizado automaticamente"
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header estilizado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (contact == null) Icons.Default.Person else Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (contact == null) "Novo Contato" else "Editar Contato",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Preencha as informações do fornecedor ou beneficiário",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Seletor de Tipo de Contato
            Text(
                text = "Tipo de Contato *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactType.entries.forEach { contactType ->
                    val isSelected = type == contactType
                    val (typeColor, _) = getContactTypeColors(contactType)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { type = contactType },
                        shape = PlatformShapes.medium,
                        color = if (isSelected) {
                            typeColor.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) typeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = getContactTypeIcon(contactType),
                                contentDescription = null,
                                tint = if (isSelected) typeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (contactType) {
                                    ContactType.PESSOA_FISICA -> AppStrings.ContactType.INDIVIDUAL_SHORT
                                    ContactType.FORNECEDOR -> AppStrings.ContactType.SUPPLIER_SHORT
                                    ContactType.ORGAO_PUBLICO -> AppStrings.ContactType.PUBLIC_ENTITY_SHORT
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) typeColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Seção 1: Dados Pessoais
            Text(
                text = "Identificação & Contato",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Completo / Razão Social *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                shape = PlatformShapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / Celular") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Seção 2: Endereço & Localização com busca por CEP
            Text(
                text = "Endereço & Localidade",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Campo de CEP com indicador de busca automática
            OutlinedTextField(
                value = zipCode,
                onValueChange = {
                    zipCode = it
                    triggerCepLookup(it)
                },
                label = { Text("CEP (busca automática ao digitar)") },
                placeholder = { Text("Ex: 01001-000") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (isCepLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (zipCode.replace(Regex("[^0-9]"), "").length == 8) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "CEP válido",
                            tint = SuccessGreen
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = PlatformShapes.medium,
                isError = cepError != null,
                supportingText = cepError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Logradouro / Rua
            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text("Logradouro (Rua, Avenida, Praça)") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                singleLine = true,
                shape = PlatformShapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            // Número e Complemento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Número") },
                    placeholder = { Text("Ex: 123, S/N") },
                    singleLine = true,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.width(130.dp)
                )

                OutlinedTextField(
                    value = complement,
                    onValueChange = { complement = it },
                    label = { Text("Complemento") },
                    placeholder = { Text("Apto 42, Bloco B") },
                    singleLine = true,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bairro e Cidade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    label = { Text("Bairro") },
                    singleLine = true,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Cidade") },
                    singleLine = true,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.weight(1f)
                )
            }

            // Estado (UF) e País
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("Estado (UF)") },
                    placeholder = { Text("Ex: SP") },
                    singleLine = true,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.width(110.dp)
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("País") },
                    singleLine = true,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(AppStrings.Actions.CANCEL)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                Contact(
                                    id = contact?.id ?: UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    type = type,
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    street = street.trim(),
                                    number = number.trim(),
                                    complement = complement.trim(),
                                    neighborhood = neighborhood.trim(),
                                    city = city.trim(),
                                    state = state.trim(),
                                    country = country.trim(),
                                    zipCode = zipCode.trim()
                                )
                            )
                        }
                    },
                    enabled = name.isNotBlank(),
                    shape = PlatformShapes.medium
                ) {
                    Text(if (contact == null) AppStrings.ContactType.SAVE_CONTACT else AppStrings.ContactType.UPDATE_CONTACT)
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
