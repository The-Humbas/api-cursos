package br.edu.nassau.apicursos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO de entrada (Request) para cadastro e atualizacao de cursos.
 * Utiliza Bean Validation para rejeitar payloads invalidos com HTTP 400 Bad Request na camada Controller.
 */
public record CursoRequest(

        @NotBlank(message = "Nome do curso e obrigatorio")
        @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres")
        String nome,

        @NotBlank(message = "Descricao do curso e obrigatoria")
        @Size(max = 500, message = "Descricao deve ter no maximo 500 caracteres")
        String descricao,

        @NotNull(message = "Carga horaria e obrigatoria")
        @Positive(message = "Carga horaria deve ser maior que zero")
        Integer cargaHoraria,

        @NotBlank(message = "Modalidade do curso e obrigatoria")
        @Size(max = 40, message = "Modalidade deve ter no maximo 40 caracteres")
        String modalidade,

        @NotNull(message = "Valor do curso e obrigatorio")
        @DecimalMin(value = "0.00", inclusive = true, message = "Valor do curso nao pode ser negativo")
        BigDecimal valor
) {
}
