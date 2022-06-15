package lan.test.oauth.oauthdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@RestController
public class OauthDemoApplication {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler("/");

        http
                .authorizeRequests(a -> a
                        .antMatchers("/health", "/", "/error", "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(l -> l
                        .logoutSuccessUrl("/").permitAll()
                )
                .csrf(c -> c
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .logout(l -> l.logoutSuccessUrl("/").permitAll())
                .csrf(c -> c
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .oauth2Login(o -> o
                        .failureHandler((request, response, exception) -> {
                            System.out.println("EXCEPTION="+exception);
                            exception.printStackTrace();
                            request.getSession().setAttribute("error.message", exception.getMessage());
                            handler.onAuthenticationFailure(request, response, exception);
                        })
                );
        // @formatter:on

        return http.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(OauthDemoApplication.class, args);
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        var name = Optional.ofNullable(principal.getAttribute("name"))
                .orElse(principal.getAttribute("login"));
        return Collections.singletonMap("name", name);
    }

    @GetMapping("/error")
    @ResponseBody
    public String error(HttpServletRequest request) {
        String message = (String) request.getSession().getAttribute("error.message");
        request.getSession().removeAttribute("error.message");
        return message;
    }
}
