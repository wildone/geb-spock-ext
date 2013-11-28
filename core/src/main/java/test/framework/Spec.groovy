package test.framework

import geb.Browser
import geb.Configuration
import geb.spock.GebSpec
import org.junit.runner.RunWith
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import test.framework.config.Target
import test.framework.geb.ExtendedConfigLoader
import test.framework.geb.ExtendedConfiguration
import test.framework.selenium.Window
import test.framework.spock.ExtendedSputnik

@RunWith(ExtendedSputnik.class)
abstract class Spec extends GebSpec {

    static final String MODE_PROPERTY = 'mode'
    static final String TEST_MODE = 'test'
    static final String LEARN_MODE = 'learn'

    // one configuration for all tests
    static ExtendedConfiguration _configuration
    static Target _target

    protected _screenshot

    private Window _window
    private File _reportDir
    private boolean _loggedIn

    // Geb modifications

    static ExtendedConfiguration createConfiguration() {
        new ExtendedConfigLoader(null, System.properties, new GroovyClassLoader(getClass().classLoader)).getConf(null)
    }

    /**
     * Use this (static) configuration for all Browser instances (one configuration for all tests).
     */
    static ExtendedConfiguration getConfiguration() {
        if (!_configuration) {
            _configuration = createConfiguration()
        }
        _configuration
    }

    @Override
    Configuration getConfig() {
        configuration
    }

    @Override
    Browser createBrowser() {
        new Browser(configuration)
    }

    // Target and baseUrl

    Target getTarget() {
        if (!_target) {
            if (config.targetConf) {
                _target = config.targetConf.call()
            }
        }
        _target
    }

    @Override
    void setBaseUrl(String path = "") {
        if (!path.matches("^http(s)?://.*")) {
            path = target.getBaseUrl(path)
        }
        super.setBaseUrl(path)
    }

    // login

    void login() {
        if (!_loggedIn) {
            if (config.loginConf) {
                if (config.verbose) {
                    println "login ${config.username}"
                }
                String currentBase = getBaseUrl()
                setBaseUrl(target.serverUrl)
                config.loginConf.call(this, config.username, config.password)
                setBaseUrl(currentBase)
            }
            _loggedIn = true
        }
    }

    @Override
    void resetBrowser() {
        super.resetBrowser()
        _loggedIn = false
    }

    void setup() {
        if (config.driverConf) {
            setBaseUrl(target.getBaseUrl())
            login()
        }
    }

    // Test Mode

    String getMode() {
        config.properties.get(MODE_PROPERTY, TEST_MODE)
    }

    boolean isLearnMode() {
        mode == LEARN_MODE
    }

    // screenshots

    Screenshot screenshot() {
        _screenshot = new Screenshot(this)
    }

    Screenshot getScreenshot() {
        if (_screenshot == null) {
            screenshot()
        }
        _screenshot
    }

    // selenium (Geb extensions)

    Window getWindow() {
        if (_window == null) {
            _window = new Window(browser)
        }
        _window
    }

    void wait(int seconds) {
        WebDriverWait wait = new WebDriverWait(driver, seconds)
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/shouldNotBeFound")))
        } catch (Exception e) {
            // ok
        }
    }

    // reporting

    void reportGroup(String path) {
        browser.reportGroup(path.replace(' ', '_'))
        report "${this.class.name} / ${path} ..."
    }

    void report(String message) {
        println message
    }

    File getReportBase() {
        new File(/*config.reportsDir ?: */ new File('test'), browser.driver.class.simpleName);
    }

    File getReportSpecDir() {
        new File(reportBase, this.class.name.replace('.', '/').toLowerCase())
    }

    File getReportDir() {
        if (!_reportDir) {
            _reportDir = new File(reportSpecDir, browser.reportGroup ?: '.')
        }
        _reportDir
    }
}
