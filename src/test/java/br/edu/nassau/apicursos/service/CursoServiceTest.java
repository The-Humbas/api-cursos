package br.edu.nassau.apicursos.service;

import br.edu.nassau.apicursos.dto.CursoRequest;
import br.edu.nassau.apicursos.dto.CursoResponse;
import br.edu.nassau.apicursos.exception.CursoNaoEncontradoException;
import br.edu.nassau.apicursos.model.Curso;
import br.edu.nassau.apicursos.repository.CursoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios da camada de regras de negocio (CursoService) - Aula 10.
 *
 * @ExtendWith(MockitoExtension.class) integra o JUnit 5 ao Mockito, ativando o processamento
 * automatico das anotacoes @Mock e @InjectMocks sem precisar subir o contexto do Spring Boot
 * nem conectar ao PostgreSQL. Isso garante testes rapidos, isolados e deterministicos.
 */
@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    // @Mock cria um objeto simulado e controlado do CursoRepository em memoria (nenhum SQL real e executado).
    @Mock
    private CursoRepository repository;

    // @InjectMocks instancia o CursoService real (a classe sob teste) e injeta o mock do repository no construtor.
    @InjectMocks
    private CursoService service;

    @Test
    void deveGerarMensagemDeInjecaoDeDependencia() {
        // Act: chama o metodo auxiliar que comprova a instanciacao do Service via construtor
        String mensagem = service.gerarMensagem();

        // Assert: confirma que a mensagem esperada foi retornada
        assertEquals("CursoService funcionando com injecao de dependencia via construtor!", mensagem);
    }

    @Test
    void deveCadastrarCursoValido() {
        // Arrange: prepara os dados de entrada (DTO), o objeto esperado de retorno e o comportamento do mock
        CursoRequest request = new CursoRequest(
                "Engenharia de Software",
                "Arquitetura, testes automatizados e boas praticas em Java",
                160,
                "Presencial",
                new BigDecimal("1499.90")
        );

        Curso cursoSalvo = new Curso(
                1L,
                "Engenharia de Software",
                "Arquitetura, testes automatizados e boas praticas em Java",
                160,
                "Presencial",
                new BigDecimal("1499.90")
        );

        // Programa o mock: quando o Service chamar repository.save(...), devolve o cursoSalvo com ID 1L
        when(repository.save(any(Curso.class))).thenReturn(cursoSalvo);

        // Act: executa o comportamento real do Service que esta sendo testado
        CursoResponse resultado = service.cadastrar(request);

        // Assert: verifica os valores retornados e confirma com verify que save() foi chamado exatamente 1 vez
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Engenharia de Software", resultado.nome());
        assertEquals(160, resultado.cargaHoraria());
        assertEquals("Presencial", resultado.modalidade());
        assertEquals(new BigDecimal("1499.90"), resultado.valor());
        verify(repository, times(1)).save(any(Curso.class));
    }

    @Test
    void deveBuscarCursoPorIdQuandoExistir() {
        // Arrange: cria o curso esperado e configura o mock do findById(1L) para retornar Optional.of(curso)
        Curso cursoExistente = new Curso(
                1L,
                "Ciencia da Computacao",
                "Fundamentos de algoritmos, estruturas de dados e sistemas distribuidos",
                200,
                "Hibrido",
                new BigDecimal("1890.00")
        );

        when(repository.findById(1L)).thenReturn(Optional.of(cursoExistente));

        // Act: invoca a busca por ID existente no Service
        CursoResponse resultado = service.buscarPorId(1L);

        // Assert: valida todos os campos retornados e confirma a chamada unica ao findById(1L)
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Ciencia da Computacao", resultado.nome());
        assertEquals(200, resultado.cargaHoraria());
        assertEquals("Hibrido", resultado.modalidade());
        assertEquals(new BigDecimal("1890.00"), resultado.valor());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void deveListarTodosOsCursos() {
        // Arrange: simula uma lista com 2 cursos retornados pelo repositorio
        List<Curso> cursosNoBanco = List.of(
                new Curso(1L, "Java Back-End", "Spring Boot e JPA", 120, "EAD", new BigDecimal("799.00")),
                new Curso(2L, "Docker na Pratica", "Containers e Docker Compose", 60, "EAD", new BigDecimal("399.00"))
        );

        when(repository.findAll()).thenReturn(cursosNoBanco);

        // Act: solicita a listagem completa ao Service
        List<CursoResponse> resultado = service.listar();

        // Assert: verifica quantidade, conteudo mapeado e interacao unica com findAll()
        assertEquals(2, resultado.size());
        assertEquals("Java Back-End", resultado.get(0).nome());
        assertEquals("Docker na Pratica", resultado.get(1).nome());
        verify(repository, times(1)).findAll();
    }

    @Test
    void deveAtualizarCursoQuandoExistir() {
        // Arrange: prepara curso existente, novos dados e os retornos simulados de findById e save
        Curso cursoExistente = new Curso(
                1L,
                "Java Basico",
                "Introducao a linguagem Java",
                40,
                "EAD",
                new BigDecimal("299.00")
        );

        CursoRequest dadosAtualizados = new CursoRequest(
                "Java Avancado",
                "Spring Boot, Testes, Docker e Observabilidade",
                100,
                "Hibrido",
                new BigDecimal("899.90")
        );

        Curso cursoAtualizado = new Curso(
                1L,
                "Java Avancado",
                "Spring Boot, Testes, Docker e Observabilidade",
                100,
                "Hibrido",
                new BigDecimal("899.90")
        );

        when(repository.findById(1L)).thenReturn(Optional.of(cursoExistente));
        when(repository.save(any(Curso.class))).thenReturn(cursoAtualizado);

        // Act: executa a atualizacao do curso ID 1L
        CursoResponse resultado = service.atualizar(1L, dadosAtualizados);

        // Assert: confirma atualizacao dos atributos e que save() foi acionado uma unica vez
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Java Avancado", resultado.nome());
        assertEquals(100, resultado.cargaHoraria());
        assertEquals("Hibrido", resultado.modalidade());
        assertEquals(new BigDecimal("899.90"), resultado.valor());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(Curso.class));
    }

    @Test
    void deveRemoverCursoQuandoExistir() {
        // Arrange: simula que o curso com ID 1L existe no repositorio
        Curso cursoExistente = new Curso(
                1L,
                "Banco de Dados PostgreSQL",
                "Modelagem relacional e SQL avancado",
                80,
                "Presencial",
                new BigDecimal("550.00")
        );

        when(repository.findById(1L)).thenReturn(Optional.of(cursoExistente));

        // Act: executa a remocao do curso existente
        service.remover(1L);

        // Assert: confirma que findById(1L) foi consultado e delete() foi chamado exatamente 1 vez
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).delete(cursoExistente);
    }

    @Test
    void deveLancarExcecaoQuandoBuscarCursoInexistente() {
        // Arrange: configura o mock para retornar Optional.empty(), simulando ID inexistente no banco
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert: assertThrows comprova que CursoNaoEncontradoException e lancada ao buscar ID 999L
        CursoNaoEncontradoException excecao = assertThrows(
                CursoNaoEncontradoException.class,
                () -> service.buscarPorId(999L)
        );

        assertEquals("Curso nao encontrado com ID: 999", excecao.getMessage());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void deveLancarExcecaoAoRemoverCursoInexistente() {
        // Arrange: simula que o ID 999L nao foi encontrado no repositorio
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert: valida o lancamento da excecao e comprova com verify(..., never()) que
        // nenhuma operacao destrutiva (delete) foi enviada ao repositorio
        assertThrows(
                CursoNaoEncontradoException.class,
                () -> service.remover(999L)
        );

        verify(repository, times(1)).findById(999L);
        verify(repository, never()).delete(any(Curso.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarCursoInexistente() {
        // Arrange: monta uma requisicao valida, mas simula que o ID 999L nao existe no banco
        CursoRequest request = new CursoRequest(
                "Arquitetura de Software",
                "Padroes arquiteturais modernos",
                90,
                "EAD",
                new BigDecimal("699.00")
        );

        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert: valida a excecao e comprova que save() NUNCA foi chamado para registro inexistente
        assertThrows(
                CursoNaoEncontradoException.class,
                () -> service.atualizar(999L, request)
        );

        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any(Curso.class));
    }

    @Test
    void deveLancarExcecaoAoCadastrarCursoComCargaHorariaInvalida() {
        // Arrange: cria um DTO com carga horaria zero (violacao de regra de negocio)
        CursoRequest requestInvalido = new CursoRequest(
                "Curso Invalido",
                "Descricao de teste",
                0,
                "EAD",
                new BigDecimal("100.00")
        );

        // Act + Assert: valida IllegalArgumentException e confirma com never() que nada foi salvo no banco
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(requestInvalido)
        );

        assertEquals("Carga horaria deve ser maior que zero", excecao.getMessage());
        verify(repository, never()).save(any(Curso.class));
    }
}
