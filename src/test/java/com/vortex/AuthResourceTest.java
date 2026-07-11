package com.vortex;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AuthResourceTest {

  @Test
  void loginComCredenciaisValidasRetornaTokens() {
    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "email", "admin@vortex.com",
                "senha", "admin123"))
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data.accessToken", notNullValue())
        .body("data.refreshToken", notNullValue())
        .body("data.tipo", is("Bearer"))
        .body("data.accessTokenExpiraEmSegundos", is(900))
        .body("data.deveDefinirSenha", is(false));
  }

  @Test
  void verificarPrimeiroAcessoComEmailCadastradoRetornaElegivel() {
    cadastrarCliente("cliente.verificar@test.com");

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("email", "cliente.verificar@test.com"))
        .when()
        .post("/api/auth/verificar-primeiro-acesso")
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data.email", is("cliente.verificar@test.com"))
        .body("data.nome", is("Cliente Teste"))
        .body("data.elegivel", is(true));
  }

  @Test
  void verificarPrimeiroAcessoComEmailNaoCadastradoRetornaNaoElegivel() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("email", "nao.existe@test.com"))
        .when()
        .post("/api/auth/verificar-primeiro-acesso")
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data.email", is("nao.existe@test.com"))
        .body("data.nome", is((String) null))
        .body("data.elegivel", is(false));
  }

  @Test
  void primeiroAcessoComEmailNaoCadastradoRetorna400() {
    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "email", "nao.existe@test.com",
                "senha", "senha123",
                "confirmarSenha", "senha123"))
        .when()
        .post("/api/auth/primeiro-acesso")
        .then()
        .statusCode(400)
        .body(
            "message",
            is(
                "Não foi possível concluir o primeiro acesso. Verifique os dados informados ou"
                    + " solicite seu cadastro à oficina."));
  }

  @Test
  void primeiroAcessoDefineSenhaERetornaTokens() {
    cadastrarCliente("cliente.primeiro@test.com");

    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "email", "cliente.primeiro@test.com",
                "senha", "senha123",
                "confirmarSenha", "senha123"))
        .when()
        .post("/api/auth/primeiro-acesso")
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data.deveDefinirSenha", is(false))
        .body("data.accessToken", notNullValue())
        .body("data.refreshToken", notNullValue());
  }

  @Test
  void loginAntesDoPrimeiroAcessoRetorna401() {
    cadastrarCliente("cliente.pendente@test.com");

    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "email", "cliente.pendente@test.com",
                "senha", "senha123"))
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(401)
        .body("message", is("Credenciais inválidas"));
  }

  private void cadastrarCliente(String email) {
    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "tipoPessoa", "PESSOA_FISICA",
                "email", email,
                "nome", "Cliente Teste",
                "cpf", email.hashCode() + "1",
                "telefone", "11999999999"))
        .when()
        .post("/api/clientes")
        .then()
        .statusCode(201);
  }

  @Test
  void loginComCredenciaisInvalidasRetorna401() {
    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "email", "admin@vortex.com",
                "senha", "senha-errada"))
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(401);
  }

  @Test
  void meComTokenValidoRetornaUsuario() {
    String token = AuthTestHelper.obterTokenAdmin();

    given()
        .auth()
        .oauth2(token)
        .when()
        .get("/api/auth/me")
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data.email", is("admin@vortex.com"))
        .body("data.perfil", is("ADMIN"));
  }

  @Test
  void refreshComTokenValidoRetornaNovosTokens() {
    String refreshToken =
        given()
            .contentType(ContentType.JSON)
            .body(
                Map.of(
                    "email", "admin@vortex.com",
                    "senha", "admin123"))
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .path("data.refreshToken");

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("refreshToken", refreshToken))
        .when()
        .post("/api/auth/refresh")
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data.accessToken", notNullValue())
        .body("data.refreshToken", notNullValue());
  }

  @Test
  void refreshComTokenInvalidoRetorna401() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("refreshToken", "token-invalido"))
        .when()
        .post("/api/auth/refresh")
        .then()
        .statusCode(401);
  }

  @Test
  void logoutInvalidaAccessToken() {
    String accessToken = AuthTestHelper.obterTokenAdmin();

    given()
        .auth()
        .oauth2(accessToken)
        .contentType(ContentType.JSON)
        .body(Map.of("refreshToken", ""))
        .when()
        .post("/api/auth/logout")
        .then()
        .statusCode(200);

    given().auth().oauth2(accessToken).when().get("/api/auth/me").then().statusCode(401);
  }

  @Test
  void atualizarPerfilComTokenValidoRetornaDadosAtualizados() {
    String token = AuthTestHelper.obterTokenAdmin();

    given()
        .auth()
        .oauth2(token)
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "nome", "Administrador Vortex",
                "email", "admin.novo@vortex.com"))
        .when()
        .put("/api/auth/me")
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data.nome", is("Administrador Vortex"))
        .body("data.email", is("admin.novo@vortex.com"))
        .body("data.perfil", is("ADMIN"));

    given()
        .auth()
        .oauth2(token)
        .when()
        .get("/api/auth/me")
        .then()
        .statusCode(200)
        .body("data.email", is("admin.novo@vortex.com"));

    given()
        .auth()
        .oauth2(token)
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "nome", "Admin",
                "email", "admin@vortex.com"))
        .when()
        .put("/api/auth/me")
        .then()
        .statusCode(200);
  }

  @Test
  void atualizarPerfilComEmailEmUsoRetorna400() {
    cadastrarCliente("cliente.email.em.uso@test.com");

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "nome", "Admin",
                "email", "cliente.email.em.uso@test.com"))
        .when()
        .put("/api/auth/me")
        .then()
        .statusCode(400)
        .body("message", is("Email já cadastrado"));
  }

  @Test
  void alterarSenhaComDadosValidosRetornaNovosTokens() {
    String token = AuthTestHelper.obterTokenAdmin();

    String novoToken =
        given()
            .auth()
            .oauth2(token)
            .contentType(ContentType.JSON)
            .body(
                Map.of(
                    "senhaAtual", "admin123",
                    "novaSenha", "admin456",
                    "confirmarSenha", "admin456"))
            .when()
            .put("/api/auth/me/senha")
            .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data.accessToken", notNullValue())
            .body("data.refreshToken", notNullValue())
            .extract()
            .path("data.accessToken");

    given().auth().oauth2(token).when().get("/api/auth/me").then().statusCode(401);

    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "email", "admin@vortex.com",
                "senha", "admin456"))
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(200);

    given()
        .auth()
        .oauth2(novoToken)
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "senhaAtual", "admin456",
                "novaSenha", "admin123",
                "confirmarSenha", "admin123"))
        .when()
        .put("/api/auth/me/senha")
        .then()
        .statusCode(200);
  }

  @Test
  void alterarSenhaRevogaRefreshTokensAnteriores() {
    String refreshTokenAntigo =
        given()
            .contentType(ContentType.JSON)
            .body(
                Map.of(
                    "email", "admin@vortex.com",
                    "senha", "admin123"))
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .path("data.refreshToken");

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "senhaAtual", "admin123",
                "novaSenha", "admin456",
                "confirmarSenha", "admin456"))
        .when()
        .put("/api/auth/me/senha")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("refreshToken", refreshTokenAntigo))
        .when()
        .post("/api/auth/refresh")
        .then()
        .statusCode(401);

    String tokenRestaurado =
        given()
            .contentType(ContentType.JSON)
            .body(
                Map.of(
                    "email", "admin@vortex.com",
                    "senha", "admin456"))
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .path("data.accessToken");

    given()
        .auth()
        .oauth2(tokenRestaurado)
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "senhaAtual", "admin456",
                "novaSenha", "admin123",
                "confirmarSenha", "admin123"))
        .when()
        .put("/api/auth/me/senha")
        .then()
        .statusCode(200);
  }

  @Test
  void alterarSenhaComSenhaAtualIncorretaRetorna401() {
    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "senhaAtual", "senha-errada",
                "novaSenha", "admin456",
                "confirmarSenha", "admin456"))
        .when()
        .put("/api/auth/me/senha")
        .then()
        .statusCode(401)
        .body("message", is("Senha atual incorreta"));
  }
}
