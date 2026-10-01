package com.platform.app.presentation.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.model.PayableUrgency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PlatformIconCatalogTest {

    @Test
    fun `getIcon should resolve primary catalog keys accurately`() {
        assertEquals(Icons.Default.Home, PlatformIconCatalog.getIcon("home"))
        assertEquals(Icons.Default.ShoppingCart, PlatformIconCatalog.getIcon("shopping_cart"))
        assertEquals(Icons.Default.Restaurant, PlatformIconCatalog.getIcon("restaurant"))
        assertEquals(Icons.Default.CreditCard, PlatformIconCatalog.getIcon("credit_card"))
        assertEquals(Icons.Default.Payments, PlatformIconCatalog.getIcon("payments"))
        assertEquals(Icons.Default.AccountBalance, PlatformIconCatalog.getIcon("account_balance"))
        assertEquals(Icons.AutoMirrored.Filled.ReceiptLong, PlatformIconCatalog.getIcon("receipt"))
        assertEquals(Icons.Default.QrCode, PlatformIconCatalog.getIcon("qr_code"))
        assertEquals(Icons.Default.Folder, PlatformIconCatalog.getIcon("folder"))
        assertEquals(Icons.Default.MoreHoriz, PlatformIconCatalog.getIcon("more_horiz"))
    }

    @Test
    fun `getIcon should be case-insensitive and handle whitespace`() {
        assertEquals(Icons.Default.Home, PlatformIconCatalog.getIcon("  HOME  "))
        assertEquals(Icons.Default.QrCode, PlatformIconCatalog.getIcon("QR_CODE"))
        assertEquals(Icons.Default.Folder, PlatformIconCatalog.getIcon("FoLdEr"))
    }

    @Test
    fun `getIcon should resolve aliases properly`() {
        assertEquals(Icons.Default.QrCode, PlatformIconCatalog.getIcon("pix"))
        assertEquals(Icons.Default.CreditCard, PlatformIconCatalog.getIcon("cartao"))
        assertEquals(Icons.Default.Payments, PlatformIconCatalog.getIcon("dinheiro"))
        assertEquals(Icons.AutoMirrored.Filled.ReceiptLong, PlatformIconCatalog.getIcon("boleto"))
        assertEquals(Icons.Default.AccountBalance, PlatformIconCatalog.getIcon("banco"))
    }

    @Test
    fun `getIcon should fallback to Category icon on unknown or blank key`() {
        assertEquals(Icons.Default.Category, PlatformIconCatalog.getIcon("non_existent_key_xyz"))
        assertEquals(Icons.Default.Category, PlatformIconCatalog.getIcon(""))
        assertEquals(Icons.Default.Category, PlatformIconCatalog.getIcon("   "))
    }

    @Test
    fun `catalog should have all necessary icons available for picker`() {
        val keys = PlatformIconCatalog.ICONS.map { it.key }
        assertNotNull(keys.find { it == "folder" })
        assertNotNull(keys.find { it == "more_horiz" })
        assertNotNull(keys.find { it == "qr_code" })
        assertNotNull(keys.find { it == "shopping_cart" })
        assertNotNull(keys.find { it == "restaurant" })
    }

    @Test
    fun `PayableItem BillPayable and InvoicePayable should expose categoryIconName`() {
        val installment = BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Conta de Luz",
            categoryName = "Moradia",
            categoryColorHex = "#3B82F6",
            categoryIconName = "lightbulb",
            amountCents = 15000L,
            dueDate = 1000L
        )

        val billPayable = PayableItem.BillPayable(
            installment = installment,
            urgency = PayableUrgency.DUE_TODAY
        )

        assertEquals("lightbulb", billPayable.categoryIconName)
        assertEquals(Icons.Default.Lightbulb, PlatformIconCatalog.getIcon(billPayable.categoryIconName))

        val invoice = CreditCardInvoice(
            id = "inv-1",
            creditCardId = "card-1",
            referenceMonth = "10/2026",
            closingDate = 1000L,
            dueDate = 2000L,
            totalAmountCents = 250000L,
            status = InvoiceStatus.ABERTA
        )

        val invoicePayable = PayableItem.InvoicePayable(
            invoice = invoice,
            cardName = "Nubank",
            cardColorHex = "#820AD1",
            urgency = PayableUrgency.NEXT_7_DAYS
        )

        assertEquals("credit_card", invoicePayable.categoryIconName)
        assertEquals(Icons.Default.CreditCard, PlatformIconCatalog.getIcon(invoicePayable.categoryIconName))
    }
}
