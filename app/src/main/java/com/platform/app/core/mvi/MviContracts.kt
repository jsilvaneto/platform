package com.platform.app.core.mvi

/**
 * Interface marcadora que representa o estado imutável de uma tela.
 */
interface UiState

/**
 * Interface marcadora que representa uma intenção ou ação do usuário vinda da UI.
 */
interface UiAction

/**
 * Interface marcadora que representa um evento de efeito colateral de disparo único
 * (ex: navegação, exibição de SnackBar, Toast ou Dialog) que não deve sobreviver a recomposições.
 */
interface UiEffect
