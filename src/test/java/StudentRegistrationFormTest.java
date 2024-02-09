import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.Random;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.*;

public class StudentRegistrationFormTest {
    private static final String LATIN_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMBER_SYMBOLS = "0123456789";
    private static final String HOST_URL = "https://demoqa.com/";
    private static final String STUDENT_REGISTRATION_FORM = "automation-practice-form";
    private static final Random RANDOM = new Random();

    private static final String monthSelectorByIndex = String.format(".react-datepicker__month-select [value='%s']", (int) (Math.random() * 11));
    private static final String yearSelectorByIndex = String.format(".react-datepicker__year-select [value='%s']", (int) (Math.random() * (2100 - 1900) + 1900));

    public String generateString(String pattern, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int patternLength = pattern.length();
            int randomIndex = RANDOM.nextInt(patternLength);
            char c = pattern.charAt(randomIndex);
            sb.append(c);
        }
        return sb.toString();
    }

    public String genderChoice() {
        switch (GENDER_TYPE) {
            case 1:
                GENDER = "[for=gender-radio-1]";
                break;
            case 2:
                GENDER = "[for=gender-radio-2]";
                break;
            case 3:
                GENDER = "[for=gender-radio-3]";
                break;
        }
        return GENDER;
    }

    public String hobbiesType() {
        switch (HOBBIE_TYPE) {
            case 1:
                HOBBIE = "[for=hobbies-checkbox-1]";
                break;
            case 2:
                HOBBIE = "[for=hobbies-checkbox-2]";
                break;
            case 3:
                HOBBIE = "[for=hobbies-checkbox-3]";
                break;
        }
        return HOBBIE;
    }

    private final String FIRST_NAME = generateString(LATIN_SYMBOLS, 8);
    private final String LAST_NAME = generateString(LATIN_SYMBOLS, 8);
    private final String EMAIL = generateString(LATIN_SYMBOLS, 6) + "@" + generateString(LATIN_SYMBOLS, 3) + ".test";
    private final int GENDER_TYPE = (int) (Math.random() * 3) + 1;
    private String GENDER = genderChoice();
    private final String MOBILE_NUMBER = generateString(NUMBER_SYMBOLS, 10);
    private final int HOBBIE_TYPE = (int) (Math.random() * 3) + 1;
    private String HOBBIE = hobbiesType();
    private final String ADDRESS = generateString(LATIN_SYMBOLS + NUMBER_SYMBOLS, 300);

    @BeforeAll
    static void beforeCondition() {
        //Configuration.browserSize = "1920x1080";
        Configuration.pageLoadStrategy = "eager";
        Configuration.holdBrowserOpen = true;

    }

    @Test
    void generateRandomUserTest() {
        // Открываем браузер с конкретным эндпоинтом и разворачиваем на весь экран
        open(HOST_URL + STUDENT_REGISTRATION_FORM);
        WebDriverRunner.getWebDriver().manage().window().maximize();
        // Убираем лишние элементы со страницы
        executeJavaScript("document.getElementById('close-fixedban').parentNode.remove()");
        executeJavaScript("document.getElementsByTagName('footer')[0].remove()");
        // Генерируем данные в регистрационной форме
        $("#firstName").setValue(FIRST_NAME);
        $("#lastName").setValue(LAST_NAME);
        $("#userEmail").setValue(EMAIL);
        $(GENDER).click();
        $("#userNumber").setValue(MOBILE_NUMBER);
        $("#dateOfBirthInput").click();
        $(".react-datepicker__month-select").click();
        $(monthSelectorByIndex).click();
        $(".react-datepicker__year-select").click();
        $(yearSelectorByIndex).click();
        $(String.format(".react-datepicker__month [aria-label*=%s]", $(monthSelectorByIndex).getText())).click();
        $("#firstName").setValue(FIRST_NAME);
        $("#subjectsInput").setValue("E"); //TODO: Придумать и подобрать как вводить 1 допустимый символ
        $("#react-select-2-option-2").click(); //TODO: Придумать как выбирать случайный элемент
        $(HOBBIE).click();
        $("#uploadPicture").uploadFile(new File("src/test/data/Test_file.txt")); //TODO: Добавить автоматическую генерацию файла и его загрузку
        $("#currentAddress").setValue(ADDRESS);
        $(withText("Select State")).scrollTo();
        $(withText("Select State")).click();
        $("#react-select-3-option-3").click(); //TODO: Прикрутить случайный выбор элемента
        $(withText("Select City")).click();
        $("#react-select-4-option-1").click(); //TODO: Прикрутить случайный выбор элемента в зависимости от штата
        $("#submit").click();
        // Проверяем заполненные поля
        assertEquals($("#example-modal-sizes-title-lg").getText(), "Thanks for submitting the form");
        // Пришлось приобразовать исходную дату в нужный формат
        String dateFromInput = $("#dateOfBirthInput").getValue();
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("dd MMM yyyy")
                .toFormatter(Locale.ENGLISH);

        String formattedDate = LocalDate.parse(dateFromInput, formatter)
                .format(DateTimeFormatter.ofPattern("dd MMMM,yyyy", Locale.ENGLISH));

        String[][] expectedData = {
                {"Student Name", FIRST_NAME + " " + LAST_NAME},
                {"Student Email", EMAIL},
                {"Gender", $(GENDER).getText()},
                {"Mobile", MOBILE_NUMBER},
                {"Date of Birth", formattedDate},
                {"Subjects", $("#subjectsContainer").getText()},
                {"Hobbies", $(HOBBIE).getText()},
                {"Picture", "Test_file.txt"},
                {"Address", ADDRESS},
                {"State and City", "Rajasthan Jaiselmer"}
        };
        for (int row = 0; row < expectedData.length; row++) {
            for (int column = 0; column < 2; column++) {
                $(".modal-body tbody")
                        .$$("tr").get(row)
                        .$$("td").get(column)
                        .shouldHave(text(expectedData[row][column]));
            }
        }
    }
}