package com.vortex;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UsuarioResourceTest {

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
                    "senha", "senha123",
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
        .body(Map.of("email", email, "senha", "senha123"))
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
}
