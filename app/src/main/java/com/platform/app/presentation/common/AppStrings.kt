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

    object Nature {
        const val MANDATORY = "Obrigatório"
        const val NECESSARY = "Necessário"
        const val WANTS = "Deseja"
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
    }

    object Home {
        const val FORECAST_REMAINING = "Restante a Pagar no Mês"
        const val TOTAL_OVERDUE = "Total vencido:"
    }

    object Dialogs {
        const val CONFIRM_TITLE = "Confirmação"
        const val DELETE_REGISTRATION_TITLE = "Excluir Registro"
        const val DELETE_CONTACT_TITLE = "Excluir Contato"
        const val WARNING = "Atenção"
    }
}
