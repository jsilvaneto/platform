# ADR 042: Menu Lateral (ModalNavigationDrawer), Suporte Responsivo e Remoção da Barra Inferior

## Status
Aprovado (Prompt 57)

## Contexto
A barra de navegação inferior (`NavigationBar`), introduzida na v1.20, apresentava limitações na prática:
1. Ocupava espaço vertical valioso em telas pequenas de smartphones.
2. Não comportava o número crescente de módulos do aplicativo sem recorrer a abas genéricas como "Mais".
3. Dificultava a visualização contextual de indicadores gerais (ex: saldo restante do mês, selo de pendências atrasadas e timestamp do último backup seguro).

## Decisão

### 1. Adoção do `ModalNavigationDrawer`
- Substituição completa da `NavigationBar` pelo `ModalNavigationDrawer` do Material 3 com suporte a gesto de arrasto pela borda esquerda (`edge drag`).
- Largura canônica de `310.dp` e cantos arredondados no lado direito com raio de `24.dp` (`PlatformShapes.drawer`).
- Remoção definitiva da tela e rota órfã `MoreScreen`.

### 2. Hierarquia e Agrupamento Semântico
- **Cabeçalho**:
  - Marca: "Platform" (`titleLarge`, negrito).
  - Indicador mensal: "Mês · restam R$ X" derivado do `GetDrawerOverviewUseCase`.
- **Grupos Canônicos**:
  - **Operacional**: Visão geral, Hoje, Contas (com selo vermelho indicando contagem de atrasadas), Cartões.
  - **Planejamento**: Análises (engloba Orçamentos, Metas e Plano de Quitação).
  - **Cadastros**: Contatos, Cadastros.
  - **Sistema**: Configurações.
- **Rodapé**:
  - Indicador de segurança: "Último backup: DD/MM/AAAA HH:mm" (ou "Nenhum backup realizado").

### 3. Estados de Seleção e Estilo Neutro
- Item ativo estilizado com preenchimento neutro (`surface2` com borda e texto destacado), sem uso de tons primários coloridos ou gradientes.

### 4. Preferência de Tela Inicial
- Configuração adicionada em `SettingsScreen`: Seleção entre "Visão geral" (padrão) e "Hoje", persistida no DataStore (`PreferencesManager`).
- Deep links de notificações (`ExpenseNotificationActionReceiver` e lembretes) são preservados e abrem diretamente a rota alvo após desbloqueio biométrico.

### 5. Layout Adaptativo (Wide Screen / Tablets / Desktop)
- Em dispositivos com largura >= 600dp, o menu se converte automaticamente em uma barra lateral fixa (`PermanentNavigationDrawer`), garantindo usabilidade ergonômica em telas grandes.

## Consequências
- Navegação direta, rápida e despoluída.
- Fim da necessidade de telas intermediárias ou de agrupamento artificial ("Mais").
- Alinhamento total com as diretrizes de design minimalista e sóbrio do Platform.
