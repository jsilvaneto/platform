# ADR 006: Backup e Restauração de Dados Offline via SAF e JSON Versionado

## Data
2026-09-28

## Status
Aprovado e Implementado (v1.3.0)

## Contexto
Como o aplicativo opera 100% offline sem banco em nuvem nesta fase, o usuário é o único responsável pelos seus dados. Faz-se indispensável um mecanismo confiável para exportar e restaurar a base em trocas de aparelho ou formatação, sem risco de corrupção ou travamento de banco SQLite com WAL.

## Decisão
1. **Formato JSON Versionado (`BackupDataDto`)**:
   - O snapshot contém `version: 1`, `exportedAt: Long` e todas as 9 tabelas do sistema.
   - Valores monetários são preservados estritamente como inteiros de centavos (`Long`).
2. **Atomicidade via Transação Room (`database.withTransaction`)**:
   - A restauração roda integralmente dentro de uma transação.
   - Limpeza em ordem de integridade referencial: filhos primeiro (`bill_installments`, `bills`, etc.) e pais depois (`categories`, `financial_accounts`).
   - Inserção em ordem inversa: pais primeiro e filhos depois.
   - Se qualquer etapa falhar, ocorre rollback total.
3. **Navegação com Storage Access Framework (SAF)**:
   - Uso de `ActivityResultContracts.CreateDocument("application/json")` e `OpenDocument()` para permitir salvar e carregar de qualquer provedor de arquivos (Google Drive, Downloads, SD Card).
4. **Compartilhamento Rápido com FileProvider**:
   - Configuração de `androidx.core.content.FileProvider` para permitir envio do arquivo JSON diretamente para WhatsApp ou e-mail.
5. **Base Contratual para Futura API (Fase 2)**:
   - O schema do DTO estabelece os contratos que serão consumidos pela futura API de sincronização bidirecional.

## Consequências
- Segurança total de dados para o usuário sem custos de infraestrutura ou dependência de login.
- Zero acoplamento com arquivos binários `.db` ou bloqueios de WAL do SQLite.
