---
name: android-testing-suite
description: >-
  Use this skill when writing, running, or debugging unit tests, Flow/Turbine tests,
  or Compose UI tests in the platform Android project.
---

# Skill: Android Testing Suite (MockK, Turbine & Coroutines)

Esta skill define as práticas recomendadas para garantir alta cobertura e confiabilidade em testes no ecossistema Android.

## 1. Testes Unitários de UseCase
Utilize MockK para simular repositórios:
```kotlin
@Test
fun `invoke should return items from repository`() = runTest {
    val repository = mockk<ItemRepository>()
    every { repository.getItems() } returns flowOf(listOf(item1, item2))
    
    val useCase = GetItemsUseCase(repository)
    val result = useCase().first()
    
    assertEquals(2, result.size)
}
```

## 2. Testes de ViewModel com Turbine
Sempre isole o `Dispatchers.Main` e teste as emissões de `StateFlow`:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class FeatureViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits success on data load`() = runTest {
        val viewModel = MyViewModel(mockUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(expectedData, state.data)
        }
    }
}
```

## 3. Testes de Banco de Dados Room
Use sempre banco em memória para testes de DAO:
```kotlin
val db = Room.inMemoryDatabaseBuilder(context, PlatformDatabase::class.java)
    .allowMainThreadQueries()
    .build()
```
No `@After`, execute `db.close()`.
