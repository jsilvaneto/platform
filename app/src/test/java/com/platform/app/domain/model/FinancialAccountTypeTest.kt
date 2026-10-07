package com.platform.app.domain.model

import com.platform.app.data.local.converter.FinancialAccountTypeConverter
import com.platform.app.data.local.entity.FinancialAccountEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialAccountTypeTest {

    @Test
    fun `should have expected display names`() {
        assertEquals("Conta Corrente", FinancialAccountType.CORRENTE.displayName)
        assertEquals("Carteira / Dinheiro", FinancialAccountType.CARTEIRA.displayName)
        assertEquals("Poupança", FinancialAccountType.POUPANCA.displayName)
        assertEquals("Investimento", FinancialAccountType.INVESTIMENTO.displayName)
    }

    @Test
    fun `fromString should parse exact enum names`() {
        assertEquals(FinancialAccountType.CORRENTE, FinancialAccountType.fromString("CORRENTE"))
        assertEquals(FinancialAccountType.CARTEIRA, FinancialAccountType.fromString("CARTEIRA"))
        assertEquals(FinancialAccountType.POUPANCA, FinancialAccountType.fromString("POUPANCA"))
        assertEquals(FinancialAccountType.INVESTIMENTO, FinancialAccountType.fromString("INVESTIMENTO"))
    }

    @Test
    fun `fromString should parse legacy aliases gracefully`() {
        assertEquals(FinancialAccountType.CORRENTE, FinancialAccountType.fromString("CHECKING"))
        assertEquals(FinancialAccountType.CORRENTE, FinancialAccountType.fromString("Conta Corrente"))
        assertEquals(FinancialAccountType.CORRENTE, FinancialAccountType.fromString("CREDIT_CARD"))
        assertEquals(FinancialAccountType.CORRENTE, FinancialAccountType.fromString("Outro"))

        assertEquals(FinancialAccountType.CARTEIRA, FinancialAccountType.fromString("CASH"))
        assertEquals(FinancialAccountType.CARTEIRA, FinancialAccountType.fromString("Carteira"))
        assertEquals(FinancialAccountType.CARTEIRA, FinancialAccountType.fromString("Dinheiro"))
        assertEquals(FinancialAccountType.CARTEIRA, FinancialAccountType.fromString("Carteira / Dinheiro"))

        assertEquals(FinancialAccountType.POUPANCA, FinancialAccountType.fromString("SAVINGS"))
        assertEquals(FinancialAccountType.POUPANCA, FinancialAccountType.fromString("Poupança"))
        assertEquals(FinancialAccountType.POUPANCA, FinancialAccountType.fromString("poupanca"))

        assertEquals(FinancialAccountType.INVESTIMENTO, FinancialAccountType.fromString("INVESTMENT"))
        assertEquals(FinancialAccountType.INVESTIMENTO, FinancialAccountType.fromString("Investimento"))
        assertEquals(FinancialAccountType.INVESTIMENTO, FinancialAccountType.fromString("Investimento / Reserva"))
        assertEquals(FinancialAccountType.INVESTIMENTO, FinancialAccountType.fromString("Reserva de Emergência"))
    }

    @Test
    fun `fromString should fallback to CORRENTE for null blank or unknown values`() {
        assertEquals(FinancialAccountType.CORRENTE, FinancialAccountType.fromString(null))
        assertEquals(FinancialAccountType.CORRENTE, FinancialAccountType.fromString(""))
        assertEquals(FinancialAccountType.CORRENTE, FinancialAccountType.fromString("   "))
        assertEquals(FinancialAccountType.CORRENTE, FinancialAccountType.fromString("VALOR_DESCONHECIDO"))
    }

    @Test
    fun `FinancialAccountEntity toDomain and fromDomain should preserve FinancialAccountType`() {
        val domain = FinancialAccount(
            id = "acc-test",
            name = "Inter Invest",
            accountType = FinancialAccountType.INVESTIMENTO,
            colorHex = "#FF5500"
        )

        val entity = FinancialAccountEntity.fromDomain(domain)
        assertEquals("acc-test", entity.id)
        assertEquals("Inter Invest", entity.name)
        assertEquals(FinancialAccountType.INVESTIMENTO, entity.accountType)
        assertEquals("#FF5500", entity.colorHex)

        val mappedBack = entity.toDomain()
        assertEquals(domain, mappedBack)
    }

    @Test
    fun `FinancialAccountTypeConverter should serialize and deserialize correctly`() {
        val converter = FinancialAccountTypeConverter()

        assertEquals("CORRENTE", converter.fromType(FinancialAccountType.CORRENTE))
        assertEquals("CARTEIRA", converter.fromType(FinancialAccountType.CARTEIRA))
        assertEquals("POUPANCA", converter.fromType(FinancialAccountType.POUPANCA))
        assertEquals("INVESTIMENTO", converter.fromType(FinancialAccountType.INVESTIMENTO))
        assertEquals("CORRENTE", converter.fromType(null))

        assertEquals(FinancialAccountType.POUPANCA, converter.toType("POUPANCA"))
        assertEquals(FinancialAccountType.POUPANCA, converter.toType("Poupança"))
        assertEquals(FinancialAccountType.CORRENTE, converter.toType(null))
        assertEquals(FinancialAccountType.CORRENTE, converter.toType("Desconhecido"))
    }
}
