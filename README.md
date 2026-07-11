# mec-backend

Backend da oficina mecânica **Vortex**, desenvolvido com [Quarkus](https://quarkus.io/). O sistema gerencia clientes, veículos, ordens de serviço, usuários e autenticação JWT com controle de acesso por perfil.

## Stack

- **Java 25** + **Quarkus 3.37**
- **PostgreSQL** (schema gerenciado fora deste repositório)
- **Hibernate ORM** (entidades JPA puras, sem Panache)
- **SQL nativo** nos repositórios
- **JWT** (SmallRye JWT) + **refresh token** com rotação
- **Valkey** (sessões ativas de login/logout)
- **OpenCEP** (consulta de endereço por CEP)
- **Bean Validation**
- **OpenAPI / Swagger** (perfil `dev`)

## Arquitetura

O projeto segue **Package by Feature**, com interfaces para `Service` e `Repository` (SOLID):

```
src/main/java/com/vortex/
├── auth/           # Login, refresh, primeiro acesso, sessão
├── cliente/        # CRUD de clientes
├── usuario/        # CRUD de usuários e listagem de técnicos
├── veiculo/        # CRUD de veículos por cliente
├── ordemservico/   # CRUD de ordens de serviço, peças, serviços e histórico de status
├── cep/            # Consulta de CEP via OpenCEP
├── localidade/     # Municípios por UF
├── endereco/       # Entidade de endereço (usada por cliente)
└── shared/         # Exceções, mappers, rate limit e utilitários globais
```

Cada feature contém:

```
entity/  repository/  service/  dto/  resource/
           └── impl/      └── impl/
```

O schema do banco **não é versionado neste repositório** (sem Flyway no app, sem geração automática pelo ORM). Migrations e provisionamento do PostgreSQL ficam em uma esteira separada.

## Infraestrutura

Arquivos de infraestrutura local (`docker-compose.yml`, `docker/`, `Dockerfile`) estão no `.gitignore` e **não fazem parte do repositório**. Para desenvolvimento local, você precisa de:

| Serviço    | Porta padrão | Uso                          |
|------------|--------------|------------------------------|
| PostgreSQL | `5432`       | Banco `mec`                  |
| Valkey     | `6379`       | Sessões JWT ativas           |

Configure a conexão via variáveis de ambiente ou `application.properties` (veja [Configuração](#configuração)).

## Pré-requisitos

- JDK 25+
- Maven (ou use o wrapper `./mvnw`)
- PostgreSQL e Valkey acessíveis (local ou remoto)
- GraalVM 25 + `native-image` (apenas para build nativo)

## Como rodar

### 1. Gerar chaves JWT (primeira vez)

```bash
./scripts/gerar-chaves-jwt.sh
```

A chave privada fica em `src/main/resources/keys/privateKey.pem` (ignorada pelo Git).

### 2. Configurar banco e Valkey

Garanta que o PostgreSQL tenha o schema aplicado pela esteira de banco e que o Valkey esteja acessível. Valores padrão de desenvolvimento:

| Parâmetro | Valor      |
|-----------|------------|
| Banco     | `mec`      |
| Usuário   | `postgres` |
| Senha     | `postgres` |
| JDBC URL  | `jdbc:postgresql://localhost:5432/mec` |
| Valkey    | `redis://localhost:6379` |

### 3. Iniciar a aplicação

```bash
./mvnw quarkus:dev
```

API disponível em: http://localhost:8080

### Swagger (somente `dev`)

- UI: http://localhost:8080/q/swagger-ui
- OpenAPI: http://localhost:8080/q/openapi

## Autenticação

### Tempos de validade

| Token          | Validade   | Configuração                          |
|----------------|------------|---------------------------------------|
| Access token   | 15 minutos | `vortex.jwt.access-token.lifespan`    |
| Refresh token  | 7 dias     | `vortex.jwt.refresh-token.lifespan`   |

### Fluxo de sessão (Valkey)

- **Login / refresh:** registra access token (`jti`) e refresh token no Valkey com TTL
- **Requisições autenticadas:** valida se a sessão ainda está ativa no Valkey
- **Logout:** remove a sessão do Valkey e revoga o refresh token (opcional no body)

### Primeiro acesso

Usuários criados pelo admin podem definir a senha no primeiro login:

1. `POST /api/auth/verificar-primeiro-acesso` — verifica se o e-mail está habilitado
2. `POST /api/auth/primeiro-acesso` — define a senha e retorna tokens

### Exemplo de login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@vortex.com","senha":"admin123"}'
```

Resposta:

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "a1b2c3...",
  "tipo": "Bearer",
  "accessTokenExpiraEmSegundos": 900,
  "refreshTokenExpiraEmSegundos": 604800
}
```

```bash
# Renovar tokens (rotação — o refresh token antigo é invalidado)
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh_token>"}'

# Usuário autenticado
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <access_token>"
```

Envie o `accessToken` no header `Authorization: Bearer <token>` em todas as rotas protegidas.

> Falhas de login retornam sempre **"Credenciais inválidas"** (anti-enumeração).

## Ordens de serviço

### Status

| Status             | Descrição                              |
|--------------------|----------------------------------------|
| `ORCAMENTO`        | Status inicial ao criar a OS           |
| `APROVADO`         | Orçamento aprovado pelo cliente        |
| `EM_EXECUCAO`      | Serviço em andamento                   |
| `AGUARDANDO_PECAS` | Aguardando peças                       |
| `CONCLUIDO`        | Serviço finalizado                     |
| `CANCELADO`        | OS cancelada                           |

Toda OS é criada com status `ORCAMENTO`. A atualização aceita `status` opcional (perfis `ADMIN` e `TECNICO`). O histórico de mudanças de status fica disponível em `/historico-status`.

### Acesso do cliente

Usuários com perfil `CLIENTE` (vinculados a um `Cliente`) acessam suas OS via `/api/auth/me/ordens-servico` e podem **aprovar** ou **rejeitar** orçamentos em status `ORCAMENTO`.

## Endpoints

### Auth

| Método | Rota                                              | Acesso    | Descrição                        |
|--------|---------------------------------------------------|-----------|----------------------------------|
| POST   | `/api/auth/login`                                 | Público   | Autenticar                       |
| POST   | `/api/auth/refresh`                               | Público   | Renovar tokens                   |
| POST   | `/api/auth/verificar-primeiro-acesso`             | Público   | Verificar elegibilidade          |
| POST   | `/api/auth/primeiro-acesso`                       | Público   | Definir senha no primeiro acesso |
| POST   | `/api/auth/logout`                                | Autenticado | Encerrar sessão                |
| GET    | `/api/auth/me`                                    | Autenticado | Dados do usuário logado        |
| PUT    | `/api/auth/me`                                    | Autenticado | Atualizar perfil               |
| PUT    | `/api/auth/me/senha`                              | Autenticado | Alterar senha                  |
| GET    | `/api/auth/me/ordens-servico`                     | Autenticado | Listar OS do cliente (paginado)|
| GET    | `/api/auth/me/ordens-servico/{id}`                | Autenticado | Buscar OS do cliente           |
| POST   | `/api/auth/me/ordens-servico/{id}/aprovar`        | Autenticado | Aprovar orçamento              |
| POST   | `/api/auth/me/ordens-servico/{id}/rejeitar`       | Autenticado | Rejeitar orçamento             |
| GET    | `/api/auth/me/ordens-servico/{id}/historico-status` | Autenticado | Histórico de status          |

### Clientes — `ADMIN`, `TECNICO`

| Método | Rota                 | Descrição        |
|--------|----------------------|------------------|
| GET    | `/api/clientes`      | Listar (paginado)|
| GET    | `/api/clientes/{id}` | Buscar por ID    |
| POST   | `/api/clientes`      | Criar            |
| PUT    | `/api/clientes/{id}` | Atualizar        |
| DELETE | `/api/clientes/{id}` | Excluir          |

### Veículos — `ADMIN`, `TECNICO`

| Método | Rota                            | Descrição              |
|--------|---------------------------------|------------------------|
| GET    | `/api/veiculos`                 | Listar (paginado)      |
| GET    | `/api/veiculos/cliente/{id}`    | Listar por cliente     |
| GET    | `/api/veiculos/{id}`             | Buscar por ID          |
| POST   | `/api/veiculos`                 | Criar                  |
| PUT    | `/api/veiculos/{id}`             | Atualizar              |
| DELETE | `/api/veiculos/{id}`             | Excluir                |

### Ordens de serviço — `ADMIN`, `TECNICO`

| Método | Rota                                    | Descrição              |
|--------|-----------------------------------------|------------------------|
| GET    | `/api/ordens-servico`                   | Listar (paginado)      |
| GET    | `/api/ordens-servico/cliente/{id}`      | Listar por cliente     |
| GET    | `/api/ordens-servico/veiculo/{id}`      | Listar por veículo     |
| GET    | `/api/ordens-servico/{id}`              | Buscar por ID          |
| GET    | `/api/ordens-servico/{id}/historico-status` | Histórico de status |
| POST   | `/api/ordens-servico`                   | Criar (status `ORCAMENTO`) |
| PUT    | `/api/ordens-servico/{id}`              | Atualizar              |
| DELETE | `/api/ordens-servico/{id}`              | Excluir                |

### Usuários — `ADMIN`

| Método | Rota                  | Descrição        |
|--------|-----------------------|------------------|
| GET    | `/api/usuarios`       | Listar (paginado)|
| GET    | `/api/usuarios/{id}`  | Buscar por ID    |
| POST   | `/api/usuarios`       | Criar            |
| PUT    | `/api/usuarios/{id}`  | Atualizar        |
| DELETE | `/api/usuarios/{id}`  | Excluir          |

### Técnicos — `ADMIN`, `TECNICO`

| Método | Rota            | Descrição              |
|--------|-----------------|------------------------|
| GET    | `/api/tecnicos` | Listar técnicos ativos |

### CEP — autenticado

| Método | Rota            | Descrição                    |
|--------|-----------------|------------------------------|
| GET    | `/api/ceps/{cep}` | Consulta endereço (OpenCEP) |

### Localidades — público

| Método | Rota                                    | Descrição           |
|--------|-----------------------------------------|---------------------|
| GET    | `/api/localidades/estados/{uf}/municipios` | Municípios por UF |

### Perfis

| Perfil    | Descrição                                              |
|-----------|--------------------------------------------------------|
| `ADMIN`   | Acesso total                                           |
| `TECNICO` | Clientes, veículos, ordens de serviço e técnicos       |
| `CLIENTE` | Próprias ordens de serviço (aprovar/rejeitar orçamento)|

Um usuário com perfil `CLIENTE` deve estar vinculado a um registro de `Cliente` (`clienteId`).

## Qualidade de código

O projeto usa [Lefthook](https://lefthook.dev/) para automação local:

```bash
brew install lefthook   # ou: curl -sSfL https://lefthook.dev/install.sh | sh
lefthook install
```

| Hook       | Ação                                              |
|------------|---------------------------------------------------|
| pre-commit | Spotless (`spotless:apply`) em arquivos `.java`   |
| pre-push   | Quality gate completo (`scripts/quality-gate.sh`) |

O quality gate executa: **Spotless**, **Checkstyle**, **PMD**, **testes** e **JaCoCo** (mínimo 14%, meta 80%).

```bash
# Rodar manualmente
./scripts/quality-gate.sh
```

## Testes

```bash
./mvnw test
```

Os testes usam H2 em memória com o schema em `src/test/resources/schema.sql` e chaves JWT em `src/test/resources/keys/`.

## CI

A pipeline (`.github/workflows/ci.yml`) dispara em **pull requests** e **push para `main`**, em runner **self-hosted Linux** com GraalVM 25:

1. Testes unitários
2. Scan de dependências (Trivy — bloqueia CRITICAL e HIGH)
3. Build nativo (`./mvnw package -Dnative`)
4. Build da imagem Docker (`src/main/docker/Dockerfile.native-micro`)
5. Scan da imagem (Trivy)
6. Push para Docker Hub (`oliveiraclaudio/vortex-mec-backend`, repositório privado)
7. Publicação do binário nativo como artefato (7 dias)

Chaves JWT são geradas no CI via `scripts/gerar-chaves-jwt.sh`.

### Secrets no GitHub

Em **Settings → Secrets and variables → Actions**, configure:

| Secret | Descrição |
|--------|-----------|
| `DOCKERHUB_USERNAME` | Usuário Docker Hub (`oliveiraclaudio`) |
| `DOCKERHUB_TOKEN` | Access Token do Docker Hub (não use a senha da conta) |

Crie o token em [Docker Hub → Account Settings → Security → Access Tokens](https://hub.docker.com/settings/security).

### Tags publicadas

| Evento | Tags |
|--------|------|
| Pull request | `<sha>`, `pr-<número>` |
| Push em `main` | `<sha>`, `latest` |

## Build

```bash
# JAR
./mvnw package

# Executar
java -jar target/quarkus-app/quarkus-run.jar

# Executável nativo (requer GraalVM 25 + native-image)
./mvnw package -Dnative

# Imagem Docker nativa (após o build nativo)
docker build -f src/main/docker/Dockerfile.native-micro -t mec-backend .
docker run -i --rm -p 8080:8080 mec-backend
```

## Configuração

Propriedades em `src/main/resources/application.properties` com suporte a variáveis de ambiente (veja `.env.example`):

| Variável | Descrição | Padrão (dev) |
|----------|-----------|--------------|
| `DB_USERNAME` | Usuário PostgreSQL | `postgres` |
| `DB_PASSWORD` | Senha PostgreSQL | `postgres` |
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/mec` |
| `REDIS_URL` | Valkey/Redis | `redis://localhost:6379` |
| `JWT_SIGN_KEY_LOCATION` | Chave privada JWT | `classpath:keys/privateKey.pem` |
| `JWT_PUBLIC_KEY_LOCATION` | Chave pública JWT | `classpath:keys/publicKey.pem` |
| `CORS_ORIGINS` | Origens permitidas (prod) | — |

### Produção

```bash
./mvnw package -Dquarkus.profile=prod
```

Configure todas as variáveis obrigatórias listadas em `application-prod.properties` (sem valores padrão). O Swagger fica desabilitado e rotas `/q/*` não são públicas.
