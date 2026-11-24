package br.edu.nassau.apicursos.exception;

/**
 * Excecao de negocio lancada quando um curso solicitado por ID nao existe no repositorio.
 * E tratada pelo GlobalExceptionHandler para retornar HTTP 404 Not Found.
 */
public class CursoNaoEncontradoException extends RuntimeException {

    public CursoNaoEncontradoException(Long id) {
        super("Curso nao encontrado com ID: " + id);
    }

    public CursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
