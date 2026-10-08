package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Caso de uso responsável pela extensão contínua de janelas temporais de contas recorrentes do tipo [RecurrenceEndType.FOREVER].
 *
 * Em vez de encerrar silenciosamente a exibição após o lote inicial (ex: 12 meses, 30 dias),
 * este caso de uso verifica periodicamente (ao abrir Dashboard/Registros ou via alarme matinal)
 * se a conta está a menos de N ocorrências do fim da janela projetada e gera novas parcelas
 * sem desvio de dia de vencimento, preservando a integridade histórica e a regra de continuidade contábil.
 */
class ExtendRecurringBillsUseCase @Inject constructor(
    private val repository: FinancialRepository,
    private val calculateInstallmentsUseCase: CalculateInstallmentsUseCase
) {

    suspend operator fun invoke(referenceTimeMillis: Long = System.currentTimeMillis()): Int {
        val allBills = repository.getBills().first()
        val foreverBills = allBills.filter { bill ->
            bill.type == BillType.RECURRING &&
                (bill.recurrenceEndType == null || bill.recurrenceEndType == RecurrenceEndType.FOREVER)
        }

        var totalGenerated = 0

        for (bill in foreverBills) {
            val existing = repository.getInstallmentsByBillId(bill.id)
            if (existing.isEmpty()) continue

            val frequency = bill.recurrenceFrequency ?: RecurrenceFrequency.MONTHLY
            val thresholdOccurrences = getThresholdOccurrences(frequency)
            val windowBatchCount = CalculateInstallmentsUseCase.getDefaultWindowOccurrences(frequency)

            // Quantas parcelas pendentes ou com vencimento futuro ainda existem no banco a partir da referência
            val futureOrPendingCount = existing.count { it.dueDate >= referenceTimeMillis || !it.isPaid }
            val latestDueDate = existing.maxOf { it.dueDate }
            val thresholdHorizonMillis = getThresholdHorizonMillis(referenceTimeMillis, frequency)

            // Se restam poucas parcelas OU se o horizonte máximo projetado está próximo do presente
            val shouldExtend = futureOrPendingCount <= thresholdOccurrences || latestDueDate <= thresholdHorizonMillis

            if (shouldExtend) {
                var currentInstallments = existing
                var batchesAdded = 0
                val maxBatches = 5 // Limite para evitar laços infinitos em caso de defasagem temporal extrema

                while (batchesAdded < maxBatches) {
                    val nextBatch = calculateInstallmentsUseCase.generateNextRecurringInstallments(
                        bill = bill,
                        existingInstallments = currentInstallments,
                        occurrencesToAdd = windowBatchCount
                    )
                    if (nextBatch.isEmpty()) break

                    repository.addInstallments(bill, nextBatch)
                    totalGenerated += nextBatch.size
                    batchesAdded++
                    currentInstallments = currentInstallments + nextBatch

                    val newLatestDueDate = currentInstallments.maxOf { it.dueDate }
                    val newFutureCount = currentInstallments.count { it.dueDate >= referenceTimeMillis || !it.isPaid }

                    if (newFutureCount > thresholdOccurrences && newLatestDueDate > thresholdHorizonMillis) {
                        break
                    }
                }
            }
        }

        repository.materializeRecurringCardInvoices(referenceTimeMillis)

        return totalGenerated
    }

    companion object {
        fun getThresholdOccurrences(frequency: RecurrenceFrequency): Int {
            return when (frequency) {
                RecurrenceFrequency.DAILY -> 7      // Menos de 7 ocorrências restantes
                RecurrenceFrequency.WEEKLY -> 4     // Menos de 4 ocorrências restantes
                RecurrenceFrequency.MONTHLY -> 3    // Menos de 3 ocorrências restantes
                RecurrenceFrequency.YEARLY -> 1     // Menos de 1 ocorrência restante
            }
        }

        fun getThresholdHorizonMillis(referenceTimeMillis: Long, frequency: RecurrenceFrequency): Long {
            return when (frequency) {
                RecurrenceFrequency.DAILY -> DateUtils.addDays(referenceTimeMillis, 7)
                RecurrenceFrequency.WEEKLY -> DateUtils.addWeeks(referenceTimeMillis, 4)
                RecurrenceFrequency.MONTHLY -> DateUtils.addMonths(referenceTimeMillis, 3)
                RecurrenceFrequency.YEARLY -> DateUtils.addYears(referenceTimeMillis, 1)
            }
        }
    }
}
