package nba.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import magic.service.IMailService;
import magic.service.Selenium;
import magic.util.Utils;

@Service
public class Box extends Selenium {
	@Autowired
	private IMailService service;

	@Override
	@Scheduled( cron = "0 0 12,14 * * *" )
	public void exec() {
		run( "--window-size=1600,3840" );
	}

	@Override
	protected void run( WebDriver driver ) {
		driver.get( "https://www.ptt.cc/bbs/NBA/search?q=box" );

		String today = new SimpleDateFormat( "MM/dd" ).format( new Date() );

		Map<String, String> box = new HashMap<>();

		driver.findElements( By.cssSelector( "#main-container > div.r-list-container > div.r-ent" ) ).stream().filter( i -> {
			return "Rambo".equals( find( i, "div.meta > div.author" ).getText() ) && today.equals( StringUtils.leftPad( find( i, "div.meta > div.date" ).getText(), 5, "0" ) );

		} ).map( i -> find( i, "div.title > a" ) ).forEach( i -> box.put( i.getAttribute( "href" ), i.getText() ) );

		box.keySet().forEach( i -> {
			driver.get( i );

			script( driver, "$('div[class^=article-metaline]').remove(),$('#main-content').width(1e3).prepend('<span></span>');" );

			script( driver, "var $m=$('#main-content').html().match(/(<span(.*?))--/s);$m&&$('#main-content').html($m[1]);" );

			String subject = String.format( "%s (%s)", box.get( i ), StringUtils.remove( today, "/" ) );

			String url = Utils.upload( base64( screenshot( driver, driver.findElement( By.cssSelector( "#main-content" ) ) ) ), subject );

			service.send( subject, String.format( "<a href='%s'><img src='%s'></a>", i, url ) );

		} );
	}

	private void script( WebDriver driver, String script ) {
		( ( JavascriptExecutor ) driver ).executeScript( script );
	}

	private WebElement find( WebElement element, String css ) {
		return element.findElement( By.cssSelector( css ) );
	}
}