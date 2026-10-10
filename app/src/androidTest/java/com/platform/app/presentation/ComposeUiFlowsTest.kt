package com.platform.app.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.presentation.bills.components.BillInstallmentItemCard
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.security.BiometricLockOverlay
import com.platform.app.presentation.settings.components.CreateBackupPasswordDialog
import com.platform.app.presentation.settings.components.RestorePasswordDialog
import com.platform.app.presentation.theme.PlatformTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeUiFlowsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun biometricLockOverlay_displaysElements_andTriggersUnlockRequest() {
        var unlockRequested = false

        composeTestRule.setContent {
            PlatformTheme {
                BiometricLockOverlay(
                    onUnlockRequest = { unlockRequested = true },
                    errorMessage = "Impressão digital não reconhecida"
                )
            }
        }

        // Verifica que o título está visível
        composeTestRule.onNodeWithText("Platform Bloqueado").assertIsDisplayed()

        // Verifica que a mensagem de erro está visível
        composeTestRule.onNodeWithText("Impressão digital não reconhecida").assertIsDisplayed()

        // Clica no botão de desbloqueio
        composeTestRule.onNodeWithText("Desbloquear com Biometria").performClick()

        assertTrue("onUnlockRequest deve ser chamado ao tocar no botão de desbloqueio", unlockRequested)
    }

    @Test
    fun billInstallmentItemCard_rendersCorrectly_andHandlesPaymentToggle() {
        var paymentToggled = false
        var installmentSelected = false

        val installment = BillInstallment(
            id = "inst_100",
            billId = "bill_1",
            billTitle = "Conta de Luz",
            amountCents = 15000L,
            dueDate = System.currentTimeMillis() + 86400000L,
            status = BillStatus.PENDING,
            type = BillType.SINGLE,
            nature = ExpenseNature.MANDATORIO
        )

        composeTestRule.setContent {
            PlatformTheme {
                BillInstallmentItemCard(
                    installment = installment,
                    onTogglePayment = { paymentToggled = true },
                    onSelectInstallment = { installmentSelected = true }
                )
            }
        }

        // Verifica que o título da conta está visível
        composeTestRule.onNodeWithText("Conta de Luz").assertIsDisplayed()

        // Clica no card para selecionar
        composeTestRule.onNodeWithText("Conta de Luz").performClick()
        assertTrue("onSelectInstallment deve ser disparado ao tocar no card", installmentSelected)
    }

    @Test
    fun createBackupPasswordDialog_rendersFields_andDismissesCorrectly() {
        var dismissed = false

        composeTestRule.setContent {
            PlatformTheme {
                CreateBackupPasswordDialog(
                    isSharing = false,
                    onDismiss = { dismissed = true },
                    onConfirm = { _ -> }
                )
            }
        }

        // Verifica que o título do diálogo é exibido
        composeTestRule.onNodeWithText(AppStrings.Dialogs.ENCRYPT_BACKUP_TITLE).assertIsDisplayed()

        // Clica no botão Cancelar
        composeTestRule.onNodeWithText(AppStrings.Actions.CANCEL).performClick()
        assertTrue("onDismiss deve ser chamado ao cancelar", dismissed)
    }

    @Test
    fun restorePasswordDialog_rendersFields_andDismissesCorrectly() {
        var dismissed = false

        composeTestRule.setContent {
            PlatformTheme {
                RestorePasswordDialog(
                    onDismiss = { dismissed = true },
                    onConfirm = { _ -> }
                )
            }
        }

        // Verifica que o título do diálogo de restauração é exibido
        composeTestRule.onNodeWithText(AppStrings.Dialogs.DECRYPT_BACKUP_TITLE).assertIsDisplayed()

        // Clica no botão Cancelar
        composeTestRule.onNodeWithText(AppStrings.Actions.CANCEL).performClick()
        assertTrue("onDismiss deve ser chamado ao cancelar restauração", dismissed)
    }
}
