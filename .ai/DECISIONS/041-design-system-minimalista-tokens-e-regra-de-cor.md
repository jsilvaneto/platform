# ADR 041: Design System Minimalista, Tokens Neutros, Regra Estrita de Cor e Superfícies Planas

## Status
Aprovado (Substitui expressamente o ADR 034)

## Contexto
O processo de alinhamento visual do Platform consolidou a transição para um Design System minimalista de alto contraste, plano e sem artifícios visuais supérfluos (Prompt 55).
Anteriormente, o sistema utilizava gradientes arbitrários, elevações tonais e sombreadas, múltiplos tons de azul, roxo e turquesa para destaque, e dispersão de cores semafóricas fora do contexto estrito de obrigações financeiras.

## Decisão

### 1. Tokens Canônicos e Paleta Neutra (`design/tokens.json` e `PlatformTokens.kt`)
- Três temas nativos com contraste matemático rigoroso (WCAG 2.1 AA, relação > 4.5:1 para textos):
  - **AMOLED**: fundo `#000000`, sup1 `#0B0C10`, sup2 `#14161D`, linha `#23262F`, texto `#F2F4FF`, texto2 `#9AA3C0`.
  - **Escuro**: fundo `#0E1220`, sup1 `#161B2E`, sup2 `#1E2540`, linha `#2A3354`, texto `#E8ECFF`, texto2 `#A3ACCB`.
  - **Claro**: fundo `#F6F7FB`, sup1 `#FFFFFF`, sup2 `#EEF0F8`, linha `#DDE1F0`, texto `#12162B`, texto2 `#5B6385`.
- **Destaque Neutro**: A cor de marca (`primary`) passa a ser idêntica à cor de texto principal (`text`), com o conteúdo em cor de fundo (`background`/`surface1`). Ficam banidos roxo, índigo, turquesa ou gradientes decorativos.

### 2. Regra de Cor e Encapsulamento de Status (`PlatformStatusStyle`)
- As cores semafóricas vivas são reservadas **exclusivamente** para o estado financeiro (pago, atenção/vence hoje, atrasado):
  - Status Escuro/AMOLED: pago `#34D399`, atenção `#FBBF24`, atrasado `#F87171`.
  - Status Claro: pago `#059669`, atenção `#B45309`, atrasado `#DC2626`.
- O acesso a essas cores passa a ser exclusivo via `PlatformStatusStyle`. Proíbe-se expressamente o uso de cores de status em categorias, formas de pagamento, gráficos de natureza ou qualquer outro elemento estrutural.

### 3. Superfícies 100% Planas (Resíduo Zero de Sombras e Gradientes)
- Sem elevação fictícia e sem gradientes (`Brush.*Gradient` e `.shadow()` barrados pelo guard rail Gradle `:app:checkGradientsAndShadows`).
- Hierarquia estabelecida unicamente pela alternância entre `surface1`, `surface2` e borda de 1dp na cor `line`.
- Formas padronizadas na escala: 8.dp (`small`), 12.dp (`medium`), 16.dp (`large`), 28.dp (`extraLarge`) e pílula (`CircleShape`).

### 4. Seletor Quádruplo em Configurações
- Suporte imediato aos 4 modos de exibição: **Automático** (segue o sistema operacional), **Claro**, **Escuro** e **AMOLED**.

## Consequências
- Acessibilidade e sobriedade financeira maximizadas.
- Economia máxima de bateria em telas OLED/AMOLED.
- Código limpo, desacoplado e protegido contra regressões visuais por testes automatizados e guard rails no Gradle.
