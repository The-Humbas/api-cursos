package br.edu.nassau.apicursos.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estrutura padronizada de resposta de erro retornada pela API em formato JSON.
 */
public record ErroResposta(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        Map<String, String> detalhes
) {
}
