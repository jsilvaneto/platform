package com.platform.app.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReleaseNotesDialog(
    versionName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🚀", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Melhorias & Correções",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Versão $versionName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "O que há de novo na versão $versionName:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ReleaseNoteItem(
                    emoji = "💳",
                    title = "Assinaturas e Recorrências no Cartão",
                    description = "Suas assinaturas mensais agora aparecem automaticamente em cada fatura de cartão sob demanda, garantindo total conferência com o extrato bancário."
                )

                ReleaseNoteItem(
                    emoji = "⚡",
                    title = "Materialização Sob Demanda",
                    description = "As faturas futuras de cartões anexam de forma transparente as ocorrências de ciclo no momento exato de consulta ou na rotina diária."
                )

                ReleaseNoteItem(
                    emoji = "📅",
                    title = "Vencimentos e Ciclos Estáveis",
                    description = "Cálculo aprimorado de extensões recorrentes mantendo a data original de compra preservada para sempre sem desvio de ciclo."
                )

                ReleaseNoteItem(
                    emoji = "🛡️",
                    title = "Integridade e Limite Real",
                    description = "As parcelas futuras não vinculam mais a faturas anteriores já quitadas, assegurando limites de crédito precisos e sem distorções."
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Entendido")
            }
        }
    )
}

@Composable
fun ReleaseNoteItem(
    emoji: String,
    title: String,
    description: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
