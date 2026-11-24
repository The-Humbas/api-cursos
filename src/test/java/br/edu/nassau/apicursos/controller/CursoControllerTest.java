package br.edu.nassau.apicursos.controller;

import br.edu.nassau.apicursos.dto.CursoRequest;
import br.edu.nassau.apicursos.dto.CursoResponse;
import br.edu.nassau.apicursos.exception.CursoNaoEncontradoException;
import br.edu.nassau.apicursos.service.CursoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada Web (CursoController) utilizando @WebMvcTest e MockMvc - Aula 10.
 *
 * @WebMvcTest carrega apenas a fatia web relacionada ao CursoController (rotas, serializacao JSON,
 * validacoes @Valid e GlobalExceptionHandler), mantendo o teste leve sem subir o banco PostgreSQL.
 * O CursoService e simulado com @MockitoBean para validar exclusivamente o contrato HTTP.
 */
@WebMvcTest(CursoController.class)
class CursoControllerTest {

    // MockMvc simula requisicoes HTTP completas contra o Controller sem precisar abrir o Postman
    @Autowired
    private MockMvc mockMvc;

    // Substitui o bean real do CursoService no contexto web por um mock controlado
    @MockitoBean
    private CursoService service;

    @Test
    void deveRetornarMensagemDeStatusCom200() throws Exception {
        // Arrange: simula a mensagem retornada pelo Service
        when(service.gerarMensagem())
                .thenReturn("CursoService funcionando com injecao de dependencia via construtor!");

        // Act + Assert: envia GET /cursos/mensagem e verifica HTTP 200 OK e corpo de texto
        mockMvc.perform(get("/cursos/mensagem"))
                .andExpect(status().isOk())
                .andExpect(content().string("CursoService funcionando com injecao de dependencia via construtor!"));
    }

    @Test
    void deveListarCursosComStatus200() throws Exception {
        // Arrange: programa o Service mockado para devolver uma lista com um curso
        CursoResponse curso = new CursoResponse(
                1L,
                "Engenharia de Software",
                "Boas praticas e arquitetura back-end",
                160,
                "Presencial",
                new BigDecimal("1499.90")
        );

        when(service.listar()).thenReturn(List.of(curso));

        // Act + Assert: envia GET /cursos e valida status 200 OK e campos do JSON retornado
        mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Engenharia de Software"))
                .andExpect(jsonPath("$[0].cargaHoraria").value(160))
                .andExpect(jsonPath("$[0].modalidade").value("Presencial"))
                .andExpect(jsonPath("$[0].valor").value(1499.90));
    }

    @Test
    void deveBuscarCursoPorIdComStatus200() throws Exception {
        // Arrange: simula retorno valido do Service para o ID 1
        CursoResponse response = new CursoResponse(
                1L,
                "Spring Boot na Pratica",
                "Desenvolvimento de APIs REST com testes e Docker",
                120,
                "EAD",
                new BigDecimal("799.00")
        );

        when(service.buscarPorId(1L)).thenReturn(response);

        // Act + Assert: envia GET /cursos/1 e verifica status 200 e payload JSON
        mockMvc.perform(get("/cursos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Spring Boot na Pratica"))
                .andExpect(jsonPath("$.cargaHoraria").value(120))
                .andExpect(jsonPath("$.modalidade").value("EAD"));
    }

    @Test
    void deveRetornar404QuandoCursoNaoExistir() throws Exception {
        // Arrange: simula o lancamento de CursoNaoEncontradoException pelo Service
        when(service.buscarPorId(999L)).thenThrow(new CursoNaoEncontradoException(999L));

        // Act + Assert: confirma que o GlobalExceptionHandler traduz a excecao para HTTP 404 Not Found
        mockMvc.perform(get("/cursos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Curso nao encontrado com ID: 999"));
    }

    @Test
    void deveCadastrarCursoValidoComStatus201() throws Exception {
        // Arrange: programa o retorno do cadastro para um payload JSON valido
        CursoResponse criado = new CursoResponse(
                1L,
                "Docker e Observabilidade",
                "Empacotamento JAR, containers e Spring Boot Actuator",
                80,
                "Hibrido",
                new BigDecimal("599.50")
        );

        when(service.cadastrar(any(CursoRequest.class))).thenReturn(criado);

        String jsonValido = """
                {
                  "nome": "Docker e Observabilidade",
                  "descricao": "Empacotamento JAR, containers e Spring Boot Actuator",
                  "cargaHoraria": 80,
                  "modalidade": "Hibrido",
                  "valor": 599.50
                }
                """;

        // Act + Assert: envia POST /cursos e verifica status 201 Created e JSON de resposta
        mockMvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Docker e Observabilidade"))
                .andExpect(jsonPath("$.cargaHoraria").value(80));
    }

    @Test
    void deveRetornar400QuandoPayloadDeCadastroForInvalido() throws Exception {
        // Arrange: payload com campos obrigatorios vazios e numeros negativos para acionar @Valid
        String jsonInvalido = """
                {
                  "nome": "",
                  "descricao": "",
                  "cargaHoraria": -10,
                  "modalidade": "",
                  "valor": -50
                }
                """;

        // Act + Assert: valida resposta 400 Bad Request, detalhes por campo e garante que o Service nao foi chamado
        mockMvc.perform(post("/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalhes.nome").exists())
                .andExpect(jsonPath("$.detalhes.cargaHoraria").exists())
                .andExpect(jsonPath("$.detalhes.valor").exists());

        verify(service, never()).cadastrar(any(CursoRequest.class));
    }

    @Test
    void deveAtualizarCursoComStatus200() throws Exception {
        // Arrange: simula retorno atualizado do Service
        CursoResponse atualizado = new CursoResponse(
                1L,
                "Engenharia de Software II",
                "Topicos avancados de testes e containers",
                180,
                "Presencial",
                new BigDecimal("1650.00")
        );

        when(service.atualizar(eq(1L), any(CursoRequest.class))).thenReturn(atualizado);

        String jsonAtualizacao = """
                {
                  "nome": "Engenharia de Software II",
                  "descricao": "Topicos avancados de testes e containers",
                  "cargaHoraria": 180,
                  "modalidade": "Presencial",
                  "valor": 1650.00
                }
                """;

        // Act + Assert: envia PUT /cursos/1 e valida 200 OK com dados atualizados
        mockMvc.perform(put("/cursos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAtualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Engenharia de Software II"))
                .andExpect(jsonPath("$.cargaHoraria").value(180));
    }

    @Test
    void deveRemoverCursoComStatus204() throws Exception {
        // Arrange: simula remocao bem-sucedida sem excecao
        doNothing().when(service).remover(1L);

        // Act + Assert: envia DELETE /cursos/1 e espera status 204 No Content
        mockMvc.perform(delete("/cursos/1"))
                .andExpect(status().isNoContent());

        verify(service).remover(1L);
    }
}
