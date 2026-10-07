package com.platform.app.presentation.contacts

import app.cash.turbine.test
import com.platform.app.domain.model.Contact
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.GetContactDetailsUseCase
import com.platform.app.domain.usecase.LookupAddressByCepUseCase
import com.platform.app.domain.usecase.ToggleInstallmentPaymentUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FinancialRepository
    private lateinit var getContactDetailsUseCase: GetContactDetailsUseCase
    private lateinit var togglePaymentUseCase: ToggleInstallmentPaymentUseCase
    private lateinit var lookupAddressByCepUseCase: LookupAddressByCepUseCase
    private lateinit var viewModel: ContactsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        getContactDetailsUseCase = mockk(relaxed = true)
        togglePaymentUseCase = mockk(relaxed = true)
        lookupAddressByCepUseCase = mockk(relaxed = true)

        every { repository.getContacts() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SaveContact should invoke repository saveContact and emit ShowSnackbar and ContactSaved`() = runTest {
        val contact = Contact(
            id = "c1",
            name = "Energisa Elétrica",
            phone = "0800",
            email = "sac@energisa.com",
            city = "Campina Grande",
            state = "PB"
        )

        viewModel = ContactsViewModel(repository, getContactDetailsUseCase, togglePaymentUseCase, lookupAddressByCepUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(ContactsUiAction.SaveContact(contact))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { repository.saveContact(contact) }

            val effect1 = awaitItem()
            assertTrue(effect1 is ContactsUiEffect.ShowSnackbar)
            assertTrue((effect1 as ContactsUiEffect.ShowSnackbar).message.contains("Energisa"))

            val effect2 = awaitItem()
            assertTrue(effect2 is ContactsUiEffect.ContactSaved)
        }
    }

    @Test
    fun `DeleteContact should invoke repository deleteContact`() = runTest {
        viewModel = ContactsViewModel(repository, getContactDetailsUseCase, togglePaymentUseCase, lookupAddressByCepUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ContactsUiAction.DeleteContact("c1"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteContact("c1") }
    }

    @Test
    fun `SearchQueryChanged should filter contacts by name or city`() = runTest {
        val c1 = Contact(id = "1", name = "João Silva", city = "Recife")
        val c2 = Contact(id = "2", name = "Maria Santos", city = "Fortaleza")
        every { repository.getContacts() } returns flowOf(listOf(c1, c2))

        viewModel = ContactsViewModel(repository, getContactDetailsUseCase, togglePaymentUseCase, lookupAddressByCepUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(ContactsUiAction.SearchQueryChanged("Maria"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
        assertEquals("Maria Santos", viewModel.uiState.value.filteredContacts[0].name)
    }

    @Test
    fun `FilterTypeSelected should filter contacts by contact type and reset when null`() = runTest {
        val c1 = Contact(id = "1", name = "Mãe", type = com.platform.app.domain.model.ContactType.PESSOA_FISICA)
        val c2 = Contact(id = "2", name = "Copel", type = com.platform.app.domain.model.ContactType.FORNECEDOR)
        val c3 = Contact(id = "3", name = "Prefeitura de Curitiba", type = com.platform.app.domain.model.ContactType.ORGAO_PUBLICO)
        every { repository.getContacts() } returns flowOf(listOf(c1, c2, c3))

        viewModel = ContactsViewModel(repository, getContactDetailsUseCase, togglePaymentUseCase, lookupAddressByCepUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Filtro por PESSOA_FISICA
        viewModel.onAction(ContactsUiAction.FilterTypeSelected(com.platform.app.domain.model.ContactType.PESSOA_FISICA))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
        assertEquals("Mãe", viewModel.uiState.value.filteredContacts[0].name)
        assertEquals(com.platform.app.domain.model.ContactType.PESSOA_FISICA, viewModel.uiState.value.selectedTypeFilter)

        // Filtro por FORNECEDOR
        viewModel.onAction(ContactsUiAction.FilterTypeSelected(com.platform.app.domain.model.ContactType.FORNECEDOR))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
        assertEquals("Copel", viewModel.uiState.value.filteredContacts[0].name)

        // Filtro por ORGAO_PUBLICO
        viewModel.onAction(ContactsUiAction.FilterTypeSelected(com.platform.app.domain.model.ContactType.ORGAO_PUBLICO))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
        assertEquals("Prefeitura de Curitiba", viewModel.uiState.value.filteredContacts[0].name)

        // Reset para Todos (null)
        viewModel.onAction(ContactsUiAction.FilterTypeSelected(null))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.filteredContacts.size)
    }

    @Test
    fun `FilterTypeSelected combined with SearchQueryChanged should apply both filters concurrently`() = runTest {
        val c1 = Contact(id = "1", name = "Mãe Maria", type = com.platform.app.domain.model.ContactType.PESSOA_FISICA)
        val c2 = Contact(id = "2", name = "Dona Maria Restaurante", type = com.platform.app.domain.model.ContactType.FORNECEDOR)
        val c3 = Contact(id = "3", name = "Supermercado Central", type = com.platform.app.domain.model.ContactType.FORNECEDOR)
        every { repository.getContacts() } returns flowOf(listOf(c1, c2, c3))

        viewModel = ContactsViewModel(repository, getContactDetailsUseCase, togglePaymentUseCase, lookupAddressByCepUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Busca por 'Maria' com filtro FORNECEDOR -> deve retornar apenas 'Dona Maria Restaurante'
        viewModel.onAction(ContactsUiAction.FilterTypeSelected(com.platform.app.domain.model.ContactType.FORNECEDOR))
        viewModel.onAction(ContactsUiAction.SearchQueryChanged("Maria"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredContacts.size)
        assertEquals("Dona Maria Restaurante", viewModel.uiState.value.filteredContacts[0].name)
    }
}
