package com.kms.katalon.composer.integration.jira.preference;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.atlassian.jira.rest.client.api.domain.User;
import com.kms.katalon.composer.components.impl.dialogs.MultiStatusErrorDialog;
import com.kms.katalon.composer.components.impl.util.ControlUtils;
import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.integration.jira.constant.ComposerJiraIntegrationMessageConstant;
import com.kms.katalon.composer.integration.jira.constant.StringConstants;
import com.kms.katalon.composer.integration.jira.preference.JiraConnectionJob.JiraConnectionResult;
import com.kms.katalon.constants.GlobalStringConstants;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.integration.jira.JiraCredential;
import com.kms.katalon.integration.jira.entity.JiraIssueType;
import com.kms.katalon.integration.jira.entity.JiraProject;
import com.kms.katalon.integration.jira.setting.JiraIntegrationSettingStore;

public class JiraSettingsComposite {

    private Composite container;

    private Composite enablerComposite;

    private Button chckEnableIntegration;

    private Composite mainComposite;

    private Text txtServerUrl, txtUsername, txtPassword;

    private Button chckUseTestCaseNameAsSumarry, chckAttachScreenshot, chckAttachLog;

    private JiraIntegrationSettingStore settingStore;

    private Button btnConnect;

    private Combo cbbIssueTypes, cbbProjects;

    private DisplayedComboboxObject<JiraProject> displayedJiraProject;

    private DisplayedComboboxObject<JiraIssueType> displayedJiraIssueType;

    private User user;

    private Shell shell;

    private Button chckAutoSubmitTestResult;

    private Button chckEncrypt;

    private Button chckShowPassword;

    public JiraSettingsComposite() {
        settingStore = new JiraIntegrationSettingStore(
                ProjectController.getInstance().getCurrentProject().getFolderLocation());
    }

    public void registerControlModifyListeners() {
        chckEnableIntegration.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                enableIntegrationComposite();
            }
        });
        btnConnect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = getShell();
                JiraConnectionJob job = new JiraConnectionJob(shell, getCredential());
                JiraConnectionResult result = job.run();
                if (result.getError() != null) {
                    MessageDialog.openError(shell, StringConstants.ERROR, result.getError().getMessage());
                    return;
                }

                if (!result.isComplete()) {
                    return;
                }
                user = result.getUser();
                displayedJiraProject = result.getJiraProjects().updateDefaultURIFrom(displayedJiraProject);
                updateCombobox(cbbProjects, displayedJiraProject);

                displayedJiraIssueType = result.getJiraIssueTypes().updateDefaultURIFrom(displayedJiraIssueType);
                updateCombobox(cbbIssueTypes, displayedJiraIssueType);
                MessageDialog.openInformation(shell, StringConstants.INFO,
                        MessageFormat.format(ComposerJiraIntegrationMessageConstant.PREF_MSG_ACCOUNT_CONNECTED,
                                result.getUser().getDisplayName()));
            }
        });

        chckShowPassword.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                maskPasswordField();
            }
        });
    }

    private void maskPasswordField() {
        if (chckShowPassword.getSelection()) {
            // show password
            txtPassword.setEchoChar('\0');
        } else {
            txtPassword.setEchoChar(GlobalStringConstants.CR_ECO_PASSWORD.charAt(0));
        }
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    private Shell getShell() {
        return shell;
    }

    private void enableIntegrationComposite() {
        ControlUtils.recursiveSetEnabled(mainComposite, chckEnableIntegration.getSelection());
    }

    public void initializeData() {
        try {
            chckEnableIntegration.setSelection(settingStore.isIntegrationEnabled());
            enableIntegrationComposite();

            boolean encryptionEnabled = settingStore.isEncryptionEnabled();
            txtServerUrl.setText(settingStore.getServerUrl(encryptionEnabled));
            txtUsername.setText(settingStore.getUsername(encryptionEnabled));
            txtPassword.setText(settingStore.getPassword(encryptionEnabled));
            chckEncrypt.setSelection(encryptionEnabled);
            maskPasswordField();

            chckUseTestCaseNameAsSumarry.setSelection(settingStore.isUseTestCaseNameAsSummaryEnabled());
            chckAttachScreenshot.setSelection(settingStore.isAttachScreenshotEnabled());
            chckAttachLog.setSelection(settingStore.isAttachLogEnabled());
            chckAutoSubmitTestResult.setSelection(settingStore.isSubmitTestResultAutomatically());

            displayedJiraProject = new DisplayedComboboxObject<>(settingStore.getStoredJiraProject());
            updateCombobox(cbbProjects, displayedJiraProject);

            displayedJiraIssueType = new DisplayedIssueTypeComboboxObject(settingStore.getStoredJiraIssueType());
            updateCombobox(cbbIssueTypes, displayedJiraIssueType);

            user = settingStore.getJiraUser();
        } catch (IOException | GeneralSecurityException e) {
            LoggerSingleton.logError(e);
            MultiStatusErrorDialog.showErrorDialog(e, StringConstants.ERROR, e.getMessage());
        }
    }

    private void updateCombobox(Combo combobox, DisplayedComboboxObject<?> displayedJiraObject) {
        combobox.setItems(displayedJiraObject.getNames());
        int defaultProjectIndex = displayedJiraObject.getDefaultObjectIndex();
        if (defaultProjectIndex >= 0) {
            combobox.select(defaultProjectIndex);
        }
    }

    public Composite createContainer(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        enablerComposite = new Composite(container, SWT.NONE);
        enablerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        enablerComposite.setLayout(new GridLayout());

        chckEnableIntegration = new Button(enablerComposite, SWT.CHECK);
        chckEnableIntegration.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        chckEnableIntegration.setText(ComposerJiraIntegrationMessageConstant.PREF_CHCK_ENABLE_INTEGRATION);

        mainComposite = new Composite(container, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        GridLayout glMainComposite = new GridLayout(1, false);
        glMainComposite.marginWidth = 0;
        glMainComposite.marginHeight = 0;
        mainComposite.setLayout(glMainComposite);

        createAuthenticationGroup();

        createSubmitOptionsGroup();

        return container;
    }

    private void createAuthenticationGroup() {
        Group grpAuthentication = new Group(mainComposite, SWT.NONE);
        grpAuthentication.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        GridLayout glGrpAuthentication = new GridLayout(2, false);
        glGrpAuthentication.horizontalSpacing = 15;
        grpAuthentication.setLayout(glGrpAuthentication);
        grpAuthentication.setText(ComposerJiraIntegrationMessageConstant.PREF_TITLE_AUTHENTICATION);

        Label lblServerUrl = new Label(grpAuthentication, SWT.NONE);
        lblServerUrl.setText(ComposerJiraIntegrationMessageConstant.PREF_LBL_SERVER_URL);

        txtServerUrl = new Text(grpAuthentication, SWT.BORDER);
        txtServerUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblUsername = new Label(grpAuthentication, SWT.NONE);
        lblUsername.setText(ComposerJiraIntegrationMessageConstant.PREF_LBL_USERNAME);

        txtUsername = new Text(grpAuthentication, SWT.BORDER);
        txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPassword = new Label(grpAuthentication, SWT.NONE);
        lblPassword.setText(ComposerJiraIntegrationMessageConstant.PREF_LBL_PASSWORD);

        Composite passwordComposite = new Composite(grpAuthentication, SWT.NONE);
        passwordComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        GridLayout glPassword = new GridLayout(2, false);
        glPassword.marginWidth = 0;
        glPassword.marginHeight = 0;
        passwordComposite.setLayout(glPassword);

        txtPassword = new Text(passwordComposite, SWT.BORDER);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        chckShowPassword = new Button(passwordComposite, SWT.CHECK);
        chckShowPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        chckShowPassword.setText(ComposerJiraIntegrationMessageConstant.PREF_CHCK_SHOW_PASSWORD);

        chckEncrypt = new Button(grpAuthentication, SWT.CHECK);
        chckEncrypt.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 2, 1));
        chckEncrypt.setText(ComposerJiraIntegrationMessageConstant.PREF_CHCK_ENCRYPT_PASSWORD);

        btnConnect = new Button(grpAuthentication, SWT.NONE);
        btnConnect.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnConnect.setText(ComposerJiraIntegrationMessageConstant.PREF_LBL_CONNECT);
        new Label(grpAuthentication, SWT.NONE);
    }

    private void createSubmitOptionsGroup() {
        Group grpSubmitOptions = new Group(mainComposite, SWT.NONE);
        grpSubmitOptions.setText(ComposerJiraIntegrationMessageConstant.PREF_TITLE_SUBMIT_OPTIONS);
        grpSubmitOptions.setLayout(new GridLayout(1, false));
        grpSubmitOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Composite projectAndIssueComposite = new Composite(grpSubmitOptions, SWT.NONE);
        projectAndIssueComposite.setLayout(new GridLayout(2, false));
        projectAndIssueComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

        Label lblDefaultJiraProject = new Label(projectAndIssueComposite, SWT.NONE);
        lblDefaultJiraProject.setText(ComposerJiraIntegrationMessageConstant.PREF_LBL_DF_JIRA_PROJECT);

        cbbProjects = new Combo(projectAndIssueComposite, SWT.READ_ONLY);
        cbbProjects.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblDefaultJiraIssue = new Label(projectAndIssueComposite, SWT.NONE);
        lblDefaultJiraIssue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDefaultJiraIssue.setText(ComposerJiraIntegrationMessageConstant.PREF_LBL_DF_JIRA_ISSUE_TYPE);

        cbbIssueTypes = new Combo(projectAndIssueComposite, SWT.READ_ONLY);
        cbbIssueTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Composite submitOptionsComposite = new Composite(grpSubmitOptions, SWT.NONE);
        submitOptionsComposite.setLayout(new GridLayout(1, false));
        submitOptionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        chckUseTestCaseNameAsSumarry = new Button(submitOptionsComposite, SWT.CHECK);
        chckUseTestCaseNameAsSumarry.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        chckUseTestCaseNameAsSumarry
                .setText(ComposerJiraIntegrationMessageConstant.PREF_CHCK_USE_TEST_CASE_NAME_AS_SUMMARY);

        chckAttachScreenshot = new Button(submitOptionsComposite, SWT.CHECK);
        chckAttachScreenshot.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        chckAttachScreenshot.setText(ComposerJiraIntegrationMessageConstant.PREF_CHCK_ATTACH_SCREENSHOT_TO_JIRA_TICKET);

        chckAttachLog = new Button(submitOptionsComposite, SWT.CHECK);
        chckAttachLog.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        chckAttachLog.setText(ComposerJiraIntegrationMessageConstant.PREF_CHCK_ATTACH_LOG_TO_JIRA_TICKET);

        chckAutoSubmitTestResult = new Button(submitOptionsComposite, SWT.CHECK);
        chckAutoSubmitTestResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        chckAutoSubmitTestResult.setText(ComposerJiraIntegrationMessageConstant.PREF_CHCK_AUTO_SUBMIT_TEST_RESULT);
    }

    public boolean okPressed() {
        try {
            settingStore.enableIntegration(chckEnableIntegration.getSelection());

            boolean encryptionEnable = chckEncrypt.getSelection();
            settingStore.saveServerUrl(getTrimedValue(txtServerUrl), encryptionEnable);
            settingStore.saveUsername(getTrimedValue(txtUsername), encryptionEnable);
            settingStore.savePassword(txtPassword.getText(), encryptionEnable);
            settingStore.saveJiraUser(user);
            settingStore.enableEncryption(encryptionEnable);

            settingStore.enableUseTestCaseNameAsSummary(chckUseTestCaseNameAsSumarry.getSelection());
            settingStore.enableAttachScreenshot(chckAttachScreenshot.getSelection());
            settingStore.enableAttachLog(chckAttachLog.getSelection());
            settingStore.enableSubmitTestResultAutomatically(chckAutoSubmitTestResult.getSelection());

            displayedJiraProject.setDefaultObjectIndex(cbbProjects.getSelectionIndex());
            settingStore.saveStoredJiraProject(displayedJiraProject.getStoredObject());

            displayedJiraIssueType.setDefaultObjectIndex(cbbIssueTypes.getSelectionIndex());
            settingStore.saveStoredJiraIssueType(displayedJiraIssueType.getStoredObject());
            return true;
        } catch (IOException | GeneralSecurityException e) {
            LoggerSingleton.logError(e);
            MultiStatusErrorDialog.showErrorDialog(e, StringConstants.ERROR, e.getMessage());
            return false;
        }
    }

    private JiraCredential getCredential() {
        JiraCredential credential = new JiraCredential();
        credential.setServerUrl(getTrimedValue(txtServerUrl));
        credential.setUsername(getTrimedValue(txtUsername));
        credential.setPassword(txtPassword.getText());
        return credential;
    }

    private String getTrimedValue(Text text) {
        return StringUtils.defaultString(text.getText()).trim();
    }
}
