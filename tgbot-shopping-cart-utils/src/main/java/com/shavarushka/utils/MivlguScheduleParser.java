package com.shavarushka.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;

public class MivlguScheduleParser {
    public static void main(String[] args) {
        // System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");
        System.setProperty("webdriver.chrome.driver", "/home/semyon/bin/chromedriver/chromedriver");
        
        var options = new ChromeOptions();
        options.setBinary("/home/semyon/bin/chrome-headless-shell/chrome-headless-shell");
        options.addArguments("--headless");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            String url = "https://www.mivlgu.ru/raspisanie?group=ИС-122&semester=2&year=2024#schedule-all";
            driver.get(url);

            // Ждем загрузки расписания
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.schedule.row")
            ));

            // Ищем блок понедельника
            List<WebElement> dayPanels = driver.findElements(By.cssSelector("div.day-group div.day"));
            WebElement mondayPanel = null;

            for (WebElement panel : dayPanels) {
                try {
                    WebElement heading = panel.findElement(By.cssSelector("div.panel.panel-primary div.title.panel-heading"));
                    if (heading.getText().trim().equalsIgnoreCase("понедельник")) {
                        mondayPanel = panel;
                        break;
                    }
                } catch (NoSuchElementException ignored) {}
            }

            if (mondayPanel == null) {
                System.out.println("Расписание на понедельник не найдено");
                return;
            }

            // Парсим пары
            List<WebElement> lessons = mondayPanel.findElements(By.cssSelector("div.para.panel-body"));
            
            System.out.println("══════════════════════════════════");
            System.out.println("    РАСПИСАНИЕ НА ПОНЕДЕЛЬНИК");
            System.out.println("══════════════════════════════════");

            for (WebElement lesson : lessons) {
                try {
                    String subject = lesson.findElement(By.cssSelector("span.discipline-name")).getText();
                    String type = lesson.findElement(By.cssSelector("span.type")).getText();
                    String teacher = lesson.findElement(By.cssSelector("span.name")).getText();
                    String room = lesson.findElement(By.cssSelector("span.aud")).getText();

                    System.out.println("▌ " + subject);
                    System.out.println("▌ Тип: " + type);
                    System.out.println("▌ Преподаватель: " + teacher);
                    System.out.println("▌ Аудитория: " + room);
                    System.out.println("──────────────────────────────────");
                } catch (NoSuchElementException e) {
                    System.out.println("▌ Неполные данные о паре");
                    System.out.println("──────────────────────────────────");
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