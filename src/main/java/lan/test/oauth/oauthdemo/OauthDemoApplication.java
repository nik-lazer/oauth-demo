package lan.test.oauth.oauthdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class OauthDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OauthDemoApplication.class, args);
    }
}
