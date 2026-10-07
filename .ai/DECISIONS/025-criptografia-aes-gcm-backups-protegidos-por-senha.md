# ADR 025: Criptografia Simétrica AES-256-GCM para Backups com Proteção por Senha/PIN

## Status
Aprovado

## Contexto
O aplicativo **Platform** é 100% offline-first e disponibiliza exportação de backup local via Storage Access Framework (SAF) e compartilhamento rápido via ShareSheet (`Intent.ACTION_SEND` para WhatsApp, E-mail, Telegram etc.).
Anteriormente, o arquivo exportado era gerado em JSON puro via Gson sem nenhuma camada de proteção criptográfica. Esse comportamento expunha dados financeiros sensíveis (saldos, faturas de cartão, compras parceladas, limites de crédito, categorias e contatos com endereço/telefone) em texto plano, vulneráveis a interceptação ou vazamento em aplicativos terceiros de mensagens ou armazenamento em nuvem compartilhada.

Além disso, chaves simétricas baseadas em hardware no Android KeyStore (`EncryptedFile`) inviabilizariam a restauração em caso de troca ou formatação do smartphone (uma vez que a chave privada do KeyStore fica presa ao dispositivo de origem).

## Decisão

### 1. Padrão Criptográfico Adotado (Skill `security-guard`)
Adotou-se o padrão da indústria para backups protegidos por credenciais do usuário:
- **Cifra Simétrica Autenticada (AEAD)**: `AES/GCM/NoPadding` de 256 bits, garantindo confidencialidade e integridade anti-tampering com tag de autenticação de 128 bits.
- **Derivação de Chave (KDF)**: `PBKDF2WithHmacSHA256` utilizando 65.536 iterações e sal criptográfico (`salt`) aleatório de 16 bytes gerado via `SecureRandom`.
- **Vetor de Inicialização (IV / Nonce)**: 12 bytes gerados aleatoriamente via `SecureRandom` para cada exportação.
- **Higiene de Memória**: Descarte defensivo imediato da senha em memória RAM (`pbeSpec.clearPassword()` e preenchimento com zeros de buffers `CharArray`).

### 2. Envelope de Transporte Versionado (`EncryptedBackupDto`)
O arquivo de exportação permanece com extensão `.json`, mas agora é empacotado em um envelope seguro:
```json
{
  "format": "PLATFORM_ENCRYPTED_BACKUP",
  "version": 1,
  "algorithm": "AES/GCM/NoPadding",
  "kdf": "PBKDF2WithHmacSHA256",
  "iterations": 65536,
  "salt": "<base64>",
  "iv": "<base64>",
  "ciphertext": "<base64>",
  "createdAt": 1728312000000
}
```
Nenhum token ou dado financeiro sensível é exportado em texto plano.

### 3. Contratos de Domínio & Casos de Uso (`domain/`)
- `BackupRepository`:
  - `suspend fun exportBackupJson(password: String): Result<String>`
  - `suspend fun restoreBackupFromJson(encryptedBackupJson: String, password: String): Result<Unit>`
- `ExportBackupUseCase`: Valida o preenchimento da senha antes de delegar para o repositório.
- `RestoreBackupUseCase`: Valida a obrigatoriedade da senha/PIN e integridade do arquivo antes da restauração.
- A camada de domínio permanece 100% pura, sem dependências de frameworks ou APIs nativas do Android.

### 4. Camada de Apresentação & Experiência do Usuário (UI/UX)
- **Fazer Backup & Compartilhar**:
  - Modal `CreateBackupPasswordDialog` solicitando criação e confirmação de senha/PIN (mínimo de 4 dígitos/caracteres), com alerta visual sobre a criptografia AES-256 e necessidade de preservação da senha.
  - Ao confirmar, o arquivo é gerado e protegido antes de ser gravado no destino SAF ou entregue ao FileProvider.
- **Restauração de Dados**:
  - Diálogo prévio de confirmação de sobreposição dos dados locais.
  - Ao selecionar o arquivo no SAF, modal `RestorePasswordDialog` solicitando a senha/PIN para descriptografia.
  - Se a senha estiver incorreta ou o arquivo tiver sido violado, a tag GCM falha imediatamente com retorno de erro seguro ("Senha ou PIN incorreto. Não foi possível descriptografar o backup.") sem realizar nenhuma alteração destrutiva no banco Room.

### 5. Cobertura de Testes Automatizados
- `BackupCryptoHelperTest`: Validação do ciclo completo de criptografia/descriptografia, confidencialidade do ciphertext contra vazamento de termos em claro, integridade contra adulteração de bytes e rejeição estrita de senhas incorretas.
- `BackupRepositoryImplTest`: Validação do ciclo exportação protegida $\rightarrow$ restauração bem-sucedida com senha correta e rejeição com senha incorreta.
- `SettingsViewModelTest`: Validação dos fluxos de emissão de efeito e tratamento de erros com a nova API tipada.
