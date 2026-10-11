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
}
