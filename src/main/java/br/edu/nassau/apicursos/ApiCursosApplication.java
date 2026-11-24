package br.edu.nassau.apicursos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da API REST de Cursos.
 * A anotacao @SpringBootApplication habilita a autoconfiguracao do Spring Boot,
 * a varredura de componentes e permite empacotar a aplicacao como um Fat JAR executavel.
 */
@SpringBootApplication
public class ApiCursosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiCursosApplication.class, args);
    }
}
