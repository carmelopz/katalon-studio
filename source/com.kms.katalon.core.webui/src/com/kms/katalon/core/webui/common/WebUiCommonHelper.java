package com.kms.katalon.core.webui.common;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.exception.StepFailedException;
import com.kms.katalon.core.helper.KeywordHelper;
import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.testobject.ConditionType;
import com.kms.katalon.core.testobject.SelectorMethod;
import com.kms.katalon.core.testobject.TestObject;
import com.kms.katalon.core.testobject.TestObjectProperty;
import com.kms.katalon.core.testobject.TestObjectXpath;
import com.kms.katalon.core.util.internal.ExceptionsUtil;
import com.kms.katalon.core.webui.common.XPathBuilder.PropertyType;
import com.kms.katalon.core.webui.common.internal.ByScreenshotLocatorController;
import com.kms.katalon.core.webui.common.internal.SmartXPathController;
import com.kms.katalon.core.webui.constants.CoreWebuiMessageConstants;
import com.kms.katalon.core.webui.constants.StringConstants;
import com.kms.katalon.core.webui.driver.DriverFactory;
import com.kms.katalon.core.webui.exception.WebElementNotFoundException;

public class WebUiCommonHelper extends KeywordHelper {

	private static final KeywordLogger logger = KeywordLogger.getInstance(WebUiCommonHelper.class);

	public static final String CSS_LOCATOR_PROPERTY_NAME = "css";

	public static final String XPATH_LOCATOR_PROPERTY_NAME = "xpath";

	public static final String WEB_ELEMENT_TAG = "tag";

	public static final String WEB_ELEMENT_ATTRIBUTE_LINK_TEXT = "link_text";

	public static final String WEB_ELEMENT_ATTRIBUTE_TEXT = "text";

	public static final String WEB_ELEMENT_XPATH = "xpath";

	private static AtomicInteger atomicCounter = new AtomicInteger(0);

	public static boolean isTextPresent(WebDriver webDriver, String text, boolean isRegex)
			throws WebDriverException, IllegalArgumentException {
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logDebug(MessageFormat.format(StringConstants.COMM_EXC_CHECKING_TEXT_PRESENT, regularExpressionLog));
		if (text == null) {
			throw new IllegalArgumentException(StringConstants.COMM_EXC_TEXT_IS_NULL);
		}

		boolean isContained = false;
		WebElement bodyElement = webDriver.findElement(By.tagName("body"));
		String pageText = bodyElement.getText();

		logger.logDebug(
				MessageFormat.format(StringConstants.COMM_LOG_INFO_FINDING_TEXT_ON_PAGE, text, regularExpressionLog));
		if (pageText != null && !pageText.isEmpty()) {
			if (isRegex) {
				Pattern pattern = Pattern.compile(text);
				Matcher matcher = pattern.matcher(pageText);
				while (matcher.find()) {
					isContained = true;
					break;
				}
			} else {
				isContained = pageText.contains(text);
			}
		}
		return isContained;
	}

	public static boolean switchToWindowUsingTitle(WebDriver webDriver, String title, int timeOutInSeconds)
			throws WebDriverException, InterruptedException {
		float timeCount = 0;
		while (timeCount < timeOutInSeconds) {
			Set<String> availableWindows = webDriver.getWindowHandles();
			for (String windowId : availableWindows) {
				webDriver = webDriver.switchTo().window(windowId);
				if (webDriver.getTitle().equals(title)) {
					return true;
				}
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean closeWindowUsingTitle(WebDriver webDriver, String title) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			Set<String> availableWindows = webDriver.getWindowHandles();
			for (String windowId : availableWindows) {
				webDriver = webDriver.switchTo().window(windowId);
				if (webDriver.getTitle().equals(title)) {
					webDriver.close();
					return true;
				}
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean switchToWindowUsingUrl(WebDriver webDriver, String url) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			Set<String> availableWindows = webDriver.getWindowHandles();
			for (String windowId : availableWindows) {
				if (webDriver.switchTo().window(windowId).getCurrentUrl().equals(url)) {
					return true;
				}
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean closeWindowUsingUrl(WebDriver webDriver, String url) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			Set<String> availableWindows = webDriver.getWindowHandles();
			for (String windowId : availableWindows) {
				if (webDriver.switchTo().window(windowId).getCurrentUrl().equals(url)) {
					webDriver.close();
					return true;
				}
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean switchToWindowUsingIndex(WebDriver webDriver, int index) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			List<String> availableWindows = new ArrayList<String>(webDriver.getWindowHandles());
			if (index >= 0 && index < availableWindows.size()) {
				webDriver.switchTo().window(availableWindows.get(index));
				return true;
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean closeWindowUsingIndex(WebDriver webDriver, int index) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			List<String> availableWindows = new ArrayList<String>(webDriver.getWindowHandles());
			if (index >= 0 && index < availableWindows.size()) {
				webDriver.switchTo().window(availableWindows.get(index));
				webDriver.close();
				return true;
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static void checkSelectIndex(Integer[] indexes, Select select) throws IllegalArgumentException {
		logger.logDebug(StringConstants.COMM_LOG_INFO_CHECKING_INDEX_PARAMS);
		List<WebElement> allSelectOptions = select.getOptions();
		if (allSelectOptions.size() > 0) {
			for (int index : indexes) {
				if (index < 0 || index >= allSelectOptions.size()) {
					throw new IllegalArgumentException(MessageFormat.format(StringConstants.COMM_EXC_INVALID_INDEX,
							index, (allSelectOptions.size() - 1)));
				}
			}
		}
	}

	public static void selectOrDeselectAllOptions(Select select, boolean isSelect, TestObject to) {
		if (isSelect) {
			logger.logDebug(
					MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTING_ALL_OPT_ON_OBJ, to.getObjectId()));
		} else {
			logger.logDebug(
					MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTING_ALL_OPTS_ON_OBJ, to.getObjectId()));
		}
		for (int index = 0; index < select.getOptions().size(); index++) {
			if (isSelect) {
				select.selectByIndex(index);
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_W_INDEX_X_IS_SELECTED, index));
			} else {
				select.deselectByIndex(index);
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTED_OPT_IDX_X, index));
			}
		}
	}

	public static void selectOrDeselectOptionsByIndex(Select select, Integer[] indexes, boolean isSelect,
			TestObject to) {
		WebUiCommonHelper.checkSelectIndex(indexes, select);
		if (isSelect) {
			logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTING_OBJ_OPTS_W_INDEX_IN,
					to.getObjectId(), WebUiCommonHelper.integerArrayToString(indexes)));
		} else {
			logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTING_OPTS_ON_OBJ_W_IDX,
					to.getObjectId(), WebUiCommonHelper.integerArrayToString(indexes)));
		}
		for (int index : indexes) {
			if (isSelect) {
				select.selectByIndex(index);
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_W_INDEX_X_IS_SELECTED, index));
			} else {
				select.deselectByIndex(index);
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTED_OPT_IDX_X, index));
			}
		}
	}

	public static boolean selectOrDeselectOptionsByValue(Select select, String value, boolean isRegex, boolean isSelect,
			TestObject to, String regularExpressionLog) {
		if (isSelect) {
			logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTING_OPTS_ON_OBJ_X_W_VAL_Y,
					to.getObjectId(), value, regularExpressionLog));
		} else {
			logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTING_OPTS_ON_OBJ_W_VAL,
					to.getObjectId(), value, regularExpressionLog));
		}
		if (isRegex) {
			return selectOrDeselectOptionsByValueByRegularExpression(select, value, isSelect, regularExpressionLog);
		} else {
			if (isSelect) {
				select.selectByValue(value);
			} else {
				select.deselectByValue(value);
			}
			return true;
		}
	}

	private static boolean selectOrDeselectOptionsByValueByRegularExpression(Select select, String value,
			boolean isSelect, String regularExpressionLog) {
		List<WebElement> allOptions = select.getOptions();
		boolean isMatched = false;
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getAttribute("value");
			if (optionValue == null || !WebUiCommonHelper.match(optionValue, value, true)) {
				continue;
			}
			if (isSelect) {
				select.selectByIndex(index);
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTED_OPT_AT_INDEX_W_VAL, index,
						optionValue, regularExpressionLog));
			} else {
				select.deselectByIndex(index);
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_AT_IDX_X_W_VAL_Y_IS_SELECTED,
						index, optionValue, regularExpressionLog));
			}
			isMatched = true;
		}
		return isMatched;
	}

	public static boolean selectOrDeselectOptionsByLabel(Select select, String label, boolean isRegex, boolean isSelect,
			TestObject to, String regularExpressionLog) {
		if (isSelect) {
			logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTING_OPTS_ON_OBJ_X_W_LBL_Y,
					to.getObjectId(), label, regularExpressionLog));
		} else {
			logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTING_OPTS_ON_OBJ_X_W_LBL_Y,
					to.getObjectId(), label, regularExpressionLog));
		}
		if (isRegex) {
			return selectOrDeselectOptionsByLabelWithRegularExpression(select, label, isSelect, regularExpressionLog);
		} else {
			if (isSelect) {
				select.selectByVisibleText(label);
			} else {
				select.deselectByVisibleText(label);
			}
			return true;
		}
	}

	private static boolean selectOrDeselectOptionsByLabelWithRegularExpression(Select select, String label,
			boolean isSelect, String regularExpressionLog) {
		List<WebElement> allOptions = select.getOptions();
		boolean isMatched = false;
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getText();
			if (optionValue == null || !WebUiCommonHelper.match(optionValue, label, true)) {
				continue;
			}
			if (isSelect) {
				select.selectByIndex(index);
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_AT_IDX_X_W_LBL_TXT_Y_IS_SELECTED,
						index, optionValue, regularExpressionLog));
			} else {
				select.deselectByIndex(index);
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_AT_IDX_X_W_LBL_TXT_Y_IS_DESELECTED,
						index, optionValue, regularExpressionLog));
			}
			isMatched = true;
		}
		return isMatched;
	}

	public static int getNumberOfOptionByLabel(Select select, String label, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_LBL_PRESENT_ON_OBJ,
				label, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionLabel = allOptions.get(index).getText();
			if (optionLabel != null && KeywordHelper.match(optionLabel, label, isRegex)) {
				count++;
				logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_LBL_IS_PRESENT, index,
						optionLabel, regularExpressionLog));
			}
		}
		return count;
	}

	public static int getNumberOfOptionByValue(Select select, String value, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_VAL_PRESENT_ON_OBJ,
				value, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getAttribute("value");
			if (optionValue != null && KeywordHelper.match(optionValue, value, isRegex)) {
				count++;
				logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_VAL_IS_PRESENT, index,
						optionValue, regularExpressionLog));
			}
		}
		return count;
	}

	public static int getNumberOfSelectedOptionByLabel(Select select, String label, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_LBL_SELECTED_ON_OBJ,
				label, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionLabel = allOptions.get(index).getText();
			if (optionLabel != null && KeywordHelper.match(optionLabel, label, isRegex)) {
				if (allSelectedOptions.contains(allOptions.get(index))) {
					count++;
					logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_LBL_IS_SELECTED,
							index, optionLabel, regularExpressionLog));
				}
			}
		}
		return count;
	}

	public static int getNumberOfNotSelectedOptionByLabel(Select select, String label, boolean isRegex,
			String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_LBL_NOT_SELECTED_ON_OBJ,
				label, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionLabel = allOptions.get(index).getText();
			if (optionLabel != null && KeywordHelper.match(optionLabel, label, isRegex)) {
				if (!allSelectedOptions.contains(allOptions.get(index))) {
					count++;
					logger.logDebug(
							MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_LBL_IS_NOT_SELECTED,
									index, optionLabel, regularExpressionLog));
				}
			}
		}
		return count;
	}

	public static int getNumberOfSelectedOptionByValue(Select select, String value, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_VAL_SELECTED_ON_OBJ,
				value, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getAttribute("value");
			if (optionValue != null && KeywordHelper.match(optionValue, value, isRegex)) {
				if (allSelectedOptions.contains(allOptions.get(index))) {
					count++;
					logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_VAL_IS_SELECTED,
							index, optionValue, regularExpressionLog));
				}
			}
		}
		return count;
	}

	public static int getNumberOfNotSelectedOptionByValue(Select select, String value, boolean isRegex,
			String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_VAL_NOT_SELECTED_ON_OBJ,
				value, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getAttribute("value");
			if (optionValue != null && KeywordHelper.match(optionValue, value, isRegex)) {
				if (!allSelectedOptions.contains(allOptions.get(index))) {
					count++;
					logger.logDebug(
							MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_VAL_IS_NOT_SELECTED,
									index, optionValue, regularExpressionLog));
				}
			}
		}
		return count;
	}

	public static int getNumberOfSelectedOptionByIndex(Select select, Integer[] indexes, String objectId)
			throws IllegalArgumentException {
		logger.logDebug(
				MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_INDEX_RANGE_SELECTED_ON_OBJ,
						integerArrayToString(indexes), objectId));
		int count = 0;
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index : indexes) {
			// Index is 0-based, lstIndexes is list of 0-based indexing
			// number
			if (allSelectedOptions.contains(select.getOptions().get(index))) {
				count++;
				logger.logDebug(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_IS_SELECTED, index));
			}
		}
		return count;
	}

	public static int getNumberOfNotSelectedOptionByIndex(Select select, Integer[] indexes, String objectId) {
		logger.logDebug(
				MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_INDEX_RANGE_NOT_SELECTED_ON_OBJ,
						integerArrayToString(indexes), objectId));
		int count = 0;
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index : indexes) {
			// Index is 0-based, lstIndexes is list of 0-based indexing
			// number
			if (!allSelectedOptions.contains(select.getOptions().get(index))) {
				count++;
				logger.logDebug(
						MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_IS_NOT_SELECTED, index));
			}
		}
		return count;
	}

	public static void focusOnBrowser() throws WebDriverException, StepFailedException {
		((JavascriptExecutor) DriverFactory.getWebDriver()).executeScript("window.focus()");
	}

	/*
	 * Build a locator based on its SelectorCollection and SelectorMethod Update
	 * SelectorCollection if previously empty
	 */
	public static By buildLocator(TestObject to) {
		SelectorMethod selectorMethod = to.getSelectorMethod();
		switch (selectorMethod) {
		case BASIC:
			// Legacy locator
			String cssLocatorValue = findActiveEqualsObjectProperty(to, CSS_LOCATOR_PROPERTY_NAME);
			if (cssLocatorValue != null) {
				return By.cssSelector(cssLocatorValue);
			}
			return buildXpath(to);
		case CSS:
			return By.cssSelector(to.getSelectorCollection().get(selectorMethod));
		case XPATH:
			// Update SelectorCollection of old TestObject for compatibility
			if (to.getSelectorCollection().isEmpty()) {
				to.setSelectorValue(selectorMethod, to.getXpaths().get(0).getValue());
			}
			return By.xpath(to.getSelectorCollection().get(selectorMethod));
		default:
			return null;
		}
	}

	/**
	 * Build a locator to find all elements satisfying at least one condition
	 * (regardless it is active or not)
	 */
	public static By buildUnionXpath(TestObject to) {
		return buildXpath(to, XPathBuilder.AggregationType.UNION, to.getProperties());
	}

	/*
	 * Get a TestObject's selector value based on its SelectorCollection and
	 * SelectorMethod Update SelectorCollection if previously empty
	 */
	public static String getSelectorValue(TestObject to) {
		SelectorMethod selectorMethod = to.getSelectorMethod();
		switch (selectorMethod) {
		case BASIC:
			String cssLocatorValue = findActiveEqualsObjectProperty(to, CSS_LOCATOR_PROPERTY_NAME);
			if (cssLocatorValue != null) {
				return cssLocatorValue;
			}
			XPathBuilder xpathBuilder = new XPathBuilder(to.getActiveProperties());
			return xpathBuilder.build();
		case XPATH:
			String ret = to.getSelectorCollection().get(selectorMethod);
			if (ret == null) {
				if (to.getXpaths() != null && !to.getXpaths().isEmpty()) {
					ret = to.getXpaths().get(0).getValue();
					// Update SelectorCollection of old TestObject for
					// compatibility
					to.setSelectorValue(selectorMethod, ret);
				}
			}
			return ret;
		case CSS:
			return to.getSelectorCollection().get(selectorMethod);
		default:
			return to.getSelectorCollection().get(selectorMethod);
		}
	}

	public static String findActiveEqualsObjectProperty(TestObject to, String propertyName) {
		for (TestObjectProperty property : to.getActiveProperties()) {
			if (property.getName().equals(propertyName) && property.getCondition() == ConditionType.EQUALS) {
				return property.getValue();
			}
		}
		return null;
	}

	/**
	 * Build a locator to find all elements satisfying all active conditions
	 */
	private static By buildXpath(TestObject to) {
		return buildXpath(to, XPathBuilder.AggregationType.INTERSECT, to.getActiveProperties());
	}

	private static By buildXpath(TestObject to, XPathBuilder.AggregationType aggregationType,
			List<TestObjectProperty> properties) {
		XPathBuilder xpathBuilder = new XPathBuilder(properties);
		return By.xpath(xpathBuilder.build(aggregationType));
	}

	/**
	 * Build locators, each corresponds to a single condition ("text" or
	 * "xpath")
	 */
	private static List<Entry<String, By>> buildXpathsFromXpathBasedConditions(TestObject to) {
		XPathBuilder xpathBuilder = new XPathBuilder(to.getProperties());
		return xpathBuilder.buildXpathBasedLocators().stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), By.xpath(entry.getValue())))
				.collect(Collectors.toList());
	}

	public static String getBrowserAndVersion(WebDriver webDriver) {
		return (String) ((JavascriptExecutor) webDriver)
				.executeScript("return navigator.sayswho= (function() {" + " var ua= navigator.userAgent, tem,"
						+ " M= ua.match(/(opera|chrome|safari|firefox|msie|trident)\\/?\\s*(\\.?\\d+(\\.\\d+)*)/i) || [];"
						+ " if (/trident/i.test(M[1])) {" + "     tem=  /\\brv[ :]+(\\d+)/g.exec(ua) || [];"
						+ "     return 'IE '+(tem[1] || '');" + " }" + " if(M[1]=== 'Chrome') {"
						+ "     tem= ua.match(/\\b(OPR|Edge)\\/(\\d+)/);"
						+ "     if(tem!= null) return tem.slice(1).join(' ').replace('OPR', 'Opera');" + " }"
						+ " M= M[2]? [M[1], M[2]]: [navigator.appName, navigator.appVersion, '-?'];"
						+ " if((tem= ua.match(/version\\/(\\d+)/i))!= null) M.splice(1, 1, tem[1]);"
						+ " return M.join(' ').replace('MSIE', 'IE');" + "})();");
	}

	public static int getViewportWidth(WebDriver webDriver) {
		Long longValue = (Long) (((JavascriptExecutor) DriverFactory.getWebDriver())
				.executeScript("return Math.max(document.documentElement.clientWidth, window.innerWidth || 0);"));
		return longValue.intValue();
	}

	public static int getViewportHeight(WebDriver webDriver) {
		Long longValue = (Long) (((JavascriptExecutor) DriverFactory.getWebDriver())
				.executeScript("return Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"));
		return longValue.intValue();
	}

	public static Rectangle getElementRect(WebDriver webDriver, WebElement element) {
		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
		Number left = (Number) (javascriptExecutor.executeScript("return arguments[0].getBoundingClientRect().left",
				element));
		Number right = (Number) (javascriptExecutor.executeScript("return arguments[0].getBoundingClientRect().right",
				element));
		Number top = (Number) (javascriptExecutor.executeScript("return arguments[0].getBoundingClientRect().top",
				element));
		Number bottom = (Number) (javascriptExecutor.executeScript("return arguments[0].getBoundingClientRect().bottom",
				element));
		return new Rectangle(left.intValue(), top.intValue(), right.intValue() - left.intValue(),
				bottom.intValue() - top.intValue());
	}

	public static boolean isElementVisibleInViewport(WebDriver driver, TestObject testObject, int timeOut)
			throws IllegalArgumentException, StepFailedException, WebElementNotFoundException {
		WebUiCommonHelper.checkTestObjectParameter(testObject);
		TestObject parentObject = testObject != null ? testObject.getParentObject() : null;
		List<TestObject> frames = new ArrayList<TestObject>();
		while (parentObject != null) {
			frames.add(parentObject);
			parentObject = parentObject.getParentObject();
		}
		boolean isSwitchIntoFrame = false;
		double xOffset = 0;
		double yOffset = 0;
		try {
			if (frames.size() > 0) {
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_OBJ_X_HAS_PARENT_FRAME,
						testObject.getObjectId()));
				for (int i = frames.size() - 1; i >= 0; i--) {
					TestObject frameObject = frames.get(i);
					logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_SWITCHING_TO_IFRAME_X,
							frameObject.getObjectId()));
					WebElement frameElement = findWebElement(frameObject, timeOut);
					if (frameElement != null) {
						logger.logDebug(
								MessageFormat.format(StringConstants.KW_LOG_INFO_CHECKING_TO_IFRAME_X_IN_VIEWPORT,
										frameObject.getObjectId()));

						Rectangle elementRect = WebUiCommonHelper.getElementRect(driver, frameElement);
						elementRect.setRect(elementRect.getX() + xOffset, elementRect.getY() + yOffset,
								elementRect.getWidth(), elementRect.getHeight());
						logger.logDebug(
								MessageFormat.format(StringConstants.KW_LOG_INFO_ELEMENT_RECT, elementRect.getX(),
										elementRect.getY(), elementRect.getWidth(), elementRect.getHeight()));
						Rectangle documentRect = new Rectangle(0, 0, WebUiCommonHelper.getViewportWidth(driver),
								WebUiCommonHelper.getViewportHeight(driver));
						logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_VIEWPORT_RECT,
								documentRect.getWidth(), documentRect.getHeight()));
						if (!documentRect.intersects(elementRect)) {
							logger.logDebug(MessageFormat.format(
									StringConstants.KW_MSG_PARENT_OBJECT_IS_NOT_VISIBLE_IN_VIEWPORT,
									frameObject.getObjectId()));
							return false;
						}
						xOffset += frameElement.getLocation().getX();
						yOffset += frameElement.getLocation().getY();
						driver.switchTo().frame(frameElement);
						isSwitchIntoFrame = true;
						logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_SWITCHED_TO_IFRAME_X,
								frameObject.getObjectId()));
					}
				}
			}

			WebElement foundElement = findWebElement(testObject, timeOut);
			return isElementVisibleInViewport(driver, foundElement);
		} finally {
			if (isSwitchIntoFrame) {
				switchToDefaultContent();
			}
		}
	}

	public static boolean isElementVisibleInViewport(WebDriver driver, WebElement element) {
		Rectangle elementRect = WebUiCommonHelper.getElementRect(driver, element);
		logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_ELEMENT_RECT, elementRect.getX(),
				elementRect.getY(), elementRect.getWidth(), elementRect.getHeight()));
		Rectangle documentRect = new Rectangle(0, 0, WebUiCommonHelper.getViewportWidth(driver),
				WebUiCommonHelper.getViewportHeight(driver));
		logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_VIEWPORT_RECT, documentRect.getWidth(),
				documentRect.getHeight()));
		return documentRect.intersects(elementRect);
	}

	// Return an empty list if no elements found,
	// Let the caller decides what to do (throw exception, not throw, etc)
	public static List<WebElement> findWebElements(TestObject testObject, int timeOut) {
		timeOut = WebUiCommonHelper.checkTimeout(timeOut);
		boolean isSwitchToParentFrame = false;
		try {
			WebDriver webDriver = DriverFactory.getWebDriver();
			Boolean smartXPathsEnabled = RunConfiguration.shouldApplySmartXPath();
			final boolean objectInsideShadowDom = testObject.getParentObject() != null
					&& testObject.isParentObjectShadowRoot();
			By defaultLocator = null;
			String cssLocator = null;
			String locatorString = null;
			final TestObject parentObject = testObject.getParentObject();
			WebElement shadowRootElement = null;
			if (objectInsideShadowDom) {
				cssLocator = CssLocatorBuilder.buildCssSelectorLocator(testObject);
				if (cssLocator == null) {
					throw new StepFailedException(
							MessageFormat.format(StringConstants.KW_EXC_WEB_ELEMENT_W_ID_DOES_NOT_HAVE_SATISFY_PROP,
									testObject.getObjectId()));
				}
				locatorString = cssLocator;
				logger.logDebug(
						MessageFormat.format(CoreWebuiMessageConstants.MSG_INFO_WEB_ELEMENT_HAVE_PARENT_SHADOW_ROOT,
								testObject.getObjectId(), testObject.getParentObject().getObjectId()));
				isSwitchToParentFrame = switchToParentFrame(parentObject);
				shadowRootElement = findWebElement(parentObject, timeOut);
				if (shadowRootElement == null) {
					return null;
				}
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_FINDING_WEB_ELEMENT_W_ID,
						testObject.getObjectId(), cssLocator, timeOut));
			} else {
				defaultLocator = WebUiCommonHelper.buildLocator(testObject);
				if (defaultLocator == null) {
					throw new StepFailedException(
							MessageFormat.format(StringConstants.KW_EXC_WEB_ELEMENT_W_ID_DOES_NOT_HAVE_SATISFY_PROP,
									testObject.getObjectId()));
				}
				locatorString = defaultLocator.toString();
				logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_FINDING_WEB_ELEMENT_W_ID,
						testObject.getObjectId(), defaultLocator.toString(), timeOut));
			}

			float timeCount = 0;
			long miliseconds = System.currentTimeMillis();
			while (timeCount < timeOut) {
				try {
					List<WebElement> webElements = null;
					if (objectInsideShadowDom) {
						webElements = doFindElementsInsideShadowDom(testObject, timeOut, webDriver, cssLocator,
								parentObject, shadowRootElement);
						return webElements;
					} else {
						webElements = webDriver.findElements(defaultLocator);
						if (webElements != null && webElements.size() > 0) {
							logger.logDebug(MessageFormat.format(
									StringConstants.KW_LOG_INFO_FINDING_WEB_ELEMENT_W_ID_SUCCESS, webElements.size(),
									testObject.getObjectId(), defaultLocator.toString(), timeOut));
							return webElements;
						}
					}
				} catch (NoSuchElementException e) {
					// not found element yet, moving on
				}
				timeCount += ((System.currentTimeMillis() - miliseconds) / 1000);
				Thread.sleep(500);
				timeCount += 0.5;
				miliseconds = System.currentTimeMillis();
			}

			// If this code is reached, then no elements were found, try to use
			// other methods
			logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_CANNOT_FIND_WEB_ELEMENT_BY_LOCATOR,
					locatorString));
			// Only apply Smart XPath to test objects that have selector method
			// of XPath AND if Smart XPath is enabled
			if (testObject.getSelectorMethod().equals(SelectorMethod.XPATH) && smartXPathsEnabled) {
				List<WebElement> elementsFoundBySmartXPath = findWebElementsWithSmartXPath(webDriver,
						objectInsideShadowDom, testObject);
				return elementsFoundBySmartXPath;
			}

			return testObject.getProperties().stream().filter(a -> a.getName().equals("screenshot")).findAny()
					.map(present -> {
						return ByScreenshotLocatorController.findElementByScreenShot(webDriver, present.getValue());
					}).orElse(Collections.emptyList());

		} catch (TimeoutException e) {
			// timeOut, do nothing
		} catch (InterruptedException e) {
			// interrupted, do nothing
		} catch (WebElementNotFoundException e) {
			// element not found, do nothing
		} finally {
			if (isSwitchToParentFrame) {
				switchToDefaultContent();
			}
		}
		return Collections.emptyList();
	}	

	private static List<WebElement> findWebElementsWithSmartXPath(WebDriver webDriver, boolean objectInsideShadowDom,
			TestObject testObject) {

		if (objectInsideShadowDom) {
			return Collections.emptyList();
		}

		SmartXPathController.setLogger(logger);

		SmartXPathController.logInfo(StringConstants.KW_LOG_INFO_SMART_XPATHS_USING);

		Map<TestObjectXpath, List<WebElement>> smartXPathsMap = new HashMap<>();
		List<TestObjectXpath> allXPaths = testObject.getXpaths();
		TestObjectXpath selectedSmartXPath = null;
		String pathToSelectedSmartXPathScreenshot = StringUtils.EMPTY;

		int index = atomicCounter.getAndIncrement();

		for (int i = 0; i < allXPaths.size(); i++) {
			TestObjectXpath thisXPath = allXPaths.get(i);
			By byThisXPath = By.xpath(thisXPath.getValue());
			List<WebElement> elementsFoundByThisXPath = null;
			try {
				elementsFoundByThisXPath = webDriver.findElements(byThisXPath);
			} catch (InvalidSelectorException e) {
				// do nothing
			}
			if (elementsFoundByThisXPath != null && elementsFoundByThisXPath.size() > 0) {

				SmartXPathController.logInfo(MessageFormat.format(
						StringConstants.KW_LOG_INFO_FOUND_WEB_ELEMENT_WITH_THIS_SMART_XPATH, thisXPath.getValue()));

				if (smartXPathsMap.get(thisXPath) == null) {
					// save the first working XPath
					if (selectedSmartXPath == null) {
						selectedSmartXPath = thisXPath;
					}
					smartXPathsMap.put(thisXPath, elementsFoundByThisXPath);
				}

				// By convention all XPath finders must abide
				// 'xpath:finder_name'
				String xpathFinder = thisXPath.getName().split(":")[1];

				String screenShotName = testObject.getObjectId() + "_" + xpathFinder;

				// Increase local index for neighbor
				if (xpathFinder.equals("neighbor")) {
					screenShotName += "_" + (index++);
				}

				// Save the first working XPath's screenshot
				if (pathToSelectedSmartXPathScreenshot.equals(StringUtils.EMPTY)) {
					pathToSelectedSmartXPathScreenshot = SmartXPathController.takeScreenShot(webDriver,
							elementsFoundByThisXPath.get(0), screenShotName);
				} else {
					SmartXPathController.takeScreenShot(webDriver, elementsFoundByThisXPath.get(0), screenShotName);
				}

			} else {
				SmartXPathController.logInfo(MessageFormat.format(
						StringConstants.KW_LOG_INFO_COULD_NOT_FIND_WEB_ELEMENT_WITH_THIS_SMART_XPATH,
						thisXPath.getValue()));
			}
		}

		if (selectedSmartXPath != null) {
			List<WebElement> elementsFoundWithSelectedSmartXPath = smartXPathsMap.get(selectedSmartXPath);
			SmartXPathController.registerBrokenTestObject(testObject, selectedSmartXPath,
					pathToSelectedSmartXPathScreenshot);
			SmartXPathController.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECT_SMART_XPATH,
					selectedSmartXPath.getValue()));
			SmartXPathController.logInfo(StringConstants.KW_LOG_INFO_SMART_XPATHS_AUTO_UPDATE_AND_CONTINUE_EXECUTION);
			return elementsFoundWithSelectedSmartXPath;
		} else {
			SmartXPathController.logInfo(StringConstants.KW_LOG_INFO_COULD_NOT_FIND_ANY_WEB_ELEMENT_WITH_SMART_XPATHS);
		}

		return Collections.emptyList();
	}

	/**
	 * Take and save screenshot of a web element on the web page WebDriver is
	 * currently on. The image will be resized to the web element's width and
	 * height
	 * 
	 * @param driver
	 *            A WebDriver instance that's being used at the time calling
	 *            this function
	 * @param ele
	 *            The web element to be taken screenshot of
	 * @param name
	 *            Name of the screenshot
	 * @param path
	 *            An absolute path to a folder to which the image will be saved
	 * @return Path to the newly taken screenshot if exists, an empty string
	 *         otherwise
	 * @throws IOException
	 *             If an exception during I/O occurs
	 * @throws InterruptedException
	 */
	public static String saveWebElementScreenshot(WebDriver driver, WebElement ele, String name, String path)
			throws IOException {
		File screenshot = ele.getScreenshotAs(OutputType.FILE);
		BufferedImage screenshotBeforeResized = ImageIO.read(screenshot);
		int eleWidth = ele.getRect().getWidth();
		int eleHeight = ele.getRect().getHeight();
		BufferedImage screenshotAfterResized = resize(screenshotBeforeResized, eleHeight, eleWidth);
		ImageIO.write(screenshotAfterResized, "png", screenshot);
		String screenshotPath = path;
		screenshotPath = screenshotPath.replaceAll("\\\\", "/");
		if (screenshotPath.endsWith("/")) {
			screenshotPath += name;
		} else {
			screenshotPath += "/" + name;
		}
		screenshotPath += ".png";
		File fileScreenshot = new File(screenshotPath);
		FileUtils.copyFile(screenshot, fileScreenshot);
		// Delete temporary image
		screenshot.deleteOnExit();
		return screenshotPath;
	}

	/**
	 * Resize the given image to the specified height and width
	 * @param img An {@link BufferedImage} instance representing the image to be resized
	 * @param height Height to resize to
	 * @param width Width to resize to
	 * @return A {@link BufferedImage}
	 */
	private static BufferedImage resize(BufferedImage img, int height, int width) {
		Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = resized.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		return resized;
	}

	

	/**
	 * Only for tag and attribute conditions
	 * 
	 * @param testObject
	 * @param webElement
	 * @return
	 */
	private static List<String> getSatisfiedConditions(TestObject testObject, WebElement webElement) {
		List<TestObjectProperty> expectedProperties = testObject.getProperties();
		List<String> satisfiedConditions = new ArrayList<>();
		for (TestObjectProperty expectedProperty : expectedProperties) {
			String propertyName = expectedProperty.getName();
			PropertyType propertyType = XPathBuilder.PropertyType.nameOf(propertyName);
			boolean matches;
			switch (propertyType) {
			case TAG:
				String expectedTag = expectedProperty.getValue();
				String actualTag = webElement.getTagName();
				matches = expectedTag.equalsIgnoreCase(actualTag);
				break;
			case ATTRIBUTE:
				String expectedPropertyValue = expectedProperty.getValue();
				String actualPropertyValue = webElement.getAttribute(propertyName);
				switch (expectedProperty.getCondition()) {
				case EQUALS:
					matches = expectedPropertyValue.equals(actualPropertyValue);
					break;
				case NOT_EQUAL:
					matches = !expectedPropertyValue.equals(actualPropertyValue);
					break;
				case CONTAINS:
					matches = expectedPropertyValue.contains(actualPropertyValue);
					break;
				case NOT_CONTAIN:
					matches = !expectedPropertyValue.contains(actualPropertyValue);
					break;
				case STARTS_WITH:
					matches = expectedPropertyValue.startsWith(actualPropertyValue);
					break;
				case ENDS_WITH:
					matches = expectedPropertyValue.endsWith(actualPropertyValue);
					break;
				case MATCHES_REGEX:
					matches = expectedPropertyValue.matches(actualPropertyValue);
					break;
				case NOT_MATCH_REGEX:
					matches = !expectedPropertyValue.matches(actualPropertyValue);
					break;
				default:
					matches = false;
					break;
				}
				break;
			default:
				matches = false;
				break;
			}
			if (matches) {
				satisfiedConditions.add(propertyName);
			}
		}
		return satisfiedConditions;
	}

	@SuppressWarnings("unused")
	private static List<WebElement> doFindElementsDefault(TestObject testObject, int timeOut, WebDriver webDriver,
			By locator) throws WebElementNotFoundException {
		List<WebElement> webElements = webDriver.findElements(locator);
		if (webElements != null && webElements.size() > 0) {
			logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_FINDING_WEB_ELEMENT_W_ID_SUCCESS,
					webElements.size(), testObject.getObjectId(), locator.toString(), timeOut));
		}
		return webElements;
	}

	@SuppressWarnings("unchecked")
	private static List<WebElement> doFindElementsInsideShadowDom(TestObject testObject, int timeOut,
			WebDriver webDriver, final String cssLocator, final TestObject parentObject, WebElement shadowRootElement)
			throws WebElementNotFoundException {
		Object shadowRootElementSandbox = ((JavascriptExecutor) webDriver)
				.executeScript("return arguments[0].shadowRoot;", shadowRootElement);
		if (shadowRootElementSandbox == null) {
			throw new StepFailedException(MessageFormat
					.format(CoreWebuiMessageConstants.MSG_FAILED_WEB_ELEMENT_X_IS_NOT_SHADOW_ROOT, parentObject));
		}
		List<WebElement> webElements = (List<WebElement>) ((JavascriptExecutor) webDriver)
				.executeScript("return arguments[0].querySelectorAll('" + cssLocator + "');", shadowRootElementSandbox);
		if (webElements != null && webElements.size() > 0) {
			logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_FINDING_WEB_ELEMENT_W_ID_SUCCESS,
					webElements.size(), testObject.getObjectId(), cssLocator, timeOut));
		}
		return webElements;
	}

	public static WebElement findWebElement(TestObject testObject, int timeOut) throws WebElementNotFoundException {
		List<WebElement> elements = findWebElements(testObject, timeOut);
		if (elements != null && elements.size() > 0) {
			return elements.get(0);
		} else {
			throw new WebElementNotFoundException(testObject.getObjectId(), buildLocator(testObject));
		}
	}

	/**
	 * Internal method to switch to default content
	 * 
	 * @throws StepFailedException
	 */
	public static void switchToDefaultContent() throws StepFailedException {
		try {
			if (DriverFactory.getAlert() != null) {
				logger.logWarning(StringConstants.KW_LOG_WARNING_SWITCHING_TO_DEFAULT_CONTENT_FAILED_BC_ALERT_ON_PAGE);
				return;
			}
			logger.logDebug(StringConstants.KW_LOG_INFO_SWITCHING_TO_DEFAULT_CONTENT);
			DriverFactory.getWebDriver().switchTo().defaultContent();
		} catch (NoSuchWindowException e) {
			// Switching to default content in IE without in frame will raise
			// this exception, so do nothing here.
		} catch (WebDriverException e) {
			// Switching to default content is optional, so exception will not
			// make it fail, therefore only warn user about the exception
			logger.logWarning(
					MessageFormat.format(StringConstants.KW_LOG_WARNING_SWITCHING_TO_DEFAULT_CONTENT_FAILED_BC_OF_X,
							ExceptionsUtil.getMessageForThrowable(e)),
					null, e);
		}
	}

	/***
	 * Switch to parent frames if test object has parent objects Switch to
	 * parent shadow roots if the parent object is shadow roots
	 * 
	 * @param testObject
	 * @param timeOut
	 * @return
	 * @throws WebElementNotFoundException
	 */
	public static boolean switchToParentFrame(TestObject testObject, int timeOut) throws WebElementNotFoundException {
		TestObject parentObject = testObject != null ? testObject.getParentObject() : null;
		boolean isParentShadowRoot = testObject.isParentObjectShadowRoot();
		List<TestObject> parentObjects = new ArrayList<TestObject>();
		while (parentObject != null && !isParentShadowRoot) {
			parentObjects.add(parentObject);
			isParentShadowRoot = parentObject.isParentObjectShadowRoot();
			parentObject = parentObject.getParentObject();
		}
		if (parentObjects.size() <= 0) {
			return false;
		}
		logger.logDebug(
				MessageFormat.format(StringConstants.KW_LOG_INFO_OBJ_X_HAS_PARENT_FRAME, testObject.getObjectId()));
		WebDriver webDriver = DriverFactory.getWebDriver();
		for (int i = parentObjects.size() - 1; i >= 0; i--) {
			TestObject currentParentObject = parentObjects.get(i);
			if (!switchToParentFrame(timeOut, webDriver, currentParentObject)) {
				return false;
			}
		}
		return true;
	}

	private static boolean switchToParentFrame(int timeOut, WebDriver webDriver, TestObject currentParentObject)
			throws WebElementNotFoundException {
		logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_SWITCHING_TO_IFRAME_X,
				currentParentObject.getObjectId()));
		WebElement frameElement = findWebElement(currentParentObject, timeOut);
		if (frameElement == null) {
			return false;
		}
		webDriver.switchTo().frame(frameElement);
		logger.logDebug(MessageFormat.format(StringConstants.KW_LOG_INFO_SWITCHED_TO_IFRAME_X,
				currentParentObject.getObjectId()));
		return true;
	}

	public static boolean switchToParentFrame(TestObject testObject) throws WebElementNotFoundException {
		return switchToParentFrame(testObject, RunConfiguration.getTimeOut());
	}
}
