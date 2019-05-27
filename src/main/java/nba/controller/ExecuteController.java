package nba.controller;

import java.text.ParseException;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import nba.service.Box;

@RestController
public class ExecuteController extends magic.controller.ExecuteController {
	@Autowired
	private Box box;

	@PostMapping( "/execute/{name}/{date}" )
	public Map<String, String> execute( @PathVariable String name, @PathVariable String date ) throws ParseException {
		box.setDate( DateUtils.parseDateStrictly( date, "MMdd" ) );

		return super.execute( name );
	}
}