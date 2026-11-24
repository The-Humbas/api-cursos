# API de Cursos — Spring Boot (Testes Automatizados, JAR, Docker e Actuator)

API REST desenvolvida em **Java 21**, **Spring Boot 3** e **PostgreSQL**, evoluída a partir do projeto base de Cursos da disciplina de **Back-End Frameworks (Ciência da Computação — 4º período — UNINASSAU)** para aplicar na prática os conceitos das **Aulas 10 e 11**:

* **Aula 10:** Testes automatizados de regra de negócio (`CursoServiceTest` com JUnit 5 e Mockito no padrão AAA) e testes da camada HTTP (`CursoControllerTest` com `@WebMvcTest`, `MockMvc` e `@MockitoBean`).
* **Aula 11:** Build com Maven e geração de Fat JAR executável (`mvn clean package` / `./mvnw clean package`), execução fora da IDE (`java -jar`), externalização de credenciais por variáveis de ambiente e Spring Profiles (`dev`/`prod`), containerização com `Dockerfile` (5 diretivas) e `compose.yml` (API + PostgreSQL + volume persistente), logs estruturados com SLF4J e monitoramento de saúde via **Spring Boot Actuator** (`/actuator/health`).

---

## Início Rápido (após `git clone`)

Como `target/` e `.env` não são versionados no Git por boa prática de engenharia e segurança, siga exatamente estes **3 passos** ao clonar o repositório em qualquer máquina:

1. **Criar o arquivo `.env` local a partir do modelo** (e definir `POSTGRES_PASSWORD`):
   * Windows (PowerShell): `Copy-Item .env.example .env`
   * Linux / macOS: `cp .env.example .env`
2. **Gerar o Fat JAR executável** (usa o Maven Wrapper incluso — não exige Maven global instalado):
   * Windows (PowerShell): `.\mvnw.cmd clean package`
   * Linux / macOS: `./mvnw clean package`
3. **Subir API + PostgreSQL com Docker Compose:**
   ```bash
   docker compose up --build
   ```

Após iniciar, acesse `http://localhost:8080/actuator/health` (esperado `{"status":"UP"}`) ou o painel web em `http://localhost:8080/`.

---

## Entidade Principal: `Curso`

Tabela `cursos` no PostgreSQL, contendo identificador (`id`) e **5 atributos**:

| Atributo | Tipo | Restrições / Validação | Exemplo |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | Chave primária autoincremento (`IDENTITY`) | `1` |
| `nome` | `String` | Obrigatório, até 120 caracteres | `"Engenharia de Software"` |
| `descricao` | `String` | Obrigatório, até 500 caracteres | `"Arquitetura, testes e containers em Java"` |
| `cargaHoraria` | `Integer` | Obrigatório, maior que zero (`> 0`) | `160` |
| `modalidade` | `String` | Obrigatório, até 40 caracteres | `"Presencial"`, `"EAD"`, `"Hibrido"` |
| `valor` | `BigDecimal` | Obrigatório, maior ou igual a zero (`>= 0.00`) | `1499.90` |

---

## Endpoints Disponíveis

| Método | Rota | Descrição | Status HTTP |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Painel Web simples (HTML/CSS/JS) para testar CRUD e Actuator no navegador | `200 OK` |
| `GET` | `/cursos/mensagem` | Comprovação de injeção de dependência via construtor | `200 OK` |
| `GET` | `/cursos` | Lista todos os cursos cadastrados | `200 OK` |
| `GET` | `/cursos/{id}` | Busca um curso pelo seu ID | `200 OK` / `404 Not Found` |
| `POST` | `/cursos` | Cadastra um novo curso (com validação `@Valid`) | `201 Created` / `400 Bad Request` |
| `PUT` | `/cursos/{id}` | Atualiza os dados de um curso existente | `200 OK` / `404 Not Found` / `400 Bad Request` |
| `DELETE` | `/cursos/{id}` | Remove um curso existente pelo ID | `204 No Content` / `404 Not Found` |
| `GET` | `/actuator/health` | Verifica a saúde técnica da API e da conexão com o banco | `200 OK` (`UP`) / `503` (`DOWN`) |

### Exemplo de Corpo JSON (Request `POST` e `PUT`)

```json
{
  "nome": "Engenharia de Software",
  "descricao": "Arquitetura, testes automatizados e boas praticas em Java",
  "cargaHoraria": 160,
  "modalidade": "Presencial",
  "valor": 1499.90
}
```

### Exemplo de Resposta JSON (`201 Created` / `200 OK`)

```json
{
  "id": 1,
  "nome": "Engenharia de Software",
  "descricao": "Arquitetura, testes automatizados e boas praticas em Java",
  "cargaHoraria": 160,
  "modalidade": "Presencial",
  "valor": 1499.90
}
```

---

## Arquitetura e Estrutura do Projeto

```text
api-cursos/
├── pom.xml                                   # Dependencias (Web, JPA, Validation, Actuator, PostgreSQL, Test)
├── mvnw / mvnw.cmd / .mvn/                   # Maven Wrapper (permite build sem Maven global instalado)
├── Dockerfile                                # Receita da imagem Docker (5 diretivas, eclipse-temurin:21-jre)
├── .dockerignore                             # Evita enviar .git, .env e lixo no contexto de build Docker
├── compose.yml                               # Orquestracao dos servicos db (PostgreSQL 16) + api + volume
├── .gitignore                                # Ignora target/, .env e arquivos de IDE
├── .gitattributes                            # Garante quebras de linha LF em mvnw, Dockerfile e compose.yml
├── .env.example                              # Modelo de variaveis de ambiente para o Compose (sem senhas reais)
├── README.md                                 # Documentacao completa de execucao e testes
└── src/
    ├── main/
    │   ├── java/br/edu/nassau/apicursos/
    │   │   ├── ApiCursosApplication.java
    │   │   ├── controller/CursoController.java
    │   │   ├── service/CursoService.java
    │   │   ├── repository/CursoRepository.java
    │   │   ├── model/Curso.java
    │   │   ├── dto/
    │   │   │   ├── CursoRequest.java
    │   │   │   └── CursoResponse.java
    │   │   └── exception/
    │   │       ├── CursoNaoEncontradoException.java
    │   │       ├── ErroResposta.java
    │   │       └── GlobalExceptionHandler.java
    │   └── resources/
    │       ├── static/
    │       │   └── index.html                # Painel Web simples (CRUD + status /actuator/health)
    │       ├── application.properties        # Configuracao base com variaveis de ambiente e Actuator
    │       ├── application-dev.properties    # Profile dev (show-sql=true, health details=always)
    │       └── application-prod.properties   # Profile prod (show-sql=false, health details=never)
    └── test/
        └── java/br/edu/nassau/apicursos/
            ├── service/CursoServiceTest.java     # 10 testes unitarios com JUnit 5 + Mockito (AAA)
            └── controller/CursoControllerTest.java # 8 testes web com @WebMvcTest + MockMvc
```

---

## Pré-requisitos

* **Java JDK 21** (ou superior compatível)
* **Maven Wrapper incluso** (`.\mvnw.cmd` no Windows / `./mvnw` no Linux/macOS) ou **Apache Maven 3.9+**
* **Docker e Docker Compose** (Docker Desktop ativo)
* Cliente HTTP (`curl.exe`, PowerShell `Invoke-RestMethod`, Postman ou Insomnia)

---

## Variáveis de Ambiente

Nenhuma senha ou URL fixa está gravada em `application.properties`, no `Dockerfile` ou no `compose.yml`.

### 1. Para execução com Docker Compose (arquivo `.env`, baseado em `.env.example`)

| Variável | Obrigatória | Padrão | Descrição |
| :--- | :---: | :--- | :--- |
| `POSTGRES_PASSWORD` | **Sim** | *(sem padrão)* | Senha do banco usada pelo serviço `db` e repassada como `DB_PASSWORD` para a `api`. |
| `POSTGRES_DB` | Não | `api_cursos` | Nome do banco PostgreSQL criado no container `db`. |
| `POSTGRES_USER` | Não | `postgres` | Usuário do PostgreSQL (repassado como `DB_USER` para a `api`). |
| `POSTGRES_PORT` | Não | `5432` | Porta do PostgreSQL exposta no host (altere para ex. `5433` se a `5432` estiver ocupada). |
| `API_PORT` | Não | `8080` | Porta HTTP da API exposta no host. |
| `SPRING_PROFILES_ACTIVE` | Não | `prod` | Perfil ativo no container (`prod` mantém `show-sql=false` e `/actuator/health` enxuto). |

### 2. Para execução direta do Fat JAR fora da IDE (`java -jar`)

| Variável | Obrigatória | Descrição | Exemplo |
| :--- | :---: | :--- | :--- |
| `DB_URL` | **Sim** | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/api_cursos` |
| `DB_USER` | **Sim** | Usuário do PostgreSQL | `postgres` |
| `DB_PASSWORD` | **Sim** | Senha do PostgreSQL | *(sua senha local)* |
| `SPRING_PROFILES_ACTIVE` | Não | Perfil Spring (`dev` ou `prod`) | `dev` |
| `SERVER_PORT` | Não | Porta HTTP da API (padrão `8080`) | `8080` |

> **Atenção:** Dentro do container da API no Docker Compose, o `compose.yml` monta automaticamente `DB_URL=jdbc:postgresql://db:5432/...` usando o nome do serviço **`db`**, **nunca `localhost`**.

---

## Passo a Passo de Execução

### 1. Executar os testes automatizados (Aula 10)

Os 18 testes rodam de forma isolada, sem precisar de banco de dados ou servidor web no ar:

```bash
# Windows (PowerShell)        # Linux / macOS        # Ou com Maven global
.\mvnw.cmd test               ./mvnw test            mvn test
```

### 2. Realizar o build e gerar o arquivo JAR executável (Aula 11)

Limpa builds antigos (`target/`), compila o projeto, executa toda a suíte de testes e gera o Fat JAR em `target/api-cursos-1.0.0.jar`:

```bash
# Windows (PowerShell)        # Linux / macOS        # Ou com Maven global
.\mvnw.cmd clean package      ./mvnw clean package   mvn clean package
```

### 3. Executar o JAR fora da IDE (Terminal)

Certifique-se de que um PostgreSQL esteja rodando com o banco `api_cursos` criado (por exemplo, mantendo apenas o container `db` ativo com `docker compose up -d db`), defina as variáveis de ambiente e execute o JAR diretamente (com a IDE fechada):

**Windows (PowerShell):**
```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_URL="jdbc:postgresql://localhost:5432/api_cursos"
$env:DB_USER="postgres"
$env:DB_PASSWORD="<sua_senha_do_env>"

java -jar target/api-cursos-1.0.0.jar
```

**Linux / macOS (Bash):**
```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/api_cursos
export DB_USER=postgres
export DB_PASSWORD="<sua_senha_do_env>"

java -jar target/api-cursos-1.0.0.jar
```

### 4. Executar a API + PostgreSQL com Docker Compose (Recomendado)

1. Copie `.env.example` para `.env` e defina `POSTGRES_PASSWORD`.
2. Gere o `.jar` atualizado (`.\mvnw.cmd clean package` ou `./mvnw clean package`).
3. Suba os containers:

```bash
docker compose up --build
```

Para verificar o status dos containers ativos em outro terminal:

```bash
docker compose ps
```

Para acompanhar os logs estruturados (SLF4J) da API:

```bash
docker compose logs -f api
```

Para encerrar e remover os containers mantendo os dados salvos no volume `postgres_data`:

```bash
docker compose down
```

> **Cuidado:** Não utilize `docker compose down -v` se quiser preservar os dados do banco, pois a flag `-v` remove também os volumes persistentes.

---

## Verificação de Saúde (Spring Boot Actuator) e Testes Manuais

### Verificar `/actuator/health`

**Windows (PowerShell):** *(use `curl.exe` para evitar o alias `Invoke-WebRequest` do PowerShell)*
```powershell
curl.exe -i http://localhost:8080/actuator/health
```

**Linux / macOS (Bash):**
```bash
curl -i http://localhost:8080/actuator/health
```

Resposta esperada (HTTP `200 OK`):

```json
{"status":"UP"}
```

### Testar CRUD e Persistência do Volume (PowerShell)

```powershell
# 1. Cadastrar um curso (POST /cursos -> 201 Created)
$body = [System.Text.Encoding]::UTF8.GetBytes('{"nome":"Engenharia de Software","descricao":"Testes, Docker e Actuator","cargaHoraria":160,"modalidade":"Presencial","valor":1499.90}')
Invoke-RestMethod -Method POST -Uri http://localhost:8080/cursos -ContentType "application/json; charset=utf-8" -Body $body

# 2. Listar cursos cadastrados (GET /cursos -> 200 OK)
Invoke-RestMethod -Uri http://localhost:8080/cursos

# 3. Buscar por ID existente (GET /cursos/1 -> 200 OK)
Invoke-RestMethod -Uri http://localhost:8080/cursos/1

# 4. Atualizar curso (PUT /cursos/1 -> 200 OK)
$update = [System.Text.Encoding]::UTF8.GetBytes('{"nome":"Engenharia de Software Avancada","descricao":"Arquitetura e Observabilidade","cargaHoraria":180,"modalidade":"Hibrido","valor":1699.90}')
Invoke-RestMethod -Method PUT -Uri http://localhost:8080/cursos/1 -ContentType "application/json; charset=utf-8" -Body $update

# 5. Validar persistencia no volume Docker:
#    Rode "docker compose down", depois "docker compose up -d", e execute novamente:
Invoke-RestMethod -Uri http://localhost:8080/cursos

# 6. Remover curso (DELETE /cursos/1 -> 204 No Content)
Invoke-RestMethod -Method DELETE -Uri http://localhost:8080/cursos/1
```

---

## Guia de Comandos: Como Escrever e Utilizar Cada Comando

Esta seção explica **para que serve cada comando**, **o significado de cada parte/parâmetro** e **como escrevê-lo corretamente** no terminal (especialmente no **Windows PowerShell**), evitando erros comuns.

> **Importante — Por que não se digita `POST /cursos` diretamente no terminal?**  
> `GET`, `POST`, `PUT` e `DELETE` são **métodos do protocolo HTTP**, não programas do Windows ou do PowerShell. Para enviar requisições HTTP pelo terminal, é obrigatório usar um cliente HTTP como **`Invoke-RestMethod`** (nativo do PowerShell) ou **`curl.exe`** (sempre com `.exe` no Windows PowerShell, pois `curl` sem `.exe` aciona o `Invoke-WebRequest`, que gera aviso de segurança e trunca o JSON do Actuator).

### 1. Comandos de Testes, Build e Execução do JAR (Maven e Java)

| Comando | Como escrever (anatomia) | Quando e como utilizar |
| :--- | :--- | :--- |
| `mvn test` | `mvn` (Maven) + `test` (fase de testes) | Execute na raiz do projeto (`api-cursos/`) para rodar todos os testes unitários e de controller sem precisar de banco de dados. |
| `mvn clean package` | `mvn` + `clean` (apaga `target/`) + `package` (compila, testa e empacota) | Execute sempre que alterar o código Java antes de rodar o JAR ou reconstruir a imagem Docker. Gera `target/api-cursos-1.0.0.jar`. |
| `$env:NOME="valor"` | `$env:` + nome exato da variável + `=` + valor entre aspas (sem espaços ao redor do `=`) | No **PowerShell**, define uma variável de ambiente válida para a sessão atual do terminal antes de rodar `java -jar`. No **Linux/macOS**, escreve-se `export NOME=valor`. |
| `java -jar target/api-cursos-1.0.0.jar` | `java` (JVM) + `-jar` (executa pacote JAR) + caminho relativo do arquivo gerado | Executa a API diretamente pelo terminal fora da IDE. Requer que `DB_URL`, `DB_USER` e `DB_PASSWORD` já tenham sido definidas e que o PostgreSQL esteja acessível. |

### 2. Comandos Docker e Docker Compose

| Comando | Como escrever (anatomia) | Quando e como utilizar |
| :--- | :--- | :--- |
| `docker build -t api-cursos .` | `docker build` + `-t <nome-da-imagem>` + `.` (diretório atual com o `Dockerfile`) | Constrói manualmente apenas a imagem da API. **Atenção:** o ponto final (`.`) é obrigatório e o `.jar` em `target/` já deve ter sido gerado com `mvn clean package`. |
| `docker run -p 8080:8080 api-cursos` | `docker run` + `-p <porta_maquina>:<porta_container>` + `<nome-da-imagem>` | Sobe um container avulso a partir da imagem criada, expondo a porta `8080` na sua máquina (para conectar ao banco, prefira o Docker Compose). |
| `docker compose up --build` | `docker compose up` + `--build` (reconstrói a imagem da API antes de subir) | Sobe **PostgreSQL + API** integrados. Use na primeira execução ou após alterar o código/JAR. Para liberar o terminal (rodar em segundo plano), adicione `-d`: `docker compose up --build -d`. |
| `docker compose up -d` | `docker compose up` + `-d` (*detached* / segundo plano) | Sobe novamente os containers já construídos em segundo plano (ideal para o teste de persistência após `docker compose down`). |
| `docker compose ps` | `docker compose` + `ps` (*process status*) | Mostra os containers do projeto em execução, portas mapeadas e status (`Up` / `healthy`). |
| `docker compose logs api` | `docker compose logs` + `<nome_do_servico>` (`api` ou `db`) | Exibe os logs estruturados (SLF4J) gerados pela aplicação dentro do container. Adicione `-f` (`docker compose logs -f api`) para acompanhar em tempo real (`Ctrl+C` encerra a visualização sem derrubar o container). |
| `docker compose down` | `docker compose` + `down` | Para e remove os containers e a rede mantendo os dados salvos no volume `postgres_data`. **Não adicione `-v`** se quiser manter os registros do banco. |

### 3. Comandos de Requisição HTTP no Windows PowerShell

#### Estrutura do `Invoke-RestMethod` (PowerShell)

```powershell
Invoke-RestMethod -Method <VERBO_HTTP> -Uri <URL_COMPLETA> -ContentType "application/json; charset=utf-8" -Body $body
```

* **`-Method`**: informa o verbo HTTP (`GET`, `POST`, `PUT` ou `DELETE`). Quando omitido, o padrão é `GET`.
* **`-Uri`**: endereço completo do endpoint começando com `http://localhost:8080` seguido da rota (ex.: `/cursos` ou `/cursos/1`).
* **`-ContentType`**: obrigatório no `POST` e `PUT`; avisa o Spring Boot que o corpo está em JSON com codificação UTF-8.
* **`-Body`**: envia o JSON do curso. No Windows PowerShell 5.1, recomenda-se envolver o texto JSON com `[System.Text.Encoding]::UTF8.GetBytes('...')` para evitar problemas com acentos.

#### Estrutura do `curl.exe` (Windows PowerShell — alternativo e ideal para ver o Status HTTP)

```powershell
curl.exe -i -X <VERBO_HTTP> <URL_COMPLETA> -H "Content-Type: application/json" -d "<JSON>"
```

* **`curl.exe`**: chama o binário real do `curl` no Windows (evita o alias `Invoke-WebRequest` do PowerShell).
* **`-i`**: inclui o cabeçalho e o código de status HTTP (`HTTP/1.1 200`, `201`, `404`, `400`) antes da resposta JSON — excelente para prints de validação (`/actuator/health` e erros `404`).
* **`-X`**: define o verbo HTTP (`POST`, `PUT`, `DELETE`). Para `GET`, não é necessário informar `-X`.
* **`-H`**: define um cabeçalho HTTP (no `POST`/`PUT`, sempre `"Content-Type: application/json"`).
* **`-d`**: envia o corpo JSON da requisição.

#### Regras obrigatórias para escrever o JSON de `Curso`

1. Todas as **chaves** e valores de texto (`nome`, `descricao`, `modalidade`) devem estar entre **aspas duplas** (`"..."`).
2. Valores numéricos (`cargaHoraria` e `valor`) vão **sem aspas** e usam **ponto (`.`)** como separador decimal (ex.: `1250.00`, nunca `1250,00`).
3. Os **5 atributos** são obrigatórios no `POST` e no `PUT` (`nome`, `descricao`, `cargaHoraria`, `modalidade`, `valor`). Enviar qualquer campo vazio ou número negativo retorna `400 Bad Request` com o detalhamento dos campos inválidos.

#### Exemplos prontos (como escrever cada operação)

```powershell
# Verificar saude da API (HTTP 200 + {"status":"UP"})
curl.exe -i http://localhost:8080/actuator/health

# Listar todos os cursos (GET /cursos)
Invoke-RestMethod -Uri http://localhost:8080/cursos

# Cadastrar novo curso (POST /cursos -> HTTP 201)
$body = [System.Text.Encoding]::UTF8.GetBytes('{"nome":"Enfermagem","descricao":"Graduacao em Enfermagem com pratica clinica","cargaHoraria":4000,"modalidade":"Presencial","valor":1250.00}')
Invoke-RestMethod -Method POST -Uri http://localhost:8080/cursos -ContentType "application/json; charset=utf-8" -Body $body

# Buscar curso por ID existente (GET /cursos/1 -> HTTP 200)
Invoke-RestMethod -Uri http://localhost:8080/cursos/1

# Buscar curso por ID inexistente (GET /cursos/999 -> HTTP 404 Not Found)
curl.exe -i http://localhost:8080/cursos/999

# Atualizar curso existente (PUT /cursos/1 -> HTTP 200)
$update = [System.Text.Encoding]::UTF8.GetBytes('{"nome":"Enfermagem","descricao":"Graduacao atualizada com estagio supervisionado","cargaHoraria":4200,"modalidade":"Presencial","valor":1320.00}')
Invoke-RestMethod -Method PUT -Uri http://localhost:8080/cursos/1 -ContentType "application/json; charset=utf-8" -Body $update

# Excluir curso existente (DELETE /cursos/1 -> HTTP 204 No Content)
Invoke-RestMethod -Method DELETE -Uri http://localhost:8080/cursos/1
```
