package com.vortex;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class VeiculoResourceTest {

  @Test
  void listarVeiculosSemTokenRetorna401() {
    given().when().get("/api/veiculos").then().statusCode(401);
  }

  @Test
  void criarEBuscarVeiculoComTokenAdminRetornaOk() {
    long clienteId = criarCliente("veiculo.cliente@test.com", "11122233344");
    String placa = TestDataHelper.placaUnica();

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "clienteId",
                clienteId,
                "placa",
                placa,
                "marca",
                "Fiat",
                "modelo",
                "Doblo",
                "anoFabricacao",
                2002,
                "motor",
                "1.6",
                "combustivel",
                "GAS",
                "kmAtual",
                150000))
        .when()
        .post("/api/veiculos")
        .then()
        .statusCode(201)
        .body("success", is(true))
        .body("data.placa", is(placa))
        .body("data.marca", is("Fiat"))
        .body("data.modelo", is("Doblo"))
        .body("data.anoFabricacao", is(2002))
        .body("data.clienteId", is((int) clienteId));

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .get("/api/veiculos/cliente/" + clienteId)
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data[0].placa", is(placa))
        .body("data[0].clienteNome", notNullValue());
  }

  private long criarCliente(String email, String cpf) {
    return ((Number)
            given()
                .auth()
                .oauth2(AuthTestHelper.obterTokenAdmin())
                .contentType(ContentType.JSON)
                .body(
                    Map.of(
                        "tipoPessoa", "PESSOA_FISICA",
                        "email", email,
                        "nome", "Proprietário Teste",
                        "cpf", cpf,
                        "telefone", "11999999999"))
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(201)
                .extract()
                .path("data.id"))
        .longValue();
  }
}
