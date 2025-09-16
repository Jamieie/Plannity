package org.mi.plannitybe.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mi.plannitybe.config.SecurityConfig;
import org.mi.plannitybe.exception.handler.CustomAccessDeniedHandler;
import org.mi.plannitybe.exception.handler.CustomAuthenticationEntryPoint;
import org.mi.plannitybe.jwt.JwtTokenProvider;
import org.mi.plannitybe.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    @DisplayName("회원가입 성공")
    void signUpOk() throws Exception {
        // GIVEN - 형식에 맞는 이메일과 비밀번호 JSON BODY
        String json = """
                {
                    "email": "test@email.com",
                    "pwd": "asdf1234@"
                }
                """;

        // WHEN & THEN - 회원가입 요청 시 반환코드 201, 회원가입 성공 메시지
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));
    }

    @Test
    @DisplayName("회원가입 실패")
    void signUpFail() throws Exception {
        // GIVEN - 형식에 맞지 않는 이메일 및 비밀번호
        String json = """
                {
                    "email": "asdfasdf",
                    "pwd": "asdf1234"
                }
                """;

        // WHEN & THEN - 회원가입 요청 시 반환코드 400, 회원가입 실패 메시지
        mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.messages", hasItem("email 형식에 맞춰 입력해 주세요.")))
                .andExpect(jsonPath("$.messages", hasItem("비밀번호는 영문자, 숫자, 특수문자를 포함한 8~64자여야 합니다.")));
    }
}