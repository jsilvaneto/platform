# Contexto de Negócio e Domínio (.ai/CONTEXT.md)

Este documento registra a visão do produto, princípios fundamentais e invariantes de domínio do aplicativo móvel **Platform**.

---

## 1. Visão Geral do Produto
O **Platform** é um aplicativo Android nativo projetado para proporcionar uma experiência fluida, reativa e offline-first para os usuários. Ele funciona como a interface móvel canônica da plataforma, conectando-se a serviços e dados locais e remotos com segurança, alta disponibilidade e ergonomia visual adaptativa.

---

## 2. Invariantes de Domínio e Arquitetura Móvel
1. **Offline-First com Cache Local**:
   - Dados primários são persistidos no Room Database local antes de serem sincronizados com APIs remotas. O usuário nunca deve ficar bloqueado em tela branca por falta de conexão.
2. **Imutabilidade e Fluxo Unidirecional (UDF)**:
   - Os estados de tela fluem em uma única direção: o ViewModel emite `UiState` imutável, a tela renderiza e emite eventos/intenções de volta para o ViewModel.
3. **Respeito ao Ciclo de Vida do Android**:
   - Nenhuma operação de rede ou banco deve continuar ativa se a tela ou o ViewModel for destruído (`viewModelScope`).
4. **Isolamento de Negócio**:
   - A camada `domain` não tem referências a bibliotecas da Google Play, classes de UI (`Context`, `View`, `Composable`) ou persistência (`Room`, `Retrofit`). É 100% Kotlin puro.
5. **Ergonomia e Acessibilidade**:
   - Suporte mandatório a temas Claro e Escuro (*Material 3 Dark Theme*), contrastes legíveis e tamanhos de toque mínimos de 48dp recomendados pelo Material Design.
