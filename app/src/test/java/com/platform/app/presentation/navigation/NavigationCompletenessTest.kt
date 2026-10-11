package com.platform.app.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavigationCompletenessTest {

    private val allScreens: List<Screen> by lazy {
        Screen::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }

    @Test
    fun `every defined screen must have a unique non-empty route and title`() {
        assertFalse("Screens list should not be empty", allScreens.isEmpty())

        val routes = allScreens.map { it.route }
        val uniqueRoutes = routes.distinct()
        assertEquals("Duplicate routes found in Screen sealed class", routes.size, uniqueRoutes.size)

        for (screen in allScreens) {
            assertTrue("Screen ${screen.title} has empty route", screen.route.isNotBlank())
            assertTrue("Screen ${screen.route} has empty title", screen.title.isNotBlank())
        }
    }

    @Test
    fun `every defined screen must be registered in NavGraph without orphan routes`() {
        val navGraphFile = File("src/main/java/com/platform/app/presentation/navigation/NavGraph.kt")
        assertTrue("NavGraph.kt must exist", navGraphFile.exists())
        val navGraphContent = navGraphFile.readText()

        // Extract registered routes in NavGraph
        val registeredScreens = allScreens.filter { screen ->
            navGraphContent.contains("route = Screen.${screen::class.simpleName}.route")
        }

        val unregistered = allScreens - registeredScreens.toSet()
        assertTrue(
            "Found orphan Screen(s) defined without destination registered in NavGraph: ${unregistered.map { it::class.simpleName }}",
            unregistered.isEmpty()
        )
    }

    @Test
    fun `navigation graph should not contain obsolete or duplicate screen routes`() {
        val navGraphFile = File("src/main/java/com/platform/app/presentation/navigation/NavGraph.kt")
        val navGraphContent = navGraphFile.readText()

        // Ensure deleted orphan routes are completely absent from NavGraph
        assertFalse(
            "RecurringInstallments route must not exist in NavGraph",
            navGraphContent.contains("Screen.RecurringInstallments")
        )
        assertFalse(
            "Screen.Home route must not exist in NavGraph",
            navGraphContent.contains("Screen.Home")
        )
        assertFalse(
            "Screen.Accounts wrapper route must not exist in NavGraph",
            navGraphContent.contains("Screen.Accounts")
        )
        assertFalse(
            "Screen.PaymentMethods wrapper route must not exist in NavGraph",
            navGraphContent.contains("Screen.PaymentMethods")
        )
        assertFalse(
            "Screen.Categories wrapper route must not exist in NavGraph",
            navGraphContent.contains("Screen.Categories")
        )
    }
}
