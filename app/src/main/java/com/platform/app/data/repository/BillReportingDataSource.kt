package com.platform.app.data.repository

import com.platform.app.core.util.DateUtils
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.domain.model.AttentionCounts
import com.platform.app.domain.model.FreeingUpCommitment
import com.platform.app.domain.model.MonthSummary
import com.platform.app.domain.model.OpenInstallmentsSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agregado especializado para consultas de relatórios, resumos e projeções analíticas de contas e parcelas.
 */
@Singleton
class BillReportingDataSource @Inject constructor(
    private val billDao: BillDao,
    private val installmentDao: BillInstallmentDao
) {

    fun getMonthSummary(startMillis: Long, endMillis: Long, nowMillis: Long): Flow<MonthSummary> {
        val startDate = DateUtils.millisToLocalDate(startMillis)
        val endDate = DateUtils.millisToLocalDate(endMillis)
        val todayDate = DateUtils.millisToLocalDate(nowMillis)
        return installmentDao.getMonthSummary(startDate, endDate, todayDate).map { it.toDomain() }
    }

    fun getOpenAmountByContact(): Flow<Map<String, Long>> {
        return installmentDao.getOpenAmountByContact().map { list ->
            list.associate { it.contactId to it.totalOpenCents }
        }
    }

    fun getAttentionCounts(startOfDayMillis: Long, endOfDayMillis: Long): Flow<AttentionCounts> {
        val todayDate = DateUtils.millisToLocalDate(startOfDayMillis)
        return installmentDao.getAttentionCounts(todayDate).map {
            AttentionCounts(overdueCount = it.overdueCount, dueTodayCount = it.dueTodayCount)
        }
    }

    fun getOpenInstallmentsOverview(): Flow<OpenInstallmentsSummary> {
        return installmentDao.getOpenInstallmentsOverview().map {
            OpenInstallmentsSummary(
                totalAmountCents = it.totalAmountCents,
                count = it.installmentCount,
                lastDueDate = it.lastDueDate?.let { d -> DateUtils.localDateToMillis(d) }
            )
        }
    }

    fun getActiveMonthlyRecurringCost(): Flow<Long> {
        return billDao.getActiveMonthlyRecurringCost()
    }

    fun getFreeingUpCommitments(): Flow<List<FreeingUpCommitment>> {
        return installmentDao.getFreeingUpInstallments().map { list ->
            list.map {
                FreeingUpCommitment(
                    billId = it.billId,
                    title = it.title,
                    endDate = it.endDate?.let { d -> DateUtils.localDateToMillis(d) } ?: 0L,
                    monthlyAmountCents = it.monthlyAmountCents
                )
            }
        }
    }
}
