package spboard.board.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import spboard.board.Repository.UserRepository;
import spboard.board.config.jwt.ApiAccessDeniedHandler;
import spboard.board.config.jwt.JwtAuthEntryPoint;
import spboard.board.config.jwt.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiAccessDeniedHandler apiAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)   // 401
                        .accessDeniedHandler(apiAccessDeniedHandler)   // 403
                )

                .authorizeHttpRequests(auth -> auth
                        // preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 인증 없이 접근
                        .requestMatchers("/api/auth/login", "/api/users/join", "/api/home").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/boards/**").permitAll()

                        // 로그인 필요
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users/mypage/**").authenticated()
                        .requestMatchers("/api/users/edit", "/api/users/delete").authenticated()
                        .requestMatchers("/api/comments/**").authenticated()

                        // 게시글 CUD는 로그인 필요
                        .requestMatchers(HttpMethod.POST,   "/api/boards/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/boards/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH,  "/api/boards/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/boards/**").authenticated()

                        // 관리자
                        .requestMatchers("/api/users/admin/**").hasAuthority("ADMIN")

                        // swagger
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // 나머지
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));

        // JWT를 Authorization 헤더로만 쓰면 보통 false 권장
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
