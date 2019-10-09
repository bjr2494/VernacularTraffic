package acc.capstone;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.LocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@SpringBootApplication
public class CapstoneProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(CapstoneProjectApplication.class, args);
	}
	
	@Bean
	@SessionScope
	SessionManager sessionManager() {
		return new SessionManager();
	}
	
	@Bean 
	@ApplicationScope
	ApplicationManager applicationManager() {
		return new ApplicationManager();
	}
}
