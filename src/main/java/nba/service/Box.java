package nba.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import magic.service.Cloudinary;
import magic.service.IMailService;
import magic.service.Selenium;

@Service
public class Box extends Selenium {
	private static final String DATE_FORMAT = "MM/dd";

	@Autowired
	private Cloudinary cloudinary;

	@Autowired
	private IMailService service;

	private String date;

	public void setDate( String date ) {
		this.date = date;
	}

	@Override
	@Scheduled( cron = "0 0 12,14 * * *" )
	public void exec() {
		run( "--window-size=1263,2487" );
	}

	@Override
	protected synchronized void run( WebDriver driver ) {
		driver.get( "https://www.ptt.cc/bbs/NBA/search?q=box" );

		String date = StringUtils.defaultIfBlank( this.date, new SimpleDateFormat( DATE_FORMAT ).format( new Date() ) );

		setDate( null ); // reset

		try {
			DateUtils.parseDateStrictly( date, DATE_FORMAT );

		} catch ( ParseException e ) {
			throw new RuntimeException( e );

		}

		Map<String, String> box = new HashMap<>();

		list( driver, "#main-container > div.r-list-container > div.r-ent" ).stream().filter( i -> {
			return date.equals( StringUtils.leftPad( find( i, "div.meta > div.date" ).getText(), 5, "0" ) );

		} ).map( i -> find( i, "div.title > a" ) ).filter( i -> i.getText().startsWith( "[BOX ]" ) ).forEach( i -> {
			box.put( i.getAttribute( "href" ), i.getText() );

		} );

		log.info( "Date: {}, box: {}", date, box );

		box.keySet().forEach( i -> {
			driver.get( i );

			script( driver, "$('div[class^=article-metaline]').remove(),$('#main-content').width(1e3).prepend('<span></span>');" );

			script( driver, "var $m=$('#main-content').html().match(/(<span(.*?))--/s);$m&&$('#main-content').html($m[1]);" );

			String subject = String.format( "%s (%s)", box.get( i ), StringUtils.remove( date, "/" ) );

			String url = cloudinary.upload( base64( screenshot( driver, find( driver, "#main-content" ) ) ), subject );

			service.send( subject, String.format( "<a href='%s'><img src='%s'></a>", i, url ) );

		} );
	}
}