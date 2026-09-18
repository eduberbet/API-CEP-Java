# 🔍 API de Consulta de CEP com Design Patterns (Spring Boot)

Esta é uma API REST desenvolvida em **Java 17** e **Spring Boot** em um **arquivo único de código (`Application.java`)**, focada em demonstrar a aplicação prática de Padrões de Projeto (*Design Patterns*) de forma limpa e direta.

---

## 🎯 Padrões de Projeto Aplicados

1. **Singleton**:
   - Gerenciado nativamente pelo Container IoC do Spring Framework através dos Beans `@Service` e `@RestController`, garantindo uma única instância dos componentes durante o ciclo de vida da aplicação.

2. **Strategy**:
   - A interface `CepStrategy` estabelece o contrato para diferentes estratégias de busca de endereço. Foram implementadas duas abordagens intercambiáveis:
     - `FeignCepStrategy`: Consumo declarativo via **Spring Cloud OpenFeign**.
     - `NativeCepStrategy`: Consumo manual usando o cliente nativo `HttpClient` do Java 11+.

3. **Facade**:
   - A classe `CepFacadeService` centraliza a lógica de validação do CEP e seleção da estratégia de consulta, oferecendo uma interface simplificada e limpa para a camada de controle (`CepController`).

---

## 🛠️ Requisitos e Configuração

### `pom.xml` mínimo necessário

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.desafio</groupId>
    <artifactId>design-patterns-cep</artifactId>
    <version>1.0.0</version>
    <name>design-patterns-cep</name>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 🚀 Como Executar

1. Clone o repositório em sua máquina:
   ```bash
   git clone https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
   ```
2. Execute o projeto usando o Maven:
   ```bash
   mvn spring-boot:run
   ```

---

## 📬 Como Usar a API

### Endpoint
`GET /api/cep/{cep}?provider={feign|native}`

### Exemplos de Requisição

- **Consulta padrão (usando OpenFeign):**
  ```http
  GET http://localhost:8080/api/cep/01001000
  ```

- **Consulta usando estratégia Nativa (HttpClient):**
  ```http
  GET http://localhost:8080/api/cep/01001000?provider=native
  ```

### Resposta (JSON)
```json
{
  "cep": "01001000",
  "logradouro": "Praça da Sé",
  "complemento": "lado ímpar",
  "bairro": "Sé",
  "localidade": "São Paulo",
  "uf": "SP"
}
```