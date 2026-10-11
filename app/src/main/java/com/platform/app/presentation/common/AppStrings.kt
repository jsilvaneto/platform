package com.platform.app.presentation.common

/**
 * Fonte única da verdade para todos os textos literais de interface (UI) do aplicativo Platform.
 *
 * Centraliza as strings hardcoded da camada de apresentação para garantir consistência,
 * evitar dispersão textual e viabilizar refatorações de nomenclatura seguras e atômicas.
 */
object AppStrings {

    object Status {
        const val PENDING = "Pendente"
        const val PAID = "Pago"
        const val PAID_FEMALE = "Paga"
        const val ALREADY_PAID = "Já Paga"
        const val TO_PAY = "A Pagar"
        const val OVERDUE = "Vencida"
        const val OVERDUE_MALE = "Vencido"
        const val OVERDUE_PAST = "Atrasado"
        const val SETTLED = "Liquidado"
        const val ALL_PAID = "Tudo quitado"
        const val ALL_PAID_PERCENT = "100% quitado"
        const val OPEN = "Aberta"
        const val CLOSED = "Fechada"
        const val PAUSED = "Pausada"
    }

    object Navigation {
        const val OVERVIEW = "Visão geral"
        const val TODAY = "Hoje"
        const val BILLS = "Contas"
        const val CREDIT_CARDS = "Cartões"
        const val ANALYTICS = "Análises"
        const val MORE = "Mais"
    }

    object Drawer {
        const val APP_NAME = "Platform"
        const val SECTION_OPERATIONAL = "Operacional"
        const val SECTION_PLANNING = "Planejamento"
        const val SECTION_MANAGEMENT = "Cadastros"
        const val SECTION_SYSTEM = "Sistema"
        const val OVERVIEW = "Visão geral"
        const val TODAY = "Hoje"
        const val BILLS = "Contas"
        const val CREDIT_CARDS = "Cartões"
        const val ANALYTICS = "Análises"
        const val CONTACTS = "Contatos"
        const val REGISTRATIONS = "Cadastros"
        const val SETTINGS = "Configurações"
        const val MONTH_REMAINING_PREFIX = "Mês · restam "
        const val MONTH_ALL_SETTLED = "Mês · tudo em dia"
        const val LAST_BACKUP_PREFIX = "Último backup: "
        const val LAST_BACKUP_NEVER = "Último backup: Nunca"
    }

    object Glossary {
        const val UPCOMING_PAYMENTS = "Próximos pagamentos"
        const val BILLS_SUMMARY = "Resumo das contas"
        const val OPEN_INSTALLMENTS = "Parcelamentos em aberto"
        const val DUE_SOON = "Vence em breve"
        const val NEXT_MONTHS_PAYMENTS = "Pagamentos nos próximos meses"
        const val SUGGESTIONS = "Sugestões"
        const val BUDGET_BALANCE = "Equilíbrio do orçamento"
        const val HOW_YOU_PAY = "Como você paga"
        const val SPENDING_BY_ACCOUNT = "Gastos por conta"
        const val FIXED_BILL = "Conta fixa"
        const val BILLS = "Contas"
        const val RECURRING_AND_INSTALLMENTS = "Recorrentes e parceladas"
        const val ITEMS = "Itens"
    }

    object Management {
        const val ACCOUNTS = "Contas"
        const val PAYMENT_METHODS = "Formas"
        const val CATEGORIES = "Categorias"
        const val EXPENSE_ITEMS = "Itens"
    }

    object Bills {
        const val ALL = "Todas"
        const val RECURRING = "Recorrentes"
        const val INSTALLMENTS = "Parceladas"
    }

    object Nature {
        const val MANDATORY = "Obrigatório"
        const val NECESSARY = "Necessário"
        const val WANTS = "Desejo"
        const val NONE = "Nenhum"
        const val SAVINGS = "Poupança"
        const val LABEL_NATURE = "Natureza do Gasto"
        const val LABEL_NATURE_COLON = "Natureza do Gasto:"
        const val DESC_MANDATORY = "Gastos indispensáveis para sobrevivência ou compromissos jurídicos inegociáveis."
        const val DESC_NECESSARY = "Gastos essenciais para a rotina diária, saúde, trabalho e conforto básico."
        const val DESC_WANTS = "Gastos de estilo de vida, lazer, supérfluos e compras por desejo pessoal."
        const val DESC_NONE = "Classificação financeira sem restrição específica."
    }

    object AccountType {
        const val CHECKING = "Conta Corrente"
        const val WALLET = "Carteira / Dinheiro"
        const val SAVINGS = "Poupança"
        const val INVESTMENT = "Investimento"
    }

    object ContactType {
        const val INDIVIDUAL = "Pessoa Física"
        const val SUPPLIER = "Fornecedor"
        const val PUBLIC_ENTITY = "Órgão Público"
        const val INDIVIDUAL_SHORT = "Pessoa"
        const val SUPPLIER_SHORT = "Empresa"
        const val PUBLIC_ENTITY_SHORT = "Público"
        const val SAVE_CONTACT = "Salvar Contato"
        const val UPDATE_CONTACT = "Atualizar Contato"
        const val DELETE_CONTACT = "Excluir Contato"
    }

    object BillType {
        const val SINGLE = "Avulsa"
        const val INSTALLMENT = "Parcelada"
        const val INSTALLMENT_MALE = "Parcelado"
        const val RECURRING = "Recorrente"
    }

    object Actions {
        const val SAVE = "Salvar"
        const val SAVE_CHANGES = "Salvar Alterações"
        const val UPDATE = "Atualizar"
        const val CANCEL = "Cancelar"
        const val DELETE = "Excluir"
        const val EDIT = "Editar"
        const val CONFIRM = "Confirmar"
        const val CLOSE = "Fechar"
        const val BACK = "Voltar"
        const val FILTER = "Filtrar"
        const val ALL = "Todos"
        const val ALL_FEMALE = "Todas"
        const val ADD = "Adicionar"
        const val NEW = "Novo"
        const val NEW_FEMALE = "Nova"
        const val SHARE = "Compartilhar"
        const val RESTORE = "Restaurar"
        const val BACKUP = "Fazer Backup"
        const val APPLY = "Aplicar"
        const val CLEAR = "Limpar"
        const val SEE_MORE = "Ver mais"
        const val SEE_LESS = "Ver menos"
    }

    object Filters {
        const val ALL_STATUS = "Todos"
        const val TO_PAY = "A Pagar"
        const val PAID = "Pagas"
        const val ALL_YEARS = "Todos os anos"
    }

    object DateShortcuts {
        const val TODAY = "Hoje"
        const val TOMORROW = "Amanhã"
        const val DAY_5 = "Dia 5"
        const val OTHER = "Outro"
    }

    object Home {
        const val FORECAST_REMAINING = "Restante a Pagar no Mês"
        const val TOTAL_OVERDUE = "Total vencido:"
        const val ATTENTION_NEEDED = "Precisa de atenção"
        const val ALL_CAUGHT_UP = "Tudo em dia"
        const val ALL_CAUGHT_UP_DESC = "Nenhuma conta atrasada ou vencendo nos próximos 7 dias."
        const val UPCOMING_PAYMENTS_SECTION = "Próximos pagamentos"
        const val SEE_ALL_BILLS = "Ver todas"
        const val MONTH_TOTAL = "Total do Mês"
        const val PAID_SO_FAR = "Já Pago"
        const val DUE_TODAY = "Vence Hoje"
        const val NEXT_7_DAYS = "Próximos 7 Dias"
        const val REMAINING_BILLS_PREFIX = "Faltam "
        const val REMAINING_BILLS_SUFFIX = " contas · a próxima vence dia "
        const val REMAINING_SINGLE_BILL = "Falta 1 conta · a próxima vence dia "
        const val REMAINING_NO_DATE_PLURAL = " contas a pagar"
        const val REMAINING_NO_DATE_SINGLE = "Falta 1 conta a pagar"
        const val REMAINING_NO_BILLS = "Nenhuma conta agendada"
        const val VIEW_LIST = "Lista"
        const val VIEW_CALENDAR = "Calendário"
        const val EMPTY_MONTH = "Nenhuma conta agendada para este mês"
        const val EMPTY_MONTH_ACTION = "Adicionar despesa"
    }

    object Dialogs {
        const val CONFIRM_TITLE = "Confirmação"
        const val DELETE_REGISTRATION_TITLE = "Excluir Registro"
        const val DELETE_CONTACT_TITLE = "Excluir Contato"
        const val WARNING = "Atenção"

        // Backup & Restore
        const val PROTECT_SHARE_TITLE = "Proteger Compartilhamento"
        const val ENCRYPT_BACKUP_TITLE = "Criptografar Backup"
        const val BACKUP_ENCRYPTION_DESC = "Defina uma senha segura para proteger seus dados financeiros com criptografia simétrica AES-256 (PBKDF2).\n\n⚠️ Esta senha será estritamente necessária para restaurar este arquivo."
        const val BACKUP_PASSWORD_LABEL = "Senha do backup (mínimo 8 caracteres)"
        const val CONFIRM_PASSWORD_LABEL = "Confirmar Senha"
        const val PASSWORDS_DONT_MATCH = "As senhas não coincidem."
        const val ENCRYPT_AND_SEND = "Criptografar e Enviar"
        const val SAVE_FILE = "Salvar Arquivo"
        const val DECRYPT_BACKUP_TITLE = "Descriptografar Backup"
        const val DECRYPT_BACKUP_DESC = "Informe a senha definida no momento da geração deste backup para desbloquear e restaurar os dados com segurança."
        const val DECRYPT_PASSWORD_LABEL = "Senha do Backup"
        const val DECRYPT_AND_RESTORE = "Descriptografar e Restaurar"

        // Baixas & Pagamentos
        const val REVIEW_SETTLEMENTS_TITLE = "Revisar Baixas"
        const val REVIEW_SETTLEMENTS_DESC = "Selecione a data real em que os pagamentos foram efetuados. As parcelas selecionadas passarão a ser contabilizadas no indicador de pontualidade."
        const val CONFIRM_PAYMENT_TITLE = "Confirmar Pagamento"
        const val CONFIRM_PAYMENT_SUBTITLE = "Informe a data real da quitação"
        const val ACTUAL_PAYMENT_DATE = "Data Real do Pagamento"
        const val ACTUAL_PAYMENT_DATE_ALT = "Data Real de Pagamento"
        const val TODAY = "Hoje"
        const val ON_DUE_DATE = "No Vencimento"
        const val ACTUAL_DATE_METRICS_INFO = "A data real informada será usada no cálculo de pontualidade no painel."
        const val PAID_AMOUNT_LABEL = "Valor Pago (R$)"
        const val INTEREST_FINE_LABEL = "Juros/Multa"
        const val DISCOUNT_LABEL = "Desconto"
        const val CENTS_ZERO_PLACEHOLDER = "0,00"
        const val SHOW_ADJUSTMENTS_LABEL = "+ Juros, multa ou desconto (opcional)"
        const val HIDE_ADJUSTMENTS_LABEL = "Ocultar acréscimos/desconto"
        const val DELETE_SELECTED_TITLE = "Excluir Selecionadas"

        // Recorrência
        const val CHANGE_MONTHLY_AMOUNT_TITLE = "Alterar Valor Mensal"
        const val RECURRING_FUTURE_UPDATE_INFO = "Apenas as próximas cobranças pendentes serão atualizadas. Pagamentos já realizados não serão alterados."
        const val NEW_MONTHLY_AMOUNT_LABEL = "Novo valor mensal (R$)"
        const val UPDATE_NEXT_CHARGES = "Atualizar Próximas Cobranças"
        const val INVALID_AMOUNT = "Informe um valor válido"
        const val SAME_AS_CURRENT_AMOUNT = "Igual ao valor atual"
        const val AMOUNT_PLACEHOLDER_SAMPLE = "Ex: 49,90"

        // Gestão & Contatos
        const val NEW_CONTACT_SUPPLIER_TITLE = "Novo Contato / Fornecedor"
        const val CONTACT_NAME_LABEL = "Nome do Contato ou Estabelecimento *"
        const val NEW_ACCOUNT_TITLE = "Nova Conta"
        const val EDIT_ACCOUNT_TITLE = "Editar Conta"
        const val ACCOUNT_NAME_LABEL = "Nome da Conta (ex: Nubank, Itaú)"
        const val ACCOUNT_TYPE_LABEL = "Tipo de Conta"
        const val NEW_PAYMENT_METHOD_TITLE = "Nova Forma de Pagamento"
        const val EDIT_PAYMENT_METHOD_TITLE = "Editar Forma de Pagamento"
        const val PAYMENT_TYPE_LABEL = "Tipo de Pagamento"
        const val CUSTOM_NAME_LABEL = "Nome Personalizado (ex: Vale Alimentação)"
        const val NEW_CATEGORY_TITLE = "Nova Categoria"
        const val EDIT_CATEGORY_TITLE = "Editar Categoria"
        const val CATEGORY_NAME_LABEL = "Nome da Categoria (ex: Alimentação, Lazer)"
    }

    object ExpenseForm {
        const val AMOUNT_REQUIRED = "Valor *"
        const val EXPENSE_ITEM_REQUIRED = "Item de Despesa *"
        const val SELECT_ITEM_PLACEHOLDER = "Selecione o item"
        const val CONTACT_OPTIONAL = "Contato / Fornecedor (Opcional)"
    }

    object Notifications {
        const val DISABLED_TITLE = "Lembretes de vencimento desativados"
        const val DISABLED_MESSAGE = "As notificações estão desligadas. Ative para receber alertas antes do vencimento das suas contas."
        const val ENABLE_BUTTON = "Ativar Notificações"
        const val PERMISSION_EXPLANATION = "A Platform avisa você no dia e na véspera do vencimento das contas para você nunca atrasar um pagamento."
        const val ALLOW_NOTIFICATIONS = "Permitir avisos"
    }

    object CreditCards {
        const val CAN_I_BUY_TITLE = "Posso comprar?"
        const val CAN_I_BUY_LABEL = "Valor da compra pretendida"
        const val ZERO_CURRENCY_PLACEHOLDER = "R$ 0,00"
        const val AVAILABLE_LIMIT = "Limite disponível:"
        const val FITS_LIMIT = "Cabe no seu limite!"
        const val EXCEEDS_LIMIT = "Não cabe no seu limite!"
        const val REMAINING_LIMIT = "Restará de limite:"
        const val MY_CARDS = "Meus Cartões"
        const val NO_CARDS_REGISTERED = "Nenhum cartão cadastrado"
        const val NO_CARDS_DESC = "Cadastre seu cartão de crédito para gerenciar limites e faturas."
        const val REGISTER_FIRST_CARD = "Cadastrar primeiro cartão"
        const val PAY_INVOICE = "Pagar fatura"
        const val INVOICE_PAID = "Fatura paga"
        const val INVOICE_INSTALLMENTS = "Lançamentos na fatura"
        const val NO_INSTALLMENTS_ON_INVOICE = "Nenhum lançamento nesta fatura."
        const val BEST_DAY_PREFIX = "Melhor dia: dia "
        const val BEST_DAY_DESC = "Compras a partir deste dia caem na próxima fatura."
        const val DELETE_CARD_TITLE = "Excluir Cartão de Crédito"
        const val DELETE_CARD_CONFIRM = "Tem certeza de que deseja excluir este cartão de crédito?"
        const val CARD_IN_USE_WARNING = "Atenção: Cartão em uso!"
    }

    object Theme {
        const val AUTO = "Automático"
        const val LIGHT = "Claro"
        const val DARK = "Escuro"
        const val AMOLED = "AMOLED"
    }

    object SettingsPreferences {
        const val SECTION_TITLE = "Navegação & Início"
        const val INITIAL_SCREEN_TITLE = "Tela Inicial"
        const val INITIAL_SCREEN_DESC = "Escolha qual tela deve abrir ao iniciar o aplicativo"
        const val INITIAL_SCREEN_OVERVIEW = "Visão Geral (padrão)"
        const val INITIAL_SCREEN_TODAY = "Hoje"
    }

    object RecurringActions {
        const val PAUSE = "Pausar"
        const val RESUME = "Retomar"
        const val END = "Encerrar"
        const val END_RECURRING = "Encerrar recorrência"
        const val REACTIVATE_RECURRING = "Reativar recorrência"
        const val EDIT_AMOUNT_FROM = "Alterar valor a partir de..."
        const val NEW_MONTHLY_AMOUNT = "Novo Valor Mensal"
        const val CURRENCY_PREFIX = "R$ "
    }

    object Overview {
        const val ALL_PERIODS = "Todos os períodos"
        const val OPEN_INSTALLMENTS = "Parcelado em aberto"
        const val NOTHING_OPEN = "Nada em aberto"
        const val NO_OPEN_INSTALLMENTS_DESC = "Nenhuma compra parcelada em aberto no momento."
        const val FIXED_COST_MONTH = "Custo fixo/mês"
        const val CARDS_IN_USE = "Cartões em uso"
        const val RESERVE = "Reserva"
        const val NO_DATA_DASH = "—"
        const val MONTHLY_SERIES_TITLE = "Projeção em 12 Meses"
        const val PEAK = "Pico"
        const val RELIEF = "Folga"
        const val WHEN_FREE_TITLE = "Quando fica livre"
        const val LIBERATES_PREFIX = "+ R$ "
        const val LIBERATES_SUFFIX = "/mês liberados"
        const val ATTENTION_PREFIX = "Atenção: "
        const val ATTENTION_SUFFIX = " pendência(s) hoje ou em atraso"
        const val ATTENTION_ACTION = "Ver em Hoje"
    }

    object Common {
        const val DASH = "—"
        const val PLUS = "+"
        const val NONE_MALE = "Nenhum"
        const val NONE_FEMALE = "Nenhuma"
        const val UNDERSTOOD = "Entendido"
        const val SELECT_FILE = "Selecionar Arquivo"
    }

    object CommitmentType {
        const val CASH = "À Vista"
        const val INSTALLMENT = "Parcelado"
        const val RECURRING = "Recorrente"
        const val DAILY = "Diariamente"
        const val WEEKLY = "Semanalmente"
        const val MONTHLY = "Mensalmente"
        const val ANNUALLY = "Anualmente"
        const val FOREVER = "Para sempre"
        const val UNTIL_DATE = "Até uma data"
        const val BY_OCCURRENCES = "Por número de eventos"
        const val CHANGE_END_DATE = "Alterar Data Final"
    }

    object ExpenseLabels {
        const val EXPENSE_NAME_REQUIRED = "Nome da Conta / Despesa *"
        const val AMOUNT_BRL_REQUIRED = "Valor (R$) *"
        const val CATEGORY = "Categoria"
        const val RECIPIENT_SUPPLIER = "Favorecido / Fornecedor"
        const val DEBIT_ACCOUNT_BANK = "Conta de Débito / Banco"
        const val PAYMENT_METHOD = "Forma de Pagamento"
        const val SPECIFIC_DESCRIPTION = "Descrição / Detalhe Específico"
        const val SPECIFIC_DESCRIPTION_PLACEHOLDER = "Ex: Compras no Carrefour, troca de filtro"
        const val NO_ITEM_REGISTERED = "Nenhum item cadastrado"
        const val EXPENSE_AMOUNT_REQUIRED = "Valor da Despesa *"
        const val EXPENSE_ITEM_REQUIRED_LABEL = "Item de Despesa * (Obrigatório)"
        const val SELECT_CATEGORIZED_ITEM = "Selecione o item categorizado"
        const val ITEM_REQUIRED_ERROR = "Item é obrigatório para classificar a despesa"
        const val CREDIT_CARD_REQUIRED = "Cartão de Crédito *"
        const val PAYMENT_METHOD_LABEL = "Forma de Pagamento (PIX, Boleto, etc.)"
        const val FINANCIAL_ACCOUNT_BANK = "Conta Financeira / Banco"
        const val CONTACT_REQUIRED_ERROR = "Contato é obrigatório no lançamento"
        const val NO_CONTACT_REGISTERED = "Nenhum contato cadastrado"
        const val CHANGE_DATE = "Alterar Data"
        const val CONTACT_SUPPLIER_REQUIRED = "Contato / Fornecedor *"
        const val SELECT_RECIPIENT = "Selecione o favorecido"
    }

    object Budgets {
        const val CREATE_FIRST = "Criar Primeiro Teto"
        const val SELECT_CATEGORY_COLON = "Selecione a Categoria:"
        const val GENERAL = "Geral"
        const val MAX_MONTHLY_LIMIT = "Limite Mensal Máximo (R$)"
        const val EDIT_BUDGET = "Editar Orçamento"
        const val DELETE_BUDGET = "Excluir Orçamento"
    }

    object Contacts {
        const val DETAILS_TITLE = "Detalhes do Contato"
        const val EDIT_CONTACT = "Editar Contato"
        const val DELETE_CONTACT = "Excluir Contato"
        const val FULL_NAME_REQUIRED = "Nome Completo / Razão Social *"
        const val PHONE_MOBILE = "Telefone / Celular"
        const val EMAIL = "E-mail"
        const val CEP_LABEL = "CEP (busca automática ao digitar)"
        const val CEP_PLACEHOLDER = "Ex: 01001-000"
        const val STREET_LABEL = "Logradouro (Rua, Avenida, Praça)"
        const val NUMBER_LABEL = "Número"
        const val NUMBER_PLACEHOLDER = "Ex: 123, S/N"
        const val COMPLEMENT_LABEL = "Complemento"
        const val COMPLEMENT_PLACEHOLDER = "Apto 42, Bloco B"
        const val NEIGHBORHOOD_LABEL = "Bairro"
        const val CITY_LABEL = "Cidade"
        const val STATE_UF_LABEL = "Estado (UF)"
        const val STATE_UF_PLACEHOLDER = "Ex: SP"
        const val COUNTRY_LABEL = "País"
    }

    object CreditCardLabels {
        const val CUTOFF_DAY_REQUIRED = "Dia Corte (1-31) *"
        const val DUE_DAY_REQUIRED = "Dia Venc. (1-31) *"
        const val CARD_NAME_REQUIRED = "Nome do Cartão *"
        const val CARD_NAME_PLACEHOLDER = "Ex: Nubank Mastercard, Unique Visa"
        const val TOTAL_LIMIT_REQUIRED = "Limite Total *"
        const val VIEW_PURCHASES = "Ver Compras"
        const val SETTLE = "Liquidar"
        const val SETTLE_INVOICE_NOW = "Liquidar Fatura Agora"
        const val CONFIRM_DELETE_BUTTON = "Sim, Excluir"
    }

    object Goals {
        const val CREATE_FIRST = "Criar Primeira Meta"
        const val ADD_CONTRIBUTION = "Adicionar Aporte"
        const val EDIT_GOAL = "Editar Meta"
        const val DELETE_GOAL = "Excluir Meta"
        const val CONTRIBUTION_AMOUNT_LABEL = "Valor do Aporte (R$)"
        const val CONFIRM_CONTRIBUTION = "Confirmar Aporte"
        const val GOAL_NAME_LABEL = "Nome da Meta (ex: Reserva, Viagem, Carro)"
        const val TARGET_AMOUNT_LABEL = "Valor Alvo Desejado (R$)"
        const val INITIAL_SAVED_AMOUNT_LABEL = "Valor Já Poupado Inicial (R$)"
    }

    object ExpenseItems {
        const val SEARCH_CATEGORY = "Buscar Categoria..."
        const val ITEM_NAME_LABEL = "Nome do Item (ex: Supermercado, Internet)"
        const val EDIT_ITEM = "Editar Item"
        const val DELETE_ITEM = "Excluir Item"
    }

    object ManagementLabels {
        const val EDIT_ACCOUNT = "Editar Conta"
        const val DELETE_ACCOUNT = "Excluir Conta"
        const val EDIT_CATEGORY = "Editar Categoria"
        const val DELETE_CATEGORY = "Excluir Categoria"
        const val EDIT_PAYMENT_METHOD = "Editar Forma de Pagamento"
        const val DELETE_PAYMENT_METHOD = "Excluir Forma de Pagamento"
    }

    object RecurringLabels {
        const val NEW_AMOUNT_REQUIRED = "Novo Valor *"
        const val DELETE_ELLIPSIS = "Excluir..."
        const val CONFIRM_DELETION = "Confirmar Exclusão"
        const val CHANGE_MONTHLY_AMOUNT = "Alterar Valor Mensal"
        const val END_RECURRING = "Encerrar Recorrência"
        const val DELETE_ALL_WITH_HISTORY = "Excluir Tudo (Inclusive Histórico)"
        const val RESUME = "Retomar"
        const val DELETE_EXPENSE_TITLE = "Excluir Lançamento"
        const val END_RECURRING_QUESTION = "Encerrar Recorrência?"
    }

    object IconsEmoji {
        const val CHART = "📊"
        const val TARGET = "🎯"
        const val ROCKET = "🚀"
    }
}
