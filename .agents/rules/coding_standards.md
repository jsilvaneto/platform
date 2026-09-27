---
trigger: always_on
---

# Padrões de Código e Convenções Kotlin/Android (coding_standards.md)

- **Tipagem Estrita e Imutabilidade**:
  - Proibido o uso de `Any?` genérico ou casts inseguros com `as`.
  - Prefira `val` em vez de `var`. Entidades e estados de UI devem ser `data class` imutáveis com propriedades `val`.
- **Nomenclatura**:
  - `PascalCase` para classes, interfaces, enums, sealed classes e funções `@Composable`.
  - `camelCase` para funções comuns, métodos, variáveis e propriedades.
  - Constantes em `UPPER_SNAKE_CASE` em `companion object`.
- **Gerenciamento de Estados**:
  - No ViewModel: `private val _uiState = MutableStateFlow(...)` e `val uiState: StateFlow<...> = _uiState.asStateFlow()`.
  - Na UI: colete com `val uiState by viewModel.uiState.collectAsState()`.
- **Suporte Obrigatório a Dark Mode**:
  - Proibido uso de cores literais como `Color.Black` ou `Color.White` nos componentes. Use `MaterialTheme.colorScheme.*`.
- **Tratamento de Exceções**:
  - Trate falhas de rede e banco de forma amigável com `Result<T>` ou classes `Resource.Error(message)`.
