package org.mi.plannitybe.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mi.plannitybe.integration.BaseIntegrationTest;
import org.mi.plannitybe.integration.UserSetUp;
import org.mi.plannitybe.user.dto.SignUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}