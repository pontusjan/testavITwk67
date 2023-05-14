import com.codeborne.selenide.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byLinkText;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LTU_Tests {



    @BeforeAll
    public static void setUp() {
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;
    }

    @AfterAll
    public static void tearDown() {
        Selenide.closeWebDriver();
    }

    private static JsonNode getCredentials() throws IOException {
        byte[] jsonData = Files.readAllBytes(Paths.get("src/main/credentials.json"));
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonData);
    }

    @Test
    @Order(1)
    // Test to login to LTU
    public void loginToLTU() throws IOException {
        // Get credentials from credentials.json
        JsonNode credentials = getCredentials();
        String email = credentials.get("ltuCredentials").get("email").asText();
        String password = credentials.get("ltuCredentials").get("password").asText();

        // Login to LTU
        Selenide.open("https://www.ltu.se/");
        $("button[id='CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll']").click();
        $(By.xpath("//a[contains(text(), 'Student')]")).click();
        $(By.linkText("Logga in")).click();
        $("#username").setValue(email);
        $("#password").setValue(password);
        $("input.btn-submit[name='submit']").click();
    }


    @Test
    @Order(2)
    // Test to create a transcript
    public void createTranscript() throws IOException, URISyntaxException {
        // Assume you are already logged in to LTU
        Selenide.open("https://portal.ltu.se/group/student/start");

        //Make sure ladok opens in same window.
        SelenideElement link = $("a[href='https://www.student.ladok.se/student/#/intyg']");
        String href = link.getAttribute("href");
        executeJavaScript("arguments[0].setAttribute('target', '_self')", link.getWrappedElement());
        link.click();


        // Login to Ladok
        $("a.btn.btn-large.btn-ladok-inloggning").click();
        $("#searchinput").setValue("Lulea");
        $("div.d-block.institution-text").click();

        // Go to the transcript page
        $("a.nav-link.no-underline[href='/student/app/studentwebb/intyg']").click();

        // Create a transcript
        $("button.btn.btn-ladok-brand[title='Skapa intyg']").click();
        $("#intygstyp").selectOptionByValue("2: Object");
        $("button.btn.btn-ladok-brand.text-nowrap.me-lg-3").click();

    }

    @Test
    @Order(3)
    // Test to download a transcript
    public void downloadTranscript() throws IOException, URISyntaxException {
        // Download the transcript
        // Find the link element by its text
        SelenideElement link = $$("a.card-link").filterBy(text("Resultatintyg")).first();

        // Get the href attribute value
        String pdfUrl = link.getAttribute("href");

        // Download the PDF document
        File downloadedFile = download(pdfUrl);

    }


    @Test
    @Order(4)
    // Test to find out when the Testing of IT System final examination is
    public void findFinalExaminationInfo() throws IOException {
        // Go back to Ladok start
        $("a.nav-link.no-underline[href='/student/app/studentwebb/start']").click();
        // Find the element with the final examination info
        SelenideElement dateTimeElement = $("div.card-body span.ladok-card-body-sub-rubrik ladok-examinationstillfalle-kort-underrubrik");

        // Print the date and time
        String dateTime = dateTimeElement.getText();
        System.out.println("Date and Time: " + dateTime);

    }

    @Test
    @Order(5)
    // Test to take a screenshot of the final examination info
    public void takeScreenshotOfFinalExaminationInfo() {
        // Assume you are already on th epage of test 4
        SelenideElement element = $("div.card-body span.ladok-card-body-rubrik > ladok-examinationstillfalle-kort-rubrik > span").shouldHave(Condition.text("I0015N"));

        // Take a screenshot of the element
        File screenshotsDirectory = new File("target/screenshots");
        if (!screenshotsDirectory.exists()) {
            screenshotsDirectory.mkdirs();
        }

        // Delete the screenshot if it already exists
        File screenshotFile = new File("target/screenshots/final_examination.jpeg");
        if (screenshotFile.exists()) {
            screenshotFile.delete();
        }

        // Take the screenshot
        Screenshots.takeScreenShotAsFile();
        File screenshot = Screenshots.getLastScreenshot();
        screenshot.renameTo(screenshotFile);

    }





    @Test
    @Order(6)
    // Test to download the syllabus for the course
    public void downloadSyllabus() throws IOException, URISyntaxException {
        // Go back to LTU start
        Selenide.open("https://www.ltu.se/"); // Open LTU.se
        $(By.xpath("//a[contains(text(), 'Student')]")).click();
        $(By.linkText("Logga in")).click();

        //Make sure canvas opens in same window.
        SelenideElement link = $(byText("Kursrum"));
        String href = link.getAttribute("href");
        executeJavaScript("arguments[0].setAttribute('target', '_self')", link.getWrappedElement());
        link.click();

        // Open course page
        $("#global_nav_courses_link").click();
        $(byLinkText("I0015N, Test av IT-system, Lp4, V23")).click();

        // Make sure the link opens in the same window
        SelenideElement link2 = $("a.external[href='https://www.ltu.se/edu/course/I00/I0015N/I0015N-Test-av-IT-system-1.81215?kursView=kursplan&termin=V23']");
        String href2 = link2.getAttribute("href");
        executeJavaScript("arguments[0].setAttribute('target', '_self')", link2.getWrappedElement());
        link2.click();

        // Download the PDF document
        String pdfUrl = $("a.utbplan-pdf-link").getAttribute("href");
        Selenide.download(pdfUrl);

    }


    @Test
    @Order(7)
    // Test to logout from LTU
    public void logout() {
        // Perform the logout action
        $(By.xpath("//a[contains(text(), 'Student')]")).click();
        $(By.linkText("Logga in")).click();

        $("li.user-avatar.dropdown").click();
        $("li.sign-out a").click();


    }

}
