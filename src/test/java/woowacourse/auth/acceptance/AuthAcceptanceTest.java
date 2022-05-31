package woowacourse.auth.acceptance;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import woowacourse.auth.dto.CustomerResponse;
import woowacourse.auth.dto.LoginRequest;
import woowacourse.auth.dto.TokenResponse;
import woowacourse.shoppingcart.acceptance.AcceptanceTest;
import woowacourse.shoppingcart.dto.ExceptionResponse;
import woowacourse.shoppingcart.dto.SignupRequest;

@DisplayName("인증 관련 기능")
public class AuthAcceptanceTest extends AcceptanceTest {
    private static final String INVALID_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkb25naG8xMDgiLCJpYXQiOjE2NTM5MDg0OTgsImV4cCI6MTY1MzkxMjA5OH0.6XAQq1jsqxnn8zMbW9nNcZ4R-BiIyQvLkraocC1aaaa";

    @DisplayName("Bearer Auth 로그인 성공")
    @Test
    void myInfoWithBearerAuth() {
        // given
        SignupRequest signupRequest = new SignupRequest("dongho108", "ehdgh1234", "01022728572", "인천 서구 검단로");

        RestAssured.given().log().all()
            .body(signupRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/api/customers/signup")
            .then().log().all()
            .extract();

        LoginRequest loginRequest = new LoginRequest("dongho108", "ehdgh1234");
        String accessToken = RestAssured.given().log().all()
            .body(loginRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/api/customers/login")
            .then().log().all()
            .extract().as(TokenResponse.class).getAccessToken();

        // when
        CustomerResponse customerResponse = RestAssured
            .given().log().all()
            .auth().oauth2(accessToken)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().get("/api/customers")
            .then().log().all()
            .statusCode(HttpStatus.OK.value()).extract().as(CustomerResponse.class);

        // then
        assertAll(
            () -> assertThat(customerResponse.getUsername()).isEqualTo(signupRequest.getUsername()),
            () -> assertThat(customerResponse.getPhoneNumber()).isEqualTo(signupRequest.getPhoneNumber()),
            () -> assertThat(customerResponse.getAddress()).isEqualTo(signupRequest.getAddress())
        );
    }

    @DisplayName("해당 아이디가 존재하지 않으면 (401)unauthorized 를 반환해야 한다.")
    @Test
    void myInfoWithInvalidUsername() {
        // given
        SignupRequest signupRequest = new SignupRequest("dongho108", "ehdgh1234", "01022728572", "인천 서구 검단로");

        RestAssured.given().log().all()
            .body(signupRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/api/customers/signup")
            .then().log().all()
            .extract();

        // when
        ValidatableResponse validatableResponse = RestAssured
            .given().log().all()
            .body(new LoginRequest("dongho109", "ehdgh1234"))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/api/customers/login")
            .then().log().all();
        ExceptionResponse exceptionResponse = validatableResponse.extract().as(ExceptionResponse.class);

        // then
        assertAll(
            () -> validatableResponse.statusCode(HttpStatus.UNAUTHORIZED.value()),
            () -> assertThat(exceptionResponse.getMessages())
                .containsExactly("해당하는 username이 없습니다.")
        );
    }

    @DisplayName("비밀번호가 일치하지 않으면 (401)unauthorized 를 반환해야 한다.")
    @Test
    void myInfoWithInvalidPassword() {
        // given
        SignupRequest signupRequest = new SignupRequest("dongho108", "ehdgh1234", "01022728572", "인천 서구 검단로");

        RestAssured.given().log().all()
            .body(signupRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/api/customers/signup")
            .then().log().all()
            .extract();

        // when
        ValidatableResponse validatableResponse = RestAssured
            .given().log().all()
            .body(new LoginRequest("dongho108", "password"))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/api/customers/login")
            .then().log().all();
        ExceptionResponse exceptionResponse = validatableResponse.extract().as(ExceptionResponse.class);

        // then
        assertAll(
            () -> validatableResponse.statusCode(HttpStatus.UNAUTHORIZED.value()),
            () -> assertThat(exceptionResponse.getMessages())
                .containsExactly("비밀번호가 일치하지 않습니다.")
        );
    }

    @DisplayName("Bearer Auth 유효하지 않은 토큰")
    @Test
    void myInfoWithWrongBearerAuth() {
        // given
        SignupRequest signupRequest = new SignupRequest("dongho108", "ehdgh1234", "01022728572", "인천 서구 검단로");

        RestAssured.given().log().all()
            .body(signupRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/api/customers/signup")
            .then().log().all()
            .extract();

        // when
        RestAssured
            .given().log().all()
            .body(new LoginRequest("dongho108", "ehdgh1234"))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/api/customers/login")
            .then().log().all().extract();

        // then
        ValidatableResponse validatableResponse = RestAssured
            .given().log().all()
            .auth().oauth2(INVALID_ACCESS_TOKEN)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().get("/api/customers")
            .then().log().all();
        ExceptionResponse response = validatableResponse.extract().as(ExceptionResponse.class);

        assertAll(
            () -> validatableResponse.statusCode(HttpStatus.UNAUTHORIZED.value()),
            () -> assertThat(response.getMessages())
                .containsExactly("유효하지 않은 토큰입니다.")
        );
    }
}
