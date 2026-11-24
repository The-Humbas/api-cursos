package br.edu.nassau.apicursos.repository;

import br.edu.nassau.apicursos.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Camada de acesso a dados (Spring Data JPA) para a entidade Curso.
 * Em testes unitarios do CursoService, esta interface e simulada com @Mock pelo Mockito
 * para isolar a regra de negocio do banco PostgreSQL.
 */
@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
}
