package com.kms.katalon.core.webui.keyword.builtin

import groovy.transform.CompileStatic

import java.text.MessageFormat
import java.util.concurrent.TimeUnit

import org.apache.commons.io.FileUtils
import org.openqa.selenium.Alert
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.NoSuchWindowException
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.WebDriverWait

import com.google.common.base.Function
import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.exception.StepFailedException
import com.kms.katalon.core.keyword.BuiltinKeywords
import com.kms.katalon.core.logging.KeywordLogger
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.TestObjectProperty
import com.kms.katalon.core.util.ExceptionsUtil
import com.kms.katalon.core.util.PathUtil
import com.kms.katalon.core.webui.common.ScreenUtil
import com.kms.katalon.core.webui.common.WebUiCommonHelper
import com.kms.katalon.core.webui.constants.StringConstants
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.webui.driver.WebUIDriverType
import com.kms.katalon.core.webui.exception.BrowserNotOpenedException
import com.kms.katalon.core.webui.exception.WebElementNotFoundException
import com.kms.katalon.core.webui.util.FileUtil
import com.kms.katalon.core.webui.keyword.WebUIKeywordMain
import com.kms.katalon.core.annotation.Action
import com.kms.katalon.core.webui.keyword.WebUIAbstractKeyword
import com.kms.katalon.core.keyword.SupportLevel
import com.kms.katalon.core.keyword.KeywordExecutor

@Action(value = "verifyImagePresent")
public class VerifyImagePresentKeyword extends WebUIAbstractKeyword {

    @CompileStatic
    @Override
    public SupportLevel getSupportLevel(Object ...params) {
        return super.getSupportLevel(params)
    }

    @CompileStatic
    @Override
    public Object execute(Object ...params) {
        TestObject to = getTestObject(params[0])
        FailureHandling flowControl = (FailureHandling)(params.length > 1 && params[1] instanceof FailureHandling ? params[1] : RunConfiguration.getDefaultFailureHandling())
        return verifyImagePresent(to,flowControl)
    }

    @CompileStatic
    public boolean verifyImagePresent(TestObject to, FailureHandling flowControl) throws StepFailedException {
        String imagePath = null
        boolean exist = false
        WebUIKeywordMain.runKeyword({
            imagePath = to.getImagePath()
            WebUiCommonHelper.checkTestObjectParameter(to)
            if (imagePath == null || imagePath.equals("")) {
                throw new IllegalArgumentException(StringConstants.KW_EXC_NO_IMAGE_FILE_PROP_IN_OBJ)
            }
            logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_WAITING_FOR_IMG_X_PRESENT, imagePath))
            // Relative path?
            if (to.getUseRelativeImagePath()) {
                String currentDirFilePath = new File(RunConfiguration.getProjectDir()).getAbsolutePath()
                imagePath = currentDirFilePath + File.separator + imagePath
            }
            exist = screenUtil.isImageExist(imagePath)
            if (exist) {
                logger.logPassed(MessageFormat.format(StringConstants.KW_LOG_PASSED_IMG_X_IS_PRESENT, imagePath))
            } else {
                WebUIKeywordMain.stepFailed(MessageFormat.format(StringConstants.KW_LOG_PASSED_IMG_X_IS_NOT_PRESENT, imagePath), flowControl, null, true)
            }
        }, flowControl, true, (imagePath != null) ?  MessageFormat.format(StringConstants.KW_MSG_CANNOT_VERIFY_IMG_X_PRESENT, imagePath) : StringConstants.KW_MSG_CANNOT_VERIFY_IMG_PRESENT)
        return exist
    }
}
