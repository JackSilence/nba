package nba.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import nba.service.Box;

@RestController
public class ExecuteController extends magic.controller.ExecuteController {
	@Autowired
	private Box box;

	@Override
	public String execute( @PathVariable String name, String command, String text ) {
		box.setText( text );

		return super.execute( name, command, text );
	}
}