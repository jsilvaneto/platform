# Status do Projeto (STATUS.md)

Este documento registra o checklist de funcionalidades, fases de implementação e governança de releases do aplicativo móvel **Platform**.

---

## 🚀 Fases do Projeto

| Fase | Escopo | Status |
| :--- | :--- | :--- |
| **Fase 1: Fundação Mobile & Governança** | Clean Architecture, Gradle Kotlin DSL, Version Catalog, Hilt, Room, Material 3 e IA | **100% CONCLUÍDO** |
| **Fase 2: Arquitetura 100% Offline-First & MVI** | Operação 100% local com Room, DataStore, MVI, biometria e navegação AppDrawer | **100% CONCLUÍDO** |
| **Fase 3: Módulo de Contas a Pagar & Planejamento** | Contas Avulsas, Parceladas, Recorrentes, Orçamentos, Metas, Estatísticas e Cadastros Base | **100% CONCLUÍDO** |
| **Fase 4: Saneamento Arquitetural & Skills de IA** | Limpeza de código zumbi/boilerplate, correção de bugs de recorrência, transações e skills | **100% CONCLUÍDO** |
| **Fase 5: Backup Local & Exportação de Dados** | Exportação e restauração local via JSON com SAF, transações atômicas Room e ShareSheet | **100% CONCLUÍDO** (v1.3.0) |
| **Fase 6: Harmonização Visual Global (4 Etapas)** | Padrão de Detalhes com BottomSheet, 3 pontos, sem botões inline e blocos ergonômicos | **100% CONCLUÍDO** (v1.3.1) |
| **Fase 7: Wallet 100% Pessoal, Contas a Pagar & Cartões** | Contexto pessoal único, ExpenseNature, Itens de despesa, Forecast semafórico e Faturas | **100% CONCLUÍDO** (v1.4.0) |
| **Fase 8: Grande Update das 4 Telas Centrais** | Hero Executivo, Privacidade, SegmentedTabs, Batch Actions, Apple Card View e Cronogramas | **100% CONCLUÍDO** (v1.4.1) |
| **Fase 9: Estatísticas & Inteligência Financeira 360°** | 3 Eixos Temporais (Passado, Presente e Futuro), Curva de Desoneração, Rigidez Orçamentária e Tomada de Decisão | **100% CONCLUÍDO** (v1.4.2) |
| **Fase 10: Excelência Operacional, UX & Lembretes Locais** | Transições Cinemáticas, Desfazer Baixa (Undo), Duplicação de Despesas e Notificações de Vencimento | **100% CONCLUÍDO** (v1.4.3) |
| **Fase 11: Sincronização em Nuvem (API Bidirecional)** | Backend remoto, motor de sync offline-first, backup automático no banco de dados | **PLANEJADA (Fase 2)** |

---

## 📋 Checklist de Funcionalidades Implementadas

### Fundação, Arquitetura e MVI
- [x] Configuração raiz do Gradle com Kotlin DSL (`build.gradle.kts` e `settings.gradle.kts`).
- [x] Version Catalog centralizado em `gradle/libs.versions.toml`.
- [x] Arquitetura Clean Architecture: Presentation, Domain, Data e Core.
- [x] Padrão **MVI (Model-View-Intent)** implementado com `UiState`, `UiAction` e `UiEffect` (Channel bufferizado).
- [x] Operação **100% Offline-First**: O Room Database é a única fonte da verdade e o app opera sem internet.
- [x] Injeção de dependências com Dagger Hilt (`@HiltAndroidApp`, `AppModule`, `RepositoryModule`).
- [x] Persistência local com Room Database (`PlatformDatabase` v7) com transações atômicas (`database.withTransaction`).
- [x] Persistência de configurações e preferências via **AndroidX DataStore** (`PreferencesManager`).
- [x] Bloqueio e segurança com **AndroidX Biometric** (`BiometricAuthManager`, `BiometricLockOverlay`).
- [x] Design System Material 3 com Dark Mode nativo (`PlatformTheme`, `Color`, `Type`).
- [x] Navegação moderna com Navigation Compose e menu lateral despoluído (`AppDrawer.kt`, `NavGraph.kt`).

### Wallet 100% Pessoal & Contas a Pagar (v1.4.0)
- [x] **Contexto Único Pessoal**: Remoção de qualquer seletor ou campo de perfil corporativo (`profileType`, `PESSOAL`/`EMPRESA`).
- [x] **Foco em Contas a Pagar**: Cadastro ágil com 3 campos mandatórios (Descrição, Valor em centavos `Long`, Vencimento `Long`), com Categoria/Item opcionais (`NewExpenseScreen`).
- [x] **Natureza do Gasto (ExpenseNature)**: 4 pilares (`OBRIGATORIO`, `NECESSARIO`, `DESEJA`, `NENHUM`), herança estrita da categoria para o item de despesa.
- [x] **Itens de Despesa**: Depreciação de subcategorias e adoção de `expense_items` vinculados à categoria mãe, com tela dedicada `ExpenseItemsScreen`.
- [x] **Dashboard com Efeito Cascata em Tempo Real**: `Total Previsto = Contas Pendentes + Faturas Abertas/Fechadas do Mês` calculado via Flow reativo (`CalculateMonthlyForecastUseCase`).
- [x] **Agrupamento Semafórico de Urgência**: 🔴 Atrasadas, 🟡 Vence Hoje, ⚪ Próximos 7 Dias e 🟢 Pagas no Mês (colapsável).
- [x] **Baixa com 1 Toque**: Ação direta de pagamento na conta/fatura com alteração de status para `PAGO` no banco e dedução instantânea do Total Previsto via Flow.
- [x] **Gestão de Cartões de Crédito & Extrato de Faturas**: Cálculo de limite disponível (`CreditCardCalculator`), fechamento por dia de corte (`closingDay`), status de faturas e liquidação em 1 toque na `CreditCardsScreen`.
- [x] **Lançamento Focado em Itens (Item-Centric Form)**: Item de despesa como protagonista (1º campo obrigatório), herança automática de título e descrição convertida em observações adicionais opcionais no final do formulário (`NewExpenseScreen`).
- [x] **Design System Premium Minimalista**:
  - Eliminação de elevação tonal com `surfaceTint = Color.Transparent` em Dark e Light Mode (zero vazamento de roxo/lavanda default M3).
  - Remoção de 100% das cores literais hardcoded em telas (`Color.White`, `Color.Black`, hexadecimais soltos), substituídas por tokens semânticos de `Theme.kt`, `Color.kt` e `MaterialTheme.colorScheme`.
  - Simetria rigorosa (grid 4/8/12/16/20/24 dp, cantos de 16.dp para cards e 10.dp para botões/inputs).
  - Unificação de FABs 100% circular (`CircleShape`) em todas as 9 telas financeiras e componentes centrais reutilizáveis (`PlatformEmptyState`, `PlatformAvatar`).
  - Higienização visual estrita: remoção de botões de exclusão inline, eliminação de emojis informais e substituição do código zumbi `DashboardScreen.kt`.
- [x] **Validação com Testes Unitários Locais**:
  - `NewExpenseValidationTest`: 6 testes unitários aprovados (campos obrigatórios, herança de título pelo item, cálculo de parcelas e natureza).
  - `CreditCardManagementTest`: 4 testes aprovados (fechamento, limites e dependências de exclusão).
  - `BillCalculationTest`, `ExpenseItemNatureTest`, `InvoiceClosingTest`: Todos aprovados com 100% de sucesso.

### Backup & Recuperação de Dados (v1.3.0)
- [x] DTO unificado `BackupDataDto` com versionamento e timestamp.
- [x] Restauração atômica via transação Room (`database.withTransaction`) com preservação de integridade referencial.
- [x] Integração com Storage Access Framework (SAF) nativo (`CreateDocument` e `OpenDocument`).
- [x] Compartilhamento direto com mensageiros via `FileProvider` (`file_paths.xml`).
- [x] Painel de status com indicador de data/hora do último backup e diálogo de advertência prévia para restaurações.

### Excelência Operacional, UX & Lembretes Locais (v1.4.3)
- [x] **Transições Cinemáticas no NavGraph**: Animações fluidas entre destinos (`slideIntoContainer` + `fadeIn`/`fadeOut`).
- [x] **Mecanismo de Desfazer (Undo)**: Reversão imediata de pagamentos de contas e faturas com 1 clique na `HomeScreen`.
- [x] **Reabertura Atômica de Faturas (`reopenInvoice`)**: Restauração consistente de faturas e parcelas vinculadas no Room.
- [x] **Duplicação Ágil de Despesas**: Ação no menu do BottomSheet de detalhes clonando todos os dados da despesa na `NewExpenseScreen`.
- [x] **Notificações Locais Offline de Vencimentos**: Lembretes inteligentes para despesas e faturas que vencem no dia (`DueReminderManager`, `DueReminderReceiver`).
- [x] **Suíte de Testes Automatizados**: Novos testes `HomeViewModelTest` e `NewExpenseViewModelTest` com 100% de sucesso.

### Gestão & Edição de Itens de Despesa (v1.4.4)
- [x] **Edição Completa de Itens de Despesa**: Fluxo reativo para alteração de nome e categoria vinculada (`AddEditExpenseItemBottomSheet`).
- [x] **Padrão de Detalhes Universal (`ExpenseItemDetailBottomSheet`)**: ModalBottomSheet de detalhes acionado por toque no card com avatar, vínculos de categoria, badge de natureza e menu de 3 pontos (`MoreVert`).
- [x] **Acesso Direto à Edição em Nova Despesa (`NewExpenseScreen`)**: Atalho "Gerenciar Itens (Criar / Editar)" no menu dropdown e botão de edição rápida de 1 toque no chip de natureza herdada.
- [x] **Suíte de Testes Automatizados**: Novos testes unitários aprovados em `ExpenseItemsViewModelTest.kt` cobrindo carga, filtros, salvamento de edição e exclusão.

### Baixa de Pagamento Retroativa & Data Real de Pagamento (v1.4.5)
- [x] **Campo `actualPaymentDate` no Domínio e Room**: Adicionado campo opcional `actualPaymentDate: Long?` em `BillInstallment` e `BillInstallmentEntity`.
- [x] **Migração de Banco de Dados Room (`PlatformDatabase` v10)**: Migração `MIGRATION_9_10` (`ALTER TABLE bill_installments ADD COLUMN actualPaymentDate INTEGER DEFAULT NULL`) registrada e injetada no `AppModule`.
- [x] **Diálogo Material 3 de Baixa (`ConfirmPaymentDialog`)**: Confirmação visual de pagamento com atalhos "Hoje", "No Vencimento" e `DatePickerDialog` customizado integrado em todas as telas de liquidação.
- [x] **Cálculo da Taxa de Pontualidade (`GetFinancialDashboardUseCase`)**: Métrica `onTimePaymentRate` atualizada para comparar `actualPaymentDate` (fallback `paidAt`) com `dueDate`, eliminando falsos atrasos em baixas retroativas.
- [x] **Suíte de Testes Automatizados**: Cobertura expandida em `ToggleInstallmentPaymentUseCaseTest`, `GetFinancialDashboardUseCaseTest`, `BillsViewModelTest` e `HomeViewModelTest`.
- [x] **Decisão Arquitetural Documentada**: Criada [ADR 020](.ai/DECISIONS/020-data-real-pagamento-e-baixa-retroativa.md).

### Governança e Testes Automatizados
- [x] Cobertura de testes unitários executada com 100% de sucesso via Gradle (`./gradlew testDebugUnitTest`).
- [x] Script de versionamento móvel sincronizado (`scripts/bump-version.ps1` e `scripts/bump-version.sh`).
- [x] Regra mandatória de governança e sincronização de versão em `.agents/rules/governance_and_versioning.md`.

---

## 🎯 Próximos Passos
- [ ] Fase 11: Estruturação da API remota e sincronização bidirecional offline-first com banco em nuvem.
