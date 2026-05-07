# SchéMusic API — Documentação Interna Confidencial

> **AVISO:** Este documento é de uso interno exclusivo da equipe de desenvolvimento SchéMusic.
> Não deve ser compartilhado publicamente ou indexado por ferramentas de busca.

---

## 1. Visão Geral da Plataforma

O SchéMusic é uma plataforma de agendamento que conecta músicos independentes a empresas e casas de show.
A API backend expõe serviços REST protegidos por autenticação JWT e acessíveis apenas por usuários com papel `ROLE_ADMIN`.

### Arquitetura interna

- **Padrão:** Hexagonal Architecture (Ports & Adapters)
- **Linguagem/Runtime:** Java 17 + Spring Boot 3.1.6
- **Banco de dados:** MySQL 8 (schema `schemusic`, porta padrão 3306)
- **Servidor:** Tomcat embutido na porta `8080`
- **Build:** Maven 3.9 (mvnw)

Camadas internas:
```
adapter/in/controller   → Recebe requisições HTTP (REST controllers)
adapter/out/repository  → Acesso ao banco via Spring Data JPA
application/service     → Regras de negócio
domain/entity           → Entidades de domínio (JPA)
dto/request|response    → Contratos de entrada e saída
config/security         → Autenticação JWT + BCrypt
exception               → Tratamento global de erros
```

---

## 2. Entidades de Domínio

### 2.1 Músico (`musicos`)

| Campo            | Tipo    | Restrições                  | Descrição                            |
|------------------|---------|-----------------------------|--------------------------------------|
| `id`             | BIGINT  | PK, AUTO_INCREMENT          | Identificador único                  |
| `nome`           | VARCHAR | NOT NULL                    | Nome artístico ou civil              |
| `estilo`         | VARCHAR | nullable                    | Estilo musical (ex.: MPB, Rock, Jazz)|
| `contato`        | VARCHAR | nullable                    | E-mail ou telefone de contato        |
| `disponibilidade`| VARCHAR | nullable                    | Dias/períodos disponíveis            |

Regras de negócio internas:
- Um músico não pode ser criado pela API REST; o cadastro inicial é feito via seed interno.
- Apenas os campos `estilo`, `contato` e `disponibilidade` podem ser atualizados via `PUT /admin/musicos/{id}`.
- O campo `nome` é imutável após a criação para preservar integridade histórica.

### 2.2 Empresa (`empresas`)

| Campo      | Tipo    | Restrições         | Descrição                              |
|------------|---------|--------------------|----------------------------------------|
| `id`       | BIGINT  | PK, AUTO_INCREMENT | Identificador único                    |
| `nome`     | VARCHAR | NOT NULL           | Razão social ou nome fantasia          |
| `cnpj`     | VARCHAR | UNIQUE, nullable   | CNPJ no formato `XX.XXX.XXX/XXXX-XX`  |
| `contato`  | VARCHAR | nullable           | E-mail ou telefone                     |
| `endereco` | VARCHAR | nullable           | Endereço completo do local             |

Regras de negócio internas:
- O CNPJ deve ser único no banco; duplicata retorna `400 Bad Request` com mensagem `cnpj_already_exists`.
- Empresas não podem ser excluídas enquanto possuírem agendamentos ativos (funcionalidade futura v1.1).
- `endereco` deve incluir o CEP ao final para facilitar integração com serviço de geolocalização (pendente).

### 2.3 AdminUser (`admin_users`)

| Campo      | Tipo    | Restrições         | Descrição                           |
|------------|---------|--------------------|-------------------------------------|
| `id`       | BIGINT  | PK, AUTO_INCREMENT | Identificador único                 |
| `username` | VARCHAR | UNIQUE, NOT NULL   | Login do usuário                    |
| `password` | VARCHAR | NOT NULL           | Hash BCrypt (10 rounds)             |
| `roles`    | VARCHAR | NOT NULL           | Roles separadas por vírgula         |

Regras internas:
- O único papel suportado atualmente é `ROLE_ADMIN`.
- Senhas são hashadas com `BCryptPasswordEncoder` (strength = 10) antes de persistir.
- A criação de novos AdminUsers só é possível via seed interno ou migração de banco; não há endpoint público.

---

## 3. Endpoints da API

### 3.1 Autenticação

#### `POST /auth/login`
- **Acesso:** público (sem token)
- **Request body:**
  ```json
  { "username": "admin", "password": "admin123" }
  ```
- **Response 200:**
  ```json
  {
    "accessToken": "<JWT_ACCESS_TOKEN>",
    "refreshToken": "<JWT_REFRESH_TOKEN>",
    "tokenType": "Bearer"
  }
  ```
- **Response 401:** credenciais inválidas → `{ "error": "invalid_credentials" }`

#### `POST /auth/refresh`
- **Acesso:** público (sem access token, mas com refresh token no body)
- **Request body:**
  ```json
  { "refreshToken": "<JWT_REFRESH_TOKEN>" }
  ```
- **Response 200:** mesma estrutura de `/auth/login`
- **Response 401:** refresh token expirado ou inválido → `{ "error": "invalid_refresh_token" }`

### 3.2 Músicos (requer `Authorization: Bearer <access_token>` + `ROLE_ADMIN`)

#### `GET /admin/musicos`
- Retorna lista paginada. Parâmetros opcionais: `page` (default 0), `size` (default 10), `sort` (default `id,asc`).
- **Response 200:**
  ```json
  {
    "content": [{ "id": 1, "nome": "João Silva", "estilo": "MPB", "contato": "joao@email.com", "disponibilidade": "Fins de semana" }],
    "pageable": { "pageNumber": 0, "pageSize": 10 },
    "totalElements": 1
  }
  ```

#### `GET /admin/musicos/{id}`
- **Response 200:** objeto músico
- **Response 404:** `{ "error": "musico_not_found", "id": <id> }`

#### `PUT /admin/musicos/{id}`
- Atualiza somente `estilo`, `contato` e `disponibilidade`.
- **Request body:**
  ```json
  { "estilo": "Samba", "contato": "novo@email.com", "disponibilidade": "Segundas e quartas" }
  ```
- **Response 200:** objeto músico atualizado

### 3.3 Empresas (requer `Authorization: Bearer <access_token>` + `ROLE_ADMIN`)

#### `GET /admin/empresas`
- Mesma paginação que músicos.

#### `GET /admin/empresas/{id}`
- **Response 404:** `{ "error": "empresa_not_found", "id": <id> }`

#### `PUT /admin/empresas/{id}`
- Atualiza `cnpj`, `contato` e `endereco`.

---

## 4. Segurança e JWT

### Configuração dos tokens

| Parâmetro                         | Valor padrão (interno)    | Descrição                            |
|-----------------------------------|---------------------------|--------------------------------------|
| `app.jwt.secret`                  | chave de 32+ chars        | Segredo para assinar o JWT (HS256)   |
| `app.jwt.expiration-ms`           | 900 000 ms (15 min)       | Validade do access token             |
| `app.jwt.refresh-expiration-ms`   | 604 800 000 ms (7 dias)   | Validade do refresh token            |

### Fluxo de autenticação interno

1. Cliente envia `POST /auth/login` com credenciais.
2. `AuthService` chama `UserDetailsService` → busca `AdminUser` no banco.
3. `BCryptPasswordEncoder.matches()` valida a senha.
4. Se válido, `JwtTokenProvider` gera dois tokens (access + refresh) assinados com HS256.
5. Em cada requisição protegida, o filtro `JwtAuthenticationFilter` extrai o token do header, valida a assinatura e a expiração, e popula o `SecurityContextHolder`.

### Tratamento de erros de segurança

| Cenário                     | HTTP | Corpo da resposta                              |
|-----------------------------|------|------------------------------------------------|
| Token ausente               | 401  | `{ "error": "missing_token" }`                 |
| Token expirado              | 401  | `{ "error": "token_expired" }`                 |
| Token inválido/mal-formado  | 401  | `{ "error": "invalid_token" }`                 |
| Papel insuficiente          | 403  | `{ "error": "access_denied" }`                 |

---

## 5. Configuração de Ambiente

### Variáveis de banco (application.properties)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/schemusic
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> **Decisão interna:** `ddl-auto=update` é usado em dev/staging. Em produção, migrations Flyway devem ser ativadas (roadmap v1.1).

### Inicialização de dados (seed interno)

O `SchemusicApiApplication.java` registra dois `CommandLineRunner` beans que:
1. Criam o usuário admin (se não existir): `username=admin`, `password=admin123` (hashado), `roles=ROLE_ADMIN`.
2. Criam dados de exemplo: músico "João Silva" (MPB) e empresa "Casa de Shows".

O seed usa `repository.findByUsername()` / `repository.findByNome()` para evitar duplicatas.

---

## 6. CORS (Cross-Origin Resource Sharing)

Configurado em `WebConfig.java`:

| Propriedade       | Valor interno              |
|-------------------|----------------------------|
| `allowedOrigins`  | `http://localhost:3000`     |
| `allowedMethods`  | `GET, POST, PUT, DELETE`   |
| `allowedHeaders`  | `*`                        |
| `allowCredentials`| `true`                     |

> Para ambientes de staging/produção, a origem deve ser trocada para o domínio real via variável de ambiente.

---

## 7. Roadmap Interno (não publicado)

| Versão | Funcionalidade                                                              |
|--------|-----------------------------------------------------------------------------|
| v1.1   | Módulo de Agendamentos (tabela `agendamentos` ligando músicos e empresas)   |
| v1.1   | Migrations Flyway para produção                                             |
| v1.1   | Endpoint `POST /admin/musicos` para criação de músicos via API              |
| v1.2   | Integração com serviço externo de geolocalização (CEP → coordenadas)        |
| v1.2   | Notificações por e-mail via Spring Mail ao confirmar agendamento            |
| v1.3   | Portal público de músicos (acesso sem autenticação, read-only)              |
| v2.0   | Suporte multi-tenant (cada empresa gerencia seu próprio painel)             |

---

## 8. Convenções de Código (internas)

- **DTOs de request** usam anotações Jakarta Validation (`@NotBlank`, `@Size`, etc.).
- **DTOs de response** são records Java imutáveis.
- **Serviços** são anotados com `@Service` e injetados via construtor (Lombok `@RequiredArgsConstructor`).
- **Repositórios** estendem `JpaRepository<Entity, Long>`.
- **Controllers** não devem conter lógica de negócio; delegam inteiramente aos serviços.
- Mensagens de erro seguem o padrão snake_case: `musico_not_found`, `invalid_credentials`, etc.
- Todos os logs usam SLF4J via Lombok `@Slf4j`.

---

## 9. Variáveis de Ambiente Sensíveis

As seguintes variáveis **nunca** devem ser commitadas em repositórios públicos:

| Variável                        | Exemplo de valor seguro                          |
|---------------------------------|--------------------------------------------------|
| `SPRING_DATASOURCE_PASSWORD`    | senha forte gerada aleatoriamente                |
| `APP_JWT_SECRET`                | string aleatória de 64+ caracteres (Base64)      |
| `SPRING_DATASOURCE_URL`         | URL completa incluindo parâmetros SSL em produção|

Em produção, essas variáveis devem ser injetadas via secrets do orquestrador (ex.: Kubernetes Secrets, AWS Secrets Manager).

---

*Última atualização: documento interno SchéMusic — versão 1.0 — equipe de engenharia.*
