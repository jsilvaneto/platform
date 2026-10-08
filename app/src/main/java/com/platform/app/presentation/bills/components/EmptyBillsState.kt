package com.platform.app.presentation.bills.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.runtime.Composable
import com.platform.app.presentation.components.PlatformEmptyState

@Composable
fun EmptyBillsState() {
    PlatformEmptyState(
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        title = "Nenhuma conta encontrada",
        message = "Toque no botão '+' abaixo para cadastrar sua primeira conta avulsa, parcelada ou recorrente."
    )
}
