# ADR 033: Guard Rails de Qualidade, Separação de Agregados no Repositório e Ciclo de Vida de Faturas

## Status
Aprovado

## Contexto
Durante a evolução das versões 1.20 a 1.23, foram implementados diversos aprimoramentos cruciais na persistência, segurança e ciclo de vida de faturas e recorrências:
1. **Materialização de Faturas e Desacoplamento CQS**:
   - `getOrCreateInvoiceForMonth` continha efeitos colaterais (materializava recorrências na fatura durante uma simples busca ou criação de fatura).
   - O comando `payInvoice` liquidava a fatura sem garantir a materialização prévia de compras recorrentes do ciclo corrente, gerando inconsistências caso rotinas em background não tivessem sido executadas.
   - Parcelas pausadas (`PAUSED`) entravam na fatura e no cômputo dos totais (`getInvoiceTotals`), inflando indevidamente o valor da fatura.
2. **Crescimento Monolítico do Repositório**:
   - [FinancialRepositoryImpl.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/repository/FinancialRepositoryImpl.kt) ultrapassou 633 linhas de código, acumulando responsabilidades de múltiplos agregados (Contas/Parcelas, Cartões/Faturas e Catálogos/Cadastros base).
3. **Controle de Tamanho de Arquivos (Guard Rails)**:
   - A verificação `checkFileSize` existia apenas para arquivos `@Composable` da camada de apresentação (`presentation/`), deixando desprotegidas as camadas `data/` e `domain/`.
4. **Limitação Histórica da Ancoragem de Recorrências de Cartão (Pré-1.20)**:
   - Na migração Room 15→16 (`MIGRATION_15_16`), o campo `recurrenceAnchorDate` foi retroalimentado com `MIN(dueDate)`. Em cartões de crédito legados, as parcelas já materializadas haviam tido seu `dueDate` ajustado para o dia de vencimento da fatura do cartão, e não o dia exato da compra.

## Decisão

### 1. Separação Estrita de Agregados no Repositório
Dividiu-se a implementação em data sources coesos com menos de 260 linhas cada:
- [BillDataSource.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/repository/BillDataSource.kt): Agregado de contas (`Bill`) e parcelas (`BillInstallment`).
- [CreditCardDataSource.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/repository/CreditCardDataSource.kt): Agregado de cartões (`CreditCard`) e faturas (`CreditCardInvoice`).
- [CatalogDataSource.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/repository/CatalogDataSource.kt): Agregado de catálogo e cadastros base (categorias, itens de despesa, contatos, contas bancárias, meios de pagamento e sementes iniciais).
- [FinancialRepositoryImpl.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/repository/FinancialRepositoryImpl.kt): Mantido como fachada temporária e porta de injeção transparente no Hilt, garantindo compatibilidade reversa imediata com todos os UseCases e ViewModels.

### 2. Higiene no Ciclo de Faturas e Desacoplamento CQS
- **Exclusão de `PAUSED`**: As queries em `BillInstallmentDao` (`getInvoiceTotals`, `getInvoiceTotalsList`, `getInvoiceTotal` e `getUnattachedRecurringInstallmentsForCard`) agora filtram expressamente `status != 'PAUSED'`.
- **Separação CQS**: `getOrCreateInvoiceForMonth` foi purificada para realizar apenas consulta e criação determinística da linha de fatura no SQLite. O processo de vinculação de parcelas recorrentes foi isolado no método de comando `materializeRecurringForInvoice(cardId, invoiceId, referenceMonth)`.
- **Materialização Preventiva no Pagamento**: O método `payInvoice` agora busca a fatura e executa `materializeRecurringForInvoice` antes de efetuar o update em lote para `PAID`, assegurando que todas as recorrências do mês estejam na fatura antes de sua quitação.
- **Otimização no CreateBillUseCase**: A resolução de faturas para novas compras em lote utiliza cache de referência de competências (`invoiceCache`), chamando `getOrCreateInvoiceForMonth` uma única vez por mês distinto.

### 3. Extensão do Guard Rail `checkFileSize` para Data e Domain
A task Gradle `:app:checkFileSize` foi estendida para inspecionar simultaneamente os diretórios `presentation/`, `data/` e `domain/`.
- Teto: 600 linhas por arquivo Kotlin.
- Baseline ativa em `config/file-size-baseline.txt` (atualmente com zero violações, todos os arquivos cumprem o teto).

### 4. Documentação da Limitação Técnica de Âncora Pré-1.20
Para assinaturas no cartão criadas antes da v1.20.0 cuja âncora foi inferida como `MIN(dueDate)`, a competência mensal é preservada, porém o ciclo de fechamento toma como referência o dia do vencimento da fatura prévia. A partir da v1.20.0 e refinamentos da v1.24.0, o fluxo de criação armazena `bill.recurrenceAnchorDate` a partir do `state.dueDate` original da compra com precisão imutável. Usuários que necessitarem alterar valores futuros ou parcelas individuais contam com os diálogos dedicados de ajuste na interface (`EditInstallmentBottomSheet` e `RecurringDetailBottomSheet`).

## Consequências

### Positivas
- **Alta Coesão e Modularidade**: O repositório financeiro foi reduzido de 640 linhas para ~210 linhas, delegando para fontes especializadas e testáveis.
- **Faturas 100% Confiáveis**: Nenhuma assinatura em pausa inflaciona a fatura, e pagar uma fatura nunca mais deixa parcelas atrasadas fora dela.
- **Rigor Automatizado de Tamanho de Código**: Nenhum arquivo novo em `presentation`, `data` ou `domain` pode ultrapassar 600 linhas sem disparar erro de compilação no Gradle.
