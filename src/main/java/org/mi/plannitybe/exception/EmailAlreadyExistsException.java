package org.mi.plannitybe.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    private final String email;  // 등록 시도한 email

    public EmailAlreadyExistsException(String email) {
        super("이미 등록된 이메일입니다. : " + email);
        this.email = email;
    }
}
