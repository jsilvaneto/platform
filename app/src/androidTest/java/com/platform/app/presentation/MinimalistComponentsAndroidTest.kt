package com.platform.app.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.platform.app.presentation.components.PlatformFab
import com.platform.app.presentation.components.PlatformListRow
import com.platform.app.presentation.components.PlatformMoneyText
import com.platform.app.presentation.components.PlatformNeutralChip
import com.platform.app.presentation.components.PlatformPrimaryButton
import com.platform.app.presentation.components.PlatformSectionTitle
import com.platform.app.presentation.components.PlatformStatusChip
import com.platform.app.presentation.components.StatusChipType
import com.platform.app.presentation.theme.PlatformStatusType
import com.platform.app.presentation.theme.PlatformTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MinimalistComponentsAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun minimalistComponents_renderProperlyInTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = true, isAmoled = true) {
                Column {
                    PlatformSectionTitle(text = "Seção AMOLED")
                    PlatformMoneyText(amountCents = 15990L)
                    PlatformStatusChip(text = "Pago", type = StatusChipType.SUCCESS)
                    PlatformNeutralChip(text = "Essencial")
                    PlatformListRow(
                        title = "Aluguel AMOLED",
                        subtitle = "Moradia",
                        amountCents = 150000L,
                        icon = Icons.Default.Receipt,
                        statusText = "Vence Hoje",
                        statusType = PlatformStatusType.ATTENTION
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("SEÇÃO AMOLED").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aluguel AMOLED").assertIsDisplayed()
        composeTestRule.onNodeWithText("Moradia").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vence Hoje").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pago").assertIsDisplayed()
        composeTestRule.onNodeWithText("Essencial").assertIsDisplayed()
    }

    @Test
    fun primaryButtonAndFab_handleClick() {
        var buttonClicked = false
        var fabClicked = false

        composeTestRule.setContent {
            PlatformTheme {
                Column {
                    PlatformPrimaryButton(
                        text = "Confirmar Pagamento",
                        onClick = { buttonClicked = true }
                    )
                    PlatformFab(
                        onClick = { fabClicked = true },
                        icon = Icons.Default.Add,
                        contentDescription = "Adicionar Novo"
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Confirmar Pagamento").performClick()
        assertTrue("Botão primário deve registrar o clique", buttonClicked)

        composeTestRule.onNodeWithContentDescription("Adicionar Novo").performClick()
        assertTrue("FAB deve registrar o clique", fabClicked)
    }

    @Test
    fun components_supportHighFontScale() {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            val highFontScaleDensity = Density(
                density = currentDensity.density,
                fontScale = 1.3f
            )

            CompositionLocalProvider(LocalDensity provides highFontScaleDensity) {
                PlatformTheme {
                    PlatformListRow(
                        title = "Internet Fibra",
                        subtitle = "Vencimento próximo",
                        amountCents = 9990L,
                        icon = Icons.Default.Receipt,
                        statusText = "Pendente",
                        statusType = PlatformStatusType.ATTENTION
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Internet Fibra").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vencimento próximo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pendente").assertIsDisplayed()
    }
}
