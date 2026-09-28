---
name: offline-backup-and-export
description: >-
  Use this skill when designing, implementing, or testing local backup, JSON export/import,
  and data restoration features in the 100% offline-first Platform application.
---

# Skill: Offline Backup & Data Export (Segurança de Dados Locais)

Esta skill orienta a implementação e manutenção de mecanismos de **exportação, backup e restauração** no aplicativo móvel **Platform**.

---

## 1. Princípio de Sobrevivência de Dados Offline
Como o **Platform** opera 100% offline sem sincronização em nuvem na versão atual:
- O usuário é o guardião único de suas informações financeiras.
- O aplicativo DEVE disponibilizar funcionalidades nativas para exportar um snapshot completo de seu banco de dados e restaurá-lo em caso de troca ou formatação do smartphone.

---

## 2. Formato de Exportação (JSON Estruturado)
O arquivo de backup gerado deve ser um JSON compacto e versionado contendo:
```json
{
  "version": 1,
  "exportedAt": 1727480000000,
  "categories": [...],
  "subcategories": [...],
  "financialAccounts": [...],
  "paymentMethods": [...],
  "contacts": [...],
  "bills": [...],
  "installments": [...],
  "budgets": [...],
  "goals": [...]
}
```

---

## 3. Diretrizes e Arquitetura Implementada no Android
1. **Contrato de Domínio e Casos de Uso (`domain/`)**:
   - `BackupRepository`: Interface pura para exportação (`exportBackupJson(): Result<String>`) e restauração (`restoreBackupFromJson(json): Result<Unit>`).
   - `ExportBackupUseCase` e `RestoreBackupUseCase`: Invocados pelo ViewModel para orquestrar dados.
2. **Camada de Dados e Transação Atômica (`data/`)**:
   - `BackupDataDto`: Modelo de transferência versionado que mapeia todas as 9 entidades do Room.
   - `BackupRepositoryImpl`:
     - Restauração atômica obrigatória via `database.withTransaction`.
     - Ordem de exclusão: filhos primeiro (`bill_installments`, `bills`, `budgets`, `goals`, `contacts`, `financial_accounts`, `payment_methods`, `subcategories`, `categories`).
     - Ordem de inserção: pais primeiro e filhos depois.
     - Rollback total se houver erro ou corrupção no JSON.
3. **Storage Access Framework (SAF) na UI (`presentation/settings/`)**:
   - `ActivityResultContracts.CreateDocument("application/json")` permite ao usuário escolher o destino no Google Drive, Downloads, SD Card ou rede.
   - `ActivityResultContracts.OpenDocument()` permite restaurar selecionando o arquivo.
   - Diálogo prévio de confirmação obrigatório antes de disparar a substituição dos dados.
4. **Compartilhamento Rápido via ShareSheet**:
   - `androidx.core.content.FileProvider` configurado via `file_paths.xml` para compartilhamento instantâneo via `Intent.ACTION_SEND` para WhatsApp, Telegram ou e-mail.
5. **Base Contratual para Futura Sincronização em Nuvem (Fase 2)**:
   - A estrutura do DTO de backup é o mesmo contrato que a API remota utilizará para sincronização bidirecional offline-first.
