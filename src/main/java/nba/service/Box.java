package nba.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import magic.service.Cloudinary;
import magic.service.IMailService;
import magic.service.Selenium;
import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;

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
		String today = new SimpleDateFormat( DATE_FORMAT ).format( new Date() ), date = StringUtils.defaultIfBlank( this.date, today );

		setDate( null ); // reset

		try {
			DateUtils.parseDateStrictly( date, DATE_FORMAT );

		} catch ( ParseException e ) {
			throw new RuntimeException( e );

		}

		Map<String, String> box = new HashMap<>();

		int max = date.equals( today ) ? 1 : 10;

		for ( int i = 1; i <= max; i++ ) {
			driver.get( "https://www.ptt.cc/bbs/NBA/search?q=box&page=" + i );

			log.info( "Page: " + i );

			list( driver, "#main-container > div.r-list-container > div.r-ent" ).stream().filter( j -> {
				return date.equals( StringUtils.leftPad( find( j, "div.meta > div.date" ).getText(), 5, "0" ) );

			} ).map( j -> find( j, "div.title > a" ) ).filter( j -> j.getText().startsWith( "[BOX ]" ) ).forEach( j -> {
				box.put( j.getAttribute( "href" ), j.getText() );

			} );

			if ( !box.isEmpty() ) {
				break;

			}
		}

		log.info( "Date: {}, box: {}", date, box );

		SlackMessage message = new SlackMessage( Utils.subject( box.isEmpty() ? date + "查無NBA賽事" : "NBA比賽結果" ) );

		for ( String i : box.keySet() ) {
			String title = String.format( "%s (%s)", box.get( i ), StringUtils.remove( date, "/" ) ), url;

			if ( ( url = cloudinary.explicit( title ) ).isEmpty() ) {
				driver.get( i );

				WebElement element = find( driver, "div.bbs-screen" );

				if ( StringUtils.isEmpty( element.getAttribute( "id" ) ) ) {
					log.error( "Url: {}, text: {}", i, element.getText() );

					continue;

				}

				script( driver, "$('div[class^=article-metaline]').remove(),$('#main-content').width(1e3).prepend('<span></span>');" );

				script( driver, "var $m=$('#main-content').html().match(/(<span(.*?))--/s);$m&&$('#main-content').html($m[1]);" );

				url = cloudinary.upload( base64( screenshot( driver, find( driver, "#main-content" ) ) ), title );

				service.send( title, String.format( "<a href='%s'><img src='%s'></a>", i, url ) ); // 原本不存在才發信
			}

			message.addAttachments( new SlackAttachment( title ).setTitle( title ).setTitleLink( i ).setImageUrl( url ) );
		}

		slack.call( message );
	}
}