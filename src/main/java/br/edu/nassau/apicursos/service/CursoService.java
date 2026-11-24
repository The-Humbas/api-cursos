package br.edu.nassau.apicursos.service;

import br.edu.nassau.apicursos.dto.CursoRequest;
import br.edu.nassau.apicursos.dto.CursoResponse;
import br.edu.nassau.apicursos.exception.CursoNaoEncontradoException;
import br.edu.nassau.apicursos.model.Curso;
import br.edu.nassau.apicursos.repository.CursoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Camada de regras de negocio da API de Cursos.
 *
 * Boas praticas aplicadas (Aulas 10 e 11):
 * - Injecao de dependencia via construtor, facilitando testes unitarios com @InjectMocks e @Mock.
 * - Uso do Logger do SLF4J no lugar de System.out.println(), garantindo niveis de severidade
 *   (INFO, WARN, ERROR), data/hora e integracao com logs de containers, sem nunca registrar
 *   senhas, tokens ou dados sensiveis.
 * - Lancamento explicito de CursoNaoEncontradoException quando o ID nao existe no banco.
 */
@Service
public class CursoService {

    // Logger estruturado do SLF4J (substitui System.out.println() em ambientes reais e containers)
    private static final Logger log = LoggerFactory.getLogger(CursoService.class);

    private final CursoRepository repository;

    public CursoService(CursoRepository repository) {
        this.repository = repository;
    }

    public String gerarMensagem() {
        return "CursoService funcionando com injecao de dependencia via construtor!";
    }

    public CursoResponse cadastrar(CursoRequest request) {
        validarDadosCurso(request);

        Curso novoCurso = new Curso(
                request.nome().trim(),
                request.descricao().trim(),
                request.cargaHoraria(),
                request.modalidade().trim(),
                request.valor()
        );

        Curso cursoSalvo = repository.save(novoCurso);

        // Log INFO com dados neutros (apenas ID e nome do curso; nunca registrar segredos)
        log.info("Curso cadastrado com sucesso. ID={} | nome={}", cursoSalvo.getId(), cursoSalvo.getNome());
        return CursoResponse.fromEntity(cursoSalvo);
    }

    public List<CursoResponse> listar() {
        List<CursoResponse> cursos = repository.findAll()
                .stream()
                .map(CursoResponse::fromEntity)
                .toList();

        log.info("Listagem de cursos executada. Total encontrado={}", cursos.size());
        return cursos;
    }

    public CursoResponse buscarPorId(Long id) {
        // Verifica no repositorio se o registro existe; caso contrario, registra WARN e lanca excecao 404
        Curso curso = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Curso nao encontrado na busca por ID={}", id);
                    return new CursoNaoEncontradoException(id);
                });

        log.info("Curso localizado com sucesso. ID={}", curso.getId());
        return CursoResponse.fromEntity(curso);
    }

    public CursoResponse atualizar(Long id, CursoRequest request) {
        // Primeiro garante que o registro existe antes de tentar atualizar e persistir no banco
        Curso cursoExistente = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tentativa de atualizacao para curso inexistente. ID={}", id);
                    return new CursoNaoEncontradoException(id);
                });

        validarDadosCurso(request);

        cursoExistente.setNome(request.nome().trim());
        cursoExistente.setDescricao(request.descricao().trim());
        cursoExistente.setCargaHoraria(request.cargaHoraria());
        cursoExistente.setModalidade(request.modalidade().trim());
        cursoExistente.setValor(request.valor());

        Curso cursoAtualizado = repository.save(cursoExistente);

        log.info("Curso atualizado com sucesso. ID={}", cursoAtualizado.getId());
        return CursoResponse.fromEntity(cursoAtualizado);
    }

    public void remover(Long id) {
        // Busca o curso antes de excluir; se nao existir, lanca excecao e nao executa delete no banco
        Curso cursoExistente = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tentativa de exclusao para curso inexistente. ID={}", id);
                    return new CursoNaoEncontradoException(id);
                });

        repository.delete(cursoExistente);
        log.info("Curso removido com sucesso. ID={}", id);
    }

    /**
     * Valida regras de negocio essenciais diretamente na camada de servico,
     * garantindo integridade mesmo quando o Service e invocado fora da camada HTTP.
     */
    private void validarDadosCurso(CursoRequest request) {
        if (request == null) {
            log.warn("Tentativa de processar curso com payload nulo");
            throw new IllegalArgumentException("Dados do curso sao obrigatorios");
        }
        if (request.nome() == null || request.nome().isBlank()) {
            log.warn("Validacao falhou: nome do curso em branco");
            throw new IllegalArgumentException("Nome do curso e obrigatorio");
        }
        if (request.descricao() == null || request.descricao().isBlank()) {
            log.warn("Validacao falhou: descricao do curso em branco");
            throw new IllegalArgumentException("Descricao do curso e obrigatoria");
        }
        if (request.cargaHoraria() == null || request.cargaHoraria() <= 0) {
            log.warn("Validacao falhou: carga horaria invalida ({})", request.cargaHoraria());
            throw new IllegalArgumentException("Carga horaria deve ser maior que zero");
        }
        if (request.modalidade() == null || request.modalidade().isBlank()) {
            log.warn("Validacao falhou: modalidade do curso em branco");
            throw new IllegalArgumentException("Modalidade do curso e obrigatoria");
        }
        if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Validacao falhou: valor negativo ou nulo ({})", request.valor());
            throw new IllegalArgumentException("Valor do curso nao pode ser negativo");
        }
    }
}
