package org.mi.plannitybe.config;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.mi.plannitybe.user.dto.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;
import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    private static final String SYSTEM_AUDITOR = "SYSTEM";
    private static final String ANONYMOUS_AUDITOR = "ANONYMOUS";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // 현재 요청 컨텍스트가 있는지 확인 (HTTP 요청인지 여부)
            boolean isWebRequest = RequestContextHolder.getRequestAttributes() != null;
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // 1. 인증 객체가 있고 인증된 경우 - 사용자 ID 반환
            if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal())) {

                // CustomUserDetails에서 사용자 ID 추출
                if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                    return Optional.of(userDetails.getId());
                }
                return Optional.of(authentication.getName());  // fallback: 이메일 반환
            }
            
            // 2. HTTP 요청인데 인증이 없는 경우
            if (isWebRequest) {
                // 회원가입 요청인 경우 Auditing 건너뛰기
                if (isSignupRequest()) {
                    return Optional.empty();  // Auditing 비활성화
                }
                return Optional.of(ANONYMOUS_AUDITOR);  // 익명 사용자 반환
            }
            
            // 3. HTTP 요청이 아닌 경우 (배치, 스케줄러 등) - 시스템으로 표시
            return Optional.of(SYSTEM_AUDITOR);
        };
    }

    @Bean
    public PhysicalNamingStrategy physicalNamingStrategy() {
        return new SnakeCasePhysicalNamingStrategy();
    }

    private boolean isSignupRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String uri = request.getRequestURI();
            String method = request.getMethod();

            // 회원가입 엔드포인트 확인
            return "POST".equals(method) && (uri.equals("/auth/signup"));
        }

        return false;
    }

    // Inner class로 SnakeCasePhysicalNamingStrategy 구현
    public static class SnakeCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

        @Override
        public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
            return apply(identifier);
        }

        @Override
        public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
            return apply(identifier);
        }

        @Override
        public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
            return apply(identifier);
        }

        @Override
        public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
            return apply(identifier);
        }

        @Override
        public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
            return apply(identifier);
        }

        private Identifier apply(Identifier identifier) {
            if (identifier == null) {
                return null;
            }
            
            String name = identifier.getText();
            String snakeCaseName = camelCaseToSnakeCase(name);
            return Identifier.toIdentifier(snakeCaseName);
        }

        private String camelCaseToSnakeCase(String name) {
            final StringBuilder result = new StringBuilder();
            result.append(name.substring(0, 1).toLowerCase(Locale.ROOT));
            
            for (int i = 1; i < name.length(); i++) {
                final char c = name.charAt(i);
                if (Character.isUpperCase(c)) {
                    result.append('_');
                    result.append(Character.toLowerCase(c));
                } else {
                    result.append(c);
                }
            }
            
            return result.toString();
        }
    }
}