# ==============================================================================
# Dockerfile - API REST de Cursos (Aula 11)
# Observacao de seguranca: nenhuma senha ou segredo e definido neste arquivo,
# pois todas as instrucoes ficam gravadas nas camadas da imagem Docker.
# ==============================================================================

# 1. FROM: Define a imagem base oficial contendo apenas o JRE do Java 21 (mesma versao do pom.xml).
#    Usar -jre em vez de -jdk reduz o tamanho final da imagem e a superficie de ataque.
FROM eclipse-temurin:21-jre

# 2. WORKDIR: Define /app como o diretorio de trabalho padrao dentro do container.
#    Todas as instrucoes seguintes (COPY e ENTRYPOINT) serao executadas a partir de /app.
WORKDIR /app

# 3. COPY: Copia o Fat JAR executavel gerado pelo "mvn clean package" em target/
#    para dentro da imagem com o nome padronizado app.jar.
#    Importante: o nome na origem deve corresponder exatamente ao artefato em target/.
COPY target/api-cursos-1.0.0.jar app.jar

# 4. EXPOSE: Documenta que a aplicacao Spring Boot escuta na porta 8080 dentro do container.
#    (A publicacao real da porta para a maquina host ocorre via "-p 8080:8080" ou "ports" no compose.yml.)
EXPOSE 8080

# 5. ENTRYPOINT: Define o comando principal executado automaticamente ao iniciar o container.
#    O formato JSON (exec form) executa a JVM diretamente como processo principal (PID 1).
ENTRYPOINT ["java", "-jar", "app.jar"]
