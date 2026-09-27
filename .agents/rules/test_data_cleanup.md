---
trigger: always_on
---

# Política de Resíduo Zero em Testes e Configurações Android (test_data_cleanup.md)

Esta diretriz é mandatória para qualquer desenvolvedor ou Agente de IA trabalhando no aplicativo móvel **Platform**.

## 1. Princípio do Resíduo Zero
É terminantemente proibido deixar dados de teste, registros descartáveis em bancos de dados locais (`Room`), arquivos de log de build ou arquivos temporários no sistema de arquivos após testes.

## 2. Isolamento de Banco de Dados nos Testes
1. **Banco em Memória Obrigatório**:
   - Em testes instrumentados ou locais de DAO e Room, utilize SEMPRE `Room.inMemoryDatabaseBuilder(context, PlatformDatabase::class.java).allowMainThreadQueries().build()`.
   - No método `@After`, execute `db.close()`, garantindo que nada persista no armazenamento real do emulador ou aparelho.
2. **Mocking e Dispatchers Isolados**:
   - Para testes de ViewModels e UseCases, mocke os repositórios via MockK e substitua `Dispatchers.Main` por `StandardTestDispatcher` com `Dispatchers.setMain` e `Dispatchers.resetMain` no `@After`.

## 3. Limpeza do Sistema de Arquivos
1. **Arquivos Temporários e Scratches**:
   - Qualquer script de teste, arquivo JSON temporário ou dump deve ser apagado imediatamente após a validação.
   - Não versione arquivos como `debug.log`, `test_payload.json` ou classes de scratch.
2. **Variáveis e Chaves**:
   - Não comite arquivos `local.properties` com chaves de teste ou segredos de API.
