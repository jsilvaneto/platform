---
trigger: always_on
---

# Fronteiras Arquiteturais no Android (architecture.md)

1. **Separação Rígida de Camadas**:
   - `domain/`: Regras de negócio puras. Proibido importar `android.*`, `androidx.*`, `room.*` ou `retrofit.*`.
   - `data/`: Acesso a banco de dados local e serviços de rede. Mapeia dados de DTOs e Entities para modelos de domínio usando funções `toDomain()`.
   - `presentation/`: Exibição visual pura em Jetpack Compose e orquestração de estado com ViewModel.
   - `di/`: Módulos de injeção de dependência Hilt.
2. **Proibição de Lógica Mista**:
   - NUNCA realize queries ou chamadas de API diretamente dentro de um Composable ou Activity.
   - NUNCA exponha classes de persistência (`Entity`, `DAO`, `DTO`) diretamente para a camada de apresentação. A apresentação consome exclusivamente modelos do `domain`.
   - O ViewModel apenas delega tarefas para Casos de Uso (`UseCases`) ou Repositórios, atualizando o `UiState`.
