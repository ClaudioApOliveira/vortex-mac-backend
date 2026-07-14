package com.vortex;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ClienteResourceTest {

  @Test
  void listarClientesSemTokenRetorna401() {
    given().when().get("/api/clientes").then().statusCode(401);
  }

  @Test
  void listarClientesComTokenAdminRetornaOk() {
    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .get("/api/clientes")
        .then()
        .statusCode(200);
  }

  @Test
  void excluirClienteComUsuarioLogadoRemoveDependencias() {
    String email = "cliente.excluir.com.login@test.com";

    Number clienteId =
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
                    "Cliente Com Login",
                    "cpf",
                    String.valueOf(Math.abs(email.hashCode())),
                    "telefone",
                    "11977776666"))
            .when()
            .post("/api/clientes")
            .then()
            .statusCode(201)
            .body("data.usuarioId", notNullValue())
            .extract()
            .path("data.id");

    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "email", email,
                "senha", "senha1234",
                "confirmarSenha", "senha1234"))
        .when()
        .post("/api/auth/primeiro-acesso")
        .then()
        .statusCode(200);

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .delete("/api/clientes/" + clienteId)
        .then()
        .statusCode(200)
        .body("message", is("Recurso excluído com sucesso"));
  }
}
