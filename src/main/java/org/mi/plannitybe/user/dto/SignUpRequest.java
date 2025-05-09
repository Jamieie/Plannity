package org.mi.plannitybe.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotNull(message = "email은 필수 입력값입니다.")
    @Email(message = "email 형식에 맞춰 입력해 주세요.")
    private String email;

    @NotNull(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()_+\\-={}\\[\\]|\\\\:;\"'<>,.?/]).{8,64}$",
            message = "비밀번호는 영문자, 숫자, 특수문자를 포함한 8~64자여야 합니다."
    )
    private String pwd;
}
