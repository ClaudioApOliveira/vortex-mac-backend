package com.vortex;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import com.vortex.cep.dto.OpenCepResponse;
import com.vortex.cep.service.CepService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CepResourceTest {

  @InjectMock CepService cepService;

  @Test
  void buscarCepComTokenRetornaEndereco() {
    when(cepService.buscarPorCep("15050305"))
        .thenReturn(
            com.vortex.cep.dto.CepResponse.from(
                new OpenCepResponse(
                    "15050-305",
                    "Rua Josina Teixeira de Carvalho",
                    "",
                    "Vila Anchieta",
                    "São José do Rio Preto",
                    "SP",
                    "São Paulo",
                    "3549805")));

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .get("/api/ceps/15050305")
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data.cep", is("15050-305"))
        .body("data.cidade", is("São José do Rio Preto"))
        .body("data.uf", is("SP"));
  }

  @Test
  void buscarCepSemTokenRetorna401() {
    given().when().get("/api/ceps/15050305").then().statusCode(401);
  }
}
