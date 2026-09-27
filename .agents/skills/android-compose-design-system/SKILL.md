---
name: android-compose-design-system
description: >-
  Use this skill when creating or updating Jetpack Compose UI components, screens,
  or previews to enforce Material 3 design system, dark/light theme consistency, and accessibility.
---

# Skill: Android Compose Design System (Material 3)

Esta skill estabelece os padrões visuais, paleta de cores semântica e boas práticas de Jetpack Compose para o app **Platform**.

## 1. Cores Semânticas Obrigatórias

Sempre utilize as cores através do `MaterialTheme.colorScheme`:

| Elemento | Propriedade MaterialTheme | Finalidade |
| :--- | :--- | :--- |
| **Fundo de Tela** | `MaterialTheme.colorScheme.background` | Fundo principal da aplicação |
| **Superfície / Cards** | `MaterialTheme.colorScheme.surface` | Containers elevados, cards e dialogs |
| **Acentos Primários** | `MaterialTheme.colorScheme.primary` | Ações principais, botões de destaque e FAB |
| **Texto Primário** | `MaterialTheme.colorScheme.onSurface` | Títulos e textos de alta ênfase |
| **Texto Secundário** | `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)` | Subtítulos e informações de apoio |
| **Erro / Alerta** | `MaterialTheme.colorScheme.error` | Mensagens de validação e botões de exclusão |

## 2. Padrões de Componentes Reutilizáveis

### 2.1 Botões
```kotlin
Button(
    onClick = { /* ação */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    shape = RoundedCornerShape(8.dp)
) {
    Text("Ação Primária", style = MaterialTheme.typography.labelLarge)
}
```

### 2.2 Previews com Suporte a Tema Claro e Escuro
Sempre forneça anotações de preview duplas:
```kotlin
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ComponentPreview() {
    PlatformTheme {
        Surface {
            // Seu componente aqui
        }
    }
}
```

## 3. Ergonomia e Acessibilidade
- Garanta alvos de toque mínimos de `48.dp` para botões e ícones clicáveis.
- Forneça `contentDescription` acessível em todos os `Icon` e `Image`.
