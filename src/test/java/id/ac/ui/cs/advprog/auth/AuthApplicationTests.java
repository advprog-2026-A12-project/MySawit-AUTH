package id.ac.ui.cs.advprog.auth;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
class AuthApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainDelegatesToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(AuthApplication.class, new String[]{}))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            AuthApplication.main(new String[]{});

            springApplication.verify(() -> SpringApplication.run(AuthApplication.class, new String[]{}));
        }
    }

}
