package br.edu.nassau.apicursos.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratador global de excecoes da API REST.
 * Converte excecoes de negocio e validacao em respostas HTTP semanticas (404, 400 e 500).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> handleCursoNaoEncontrado(CursoNaoEncontradoException ex) {
        log.warn("Recurso nao encontrado: {}", ex.getMessage());

        ErroResposta resposta = new ErroResposta(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso nao encontrado",
                ex.getMessage(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> handleRotaNaoEncontrada(NoResourceFoundException ex) {
        log.warn("Rota nao encontrada: /{}", ex.getResourcePath());

        ErroResposta resposta = new ErroResposta(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Rota nao encontrada",
                "Endpoint nao existe. Utilize /cursos ou /actuator/health.",
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> detalhes = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            detalhes.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Erro de validacao nos dados enviados. Campos invalidos: {}", detalhes.keySet());

        ErroResposta resposta = new ErroResposta(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validacao",
                "Um ou mais campos enviados sao invalidos",
                detalhes
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> handleRegraNegocio(IllegalArgumentException ex) {
        log.warn("Violacao de regra de negocio: {}", ex.getMessage());

        ErroResposta resposta = new ErroResposta(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Requisicao invalida",
                ex.getMessage(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResposta> handleFormatoInvalido(Exception ex) {
        log.warn("Formato de requisicao invalido: {}", ex.getMessage());

        ErroResposta resposta = new ErroResposta(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Requisicao invalida",
                "Verifique o formato JSON e os parametros enviados na requisicao.",
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> handleErroInesperado(Exception ex) {
        // Registra o erro no log interno (ERROR) sem expor detalhes internos ou sensiveis ao cliente HTTP
        log.error("Erro interno inesperado ao processar requisicao", ex);

        ErroResposta resposta = new ErroResposta(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
    }
}
