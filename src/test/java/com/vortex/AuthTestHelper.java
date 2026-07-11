package com.vortex;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import java.util.Map;

public final class AuthTestHelper {

  private AuthTestHelper() {}

  public static String obterTokenAdmin() {
    return given()
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
        .path("data.accessToken");
  }
}
