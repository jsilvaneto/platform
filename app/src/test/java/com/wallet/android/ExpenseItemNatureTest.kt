package com.wallet.android

import com.platform.app.data.local.entity.ExpenseItemEntity
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.ExpenseNature
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class ExpenseItemNatureTest {

    @Test
    fun `expense item should correctly inherit OBRIGATORIO nature from category`() {
        val categoryMoradia = Category(
            id = "cat-moradia",
            name = "Moradia",
            colorHex = "#EF4444",
            nature = ExpenseNature.OBRIGATORIO
        )

        val entity = ExpenseItemEntity(
            id = UUID.randomUUID().toString(),
            name = "Aluguel & Condomínio",
            categoryId = categoryMoradia.id
        )

        // Ao mapear para o domínio com os dados da Categoria, o Item herda sua natureza
        val domainItem = entity.toDomain(
            categoryName = categoryMoradia.name,
            categoryColorHex = categoryMoradia.colorHex,
            nature = categoryMoradia.nature
        )

        assertEquals("Aluguel & Condomínio", domainItem.name)
        assertEquals(categoryMoradia.id, domainItem.categoryId)
        assertEquals(categoryMoradia.name, domainItem.categoryName)
        assertEquals(ExpenseNature.OBRIGATORIO, domainItem.nature)
    }

    @Test
    fun `expense item should correctly inherit NECESSARIO nature from category`() {
        val categoryAlimentacao = Category(
            id = "cat-alim",
            name = "Alimentação",
            colorHex = "#F59E0B",
            nature = ExpenseNature.NECESSARIO
        )

        val entity = ExpenseItemEntity(
            id = UUID.randomUUID().toString(),
            name = "Supermercado Básico",
            categoryId = categoryAlimentacao.id
        )

        val domainItem = entity.toDomain(
            categoryName = categoryAlimentacao.name,
            categoryColorHex = categoryAlimentacao.colorHex,
            nature = categoryAlimentacao.nature
        )

        assertEquals(ExpenseNature.NECESSARIO, domainItem.nature)
    }

    @Test
    fun `expense item should correctly inherit DESEJA nature from category`() {
        val categoryLazer = Category(
            id = "cat-lazer",
            name = "Lazer & Conforto",
            colorHex = "#3B82F6",
            nature = ExpenseNature.DESEJA
        )

        val entity = ExpenseItemEntity(
            id = UUID.randomUUID().toString(),
            name = "Restaurante e Delivery",
            categoryId = categoryLazer.id
        )

        val domainItem = entity.toDomain(
            categoryName = categoryLazer.name,
            categoryColorHex = categoryLazer.colorHex,
            nature = categoryLazer.nature
        )

        assertEquals(ExpenseNature.DESEJA, domainItem.nature)
    }

    @Test
    fun `expense item should correctly inherit NENHUM nature from category`() {
        val categoryOutros = Category(
            id = "cat-outros",
            name = "Despesas Transitórias",
            colorHex = "#64748B",
            nature = ExpenseNature.NENHUM
        )

        val entity = ExpenseItemEntity(
            id = UUID.randomUUID().toString(),
            name = "Ajuste Contábil",
            categoryId = categoryOutros.id
        )

        val domainItem = entity.toDomain(
            categoryName = categoryOutros.name,
            categoryColorHex = categoryOutros.colorHex,
            nature = categoryOutros.nature
        )

        assertEquals(ExpenseNature.NENHUM, domainItem.nature)
    }

    @Test
    fun `all four ExpenseNature pillars should be defined and valid`() {
        val pillars = ExpenseNature.entries
        assertEquals(4, pillars.size)
        assertEquals(listOf(ExpenseNature.OBRIGATORIO, ExpenseNature.NECESSARIO, ExpenseNature.DESEJA, ExpenseNature.NENHUM), pillars)

        assertEquals("Obrigatório", ExpenseNature.OBRIGATORIO.displayName)
        assertEquals("Necessário", ExpenseNature.NECESSARIO.displayName)
        assertEquals("Deseja", ExpenseNature.DESEJA.displayName)
        assertEquals("Nenhum", ExpenseNature.NENHUM.displayName)
    }
}
