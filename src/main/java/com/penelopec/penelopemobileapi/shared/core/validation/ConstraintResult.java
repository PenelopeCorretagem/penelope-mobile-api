package com.penelopec.penelopemobileapi.shared.core.validation;

/**
 * Representa um erro de validação mapeado de forma purista, desvinculado do framework.
 * @param field   O caminho do campo que falhou (ex: "endereco.cep").
 * @param message A mensagem de erro gerada pelo validador.
 */
public record ConstraintResult(String field, String message) {}
