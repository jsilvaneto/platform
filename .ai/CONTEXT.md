# Contexto de Negócio e Domínio (.ai/CONTEXT.md)

Este documento registra a visão do produto, princípios fundamentais e invariantes de domínio do aplicativo móvel **Platform**.

---

## 1. Visão Geral do Produto
O **Platform** é uma plataforma pessoal de produtividade e gestão no Android, projetada para operar prioritariamente de forma **100% offline**. O primeiro módulo funcional implementado é o **Módulo Financeiro Pessoal**, especializado no controle rigoroso de **Contas a Pagar, Contas Parceladas e Contas Recorrentes**.

Qualquer integração remota ou sincronização com servidores externos está planejada exclusivamente para fases futuras, não existindo bloqueios ou chamadas ativas de rede na versão atual.

---

## 2. Invariantes de Domínio e Arquitetura Móvel
1. **Operação 100% Offline (Single Source of Truth Local)**:
   - Todo dado gerado ou manipulado pelo usuário é persistido exclusivamente no banco local **Room (SQLite)** e nas preferências locais **DataStore**. O aplicativo opera com 100% de funcionalidade mesmo em Modo Avião.
2. **Integridade Financeira em Centavos (`amountCents: Long`)**:
   - Todo valor monetário é um inteiro em centavos, eliminando perdas ou distorções causadas por aritmética de ponto flutuante (`Double`/`Float`).
3. **Padrão MVI com Efeitos Seguros**:
   - Estados de tela são imutáveis (`UiState`), intenções de usuário são enviadas via `UiAction` e efeitos colaterais de disparo único (como Snackbars e navegação) trafegam via canal dedicado (`UiEffect`) para evitar repetições em recomposição.
4. **Respeito ao Ciclo de Vida do Android**:
   - Todas as operações assíncronas são encapsuladas em `viewModelScope`, evitando vazamentos de memória (*memory leaks*) ao rotacionar a tela ou encerrar a Activity.
5. **Isolamento de Negócio (Clean Architecture)**:
   - A camada `domain` é 100% Kotlin puro, livre de dependências da Google Play, Android Framework (`Context`, `View`, `Composable`) ou persistência (`Room`, `Retrofit`).
6. **Ergonomia e Design System**:
   - Suporte nativo e obrigatório a temas Claro e Escuro (*Material 3 Dark Theme*), contrastes legíveis e alvos de toque mínimos de 48dp recomendados pelo Material Design.

---

## 3. Entidades do Módulo Financeiro
- **Conta (`Bill`)**: Contrato mestre da despesa (título, descrição, tipo, total em centavos, categoria vinculada).
- **Tipos de Conta (`BillType`)**:
  - `SINGLE`: Conta avulsa/pontual com vencimento único.
  - `INSTALLMENT`: Compra parcelada em N vezes com distribuição matemática precisa de centavos.
  - `RECURRING`: Conta mensal contínua ou assinatura (aluguel, internet, streaming).
- **Parcela / Vencimento (`BillInstallment`)**: Ocorrência individual com data de vencimento, status (`PENDING`, `PAID`, `OVERDUE`) e data de liquidação (`paidAt`).
- **Categoria (`Category`)**: Categorização visual com cor identificadora e auto-seeding inicial.
