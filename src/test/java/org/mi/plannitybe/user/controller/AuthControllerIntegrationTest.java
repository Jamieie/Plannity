package org.mi.plannitybe.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mi.plannitybe.integration.BaseIntegrationTest;
import org.mi.plannitybe.integration.UserSetUp;
import org.mi.plannitybe.user.dto.LoginRequest;
import org.mi.plannitybe.user.dto.SignUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    String EMAIL;
    String PWD;

    @Autowired
    private UserSetUp userSetUp;

    @Test
    @DisplayName("회원가입 성공")
    public void signUp_ok() throws Exception {

        // TODO) 가입된 이메일이 아니라는 것 보장 필요 -> test DB를 별도로 두고 테스트 전 DB의 user 모두 지우는 방법?

        // GIVEN - 유효한 이메일과 비밀번호
        EMAIL = "test@email.com";
        PWD = "asdf1234@";

        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmail(EMAIL);
        signUpRequest.setPwd(PWD);

        // WHEN - 회원가입 api 호출
        ResultActions resultActions = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        // THEN - 응답코드 201, 회원가입 성공 메시지
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일과 비밀번호 형식 유효성 검사 실패")
    public void signUp_fail_invalidValue() throws Exception {

        // GIVEN - 유효하지 않은 이메일과 비밀번호
        EMAIL = "test";
        PWD = "asdf1234";

        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmail(EMAIL);
        signUpRequest.setPwd(PWD);

        // WHEN - 회원가입 api 호출
        ResultActions resultActions = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        // THEN - 응답코드 400, 회원가입 실패 메시지
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.messages", hasItem("email 형식에 맞춰 입력해 주세요.")))
                .andExpect(jsonPath("$.messages", hasItem("비밀번호는 영문자, 숫자, 특수문자를 포함한 8~64자여야 합니다.")));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 가입된 이메일로 가입 시도")
    public void signUp_fail_emailAlreadyExists() throws Exception {

        // GIVEN - 이미 가입된 이메일
        EMAIL = "test@email.com";
        PWD = "asdf1234@";
        userSetUp.saveUser(EMAIL, PWD);

        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmail(EMAIL);
        signUpRequest.setPwd(PWD);

        // WHEN - 회원가입 api 호출
        ResultActions resultActions = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        // THEN - 응답코드 409, 회원가입 실패 메시지
        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    @DisplayName("로그인 성공")
    public void login_ok() throws Exception {
        // GIVEN - DB에 존재하는 이메일과 올바른 비밀번호
        EMAIL = "test@email.com";
        PWD = "asdf1234@";
        userSetUp.saveUser(EMAIL, PWD);

        // WHEN - 로그인 api 호출
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(EMAIL);
        loginRequest.setPwd(PWD);

        ResultActions resultActions = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        // THEN - 응답코드 200, 로그인 성공 메시지, header에 accessToken, cookie에 refreshToken
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."))
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
                .andExpect(cookie().exists("refreshToken"));
    }

    @ParameterizedTest
    @MethodSource("invalidLoginProvider")
    @DisplayName("로그인 실패 - 유효하지 않는 이메일 또는 비밀번호 틀림")
    public void login_fail_invalidValue(String email, String pwd, String testCaseDescription, String expectedCode, String expectedMessage) throws Exception {
        // GIVEN - 유효하지 않은 이메일 또는 틀린 비밀번호
            // 1. DB에 존재하지 않는 이메일
            // 2. DB에 존재하고 상태가 ACTIVE인 이메일이지만 틀린 비밀번호
            // 3. DB에 존재하지만 상태가 ACTIVE가 아닌 이메일

        if ("비밀번호 틀림".equals(testCaseDescription)) {
            userSetUp.saveUser(email, "correctPwd@");
        } else if ("계정 상태 비활성".equals(testCaseDescription)) {
            userSetUp.saveInactiveUser(email, pwd);
        }

        // WHEN - 로그인 api 호출
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPwd(pwd);

        ResultActions resultActions = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        // THEN - 응답코드 401(Unauthorized), 로그인 실패 코드 및 메시지
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(expectedCode))
                .andExpect(jsonPath("$.message").value(expectedMessage));

    }

    private static Stream<Arguments> invalidLoginProvider() {
        return Stream.of(
                Arguments.of("nonexistent@email.com", "password123!", "이메일 없음", "INVALID_EMAIL_OR_PASSWORD", "아이디 또는 비밀번호가 올바르지 않습니다."),
                Arguments.of("test@email.com", "wrongPassword!", "비밀번호 틀림", "INVALID_EMAIL_OR_PASSWORD", "아이디 또는 비밀번호가 올바르지 않습니다."),
                Arguments.of("inactive@email.com", "password123!", "계정 상태 비활성", "DISABLED_ACCOUNT", "계정이 비활성화 상태입니다. 관리자에게 문의하세요.")
        );
    }
}