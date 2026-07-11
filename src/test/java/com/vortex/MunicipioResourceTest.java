package com.vortex;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MunicipioResourceTest {

  @Test
  void listarMunicipiosPorUfSemAutenticacaoRetornaOk() {
    given()
        .when()
        .get("/api/localidades/estados/MG/municipios")
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data.size()", is(3))
        .body("data[0].uf", is("MG"));
  }

  @Test
  void listarMunicipiosComUfInvalidaRetorna400() {
    given()
        .when()
        .get("/api/localidades/estados/XX/municipios")
        .then()
        .statusCode(400)
        .body("message", is("UF inválida: XX"));
  }
}
