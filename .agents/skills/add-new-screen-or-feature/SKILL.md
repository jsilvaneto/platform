---
name: add-new-screen-or-feature
description: >-
  Use this skill when creating a new Android screen, feature, Room entity,
  DAO, UseCase, or MVI presentation flow in the 100% offline-first platform app.
---

# Workflow: Adicionar Nova Tela ou Feature no Android (100% Offline-First)

Este guia detalha o passo a passo exato para criar uma nova funcionalidade no **Platform**, mantendo Clean Architecture, MVI estrito, persistência local com Room e injeção de dependências com Hilt, sem dependências remotas ou APIs desnecessárias.

---

## 1. Passo 1: Domínio Puro (Kotlin Puro)
1. Crie o modelo imutável em `app/src/main/java/com/platform/app/domain/model/<Feature>.kt`.
   - Se houver valores monetários, use `amountCents: Long`.
2. Declare os contratos na interface do repositório em `app/src/main/java/com/platform/app/domain/repository/<Feature>Repository.kt`. Retorne `Flow<List<T>>` para listas reativas.
3. Implemente Casos de Uso (`UseCases`) de responsabilidade única em `app/src/main/java/com/platform/app/domain/usecase/<Acao><Feature>UseCase.kt`.

---

## 2. Passo 2: Camada de Dados Local (Room)
1. Crie a entidade em `app/src/main/java/com/platform/app/data/local/entity/<Feature>Entity.kt`.
   - Mapeamentos bidirecionais obrigatórios: `toDomain()` e `companion object fromDomain()`.
2. Crie o DAO em `app/src/main/java/com/platform/app/data/local/dao/<Feature>Dao.kt` com `@Dao`.
   - Use queries SQL diretas e evite carregar listas inteiras para filtrar em memória.
   - Operações compostas devem utilizar `@Transaction`.
3. Registre a entidade e o DAO em `app/src/main/java/com/platform/app/data/local/PlatformDatabase.kt`.
4. Implemente o repositório em `app/src/main/java/com/platform/app/data/repository/<Feature>RepositoryImpl.kt`.

---

## 3. Passo 3: Injeção de Dependências (Dagger Hilt)
1. Em `di/AppModule.kt`, proveja o novo DAO:
   ```kotlin
   @Provides
   @Singleton
   fun provide<Feature>Dao(db: PlatformDatabase): <Feature>Dao = db.<feature>Dao
   ```
2. Em `di/RepositoryModule.kt`, vincule a interface à implementação com `@Binds`:
   ```kotlin
   @Binds
   @Singleton
   abstract fun bind<Feature>Repository(impl: <Feature>RepositoryImpl): <Feature>Repository
   ```

---

## 4. Passo 4: Camada de Apresentação (MVI + Jetpack Compose)
1. Em `presentation/<feature>/`:
   - `<Feature>UiState.kt`: `data class` imutável com `isLoading`, `errorMessage` e dados da tela.
   - `<Feature>UiAction.kt`: `sealed interface` com intenções explícitas do usuário.
   - `<Feature>UiEffect.kt`: `sealed interface` para efeitos colaterais de disparo único (Snackbars, navegação).
   - `<Feature>ViewModel.kt`: Anotada com `@HiltViewModel`, recebe `UseCases`, expõe `StateFlow<UiState>` e `Channel<UiEffect>`.
   - `<Feature>Screen.kt`: Composable puro com suporte a Light/Dark Theme, proporções elegantes via `ui-elegance-and-proportions`, busca inline animada no TopAppBar e FAB com ícone nativo.
2. Adicione a rota em `presentation/navigation/Screen.kt` e registre no `presentation/navigation/NavGraph.kt`.
3. Se a tela fizer parte da navegação primária, adicione-a no `presentation/navigation/AppDrawer.kt`.

---

## 5. Passo 5: Testes e Validação
1. Crie testes unitários para a ViewModel e UseCases em `app/src/test/`.
2. Execute a compilação e validação do código.
