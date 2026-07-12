package com.vortex;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;

public final class AuthTestHelper {

  private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

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

  public static String obterRefreshTokenAdmin() {
    return extrairRefreshToken(
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
            .response());
  }

  public static String extrairRefreshToken(Response response) {
    return response.getDetailedCookie(REFRESH_TOKEN_COOKIE).getValue();
  }
}
