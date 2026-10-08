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
                    emoji = "🎯",
                    title = "Rigidez Orçamentária Real 50-30-20",
                    description = "Avaliação holística do orçamento integrando as 4 naturezas de gastos (Obrigatório, Necessário, Deseja, Nenhum) com aportes mensais em Metas."
                )

                ReleaseNoteItem(
                    emoji = "🏦",
                    title = "Integração de Metas como Poupança",
                    description = "Seus aportes em metas agora compõem o pilar de Poupança (20%) nas estatísticas, viabilizando o cálculo completo da regra 50-30-20."
                )

                ReleaseNoteItem(
                    emoji = "🏷️",
                    title = "Diagnóstico Orçamentário Preciso",
                    description = "Novos selos e alertas inteligentes por faixa, eliminando falsos positivos de 'Excelente' quando lazer e poupança estiverem zerados."
                )

                ReleaseNoteItem(
                    emoji = "💳",
                    title = "Data Real de Pagamento e Baixa Retroativa",
                    description = "Registre a data efetiva de pagamento na quitação de contas e preserve sua taxa de pontualidade real sem falsos atrasos."
                )

                ReleaseNoteItem(
                    emoji = "✏️",
                    title = "Edição Completa de Itens de Despesa",
                    description = "Edite o nome ou a categoria de qualquer item de despesa existente com herança e sincronização instantânea de natureza."
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
