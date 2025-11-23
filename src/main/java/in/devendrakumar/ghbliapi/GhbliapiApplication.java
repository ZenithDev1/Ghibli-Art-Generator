package in.devendrakumar.ghbliapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
@EnableFeignClients
public class GhbliapiApplication {

	public static void main(String[] args) {

		SpringApplication.run(GhbliapiApplication.class, args);
	}

}
