# ADR 035: Política de Backup E2E do Android e Checkpoint WAL do SQLite

- **Status**: Aceito
- **Data**: 2026-10-10
- **Contexto**: Prompts 39 e 40 (Endurecimento do Release e Política de Backup)

## Contexto e Problema

O aplicativo Platform adota como princípio basilar ser **100% Offline-First**, garantindo total soberania de dados para o usuário. No entanto, as configurações de manifesto e backup do Android apresentavam duas vulnerabilidades potenciais:
1. `data_extraction_rules.xml` incluía `domain="root" path="."` indiscriminadamente no `cloud-backup`, permitindo que o banco de dados Room (sem criptografia nativa no disco além da do dispositivo) fosse enviado aos servidores do Google sem garantia explícita de criptografia ponta a ponta (E2E) pelo usuário.
2. O SQLite no Android opera por padrão no modo WAL (Write-Ahead Logging), mantendo transações recentes nos arquivos auxiliares `-wal` e `-shm`. A execução de cópias de segurança do sistema ou exportações manuais de backup sem a fusão prévia do log WAL pode originar snapshots inconsistentes ou arquivos corrompidos.

## Decisões Tomadas

1. **Exigência de Criptografia de Ponta a Ponta para Cloud Backup**:
   - No arquivo `data_extraction_rules.xml` (Android 12+), configuramos `<cloud-backup disableIfNoEncryptionCapabilities="true">`.
   - Isso bloqueia o envio dos arquivos do app para o Google Drive em dispositivos que não possuam bloqueio de tela ativo ou quando o serviço de nuvem não suportar criptografia ponta a ponta vinculada à chave do usuário.
2. **Escopo Estrito de Inclusão e Exclusão**:
   - Limitamos as inclusões em `data_extraction_rules.xml` e `backup_rules.xml` aos domínios `database` e `sharedpref`.
   - Excluímos explicitamente `platform_db-wal`, `platform_db-shm` e `device_keystore.xml`, evitando arquivos transitórios de log e credenciais locais do Keystore.
3. **Truncamento Periódico de WAL (`wal_checkpoint(TRUNCATE)`)**:
   - Adicionamos o método `checkpointWal()` na classe abstrata `PlatformDatabase`.
   - Conectamos um observador `DefaultLifecycleObserver` via `ProcessLifecycleOwner` em `PlatformApplication` para executar `wal_checkpoint(TRUNCATE)` sempre que a aplicação entrar em segundo plano (`onStop`).
   - Invocamos `database.checkpointWal()` antes da leitura e geração do payload em `BackupRepositoryImpl.exportBackupJson()`.

## Consequências

- **Positivas**:
  - Alinhamento total entre a promessa de privacidade ("100% Offline") e o comportamento real do Android.
  - Banco de dados em disco sempre consolidado e íntegro no arquivo principal `platform_db`, prevenindo perda de dados em transferências de dispositivo.
  - Zero risco de vazamento de dados em nuvens desprotegidas.
- **Negativas / Mitigações**:
  - Usuários que não tenham configurado senha/PIN no aparelho não terão backup em nuvem do Google; a cópia manual cifrada com AES-GCM continua disponível nas Configurações.
