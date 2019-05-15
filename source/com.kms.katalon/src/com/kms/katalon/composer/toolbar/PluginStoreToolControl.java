package com.kms.katalon.composer.toolbar;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.kms.katalon.composer.components.impl.control.DropdownToolItemSelectionListener;
import com.kms.katalon.composer.handlers.ManageKStoreCLIKeysHandler;
import com.kms.katalon.composer.handlers.ManagePluginsHandler;
import com.kms.katalon.composer.handlers.OpenPluginHelpPageHandler;
import com.kms.katalon.composer.handlers.ReloadPluginsHandler;
import com.kms.katalon.composer.handlers.SearchPluginsHandler;
import com.kms.katalon.constants.ImageConstants;

public class PluginStoreToolControl {

    @PostConstruct
    void createWidget(Composite parent, MToolControl toolControl) {
        ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
        ToolItem pluginStoreToolItem = new ToolItem(toolbar, SWT.DROP_DOWN);
        pluginStoreToolItem.setText("Plugin Store");
        pluginStoreToolItem.setImage(ImageConstants.IMG_KATALON_STORE_24);
        pluginStoreToolItem.addSelectionListener(new DropdownToolItemSelectionListener() {

            @Override
            protected Menu getMenu() {
                Menu menu = new Menu(toolbar);
                MenuItem visitStoreMenuItem = new MenuItem(menu, SWT.PUSH);
                visitStoreMenuItem.setText("Visit Plugin Store");
                visitStoreMenuItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        new SearchPluginsHandler().execute();
                    }
                });

                new MenuItem(menu, SWT.SEPARATOR);

                MenuItem reloadPluginMenuItem = new MenuItem(menu, SWT.PUSH);
                reloadPluginMenuItem.setText("Reload Plugins");
                reloadPluginMenuItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        new ReloadPluginsHandler().execute();
                    }
                });

                MenuItem managePluginMenuItem = new MenuItem(menu, SWT.PUSH);
                managePluginMenuItem.setText("Manage Plugins");
                managePluginMenuItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        new ManagePluginsHandler().execute();
                    }
                });

                MenuItem manageApiKeyMenuItem = new MenuItem(menu, SWT.PUSH);
                manageApiKeyMenuItem.setText("Manage API Keys");
                manageApiKeyMenuItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        new ManageKStoreCLIKeysHandler().execute();
                    }
                });

                new MenuItem(menu, SWT.SEPARATOR);

                MenuItem helpMenuItem = new MenuItem(menu, SWT.PUSH);
                helpMenuItem.setText("Help");
                helpMenuItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        new OpenPluginHelpPageHandler().execute();
                    }
                });

                return menu;
            }
        });

    }
}
