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

## 3. Diretrizes de Implementação no Android
1. **Storage Access Framework (SAF)**:
   - Use `ActivityResultContracts.CreateDocument("application/json")` para permitir ao usuário escolher o destino (Google Drive, Download, Pasta Local, Cartão SD).
   - Use `ActivityResultContracts.OpenDocument()` para importação.
2. **Validação Estrita na Restauração**:
   - Valide se o JSON contém todas as chaves obrigatórias.
   - Execute a substituição/restauração dentro de uma transação atômica (`database.withTransaction`).
   - Todos os valores numéricos devem ser lidos como inteiros de centavos (`Long`).
3. **Compartilhamento Rápido via ShareSheet**:
   - Permita compartilhar o arquivo exportado diretamente para e-mail, WhatsApp ou mensageiro via `Intent.ACTION_SEND` e `FileProvider`.
