package com.platform.app.domain.usecase

import app.cash.turbine.test
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BudgetRigidityStatus
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.repository.GoalRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetFinancialDashboardUseCaseTest {

    private lateinit var repository: FinancialRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var useCase: GetFinancialDashboardUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        goalRepository = mockk(relaxed = true)
        every { goalRepository.getMonthlyContribution(any(), any()) } returns flowOf(0L)
        useCase = GetFinancialDashboardUseCase(repository, goalRepository)
    }

    @Test
    fun `should calculate dashboard metrics and future projections accurately`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)
        val endOfCurrentMonth = DateUtils.getEndOfMonth(now)

        val paidInst = BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Internet Fibra",
            categoryId = "cat-1",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 15000L, // R$ 150,00
            dueDate = currentMonthEpoch + 1000L,
            paidAt = currentMonthEpoch + 1000L,
            categoryName = "Moradia",
            categoryColorHex = "#4CAF50"
        )

        val pendingInst = BillInstallment(
            id = "inst-2",
            billId = "bill-2",
            billTitle = "Energia Elétrica",
            categoryId = "cat-1",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 25000L, // R$ 250,00
            dueDate = endOfCurrentMonth - 1000L, // final do mês atual (garantido pendente e no mês atual)
            paidAt = null,
            categoryName = "Moradia",
            categoryColorHex = "#4CAF50"
        )

        val nextMonthEpoch = DateUtils.addMonths(currentMonthEpoch, 1)
        val futureStart = DateUtils.getStartOfMonth(nextMonthEpoch)
        val futureInst = BillInstallment(
            id = "inst-3",
            billId = "bill-3",
            billTitle = "Seguro Carro 2/10",
            categoryId = "cat-2",
            installmentNumber = 2,
            totalInstallments = 10,
            amountCents = 30000L, // R$ 300,00
            dueDate = futureStart + (2L * 86400000L), // dia 3 do próximo mês
            paidAt = null,
            categoryName = "Transporte",
            categoryColorHex = "#2196F3"
        )

        val monthInstallments = listOf(paidInst, pendingInst)
        val allInstallments = listOf(paidInst, pendingInst, futureInst)

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(monthInstallments)
        every { repository.getAllInstallments() } returns flowOf(allInstallments)
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()

            // Month totals
            assertEquals(40000L, metrics.totalDueMonthCents) // 150 + 250
            assertEquals(15000L, metrics.totalPaidMonthCents) // 150
            assertEquals(25000L, metrics.totalPendingMonthCents) // 250
            assertEquals(0L, metrics.totalOverdueMonthCents)

            // Future projections (6 months)
            assertEquals(6, metrics.futureMonthsProjections.size)
            val monthPlusOne = metrics.futureMonthsProjections.first()
            assertEquals(30000L, monthPlusOne.totalCommittedCents)
            assertEquals(1, monthPlusOne.installmentsCount)

            // Total future committed: pendingInst (250) + futureInst (300) = 550
            assertEquals(55000L, metrics.totalCommittedFutureCents)

            // Category distribution
            assertEquals(1, metrics.categoryDistribution.size)
            assertEquals("Moradia", metrics.categoryDistribution.first().categoryName)
            assertEquals(40000L, metrics.categoryDistribution.first().amountCents)
            assertEquals(100f, metrics.categoryDistribution.first().percentage, 0.01f)

            // Past History (6 months)
            assertEquals(6, metrics.pastMonthsHistory.size)
            val currentInHistory = metrics.pastMonthsHistory.last()
            assertEquals(40000L, currentInHistory.totalDueCents)
            assertEquals(15000L, currentInHistory.totalPaidCents)
            assertEquals(37, currentInHistory.paidRate) // 15000 / 40000 = 37.5% -> 37%

            // Payment Methods Breakdown (neither has invoiceId, so 0 card and 40000 nonCard)
            assertEquals(0L, metrics.creditCardSpendCents)
            assertEquals(40000L, metrics.nonCardSpendCents)
            assertEquals(0f, metrics.creditCardPercentage, 0.01f)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should calculate paymentMethodsDistribution with percentages and counts accurately`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)

        val pixInst = BillInstallment(
            id = "inst-pix",
            billId = "bill-pix",
            billTitle = "Aluguel",
            categoryId = "cat-1",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 60000L, // R$ 600,00 (60%)
            dueDate = currentMonthEpoch + 1000L,
            paymentMethodId = "pm-pix",
            paymentMethodName = "Pix"
        )

        val cardInst = BillInstallment(
            id = "inst-card",
            billId = "bill-card",
            billTitle = "Mercado",
            categoryId = "cat-2",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 40000L, // R$ 400,00 (40%)
            dueDate = currentMonthEpoch + 2000L,
            paymentMethodId = "pm-card",
            paymentMethodName = "Cartão de Crédito"
        )

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(listOf(pixInst, cardInst))
        every { repository.getAllInstallments() } returns flowOf(listOf(pixInst, cardInst))
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()

            assertEquals(2, metrics.paymentMethodsDistribution.size)
            val firstMethod = metrics.paymentMethodsDistribution[0]
            assertEquals("Pix", firstMethod.methodName)
            assertEquals(60000L, firstMethod.amountCents)
            assertEquals(60f, firstMethod.percentage, 0.01f)
            assertEquals(1, firstMethod.count)

            val secondMethod = metrics.paymentMethodsDistribution[1]
            assertEquals("Cartão de Crédito", secondMethod.methodName)
            assertEquals(40000L, secondMethod.amountCents)
            assertEquals(40f, secondMethod.percentage, 0.01f)
            assertEquals(1, secondMethod.count)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should consider retroactive payment on-time when actualPaymentDate is on or before dueDate`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)
        val dueDate = currentMonthEpoch + (5L * 86400000L) // 5º dia do mês
        val paidAtActionTime = dueDate + (5L * 86400000L) // 10º dia do mês (quando tocou no app)
        val actualPaymentDate = dueDate // Realmente pago no dia do vencimento

        val retroactivePaidInst = BillInstallment(
            id = "inst-retro-1",
            billId = "bill-retro-1",
            billTitle = "Energia",
            amountCents = 10000L,
            dueDate = dueDate,
            paidAt = paidAtActionTime,
            actualPaymentDate = actualPaymentDate
        )

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(listOf(retroactivePaidInst))
        every { repository.getAllInstallments() } returns flowOf(listOf(retroactivePaidInst))
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()
            // Como actualPaymentDate <= dueDate, deve ser contabilizado como pontual (100%)
            assertEquals(100, metrics.onTimePaymentRate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should consider retroactive payment late when actualPaymentDate is after dueDate`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)
        val dueDate = currentMonthEpoch + (5L * 86400000L) // 5º dia do mês
        val paidAtActionTime = dueDate + (10L * 86400000L) // 15º dia do mês
        val actualPaymentDate = dueDate + (3L * 86400000L) // Pago de fato no 8º dia (atrasado)

        val latePaidInst = BillInstallment(
            id = "inst-late-1",
            billId = "bill-late-1",
            billTitle = "Telefone",
            amountCents = 8000L,
            dueDate = dueDate,
            paidAt = paidAtActionTime,
            actualPaymentDate = actualPaymentDate
        )

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(listOf(latePaidInst))
        every { repository.getAllInstallments() } returns flowOf(listOf(latePaidInst))
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()
            // Como actualPaymentDate > dueDate, deve ser contabilizado como atrasado (0%)
            assertEquals(0, metrics.onTimePaymentRate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should exclude paid installments with null actualPaymentDate from onTime calculation`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)
        val dueDate = currentMonthEpoch + (5L * 86400000L) // 5º dia do mês
        val paidAtActionTime = dueDate + (5L * 86400000L) // 10º dia do mês (momento do clique)

        // Parcela 1: legada com actualPaymentDate == null (deve sair do cálculo, sem veredito)
        val legacyInst = BillInstallment(
            id = "inst-legacy-1",
            billId = "bill-legacy-1",
            billTitle = "Água Antiga",
            amountCents = 5000L,
            dueDate = dueDate,
            paidAt = paidAtActionTime,
            actualPaymentDate = null
        )

        // Parcela 2: com actualPaymentDate pontual (deve definir 100%)
        val onTimeInst = BillInstallment(
            id = "inst-ontime-2",
            billId = "bill-ontime-2",
            billTitle = "Internet Nova",
            amountCents = 7000L,
            dueDate = dueDate,
            paidAt = paidAtActionTime,
            actualPaymentDate = dueDate
        )

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(listOf(legacyInst, onTimeInst))
        every { repository.getAllInstallments() } returns flowOf(listOf(legacyInst, onTimeInst))
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()
            // Apenas onTimeInst é amostrada -> 1 de 1 pontual -> 100% (em vez de 50% se legacy entrasse como atrasada)
            assertEquals(100, metrics.onTimePaymentRate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should return null onTimePaymentRate when sample has no installments with actualPaymentDate`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)
        val dueDate = currentMonthEpoch + (5L * 86400000L)

        // Pagas apenas com actualPaymentDate = null
        val legacyInst = BillInstallment(
            id = "inst-legacy-only",
            billId = "bill-legacy-only",
            billTitle = "Conta Antiga",
            amountCents = 5000L,
            dueDate = dueDate,
            paidAt = dueDate + 100000L,
            actualPaymentDate = null
        )

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(listOf(legacyInst))
        every { repository.getAllInstallments() } returns flowOf(listOf(legacyInst))
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()
            // Sem nenhuma data real informada, taxa deve ser null (UI exibe "Sem dados")
            assertNull(metrics.onTimePaymentRate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should return null onTimePaymentRate when there are no paid installments at all`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)
        val dueDate = currentMonthEpoch + (5L * 86400000L)

        val pendingInst = BillInstallment(
            id = "inst-pending-only",
            billId = "bill-pending-only",
            billTitle = "Conta Pendente",
            amountCents = 5000L,
            dueDate = dueDate,
            paidAt = null,
            actualPaymentDate = null
        )

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(listOf(pendingInst))
        every { repository.getAllInstallments() } returns flowOf(listOf(pendingInst))
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()
            // Sem pagamentos realizados, taxa deve ser null (UI exibe "Sem dados")
            assertNull(metrics.onTimePaymentRate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should not classify as Excelente when Deseja and Poupanca are zero percent`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)

        // Cenário: 50% Obrigatório, 50% Necessário, 0% Deseja, 0% Poupança
        // Anteriormente, Obrigatório <= 55% e Deseja <= 30% classificava erroneamente como "Excelente"
        val mandatoryInst = BillInstallment(
            id = "inst-obrig",
            billId = "bill-obrig",
            billTitle = "Aluguel",
            nature = ExpenseNature.OBRIGATORIO,
            amountCents = 50000L, // R$ 500,00 (50%)
            dueDate = currentMonthEpoch + 1000L
        )

        val necessaryInst = BillInstallment(
            id = "inst-neces",
            billId = "bill-neces",
            billTitle = "Supermercado Básico",
            nature = ExpenseNature.NECESSARIO,
            amountCents = 50000L, // R$ 500,00 (50%)
            dueDate = currentMonthEpoch + 2000L
        )

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(listOf(mandatoryInst, necessaryInst))
        every { repository.getAllInstallments() } returns flowOf(listOf(mandatoryInst, necessaryInst))
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())
        every { goalRepository.getMonthlyContribution(any(), any()) } returns flowOf(0L) // 0% Poupança

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()

            assertNotNull(metrics.budgetRigidity)
            // Valida que NÃO classifica como Excelente
            assertNotEquals(BudgetRigidityStatus.EXCELENTE, metrics.budgetRigidity?.status)
            assertEquals(BudgetRigidityStatus.SOBRECARREGADO, metrics.budgetRigidity?.status)
            assertEquals("Atenção: Sobrecarga Essencial", metrics.budgetRigidity?.badgeLabel)
            assertEquals(0L, metrics.savingsCents)
            assertEquals(0f, metrics.savingsPercentage, 0.01f)
            assertEquals(100000L, metrics.totalDueMonthCents)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should classify as Excelente when real 50-30-20 distribution is achieved with Goal savings`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)

        // Cenário 50-30-20 real:
        // Essenciais = 50% (35% Obrigatório + 15% Necessário)
        // Desejos = 30%
        // Poupança (Goal) = 20%
        // Total orçado = R$ 1.000,00 (100000L)
        val mandatoryInst = BillInstallment(
            id = "inst-obrig",
            billId = "bill-obrig",
            billTitle = "Aluguel",
            nature = ExpenseNature.OBRIGATORIO,
            amountCents = 35000L, // R$ 350,00 (35%)
            dueDate = currentMonthEpoch + 1000L
        )

        val necessaryInst = BillInstallment(
            id = "inst-neces",
            billId = "bill-neces",
            billTitle = "Mercado",
            nature = ExpenseNature.NECESSARIO,
            amountCents = 15000L, // R$ 150,00 (15%)
            dueDate = currentMonthEpoch + 2000L
        )

        val wantsInst = BillInstallment(
            id = "inst-wants",
            billId = "bill-wants",
            billTitle = "Jantar Fora",
            nature = ExpenseNature.DESEJA,
            amountCents = 30000L, // R$ 300,00 (30%)
            dueDate = currentMonthEpoch + 3000L
        )

        val monthInstallments = listOf(mandatoryInst, necessaryInst, wantsInst)
        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(monthInstallments)
        every { repository.getAllInstallments() } returns flowOf(monthInstallments)
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())
        every { goalRepository.getMonthlyContribution(any(), any()) } returns flowOf(20000L) // R$ 200,00 (20%) em metas

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()

            assertNotNull(metrics.budgetRigidity)
            assertEquals(BudgetRigidityStatus.EXCELENTE, metrics.budgetRigidity?.status)
            assertEquals("Excelente: Padrão 50-30-20 Equilibrado", metrics.budgetRigidity?.badgeLabel)
            assertEquals(20000L, metrics.savingsCents)
            assertEquals(20f, metrics.savingsPercentage, 0.01f)

            // Percentuais das naturezas relativas ao orçamento total (R$ 1000,00)
            val wants = metrics.natureDistribution.first { it.nature == ExpenseNature.DESEJA }
            assertEquals(30f, wants.percentage, 0.01f)
            val mandatory = metrics.natureDistribution.first { it.nature == ExpenseNature.OBRIGATORIO }
            assertEquals(35f, mandatory.percentage, 0.01f)
            val necessary = metrics.natureDistribution.first { it.nature == ExpenseNature.NECESSARIO }
            assertEquals(15f, necessary.percentage, 0.01f)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should classify as Sem Poupanca when wants are present but savings are zero`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)

        val mandatoryInst = BillInstallment(
            id = "inst-obrig",
            billId = "bill-obrig",
            billTitle = "Aluguel",
            nature = ExpenseNature.OBRIGATORIO,
            amountCents = 50000L, // 50%
            dueDate = currentMonthEpoch + 1000L
        )
        val necessaryInst = BillInstallment(
            id = "inst-neces",
            billId = "bill-neces",
            billTitle = "Supermercado",
            nature = ExpenseNature.NECESSARIO,
            amountCents = 20000L, // 20% (essenciais 70%)
            dueDate = currentMonthEpoch + 1500L
        )
        val wantsInst = BillInstallment(
            id = "inst-wants",
            billId = "bill-wants",
            billTitle = "Lazer",
            nature = ExpenseNature.DESEJA,
            amountCents = 30000L, // 30%
            dueDate = currentMonthEpoch + 2000L
        )

        val monthInsts = listOf(mandatoryInst, necessaryInst, wantsInst)
        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(monthInsts)
        every { repository.getAllInstallments() } returns flowOf(monthInsts)
        every { repository.getBills() } returns flowOf(emptyList())
        every { repository.getFinancialAccounts() } returns flowOf(emptyList())
        every { goalRepository.getMonthlyContribution(any(), any()) } returns flowOf(0L) // 0%

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()

            assertEquals(BudgetRigidityStatus.SEM_POUPANCA, metrics.budgetRigidity?.status)
            assertEquals("Atenção: Poupança Insuficiente", metrics.budgetRigidity?.badgeLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
