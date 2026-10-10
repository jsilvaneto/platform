# ADR 036: Bloqueio Biométrico em Memória via AppLockState e Proteção Anti-Snooping

- **Status**: Aceito
- **Data**: 2026-10-10
- **Contexto**: Prompt 38 (Segurança Biométrica, Timeout e FLAG_SECURE)

## Contexto e Problema

O mecanismo anterior de bloqueio biométrico apresentava fragilidades críticas de segurança:
1. O estado de desbloqueio (`isUnlocked`) era gerenciado na UI via `rememberSaveable`. Quando a Activity sofria recriação (por mudança de tema, orientação ou reinicialização do sistema operacional), o estado de desbloqueio era restaurado como `true`, bypassando a autenticação biométrica.
2. O aplicativo não trancava ao ir para segundo plano; uma vez autenticado, permanecia permanentemente acessível durante todo o ciclo de vida do processo.
3. Na inicialização a frio, o estado de `isBiometricEnabled` iniciava como `false` de forma assíncrona enquanto o `DataStore` era lido. Durante alguns milissegundos ("flash de conteúdo"), os dados financeiros do usuário podiam ser visualizados antes do overlay de bloqueio entrar em cena.
4. Na listagem de apps recentes do sistema operacional ("App Switcher"), telas e saldos ficavam expostos em capturas de tela desprotegidas.

## Decisões Tomadas

1. **Gestor de Bloqueio em Memória `@Singleton` (`AppLockState`)**:
   - Criamos `AppLockState` desacoplado do Compose e injetado via Hilt.
   - O estado de desbloqueio é um `MutableStateFlow<Boolean>` volátil em memória RAM, garantindo que nunca seja salvo no `SavedStateHandle` ou `Bundle`.
2. **Tempo Limite de Bloqueio Configurável (`lockTimeoutSeconds`)**:
   - `AppLockState` implementa `DefaultLifecycleObserver` e escuta `ProcessLifecycleOwner.get().lifecycle`.
   - Ao detectar `onStop`, armazena `lastBackgroundAt`. Ao retornar em `onStart`, compara o tempo decorrido com o timeout selecionado pelo usuário (Imediato, 30 segundos, 1 minuto ou 5 minutos) e bloqueia o aplicativo se o tempo expirou.
3. **Prevenção do Flash de Conteúdo**:
   - `isBiometricEnabled` em `MainActivity` inicia como `null` até que a primeira leitura do `DataStore` seja concluída.
   - Enquanto for `null`, o app renderiza uma tela neutra (apenas o fundo do tema), impedindo qualquer renderização prematura do grafo de navegação ou dados sensíveis.
4. **Proteção Anti-Snooping com `FLAG_SECURE`**:
   - Se a biometria estiver ativa e o usuário habilitar a opção "Ocultar conteúdo em apps recentes", a Activity aplica dinamicamente `WindowManager.LayoutParams.FLAG_SECURE`.
   - Isso bloqueia capturas de tela e previne a geração de miniaturas legíveis no seletor de tarefas recentes do Android.

## Consequências

- **Positivas**:
  - Segurança de nível bancário contra acessos indevidos e "shoulder surfing".
  - Recriação de Activity ou transições de segundo plano não quebram o isolamento criptográfico e de acesso.
  - Zero exposição de dados na inicialização ou no histórico do sistema.
- **Negativas / Mitigações**:
  - Capturas de tela legítimas solicitadas pelo próprio usuário para suporte ficam desabilitadas enquanto `FLAG_SECURE` estiver ativo (comportamento desejado e documentado nas Configurações).
