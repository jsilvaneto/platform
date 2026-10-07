# ADR 024: Enum FinancialAccountType e Migração Room v13

## Status
Aprovado

## Contexto
No modelo `FinancialAccount.kt` e na entidade `FinancialAccountEntity.kt`, o campo `accountType` era modelado como uma `String` livre com valores arbitrários (ex: `"Corrente"`, `"Carteira"`, `"Poupança"`, `"Investimento"`, `"CHECKING"`, `"CASH"`, `"SAVINGS"`, etc.).
Enquanto o restante do domínio do projeto adota enums estritos e tipagem forte (`BillType`, `BillStatus`, `ExpenseNature`, `ContactType`, `InvoiceStatus`), a ausência de um enum para contas bancárias gerava inconsistências:
1. Risco de persistência de valores inválidos ou variações de grafia sem detecção em tempo de compilação.
2. Fragmentação de identificação no Dashboard de métricas financeiras (`AccountSpend`), onde nomes de tipos e instituições se misturavam sem padronização.
3. Componentes de UI (`ManagementScreen`) contendo listas de strings hardcoded e lógicas duplicadas para ícones e rótulos de contas.

## Decisão

### 1. Criação do Enum de Domínio Puro (`FinancialAccountType`)
Criado o enum `FinancialAccountType` no pacote `com.platform.app.domain.model`:
```kotlin
enum class FinancialAccountType(val displayName: String) {
    CORRENTE("Conta Corrente"),
    CARTEIRA("Carteira / Dinheiro"),
    POUPANCA("Poupança"),
    INVESTIMENTO("Investimento");

    companion object {
        fun fromString(value: String?): FinancialAccountType {
            if (value.isNullOrBlank()) return CORRENTE
            return when (value.trim().uppercase()) {
                "CORRENTE", "CONTA CORRENTE", "CHECKING", "CREDIT_CARD", "CARTÃO DE CRÉDITO", "OUTRO" -> CORRENTE
                "CARTEIRA", "DINHEIRO", "CASH", "CARTEIRA / DINHEIRO", "DINHEIRO / CARTEIRA" -> CARTEIRA
                "POUPANCA", "POUPANÇA", "SAVINGS" -> POUPANCA
                "INVESTIMENTO", "INVESTMENT", "INVESTIMENTO / RESERVA", "RESERVA", "RESERVA DE EMERGÊNCIA", "RESERVA DE EMERGENCIA" -> INVESTIMENTO
                else -> entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: CORRENTE
            }
        }
    }
}
```
- Totalmente desacoplado de dependências do Android Framework (`android.*`, `androidx.*`, `room.*`).
- Fornece rótulos humanizados (`displayName`) e resolução resiliente de aliases históricos (`fromString`) com fallback seguro para `CORRENTE`.

### 2. Migração de Banco de Dados Room (v12 -> v13)
- Criado o conversor de tipos `FinancialAccountTypeConverter` em `com.platform.app.data.local.converter`, registrado via `@TypeConverters` na entidade e no banco.
- Incrementada a versão do banco de dados em `PlatformDatabase.kt` para `version = 13`.
- Implementada a migração `MIGRATION_12_13` que normaliza registros existentes no SQLite para os nomes canônicos do enum:
  ```kotlin
  val MIGRATION_12_13 = object : Migration(12, 13) {
      override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("UPDATE financial_accounts SET accountType = 'CORRENTE' WHERE accountType IN ('CHECKING', 'Conta Corrente', 'Corrente', 'CREDIT_CARD', 'Cartão de Crédito', 'Outro')")
          db.execSQL("UPDATE financial_accounts SET accountType = 'CARTEIRA' WHERE accountType IN ('CASH', 'Dinheiro / Carteira', 'Carteira', 'Dinheiro')")
          db.execSQL("UPDATE financial_accounts SET accountType = 'POUPANCA' WHERE accountType IN ('SAVINGS', 'Poupança', 'Poupanca')")
          db.execSQL("UPDATE financial_accounts SET accountType = 'INVESTIMENTO' WHERE accountType IN ('INVESTMENT', 'Investimento / Reserva', 'Investimento', 'Reserva de Emergência')")
          db.execSQL("UPDATE financial_accounts SET accountType = 'CORRENTE' WHERE accountType NOT IN ('CORRENTE', 'CARTEIRA', 'POUPANCA', 'INVESTIMENTO')")
      }
  }
  ```
- Migração devidamente registrada no provider `providePlatformDatabase` em `AppModule.kt`.

### 3. Integração com Casos de Uso e Dashboard
- `AccountSpend` (`FinancialDashboardMetrics.kt`) atualizado para incorporar o campo `accountType: FinancialAccountType = FinancialAccountType.CORRENTE`.
- `GetFinancialDashboardUseCase.kt` mapeia o tipo de conta e atribui `bankName = acc.accountType.displayName` e `accountType = acc.accountType`.
- No card de concentração de contas bancárias da `StatisticsScreen` (`AccountsDistributionCard`), a renderização utiliza ícones contextuais por tipo (`AccountBalance`, `Payments`, `Savings`, `TrendingUp`) e exibe o rótulo do tipo de conta.

### 4. Apresentação & MVI (`ManagementScreen`)
- Cards de contas e visualizações de detalhe utilizam `account.accountType.displayName`.
- O diálogo de cadastro/edição (`AddEditAccountDialog`) substitui as opções hardcoded por `FinancialAccountType.entries`, garantindo seleção segura e visualmente enriquecida.

### 5. Cobertura de Testes
- Adicionado `FinancialAccountTypeTest.kt` validando exaustivamente nomes de exibição, resolução de aliases legados, tolerância a nulos/desconhecidos, mapeamento bidirecional em `FinancialAccountEntity` e o conversor `FinancialAccountTypeConverter`.
- Ajustado `ManagementViewModelTest.kt` para utilizar o enum `FinancialAccountType.CORRENTE`.
