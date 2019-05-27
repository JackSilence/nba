package nba.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import nba.service.Box;

@RestController
public class ExecuteController extends magic.controller.ExecuteController {
	@Autowired
	private Box box;

	@Override
	public Map<String, String> execute( @PathVariable String name, String text ) {
		box.setDate( text );

		return super.execute( name, text );
	}
}