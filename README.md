# mec-backend

Backend da oficina mecânica **Vortex**, desenvolvido com [Quarkus](https://quarkus.io/). O sistema gerencia clientes e usuários do sistema, com autenticação JWT e controle de acesso por perfil — base para futuras funcionalidades como Ordens de Serviço (OS).

## Stack

- **Java 25** + **Quarkus 3.37**
- **PostgreSQL** (via Docker Compose)
- **Hibernate ORM** (entidades JPA puras, sem Panache)
- **SQL nativo** nos repositórios
- **JWT** (SmallRye JWT) + **refresh token** com rotação
- **Valkey** (sessões ativas de login/logout)
- **Bean Validation**

## Arquitetura

O projeto segue **Package by Feature**, com interfaces para `Service` e `Repository` (SOLID):

```
src/main/java/com/vortex/
├── auth/           # Login, refresh token, JWT
├── cliente/        # CRUD de clientes
├── usuario/        # CRUD de usuários
└── shared/         # Exceções e mappers globais
```

Cada feature contém:

```
entity/  repository/  service/  dto/  resource/
           └── impl/      └── impl/
```

O schema do banco é versionado em `docker/postgres/init.sql` (sem Flyway, sem geração automática pelo ORM).

## Pré-requisitos

- JDK 25+
- Docker e Docker Compose
- Maven (ou use o wrapper `./mvnw`)

## Como rodar

### 1. Subir PostgreSQL e Valkey

```bash
docker compose up -d
```

O PostgreSQL sobe na porta `5432` e o Valkey na porta `6379`.

| Parâmetro | Valor      |
|-----------|------------|
| Banco     | `mec`      |
| Usuário   | `postgres` |
| Senha     | `postgres` |

Na primeira execução, o script `docker/postgres/init.sql` cria as tabelas. O usuário admin de desenvolvimento é criado separadamente por `docker/postgres/seeds/dev-admin.sql` (somente Docker Compose local).

> **Produção:** não monte `dev-admin.sql`. Crie o primeiro administrador manualmente ou via processo de provisionamento.

> Se o banco já existia de uma versão anterior, recrie o volume:
>
> ```bash
> docker compose down -v
> docker compose up -d
> ```

### 2. Iniciar a aplicação

```bash
./mvnw quarkus:dev
```

API disponível em: http://localhost:8080

## Autenticação

### Usuário de desenvolvimento (Docker Compose)

Disponível apenas quando o seed `dev-admin.sql` é carregado (ambiente local):

| Campo  | Valor                |
|--------|----------------------|
| Email  | `admin@vortex.com`   |
| Senha  | `admin123`           |
| Perfil | `ADMIN`              |

> Não use essas credenciais em produção.

### Tempos de validade

| Token          | Validade   | Configuração                          |
|----------------|------------|---------------------------------------|
| Access token   | 15 minutos | `vortex.jwt.access-token.lifespan`    |
| Refresh token  | 7 dias     | `vortex.jwt.refresh-token.lifespan`   |

### Fluxo de sessão (Valkey)

- **Login / refresh:** registra access token (`jti`) e refresh token no Valkey com TTL
- **Requisições autenticadas:** valida se a sessão ainda está ativa no Valkey
- **Logout:** remove a sessão do Valkey e revoga o refresh token (opcional no body)

```bash
# Logout (invalida o access token atual)
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh_token>"}'
```

### Fluxo

```bash
# Login
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

Envie o `accessToken` no header `Authorization: Bearer <token>` em todas as demais rotas.

## Endpoints

### Auth (público)

| Método | Rota               | Descrição              |
|--------|--------------------|------------------------|
| POST   | `/api/auth/login`    | Autenticar             |
| POST   | `/api/auth/refresh`  | Renovar tokens         |
| POST   | `/api/auth/logout`   | Encerrar sessão        |
| GET    | `/api/auth/me`       | Dados do usuário logado |

### Clientes — perfis `ADMIN`, `TECNICO`

| Método | Rota                 | Descrição        |
|--------|----------------------|------------------|
| GET    | `/api/clientes`      | Listar           |
| GET    | `/api/clientes/{id}` | Buscar por ID    |
| POST   | `/api/clientes`      | Criar            |
| PUT    | `/api/clientes/{id}` | Atualizar        |
| DELETE | `/api/clientes/{id}` | Excluir          |

### Usuários — perfil `ADMIN`

| Método | Rota                  | Descrição        |
|--------|-----------------------|------------------|
| GET    | `/api/usuarios`       | Listar           |
| GET    | `/api/usuarios/{id}`  | Buscar por ID    |
| POST   | `/api/usuarios`       | Criar            |
| PUT    | `/api/usuarios/{id}`  | Atualizar        |
| DELETE | `/api/usuarios/{id}`  | Excluir          |

### Perfis

| Perfil    | Descrição                                      |
|-----------|------------------------------------------------|
| `ADMIN`   | Acesso total (clientes + usuários)             |
| `TECNICO` | Gestão de clientes                             |
| `CLIENTE` | Vinculado a um cliente (futuro acesso às OS)   |

Um usuário com perfil `CLIENTE` deve estar vinculado a um registro de `Cliente` (`clienteId`).

## Testes

```bash
./mvnw test
```

Os testes usam H2 em memória com o schema em `src/test/resources/schema.sql`.

## Build

```bash
# JAR
./mvnw package

# Executar
java -jar target/quarkus-app/quarkus-run.jar

# Executável nativo (requer GraalVM ou build em container)
./mvnw package -Dnative -Dquarkus.native.container-build=true
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

### Chaves JWT

```bash
./scripts/gerar-chaves-jwt.sh
```

A chave privada (`privateKey.pem`) está no `.gitignore` e **não deve ser commitada**. Os testes usam chaves separadas em `src/test/resources/keys/`.

### Produção

```bash
./mvnw package -Dquarkus.profile=prod
```

Configure todas as variáveis obrigatórias listadas em `application-prod.properties` (sem valores padrão). O Swagger fica desabilitado e `/q/*` não é público.
