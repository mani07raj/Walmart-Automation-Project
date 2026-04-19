package com.walmart.framework.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigReader — Loads config.properties and exposes typed property getters.
 * System properties (-D flags) take precedence over file-based values.
 *
 * @author Maniraj
 */
public class ConfigReader {

    public static final Logger logger = LogManager.getLogger(ConfigReader.class);

    private Properties prop;

    public Properties init_prop() {
        prop = new Properties();
        try {
            FileInputStream ip = new FileInputStream(
                    "./src/test/resources/config/config.properties");
            prop.load(ip);
            logger.info("config.properties loaded successfully.");
        } catch (FileNotFoundException e) {
            logger.error("config.properties not found.", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Failed to read config.properties.", e);
            e.printStackTrace();
        }
        return prop;
    }

    public String getBrowser() {
        String sys = System.getProperty("browser");
        return (sys != null && !sys.isBlank()) ? sys : prop.getProperty("browser", "chrome");
    }

    public String getBaseUrl() {
        return prop.getProperty("base.url", "https://www.walmart.com");
    }

    public int getImplicitWait() {
        return Integer.parseInt(prop.getProperty("implicit.wait", "10"));
    }

    public int getExplicitWait() {
        return Integer.parseInt(prop.getProperty("explicit.wait", "20"));
    }

    public boolean isHeadless() {
        String sys = System.getProperty("headless");
        if (sys != null && !sys.isBlank()) return Boolean.parseBoolean(sys);
        return Boolean.parseBoolean(prop.getProperty("headless", "false"));
    }

    public String getScreenshotPath() {
        return prop.getProperty("screenshot.path", "target/screenshots");
    }

    public String getReportPath() {
        return prop.getProperty("report.path", "target/reports");
    }

    public String getChromeUserDataDir() {
        return prop.getProperty("chrome.user.data.dir", "");
    }

    public String getChromeProfileName() {
        return prop.getProperty("chrome.profile.name", "Default");
    }
}
