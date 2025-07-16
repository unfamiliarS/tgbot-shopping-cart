package com.shavarushka.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

public class WildberriesParser {
    public static void main(String[] args) {
        // Указываем путь к geckodriver
        System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");

        // Настройки Firefox
        // FirefoxOptions options = new FirefoxOptions();
        // // options.addArguments("--headless");
        // options.addArguments("--width=1920");
        // options.addArguments("--height=1080");

        WebDriver driver = new FirefoxDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            String url = "https://www.wildberries.ru/catalog/147274738/detail.aspx";
            driver.get(url);

            // Явное ожидание загрузки элементов
            WebElement nameElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("h1.product-page__title")
                )
            );

            WebElement priceElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("ins.price-block__final-price.wallet")
                )
            );

            // Получаем данные
            String productName = nameElement.getText();
            String price = priceElement.getText()
                .replaceAll("[^0-9]", "");  // Оставляем только цифры

            System.out.println("Название товара: " + productName);
            System.out.println("Цена: " + price + " руб.");

        } catch (Exception e) {
            System.err.println("Ошибка при парсинге:");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}