# Contexto de Negócio e Domínio (.ai/CONTEXT.md)

Este documento registra a visão do produto, princípios fundamentais e invariantes de domínio do aplicativo móvel **Platform**.

---

## 1. Visão Geral do Produto
O **Platform** é um aplicativo Android nativo projetado para operar prioritariamente de forma **100% offline**. Ele funciona como um gestor de dados, notas e tarefas local de alta performance, proporcionando uma experiência reativa, privativa e sem qualquer dependência de conectividade de rede para sua usabilidade cotidiana.

Qualquer integração remota ou sincronização com servidores externos está planejada exclusivamente para fases futuras, não existindo bloqueios ou chamadas ativas de rede na versão atual.

---

## 2. Invariantes de Domínio e Arquitetura Móvel
1. **Operação 100% Offline (Single Source of Truth Local)**:
   - Todo dado gerado ou manipulado pelo usuário é persistido exclusivamente no banco local **Room (SQLite)** e nas preferências locais **DataStore**. O aplicativo opera com 100% de funcionalidade mesmo em Modo Avião.
2. **Padrão MVI com Efeitos Seguros**:
   - Estados de tela são imutáveis (`UiState`), intenções de usuário são enviadas via `UiAction` e efeitos colaterais de disparo único (como Snackbars e navegação) trafegam via canal dedicado (`UiEffect`) para evitar repetições em recomposição.
3. **Respeito ao Ciclo de Vida do Android**:
   - Todas as operações assíncronas são encapsuladas em `viewModelScope`, evitando vazamentos de memória (*memory leaks*) ao rotacionar a tela ou encerrar a Activity.
4. **Isolamento de Negócio (Clean Architecture)**:
   - A camada `domain` é 100% Kotlin puro, livre de dependências da Google Play, Android Framework (`Context`, `View`, `Composable`) ou persistência (`Room`, `Retrofit`).
5. **Ergonomia e Design System**:
   - Suporte nativo e obrigatório a temas Claro e Escuro (*Material 3 Dark Theme*), contrastes legíveis e alvos de toque mínimos de 48dp recomendados pelo Material Design.
