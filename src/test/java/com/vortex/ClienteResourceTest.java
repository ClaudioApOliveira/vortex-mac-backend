package com.vortex;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
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
}
