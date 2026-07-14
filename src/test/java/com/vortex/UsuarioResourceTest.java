package com.vortex;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.vortex.shared.exception.BusinessException;
import com.vortex.usuario.service.UsuarioExclusaoService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UsuarioResourceTest {

  @Inject UsuarioExclusaoService usuarioExclusaoService;

  @Test
  void excluirUsuarioComRefreshTokensRemoveDependencias() {
    String email = "tecnico.excluir@test.com";

    Number usuarioId =
        given()
            .auth()
            .oauth2(AuthTestHelper.obterTokenAdmin())
            .contentType(ContentType.JSON)
            .body(
                Map.of(
                    "email", email,
                    "senha", "senha1234",
                    "nome", "Técnico Excluir",
                    "perfil", "TECNICO",
                    "ativo", true))
            .when()
            .post("/api/usuarios")
            .then()
            .statusCode(201)
            .extract()
            .path("data.id");

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("email", email, "senha", "senha1234"))
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(200);

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .delete("/api/usuarios/" + usuarioId)
        .then()
        .statusCode(200)
        .body("message", is("Recurso excluído com sucesso"));

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .get("/api/usuarios/" + usuarioId)
        .then()
        .statusCode(404);
  }

  @Test
  void excluirUsuarioClienteDesvinculaClienteSemErro() {
    String email = "cliente.excluir.usuario@test.com";

    Number usuarioId =
        given()
            .auth()
            .oauth2(AuthTestHelper.obterTokenAdmin())
            .contentType(ContentType.JSON)
            .body(
                Map.of(
                    "tipoPessoa",
                    "PESSOA_FISICA",
                    "email",
                    email,
                    "nome",
                    "Cliente Excluir Usuario",
                    "cpf",
                    String.valueOf(Math.abs(email.hashCode())),
                    "telefone",
                    "11988887777"))
            .when()
            .post("/api/clientes")
            .then()
            .statusCode(201)
            .extract()
            .path("data.usuarioId");

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .delete("/api/usuarios/" + usuarioId)
        .then()
        .statusCode(200)
        .body("message", is("Recurso excluído com sucesso"));

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .get("/api/usuarios/" + usuarioId)
        .then()
        .statusCode(404);
  }

  @Test
  void excluirProprioUsuarioRetorna400() {
    Number adminId =
        given()
            .auth()
            .oauth2(AuthTestHelper.obterTokenAdmin())
            .when()
            .get("/api/auth/me")
            .then()
            .statusCode(200)
            .extract()
            .path("data.id");

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .delete("/api/usuarios/" + adminId)
        .then()
        .statusCode(400)
        .body("message", is("Não é possível excluir o próprio usuário"));
  }

  @Test
  void excluirUltimoAdminAtivoLancaBusinessException() {
    Number adminId =
        given()
            .auth()
            .oauth2(AuthTestHelper.obterTokenAdmin())
            .when()
            .get("/api/auth/me")
            .then()
            .statusCode(200)
            .extract()
            .path("data.id");

    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> usuarioExclusaoService.excluir(adminId.longValue()));
    assertEquals("Não é possível excluir o último administrador ativo", exception.getMessage());
  }

  @Test
  void excluirOutroAdminQuandoHaMaisDeUmRetorna200() {
    Number segundoAdminId =
        given()
            .auth()
            .oauth2(AuthTestHelper.obterTokenAdmin())
            .contentType(ContentType.JSON)
            .body(
                Map.of(
                    "email", "admin2.excluir@test.com",
                    "senha", "senha1234",
                    "nome", "Admin Dois",
                    "perfil", "ADMIN",
                    "ativo", true))
            .when()
            .post("/api/usuarios")
            .then()
            .statusCode(201)
            .extract()
            .path("data.id");

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .delete("/api/usuarios/" + segundoAdminId)
        .then()
        .statusCode(200);
  }
}
