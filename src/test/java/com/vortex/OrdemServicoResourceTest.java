package com.vortex;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OrdemServicoResourceTest {

  @Test
  void listarOrdensSemTokenRetorna401() {
    given().when().get("/api/ordens-servico").then().statusCode(401);
  }

  @Test
  void criarEBuscarOrdemServicoComTokenAdminRetornaOk() {
    long clienteId = criarCliente("os.cliente@test.com", "22233344455");
    String placa = TestDataHelper.placaUnica();
    long veiculoId = criarVeiculo(clienteId, placa);
    long tecnicoId = obterTecnicoId();

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .contentType(ContentType.JSON)
        .body(bodyCriarOrdemServico(clienteId, veiculoId, tecnicoId))
        .when()
        .post("/api/ordens-servico")
        .then()
        .statusCode(201)
        .body("success", is(true))
        .body("data.clienteId", is((int) clienteId))
        .body("data.veiculoId", is((int) veiculoId))
        .body("data.tecnicoId", is((int) tecnicoId))
        .body("data.custoPecas", is(45.0f))
        .body("data.custoMaoDeObra", is(120.0f))
        .body("data.precoTotal", is(215.0f))
        .body("data.status", is("ORCAMENTO"))
        .body("data.itens.size()", is(2));

    given()
        .auth()
        .oauth2(AuthTestHelper.obterTokenAdmin())
        .when()
        .get("/api/ordens-servico/veiculo/" + veiculoId)
        .then()
        .statusCode(200)
        .body("success", is(true))
        .body("data[0].clienteNome", notNullValue())
        .body("data[0].veiculoPlaca", is(placa))
        .body("data[0].status", is("ORCAMENTO"));
  }

  private Map<String, Object> bodyCriarOrdemServico(
      long clienteId, long veiculoId, long tecnicoId) {
    Map<String, Object> body = new HashMap<>();
    body.put("clienteId", clienteId);
    body.put("veiculoId", veiculoId);
    body.put("tecnicoId", tecnicoId);
    body.put("data", "2026-03-22");
    body.put("hora", "14:30:00");
    body.put("kmEntrada", 150000);
    body.put("kmSaida", 150120);
    body.put("custoServicosTerceirizados", 50.00);
    body.put("descricaoServicosTerceirizados", "Alinhamento externo");
    body.put("custoMaoDeObra", 120.00);
    body.put("descricaoMaoDeObra", "Troca de óleo e filtros");
    body.put(
        "itens",
        List.of(
            Map.of(
                "descricao",
                "Filtro de Óleo",
                "quantidade",
                1,
                "valorUnitario",
                45.00,
                "tipo",
                "PECA"),
            Map.of(
                "descricao",
                "Troca de óleo",
                "quantidade",
                1,
                "valorUnitario",
                120.00,
                "tipo",
                "SERVICO")));
    return body;
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
                        "nome", "Proprietário OS",
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

  private long criarVeiculo(long clienteId, String placa) {
    return ((Number)
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
                .extract()
                .path("data.id"))
        .longValue();
  }

  private long obterTecnicoId() {
    return ((Number)
            given()
                .auth()
                .oauth2(AuthTestHelper.obterTokenAdmin())
                .when()
                .get("/api/tecnicos")
                .then()
                .statusCode(200)
                .extract()
                .path("data[0].id"))
        .longValue();
  }
}
