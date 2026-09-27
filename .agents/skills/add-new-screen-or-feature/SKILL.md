---
name: add-new-screen-or-feature
description: >-
  Use this skill when the user asks to create a new Android screen, feature, Room entity,
  API endpoint, or complete flow in the platform app.
---

# Workflow: Adicionar Nova Tela ou Feature no Android

Este guia detalha o passo a passo exato para criar uma nova funcionalidade completa, preservando o desacoplamento de Clean Architecture, MVVM e injeção de dependências com Hilt.

## 1. Passo 1: Domínio Puro
1. Crie o modelo em `app/src/main/java/com/platform/app/domain/model/<Feature>.kt`.
2. Declare a interface do repositório em `app/src/main/java/com/platform/app/domain/repository/<Feature>Repository.kt`.
3. Implemente os Casos de Uso em `app/src/main/java/com/platform/app/domain/usecase/Get<Feature>UseCase.kt`.

## 2. Passo 2: Camada de Dados (Room e/ou Retrofit)
1. Crie a entidade Room em `app/src/main/java/com/platform/app/data/local/entity/<Feature>Entity.kt`.
2. Crie o DAO em `app/src/main/java/com/platform/app/data/local/dao/<Feature>Dao.kt`.
3. Registre no `PlatformDatabase.kt`.
4. Se houver integração remota, declare os métodos no `PlatformApiService.kt` e DTOs correspondentes.
5. Implemente o repositório em `app/src/main/java/com/platform/app/data/repository/<Feature>RepositoryImpl.kt`.

## 3. Passo 3: Módulos de Injeção de Dependências
1. No `di/AppModule.kt`, adicione o provider do novo DAO.
2. No `di/RepositoryModule.kt`, faça o `@Binds` do novo repositório.

## 4. Passo 4: Camada de Apresentação (Compose)
1. Crie o estado imutável em `presentation/<feature>/<Feature>UiState.kt`.
2. Crie a ViewModel em `presentation/<feature>/<Feature>ViewModel.kt` com `@HiltViewModel`.
3. Crie a tela Composable em `presentation/<feature>/<Feature>Screen.kt`.
4. Adicione a rota em `presentation/navigation/Screen.kt` e registre no `NavGraph.kt`.

## 5. Passo 5: Testes e Validação
1. Crie testes unitários para a ViewModel e UseCase em `app/src/test/`.
2. Execute `./gradlew test` para validar a compilação e integridade dos testes.
