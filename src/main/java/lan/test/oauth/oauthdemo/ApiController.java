package lan.test.oauth.oauthdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@RestController
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private final WebClient webClient;
    @Value("${demo.url}")
    private String url;

    public ApiController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        var name = Optional.ofNullable(principal.getAttribute("name"))
                .orElse(principal.getAttribute("login"));
        var provider = detectProvider(principal.getAttribute("iss"), principal.getAttribute("url"));
        return Map.of("name", name, "provider", provider);
    }

    private String detectProvider(Object issued, Object url) {
        return Optional.ofNullable(detectByString(issued))
                .or(() -> Optional.ofNullable(detectByString(url)))
                .orElse("no idea");
    }

    private String detectByString(Object attribute) {
        if (attribute != null) {
            var attr = attribute.toString();
            if (attr.contains("microsoft"))
                return "azure";
            if (attr.contains("google"))
                return "google";
            if (attr.contains("github"))
                return "github";
        }
        return null;
    }

    @GetMapping("/error")
    @ResponseBody
    public String error(HttpServletRequest request) {
        String message = (String) request.getSession().getAttribute("error.message");
        request.getSession().removeAttribute("error.message");
        return message;
    }

    @GetMapping("call/{provider}")
    @ResponseBody
    public String call(@PathVariable String provider) {
        return webClient.get()
                .uri(url)
                .attributes(clientRegistrationId(provider))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(s -> logger.info("completed, fetched {}", s))
                .doOnError(ex -> logger.error("Fetching error ", ex))
                .block(Duration.ofSeconds(2));
    }
}
