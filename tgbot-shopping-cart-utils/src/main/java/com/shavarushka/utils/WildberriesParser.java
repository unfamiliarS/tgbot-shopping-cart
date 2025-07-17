package com.shavarushka.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class WildberriesParser {
    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");
        
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        // Добавляем параметры для лучшего обнаружения
        options.setCapability("moz:firefoxOptions", Map.of(
            "args", List.of("--disable-blink-features=AutomationControlled")
        ));

        WebDriver driver = new FirefoxDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        List<String> arts = List.of("181413327", "163352671", "147274740", "263447859", "391018945",
                                "307093242", "6034394", "94341513", "188428974", "164696329");

        try {

            for (String art : arts) {
                String url = "https://www.wildberries.ru/catalog/" + art + "/detail.aspx";
                
                driver.get(url);
    
                wait.until(webDriver -> 
                    ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")
                        .equals("complete")
                );
    
                wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("ins.price-block__final-price.wallet")
                    )
                );
                String price = (String) ((JavascriptExecutor) driver).executeScript(
                    "return document.querySelector('ins.price-block__final-price.wallet')?.innerText || " +
                    "document.querySelector('div.price-block__final-price')?.innerText || " +
                    "document.querySelector('span.final-price')?.innerText"
                );
    
                // Получаем название
                WebElement nameElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("h1.product-page__title")
                    )
                );
                String productName = nameElement.getText();
    
                if (price != null) {
                    price = price.replaceAll("[^0-9]", "").trim();
                    System.out.println("Название товара: " + productName);
                    System.out.println("Цена: " + price + " руб.");
                } else {
                    System.out.println("Цена не найдена. Возможные причины:");
                    System.out.println("1. Товар отсутствует или не продается");
                    System.out.println("2. Изменилась структура страницы");
                    System.out.println("3. Сработала защита от парсинга");
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка при парсинге:");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}