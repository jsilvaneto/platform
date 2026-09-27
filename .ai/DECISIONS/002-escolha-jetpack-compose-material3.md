# ADR 002: Adoção do Jetpack Compose e Material 3 com Dynamic Color

## Status
Aprovado

## Contexto
O desenvolvimento tradicional de interfaces Android com XML e Views imperativas gera complexidade de sincronização de estado, fragmentação de código e dificuldade para manipulação programática por agentes de IA.

## Decisão
1. **Toolkit de UI**: Jetpack Compose em substituição completa aos layouts XML legados.
2. **Design System**: Material Design 3 (Material You), com suporte nativo a cores dinâmicas do Android 12+ (`dynamicDarkColorScheme` e `dynamicLightColorScheme`).
3. **Navegação**: Navigation Compose com rotas tipadas em sealed class.
4. **Governança de Temas**: Suporte obrigatório a Dark Theme sem uso de cores hardcoded nos componentes.

## Consequências
- Código de UI declarativo, legível e reativo.
- Reutilização simplificada de componentes visuais (`PlatformAppBar`, `Card`, `BottomSheet`).
- Eliminação de crashes por `NullPointerException` associados a `findViewById` ou ViewBinding.
