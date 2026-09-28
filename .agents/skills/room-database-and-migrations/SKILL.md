---
name: room-database-and-migrations
description: >-
  Use this skill for Room database operations, writing high-performance SQL queries,
  compound transactions (@Transaction), database schema migrations, and relational integrity.
---

# Skill: Room Database & Migrations (Persistência Offline de Alta Performance)

Esta skill estabelece as regras mandatórias para manipulação do banco de dados local **Room (SQLite)** no aplicativo **Platform**.

---

## 1. Princípio da Performance em Queries (Proibição de `getAll` na Memória)
- **Nunca carregue coleções completas para filtrar em memória**:
  É estritamente proibido carregar toda a tabela (ex: `getAllInstallments()`) para executar `.filter { }` e `.sumOf { }` em Kotlin.
- **Delegação ao SQLite**:
  Cálculos de métricas, totais mensais e contagens devem ser resolvidos via queries SQL diretamente no Room DAO:
  ```kotlin
  @Query("SELECT SUM(amountCents) FROM bill_installments WHERE dueDate BETWEEN :start AND :end AND status = 'PAID'")
  fun getPaidSumForPeriod(start: Long, end: Long): Flow<Long?>
  ```

---

## 2. Transações Atômicas Obrigatórias (`@Transaction` ou `withTransaction`)
Toda operação que envolva mais de uma escrita em tabelas relacionadas DEVE ser atômica:
```kotlin
// Em Repositórios injetando PlatformDatabase:
database.withTransaction {
    billDao.insert(BillEntity.fromDomain(bill))
    installmentDao.insertAll(installments.map { BillInstallmentEntity.fromDomain(it) })
}
```
Isso impede que uma falha intermediária gere contas sem parcelas ou estados inconsistentes.

---

## 3. Integridade Referencial e Chaves Estrangeiras
- Entidades filhas (`BillInstallmentEntity`, `SubcategoryEntity`) DEVEM declarar explicitamente `foreignKeys` com `onDelete = ForeignKey.CASCADE` ou `ForeignKey.RESTRICT`.
- Crie índices (`indices = [Index("coluna")]`) em todas as colunas que façam parte de cláusulas `WHERE`, `JOIN` ou `ORDER BY` frequentes (`dueDate`, `categoryId`, `contactId`).

---

## 4. Evolução de Schema e Migrações
- Ao alterar qualquer entidade Room:
  1. Incremente a `version` no `@Database`.
  2. Implemente a migração correspondente (`Migration(from, to)`).
  3. Evite `fallbackToDestructiveMigration()` em ambientes de produção para não apagar os dados do usuário.
