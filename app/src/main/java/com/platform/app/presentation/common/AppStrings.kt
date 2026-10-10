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
        const val TODAY = "Hoje"
        const val BILLS = "Contas"
        const val CREDIT_CARDS = "Cartões"
        const val ANALYTICS = "Análises"
        const val MORE = "Mais"
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
}
