# ADR 004: Módulo Financeiro Pessoal (Contas a Pagar, Parceladas e Recorrentes)

## Status
Aprovado

## Contexto
O aplicativo **Platform** é uma plataforma pessoal cujo primeiro módulo funcional canônico é o **Controle Financeiro**, especializado em **Contas a Pagar**. Era necessário definir a modelagem para atender contas avulsas, compras parceladas e gastos recorrentes com precisão contábil e usabilidade fluida offline.

## Decisão
1. **Representação Monetária**: Uso exclusivo de `amountCents: Long` para todos os cálculos e armazenamento no SQLite.
2. **Separação entre Conta (`Bill`) e Parcela/Vencimento (`BillInstallment`)**:
   - Uma `Bill` armazena a intenção/contrato (título, descrição, tipo, total em centavos, categoria).
   - `BillInstallment` armazena a ocorrência física do vencimento com data, status e liquidação.
   - Para contas parceladas, a distribuição de centavos joga o resto da divisão na 1ª parcela, garantindo que a soma bata no centavo exato.
   - Para contas recorrentes, é gerada uma projeção inicial de 12 meses de vencimentos recorrentes.
3. **Navegação com Bottom Navigation Bar**:
   - `Dashboard`: KPIs de fluxo de caixa (Total, Pago, Pendente, Atrasado), próximos vencimentos e distribuição por categoria.
   - `Contas`: Listagem de vencimentos com busca, filtros de tipo/status e liquidação rápida com feedback via Snackbar.
   - `Categorias`: Gerenciamento com auto-seed inteligente inicial.

## Consequências
- Solução contábil sem perdas de precisão ou inconsistências de arredondamento.
- Usabilidade rápida para o usuário marcar contas como pagas no dia a dia com 1 toque.
- Facilidade para expansões futuras (ex: relatórios anuais, exportação de extrato, etc.).
