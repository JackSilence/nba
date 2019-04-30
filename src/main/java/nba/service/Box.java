package nba.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import io.github.bonigarcia.wdm.WebDriverManager;

@Service
public class Box implements IService {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	@Autowired
	private IMailService service;

	@Value( "${GOOGLE_CHROME_SHIM:}" )
	private String bin;

	@Override
	public void exec() {
		WebDriver driver = init();

		driver.get( "https://www.ptt.cc/bbs/NBA/search?q=box" );

		String today = new SimpleDateFormat( "MM/dd" ).format( new Date() );

		Map<String, String> box = new HashMap<>();

		driver.findElements( By.cssSelector( "#main-container > div.r-list-container > div.r-ent" ) ).stream().filter( i -> {
			return today.equals( StringUtils.leftPad( find( i, "div.meta > div.date" ).getText(), 5, "0" ) );

		} ).map( i -> find( i, "div.title > a" ) ).forEach( i -> box.put( i.getAttribute( "href" ), i.getText() ) );

		box.keySet().forEach( i -> {
			driver.get( i );

			( ( JavascriptExecutor ) driver ).executeScript( "$('#main-content').prepend('<span></span>');var $m=$('#main-content').html().match(/(<span(.*?))--/s);$m&&$('#main-content').html($m[1]);" );

			WebElement element = driver.findElement( By.cssSelector( "#main-content" ) );

			File screenshot = ( ( TakesScreenshot ) driver ).getScreenshotAs( OutputType.FILE );

			Point point = element.getLocation();

			Dimension size = element.getSize();

			int x = point.getX(), y = point.getY(), width = size.getWidth(), height = size.getHeight();

			try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
				ImageIO.write( ImageIO.read( screenshot ).getSubimage( x, y, width, height ), "png", stream );

				String file = String.format( "data:image/png;base64,%s", DatatypeConverter.printBase64Binary( stream.toByteArray() ) );

				String subject = String.format( "%s (%s)", box.get( i ), StringUtils.remove( today, "/" ) );

				Map<?, ?> result = new Cloudinary().uploader().upload( file, ObjectUtils.asMap( "public_id", subject ) );

				service.send( subject, String.format( "<img src='%s'>", result.get( "secure_url" ) ).toString() );

			} catch ( IOException e ) {
				log.error( "", e );

			}
		} );
	}

	private WebDriver init() {
		ChromeOptions options = new ChromeOptions();

		if ( bin.isEmpty() ) {
			WebDriverManager.chromedriver().setup();

		} else {
			System.setProperty( "webdriver.chrome.driver", "/app/.chromedriver/bin/chromedriver" );

			options.setBinary( bin );

		}

		options.addArguments( "--headless", "--disable-gpu", "--window-size=1600,3840" );

		return new ChromeDriver( options );
	}

	private WebElement find( WebElement element, String css ) {
		return element.findElement( By.cssSelector( css ) );
	}
}