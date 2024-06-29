package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
// import io.github.bonigarcia.wdm.WebDriverManager;
import demo.wrappers.Wrappers;

public class TestCases {
    ChromeDriver driver;
    private Wrappers wrappers;
    


    /*
     * TODO: Write your tests here with testng @Test annotation. 
     * Follow `testCase01` `testCase02`... format or what is provided in instructions
     */

     
    /*
     * Do not change the provided methods unless necessary, they will help in automation and assessment
     */
    @BeforeTest
    public void startBrowser()
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        // NOT NEEDED FOR SELENIUM MANAGER
        // WebDriverManager.chromedriver().timeout(30).setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log"); 

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
        wrappers = new Wrappers(driver);

    }

    @AfterTest
    public void endTest()
    {
        driver.close();
        driver.quit();

    }

    @Test
    public void testCase01() throws IOException {
        System.out.println("Test case 01 started");
        wrappers.navigateToUrl("https://www.scrapethissite.com/pages/");
        wrappers.clickElement(By.linkText("Hockey Teams: Forms, Searching and Pagination"));

        wrappers.waitForPageToLoad();
        List<HashMap<String, Object>> hockeyData = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            List<WebElement> rows = wrappers.findElements(By.xpath("//table[@class='table']//tr[@class='team']"));
            
            for (WebElement row : rows) {
                List<WebElement> cols = row.findElements(By.tagName("td"));
            
                if (cols.size() > 0) {
                    String teamName = cols.get(0).getText();
                    String year = cols.get(1).getText();
                    String winPercentageStr = cols.get(5).getText().trim();
                    double winPercentage = Double.parseDouble(winPercentageStr);

                    if (winPercentage < 0.40) {
                        HashMap<String, Object> teamData = new HashMap<>();
                        teamData.put("Epoch Time of Scrape", System.currentTimeMillis());
                        teamData.put("Team Name", teamName);
                        teamData.put("Year", year);
                        teamData.put("Win %", winPercentageStr);
                        hockeyData.add(teamData);
                    }
                }
            }

            // Check if there is a next page and click it
            WebElement nextButton = driver.findElement(By.xpath("//a[@aria-label='Next']"));
            if (nextButton != null && nextButton.isDisplayed() && nextButton.isEnabled()) {
                nextButton.click();
                wrappers.waitForPageToLoad();
            } else {
                System.out.println("Next Button is not clicked");
                break;
            }
        }

        // Convert ArrayList to JSON and save to file
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("hockey-team-data.json"), hockeyData);
        System.out.println("Test case 01 completed");
    }

    @Test
    public void testCase02() throws IOException {
        System.out.println("Test case 02 started");
        wrappers.navigateToUrl("https://www.scrapethissite.com/pages/");
        wrappers.clickElement(By.linkText("Oscar Winning Films: AJAX and Javascript"));

        wrappers.waitForPageToLoad();
        List<HashMap<String, Object>> oscarData = new ArrayList<>();

        List<WebElement> yearLinks = driver.findElements(By.cssSelector(".year-link"));

        for (WebElement yearLink : yearLinks) {
            String year = yearLink.getText();
            yearLink.click();
            wrappers.waitForElement(By.xpath("//tbody[@id='table-body']//tr"));

            List<WebElement> movieRows = driver.findElements(By.xpath("//tbody[@id='table-body']//tr"));
            for (int i = 0; i < Math.min(5, movieRows.size()); i++) {
                WebElement row = movieRows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() > 0) {
                    String title = cols.get(0).getText();
                    String nomination = cols.get(1).getText();
                    String awards = cols.get(2).getText();
                    
                    boolean isWinner = false;
                    WebElement tdElement = cols.get(3); // Assuming cols.get(3) is the fourth column (index 3)
                    List<WebElement> iconElements = tdElement.findElements(By.cssSelector("i.glyphicon"));
                    for (WebElement iconElement : iconElements) {
                    if (iconElement.getAttribute("class").contains("glyphicon")) {
                    isWinner = true;
                    break; // Exit the loop if glyphicon class is found
                    }
}


                    HashMap<String, Object> movieData = new HashMap<>();
                    movieData.put("Epoch Time of Scrape", System.currentTimeMillis());
                    movieData.put("Year", year);
                    movieData.put("Title", title);
                    movieData.put("Nomination", nomination);
                    movieData.put("Awards", awards);
                    movieData.put("isWinner", isWinner);
                    oscarData.add(movieData);                    
                }
            }

            // Go back to the year selection page
            driver.navigate().back();
            wrappers.waitForPageToLoad();
        }

        // Convert ArrayList to JSON and save to file
        File outputFile = new File("output/oscar-winner-data.json");
        outputFile.getParentFile().mkdirs(); // Create directories if they do not exist
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputFile, oscarData);

        // Assert that the file is created and is not empty
        Assert.assertTrue(outputFile.exists(), "File does not exist");
        Assert.assertTrue(outputFile.length() > 0, "File is empty");

        System.out.println("Test case 02 completed");
    }


}