package nba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import magic.service.AsyncExecutor;
import magic.service.Cloudinary;
import magic.service.SendGrid;
import magic.service.Slack;

@SpringBootApplication
@EnableScheduling
@Import( { SendGrid.class, AsyncExecutor.class, Slack.class, Cloudinary.class } )
public class App {
	public static void main( String[] args ) {
		SpringApplication.run( App.class, args );
	}
}