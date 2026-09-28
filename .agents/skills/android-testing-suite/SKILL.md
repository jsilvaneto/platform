---
name: android-testing-suite
description: >-
  Use this skill when writing, running, or debugging unit tests, Flow/Turbine tests,
  or Room DAO tests in the platform Android project.
---

# Skill: Android Testing Suite (MockK, Turbine & Coroutines)

Esta skill define as práticas recomendadas e padrões oficiais para testes unitários no projeto móvel **Platform**.

---

## 1. Testes Unitários de UseCase
Utilize MockK para simular o comportamento de repositórios e testar cálculos matemáticos de centavos:

```kotlin
@Test
fun `calculate installments divides cents exactly with remainder on first installment`() {
    val useCase = CalculateInstallmentsUseCase()
    val bill = Bill(
        id = "bill-1",
        title = "Notebook",
        type = BillType.INSTALLMENT,
        totalAmountCents = 10000L, // R$ 100,00 em 3x
        totalInstallments = 3
    )

    val installments = useCase(bill, System.currentTimeMillis())

    assertEquals(3, installments.size)
    assertEquals(3334L, installments[0].amountCents) // 3333 + 1
    assertEquals(3333L, installments[1].amountCents)
    assertEquals(3333L, installments[2].amountCents)
    assertEquals(10000L, installments.sumOf { it.amountCents })
}
```

---

## 2. Testes de ViewModel com Turbine e MVI
Isole o `Dispatchers.Main` com `StandardTestDispatcher` e teste as emissões de `StateFlow` e eventos do `Channel`:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class BillsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<FinancialRepository>(relaxed = true)
    private val createBillUseCase = mockk<CreateBillUseCase>()
    private val togglePaymentUseCase = mockk<ToggleInstallmentPaymentUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggle payment updates installment status`() = runTest {
        val viewModel = BillsViewModel(repository, createBillUseCase, togglePaymentUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
        }
    }
}
```

---

## 3. Testes de Banco de Dados Room
Use sempre banco em memória para testes de DAO:
```kotlin
val db = Room.inMemoryDatabaseBuilder(context, PlatformDatabase::class.java)
    .allowMainThreadQueries()
    .build()
```
No método `@After`, execute `db.close()`, garantindo que nenhum resíduo permaneça.
