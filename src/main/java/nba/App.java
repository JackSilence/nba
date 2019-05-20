package nba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import magic.controller.ExecuteController;
import magic.service.SendGrid;

@SpringBootApplication
@EnableScheduling
@Import( { ExecuteController.class, SendGrid.class } )
public class App {
	public static void main( String[] args ) {
		SpringApplication.run( App.class, args );
	}
}