---
name: token-optimizer
description: >-
  Use this skill to optimize context consumption and reduce token usage in Android projects
  through surgical diff edits, filtering build artifacts, and concise communication.
---

# Skill: Token Optimizer (Projetos Android)

## Diretrizes de Resposta e Consumo de Contexto
Para otimizar o uso da janela de contexto e economizar tokens no desenvolvimento Android:

1. **Edição Cirúrgica (Diffs em vez de arquivos inteiros):**
   - Nunca reescreva um arquivo Composable ou ViewModel inteiro para alterar poucas linhas.
   - Forneça apenas o bloco relevante alterado com contexto mínimo suficiente para localizar a inserção.

2. **Exclusão de Ruído de Contexto:**
   - Não analise nem processe diretórios gerados como `build/`, `.gradle/`, `.cxx/`, `.idea/` ou dumps de log de build.
   - Se precisar inspecionar dependências, consulte unicamente o catálogo `gradle/libs.versions.toml` ou o `app/build.gradle.kts`.

3. **Comunicação Concisa:**
   - Elimine explicações teóricas repetitivas sobre Android.
   - Apresente o código, a justificativa direta em 1 ou 2 tópicos objetivos e o caminho do arquivo alterado.
