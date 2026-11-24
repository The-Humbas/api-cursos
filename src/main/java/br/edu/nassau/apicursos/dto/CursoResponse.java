package br.edu.nassau.apicursos.dto;

import br.edu.nassau.apicursos.model.Curso;

import java.math.BigDecimal;

/**
 * DTO de saida (Response) retornado nos endpoints da API.
 * Desacopla o contrato JSON exposto ao cliente da entidade JPA interna.
 */
public record CursoResponse(
        Long id,
        String nome,
        String descricao,
        Integer cargaHoraria,
        String modalidade,
        BigDecimal valor
) {
    public static CursoResponse fromEntity(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getNome(),
                curso.getDescricao(),
                curso.getCargaHoraria(),
                curso.getModalidade(),
                curso.getValor()
        );
    }
}
