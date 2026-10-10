# ADR 032: Exportação de Esquemas Room, Correção de Cascata em FKs (14→15) e Testes Instrumentados com MigrationTestHelper

## Status
Aprovado

## Contexto
Anteriormente, o banco de dados `PlatformDatabase` operava com `exportSchema = false` e migrações escritas à mão sem validação formal por `MigrationTestHelper` ou suíte de `androidTest`.

Além disso, a migração `MIGRATION_14_15` possuía uma falha crítica de integridade referencial:
1. **Ineficácia de PRAGMA foreign_keys**: Executava `PRAGMA foreign_keys = OFF` e `PRAGMA foreign_keys = ON` dentro da transação de migração. No SQLite, essa diretiva não tem efeito prático dentro de blocos de transação gerenciados pelo Room (`BEGIN TRANSACTION ... COMMIT`).
2. **Deleção em Cascata Acidental (Data Loss)**: Quando `PRAGMA foreign_keys` permanecia ativo no ambiente SQLite da conexão, executar `DROP TABLE credit_cards` antes de copiar e recriar as tabelas-filhas (`credit_card_invoices`) causava a exclusão em cascata (`ON DELETE CASCADE`) de todas as faturas cadastradas e seus relacionamentos com parcelas de despesas antes que pudessem ser salvas.

## Decisão

### 1. Ativação e Versionamento de Esquemas Room
- Em [PlatformDatabase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/local/PlatformDatabase.kt), definido `exportSchema = true` na anotação `@Database`.
- Em [build.gradle.kts](file:///c:/Users/jsilvaneto/drive/projects/platform/app/build.gradle.kts), configurado o argumento KSP:
  ```kotlin
  ksp {
      arg("room.schemaLocation", "$projectDir/schemas")
  }
  ```
- Configurado `sourceSets` para disponibilizar os esquemas como assets de testes:
  ```kotlin
  sourceSets {
      getByName("androidTest").assets.srcDirs("$projectDir/schemas")
      getByName("test").assets.srcDirs("$projectDir/schemas")
  }
  ```
- Versionados todos os esquemas JSON de 9 a 17 no diretório `app/schemas/com.platform.app.data.local.PlatformDatabase/`.

### 2. Correção da Transação de Migração 14→15
- Removidos os comandos ineficazes `PRAGMA foreign_keys = OFF` e `PRAGMA foreign_keys = ON`.
- Adicionado `PRAGMA defer_foreign_keys = ON` no início do método `migrate`, instruindo o motor SQLite a adiar a validação de restrições de chaves estrangeiras até o commit da transação.
- Invertida e ordenada a recriação das tabelas dependentes:
  1. Criação e população de `categories_new` e `expense_items_new` (filha de categories).
  2. Criação e população de `credit_card_invoices_new` (filha de credit_cards) **antes** de dropar a tabela-pai `credit_cards`.
  3. Criação e população de `credit_cards_new`.
  4. Drops ordenados (tabela-filha `credit_card_invoices` antes da pai `credit_cards`, tabela-filha `expense_items` antes da mãe `categories`).
  5. Renomeação das tabelas `_new` para os nomes finais e recriação dos índices.

### 3. Implementação de Testes Instrumentados com MigrationTestHelper
- Adicionada a dependência `androidx.room:room-testing` em `gradle/libs.versions.toml` e `app/build.gradle.kts`.
- Criado [PlatformDatabaseMigrationAndroidTest.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/androidTest/java/com/platform/app/data/local/PlatformDatabaseMigrationAndroidTest.kt) com `MigrationTestHelper` e `FrameworkSQLiteOpenHelperFactory`.
- O teste cobre o encadeamento das migrações 9→10 até 14→15 (e a cadeia completa 9→17) sobre um banco de dados na versão 9 previamente populado com entidades relacionais (cartões, faturas, despesas, parcelas vinculadas, contatos, categorias, contas e orçamentos).
- O teste valida:
  - Preservação estrita das contagens de linhas de `credit_cards`, `credit_card_invoices` e `bill_installments` antes e após as migrações.
  - Vínculo inalterado das parcelas às faturas (`invoiceId`).
  - Execução de `PRAGMA foreign_key_check` com asserção de **zero violações** de chave estrangeira.
  - Limpeza de bancos de teste no `@After` em conformidade com o princípio de Resíduo Zero.

## Consequências

### Positivas
- **Eliminação de Risco de Perda de Dados**: Faturas e parcelas vinculadas não sofrem mais deleção em cascata indevida durante atualizações de banco de dados.
- **Rastreabilidade e Governança de Schemas**: Com `exportSchema = true` e arquivos JSON versionados, qualquer alteração estrutural no Room passa por revisão de diff e controle de versão estrito.
- **Validação Confiável em Tempo de CI/Build**: `MigrationTestHelper` valida automaticamente se a estrutura final do SQLite corresponde com precisão milimétrica ao esquema esperado pelo Room.
- **Respeito Estrito à Arquitetura e Resíduo Zero**: Todos os testes instrumentados limpam os bancos temporários gerados durante a execução.

### 4. Evidência de Execução de Testes Instrumentados (`connectedAndroidTest`)
- **Alvo**: `PlatformDatabaseMigrationAndroidTest` (validação das cadeias 9→10 a 14→15 e 9→17 ponta a ponta com `MigrationTestHelper`).
- **Ambientes Testados**: Emuladores Android API 26 (Android 8.0 Oreo - minSdk) e API 34 (Android 14 - targetSdk).
- **Resultado dos Testes**:
  - `migration9To15_preservesInvoicesAndForeignKeys`: **SUCCESS** (0 falhas, integridade referencial mantida, zero registros descartados ou deletados acidentalmente).
  - `migration9To17_fullChain_maintainsCompleteIntegrity`: **SUCCESS** (0 falhas, transição completa com integridade referencial e zero resíduo no SQLite).
  - Verificação de chave estrangeira via `PRAGMA foreign_key_check`: **0 violações registradas**.
  - Isolamento de dados de teste: banco limpo no `@After`, respeitando rigorosamente a política de Resíduo Zero (`test_data_cleanup.md`).
