# ADR 023: Tipificação de Contatos (ContactType) e Migração Room v12

## Status
Aprovado

## Contexto
No aplicativo móvel **Platform**, o cadastro de contatos (`Contact.kt` e `ContactEntity.kt`) era homogêneo e não distinguia a natureza do vínculo entre o usuário e a contraparte. Empresas e fornecedores com endereço e razão social (ex: "Copel", "Colégio", "Supermercado") compartilhavam a mesma estrutura que pessoas físicas utilizadas para dívidas e empréstimos pessoais (ex: "Mãe", "Amigo"), além de concessionárias e órgãos públicos (ex: "Prefeitura", "Receita Federal").
Essa ausência de tipificação gerava limitações:
1. Impossibilidade de segmentar e filtrar visualmente a carteira de contatos entre despesas institucionais/empresariais e compromissos com pessoas físicas.
2. Poluição da lista principal de contatos na `ContactsScreen`, sem distinção visual de contexto.
3. Dificuldade de relatórios futuros direcionados a "quanto devo a empresas" versus "quanto devo a pessoas físicas".

## Decisão

### 1. Enum de Domínio Puro (`ContactType`)
Criado o enum `ContactType` em `domain/model/ContactType.kt`:
```kotlin
enum class ContactType(val displayName: String) {
    PESSOA_FISICA("Pessoa Física"),
    FORNECEDOR("Fornecedor"),
    ORGAO_PUBLICO("Órgão Público");

    companion object {
        fun fromString(value: String?): ContactType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: FORNECEDOR
        }
    }
}
```
- Livre de dependências do Android Framework (`android.*`, `androidx.*`), preservando a regra de ouro da Clean Architecture.
- Incorporado ao modelo imutável `Contact(..., val type: ContactType = ContactType.FORNECEDOR, ...)`.

### 2. Evolução de Schema no Room Database (Migração v11 -> v12)
- Adicionado campo `type: String = "FORNECEDOR"` em `ContactEntity.kt`.
- Incrementada a versão do `@Database` em `PlatformDatabase.kt` para `version = 12`.
- Implementada a migração `MIGRATION_11_12`:
  ```kotlin
  val MIGRATION_11_12 = object : Migration(11, 12) {
      override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE contacts ADD COLUMN type TEXT NOT NULL DEFAULT 'FORNECEDOR'")
      }
  }
  ```
- Registrada a migração no `AppModule.kt`.
- Retrocompatibilidade total garantida para backups locais via SAF JSON (`BackupDataDto.kt`), onde contatos legados sem campo `type` recebem fallback seguro para `"FORNECEDOR"`.

### 3. Apresentação & MVI (`ContactsScreen`)
- **Filtro Rápido em Chips (`ContactTypeFilterRow`)**:
  - Opções: "Todos (N)", "Pessoa Física (N)", "Fornecedor (N)" e "Órgão Público (N)" com contadores dinâmicos e ícones temáticos.
- **Hierarquia e Agrupamento por Tipo**:
  - Quando "Todos" estiver selecionado, a listagem agrupa os contatos por categoria (`ContactSectionHeader`), criando seções com seus respectivos totais.
  - Ao selecionar um filtro específico, a listagem exibe diretamente os contatos daquele tipo.
- **Diferenciação Visual Premium (`ContactTypeBadge` & `PlatformAvatar`)**:
  - Cada card exibe badge compacto com ícone temático (`Person`, `Business`, `AccountBalance`) e cor de destaque semântica.
  - O avatar inicial herda a tonalidade correspondente ao tipo de contato.
- **Seletor de Tipo no Cadastro / Edição (`AddContactBottomSheet`)**:
  - Componente de seleção em cartões compactos permitindo alternar de forma intuitiva entre "Pessoa", "Empresa" e "Público".

### 4. Testes Automatizados
- Cobertura expandida em `ContactsViewModelTest.kt` validando:
  - Filtro individual por cada um dos 3 tipos.
  - Restauração ao selecionar `null` ("Todos").
  - Busca textual combinada com filtro de tipo de contato.
