# Governança de Releases, Versionamento e Manutenção de Skills (governance_and_versioning.md)

Esta diretriz é mandatória para qualquer desenvolvedor ou Agente de IA trabalhando no aplicativo móvel **Platform**.

## 1. Princípio do Versionamento Ativo e Contínuo
1. **Proibição de Estagnação de Versão**:
   - É terminantemente proibido realizar alterações visuais significativas, novas features, novos casos de uso ou refatorações estruturais mantendo a mesma versão no arquivo `VERSION`.
2. **Execução Mandatória de Bump de Versão**:
   - Para novas funcionalidades, melhorias de telas ou módulos: execute `powershell -ExecutionPolicy Bypass -File .\scripts\bump-version.ps1 minor` (ex: 1.2.0 -> 1.3.0).
   - Para correções de bugs pontuais: execute `powershell -ExecutionPolicy Bypass -File .\scripts\bump-version.ps1 patch` (ex: 1.3.0 -> 1.3.1).
3. **Registro Obrigatório no CHANGELOG.md**:
   - Toda versão gerada DEVE ter sua seção preenchida no `CHANGELOG.md` detalhando o que foi entregue no formato [Keep a Changelog].
4. **Sincronização do ReleaseNotesDialog**:
   - Em `SettingsScreen.kt`, atualize o `ReleaseNotesDialog` para que o botão "Novidades" exiba exatamente as melhorias da versão atual do app.

## 2. Manutenção Viva de Skills e Padrões
1. **Skills Não São Estáticas**:
   - Sempre que um novo padrão arquitetural (ex: transações atômicas de backup) ou padrão de UI (ex: ModalBottomSheet com menu 3 pontos e confirmação de exclusão) for definido ou aprovado pelo usuário, a respectiva skill em `.agents/skills/<skill-name>/SKILL.md` DEVE ser atualizada imediatamente.
2. **Consulta Prévia Obrigatória**:
   - Antes de iniciar qualquer tela ou refatoração, o agente DEVE consultar as skills ativas (`ui-elegance-and-proportions`, `financial-domain-guard`, `offline-backup-and-export`, etc.) para aplicar os padrões vigentes sem desvios.

## 3. Manutenção da Documentação Central (`.ai/` e `README.md`)
1. **Checklist e Fases**:
   - Ao concluir uma fase, atualize `.ai/STATUS.md` imediatamente para "100% CONCLUÍDO".
2. **Decisões Arquiteturais (ADRs)**:
   - Registre novas decisões estruturais em `.ai/DECISIONS/` (formato: `NNN-titulo.md`).
3. **README.md Vivo**:
   - Mantenha a descrição de recursos, módulos e comandos do `README.md` sincronizados com o estado real do projeto.
