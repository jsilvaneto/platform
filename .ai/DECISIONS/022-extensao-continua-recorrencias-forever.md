# ADR 022: Extensão Contínua de Recorrências FOREVER via Janela Deslizante Automática

## Status
Aprovado

## Contexto
No aplicativo **Platform**, contas a pagar cadastradas com tipo recorrente (`BillType.RECURRING`) e término contínuo (`RecurrenceEndType.FOREVER`) geravam apenas um lote estático inicial de instâncias no banco de dados via `CalculateInstallmentsUseCase` (ex: 12 ocorrências para frequência mensal, 30 para diária, 26 para semanal, 5 para anual).
À medida que os meses passavam e o usuário liquidava os pagamentos, após o término das 12 ocorrências iniciais, a conta sumia silenciosamente das projeções futuras, do dashboard, dos registros e dos lembretes diários de vencimento, sem nenhum aviso prévio.

## Decisão

### 1. Extensão sem Desvio Temporal em `CalculateInstallmentsUseCase`
- Adicionado o método de domínio:
  ```kotlin
  fun generateNextRecurringInstallments(
      bill: Bill,
      existingInstallments: List<BillInstallment>,
      occurrencesToAdd: Int? = null
  ): List<BillInstallment>
  ```
- **Preservação Estrita do Dia de Vencimento**: O cálculo da data de cada nova parcela utiliza como âncora a data da primeira parcela (`firstInstallment.dueDate`) com passo proporcional `(installmentNumber - firstInstallment.installmentNumber)`. Isso previne o efeito de drift cumulativo de dia do mês (ex: uma conta vencendo dia 31 de cada mês não é degradada permanentemente para o dia 28 após cruzar o mês de fevereiro).
- **Continuidade de Numeração**: As novas instâncias continuam estritamente a sequência numérica (`installmentNumber = lastNumber + 1..lastNumber + count`) e recalculam o total projetado da conta.

### 2. Caso de Uso de Domínio Puro (`ExtendRecurringBillsUseCase`)
- Criado o caso de uso `ExtendRecurringBillsUseCase` em `domain/usecase/`, livre de quaisquer dependências do Android Framework (`android.*`, `androidx.*`, `room.*`).
- Limiares de extensão por frequência ($N$ ocorrências ou proximidade de horizonte temporal):
  - **Mensal (`MONTHLY`)**: Estende +12 parcelas quando restarem $\le 3$ ocorrências futuras ou horizonte restante $\le 3$ meses.
  - **Diário (`DAILY`)**: Estende +30 parcelas quando restarem $\le 7$ ocorrências futuras.
  - **Semanal (`WEEKLY`)**: Estende +26 parcelas quando restarem $\le 4$ ocorrências futuras.
  - **Anual (`YEARLY`)**: Estende +5 parcelas quando restar $\le 1$ ocorrência futura.
- **Laço Auto-Recuperativo (`maxBatches = 5`)**: Caso o usuário passe períodos prolongados sem acessar o aplicativo, o caso de uso recupera automaticamente os ciclos passados e futuros até restabelecer a projeção adiante.
- **Idempotência**: Se a conta ainda dispõe de parcelas futuras além do limiar, nenhuma operação de escrita é disparada no banco.
- **Isolamento de Contas Delimitadas**: Contas com `RecurrenceEndType.BY_OCCURRENCES` (carnês/planos fechados) ou `RecurrenceEndType.UNTIL_DATE` (contratos temporários) nunca são estendidas.

### 3. Persistência Transacional no Room
- Em `BillInstallmentDao`, adicionada a query `getInstallmentsWithDetailsByBillId(billId)`.
- Em `FinancialRepositoryImpl`, adicionado o método transacional:
  ```kotlin
  override suspend fun addInstallments(bill: Bill, installments: List<BillInstallment>) {
      database.withTransaction {
          installmentDao.insertAll(installments.map { BillInstallmentEntity.fromDomain(it) })
          val allForBill = installmentDao.getInstallmentsByBillId(bill.id)
          val maxDueDate = allForBill.maxOfOrNull { it.dueDate }
          billDao.updateBillEndDateAndTotalInstallments(
              id = bill.id,
              recurrenceEndDate = maxDueDate,
              totalInstallments = allForBill.size
          )
      }
  }
  ```

### 4. Ciclo de Execução Periódica Oportuna e em Segundo Plano
- Em vez de introduzir a biblioteca pesada `WorkManager` em um app 100% offline-first, a execução da extensão é garantida em múltiplos pontos estratégicos:
  1. **Ao abrir o Dashboard (`DashboardViewModel`)**: Garante que os números e projeções do mês corrente estejam atualizados na inicialização e ao puxar para atualizar (`Refresh`).
  2. **Ao abrir Registros (`BillsViewModel`)**: Garante que a lista de contas e filtros disponham das parcelas projetadas.
  3. **Ao abrir Assinaturas & Recorrências (`RecurringInstallmentsViewModel`)**: Garante sincronia completa dos compromissos recorrentes.
  4. **Em Segundo Plano Diário via `AlarmManager` (`DueReminderReceiver`)**: Executa diariamente às 09:00 junto com a verificação matinal de contas vencendo no dia.
  5. **Ao Desbloquear o Aplicativo (`MainActivity`)**: Executa imediatamente ao sair do lock biométrico.

## Consequências
- Contas recorrentes infinitas (`FOREVER`) nunca mais somem do aplicativo.
- O usuário sempre terá uma janela rolante de projeção de pelo menos 1 ano no futuro para compromissos contínuos.
- Sem adição de dependências externas ou serviços em background consumidores de bateria.
- Cobertura por testes unitários cobrindo idempotência, cálculos contínuos, preservação de dia do mês e isolamento de contas finitas.
