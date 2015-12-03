package com.kms.katalon.composer.mobile.objectspy.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.kms.katalon.composer.components.tree.ITreeEntity;
import com.kms.katalon.composer.mobile.constants.ImageConstants;
import com.kms.katalon.composer.mobile.constants.StringConstants;
import com.kms.katalon.composer.mobile.objectspy.element.MobileElement;
import com.kms.katalon.composer.mobile.objectspy.element.tree.MobileElementLabelProvider;
import com.kms.katalon.composer.mobile.objectspy.element.tree.MobileElementTreeContentProvider;
import com.kms.katalon.composer.mobile.objectspy.util.MobileElementUtil;
import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.constants.IdConstants;
import com.kms.katalon.controller.FolderController;
import com.kms.katalon.controller.ObjectRepositoryController;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.core.mobile.keyword.GUIObject;
import com.kms.katalon.entity.folder.FolderEntity;
import com.kms.katalon.entity.folder.FolderEntity.FolderType;
import com.kms.katalon.entity.repository.WebElementEntity;

@SuppressWarnings("restriction")
public class MobileObjectSpyDialog extends Dialog implements EventHandler {
	
	private static final String DIALOG_TITLE = StringConstants.DIA_DIALOG_TITLE_MOBILE_OBJ_INSPECTOR;

	private Text txtAppFile;
	private Combo cbbDevices, cbbAppType;
	private Button btnBrowse;
	private TableViewer attributesTableViewer;
	private CheckboxTreeViewer elementTreeViewer;
	private Label lblStatus;
	
	private Logger logger;
	private ToolItem btnStart, btnCapture, btnAdd, btnStop;
	private IEventBroker eventBroker;
	private ESelectionService selectionService;
	private MobileElement selectedElement;

	private FolderEntity parentFolder;
	
	private FolderEntity orsRootNode;
	
	private MobileElement appRootElement;

	private boolean isDisposed;

	private static final String[] FILTER_NAMES = { "Mobile Application Files (*.apk, *.ipa)" };

	private static final String[] FILTER_EXTS = { "*.apk; *.ipa" };
	
	private MobileInspectorController inspectorController;

	public MobileObjectSpyDialog(Shell parentShell, Logger logger, IEventBroker eventBroker, ESelectionService selectionService) throws Exception {
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM | SWT.NONE);
		this.logger = logger;
		this.eventBroker = eventBroker;
		this.selectionService = selectionService;
		this.isDisposed = false;
		this.inspectorController =  new MobileInspectorController();
		this.orsRootNode = FolderController.getInstance().getObjectRepositoryRoot(ProjectController.getInstance().getCurrentProject());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm sashForm = new SashForm(container, SWT.NONE);

		Composite explorerComposite = new Composite(sashForm, SWT.NONE);
		explorerComposite.setLayout(new GridLayout());

		addElementTreeToolbar(explorerComposite);

		elementTreeViewer = new CheckboxTreeViewer(explorerComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI){
			@Override
			public boolean setSubtreeChecked(Object element, boolean state){
				Widget widget = internalExpand(element, false);
		        if (widget instanceof TreeItem) {
		            TreeItem item = (TreeItem) widget;
		            item.setChecked(state);
		            return true;
		        }
		        return false;
			}
		};
		
		MobileElementTreeContentProvider contentProvider = new MobileElementTreeContentProvider();
		elementTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		elementTreeViewer.setContentProvider(contentProvider);
		elementTreeViewer.setLabelProvider(new MobileElementLabelProvider());
		
		elementTreeViewer.setCellEditors(new CellEditor[] { new TextCellEditor(elementTreeViewer.getTree()) });
		elementTreeViewer.setColumnProperties(new String[] { "col1" });
		elementTreeViewer.setCellModifier(new ICellModifier() {

			public boolean canModify(Object element, String property) {
				return true;
			}

			public Object getValue(Object element, String property) {
				return ((MobileElement) element).getName();
			}
			
			public void modify(Object element, String property, Object value) {
				element = ((Item) element).getData();
				((MobileElement) element).setName(value.toString());
				elementTreeViewer.update(element, null);
			}
		});

		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(elementTreeViewer, new FocusCellOwnerDrawHighlighter(elementTreeViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(elementTreeViewer) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TreeViewerEditor.create(elementTreeViewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);
		
		elementTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof TreeSelection) {
					TreeSelection treeSelection = (TreeSelection) event.getSelection();
					if (treeSelection.getFirstElement() instanceof MobileElement) {
						selectedElement = (MobileElement) treeSelection.getFirstElement();
						refreshAttributesTable(selectedElement);
						highLightObject(selectedElement);
					}
				}
			}

		});

		//Empty label for space holder
		new Label(explorerComposite, SWT.FLAT | SWT.FILL);
		
		Composite contentComposite = new Composite(sashForm, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(2, false);
		gl_composite_1.horizontalSpacing = 10;
		contentComposite.setLayout(gl_composite_1);

		Label nameLabel = new Label(contentComposite, SWT.NONE);
		nameLabel.setText(StringConstants.DIA_LBL_DEVICE_NAME);

		cbbDevices = new Combo(contentComposite, SWT.READ_ONLY);
		cbbDevices.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		try{
			cbbDevices.setItems(inspectorController.getDevices().toArray(new String[]{}));
		}
		catch(Exception ex){
			MessageDialog.openInformation(getParentShell(), "Info", "No device found");
		}

		Label typeLabel = new Label(contentComposite, SWT.NONE);
		typeLabel.setText(StringConstants.DIA_LBL_APP_TYPE);

		cbbAppType = new Combo(contentComposite, SWT.READ_ONLY);
		cbbAppType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cbbAppType.setItems(new String[] { StringConstants.DIA_APP_TYPE_NATIVE_APP, StringConstants.DIA_APP_TYPE_MOBILE_WEB });
		
		Label appFileLabel = new Label(contentComposite, SWT.NONE);
		appFileLabel.setText(StringConstants.DIA_LBL_APP_FILE);

		txtAppFile = new Text(contentComposite, SWT.BORDER);
		txtAppFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtAppFile.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (selectedElement != null) {
					selectedElement.setType(txtAppFile.getText());
				}
			}
		});

		//Empty label for space holder
		new Label(contentComposite, SWT.NONE);
		btnBrowse = new Button(contentComposite, SWT.PUSH);
		btnBrowse.setText(StringConstants.DIA_BTN_BROWSE);
		btnBrowse.setLayoutData(new GridData(SWT.END, SWT.LEFT, false, false, 1, 1));
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(btnBrowse.getShell());
				dialog.setFilterNames(FILTER_NAMES);
				dialog.setFilterExtensions(FILTER_EXTS);
				String absolutePath = dialog.open();
				if (absolutePath == null)
					return;
				txtAppFile.setText(absolutePath);				
			}
		});
		
		Composite attributesTableComposite = new Composite(contentComposite, SWT.NONE);
		
		lblStatus = new Label(contentComposite, SWT.FLAT);
		lblStatus.setLayoutData(new GridData(SWT.END, SWT.LEFT, true, false, 2, 1));
		lblStatus.setText("\t\t\t");

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		attributesTableComposite.setLayout(tableColumnLayout);

		GridData attributesTableCompositeGridData = new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1);
		attributesTableCompositeGridData.heightHint = 10000;
		attributesTableCompositeGridData.widthHint = 10000;
		attributesTableComposite.setLayoutData(attributesTableCompositeGridData);

		attributesTableViewer = new TableViewer(attributesTableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);

		createColumns(attributesTableViewer, tableColumnLayout);

		// make lines and header visible
		final Table table = attributesTableViewer.getTable();

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		attributesTableViewer.setContentProvider(ArrayContentProvider.getInstance());

		attributesTableViewer.setInput(Collections.emptyList());

		sashForm.setWeights(new int[] { 1, 1 });
		
		return container;
	}

	private void addElementTreeToolbar(Composite explorerComposite) {
		ToolBar elementTreeToolbar = new ToolBar(explorerComposite, SWT.FLAT | SWT.RIGHT);

		btnStart = new ToolItem(elementTreeToolbar, SWT.NONE);
		btnStart.setImage(ImageConstants.IMG_24_START_DEVICE);
		btnStart.setText(StringConstants.DIA_TIP_START_APP);
		btnStart.setToolTipText(StringConstants.DIA_TIP_START_APP);
		btnStart.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startObjectInspectorAction();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		btnCapture = new ToolItem(elementTreeToolbar, SWT.NONE);
		btnCapture.setImage(ImageConstants.IMG_24_CAPTURE);
		btnCapture.setText(StringConstants.DIA_TIP_CAPTURE_OBJ);
		btnCapture.setToolTipText(StringConstants.DIA_TIP_CAPTURE_OBJ);
		btnCapture.setEnabled(false);
		btnCapture.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					captureObjectAction();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		btnAdd = new ToolItem(elementTreeToolbar, SWT.NONE);
		btnAdd.setImage(ImageConstants.IMG_24_NEW_TEST_OBJECT);
		btnAdd.setText(StringConstants.DIA_TIP_ADD);
		btnAdd.setToolTipText(StringConstants.DIA_TIP_ADD);
		btnAdd.setEnabled(false);
		btnAdd.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if(appRootElement != null){						
						//eventBroker.send(EventConstants.OBJECT_SPY_RESET_SELECTED_TARGET, "");
						parentFolder = findSelectedFolderInExplorer();
						if(parentFolder == null || parentFolder == orsRootNode){
							parentFolder = orsRootNode;
							FolderEntity appRootFolderEntity = FolderController.getInstance().getFolder(orsRootNode.getLocation() + File.separator + appRootElement.getName());
							if(appRootFolderEntity == null){
								FolderEntity folder = MobileElementUtil.convertPageElementToFolderEntity(appRootElement, parentFolder);
								parentFolder = ObjectRepositoryController.getInstance().importWebElementFolder(folder, parentFolder);
							}
							else{
								parentFolder = appRootFolderEntity;
							}
						}
						for(MobileElement childOfRoot : appRootElement.getChildrenElement()){
							addElement(childOfRoot, parentFolder);	
						}
						//Clear all selected element
						for(Object obj : elementTreeViewer.getCheckedElements()){
							elementTreeViewer.setChecked(obj, false);
						}
						//Refresh explorer
						eventBroker.post(EventConstants.OBJECT_SPY_REFRESH_SELECTED_TARGET, "");
						eventBroker.post(EventConstants.EXPLORER_REFRESH_TREE_ENTITY, null);
					}
				} catch (Exception ex) {
					logger.error(ex);
					MessageDialog.openError(getParentShell(), StringConstants.ERROR_TITLE, ex.getMessage());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		btnStop = new ToolItem(elementTreeToolbar, SWT.NONE);
		btnStop.setImage(ImageConstants.IMG_24_STOP_DEVICE);
		btnStop.setText(StringConstants.DIA_TIP_STOP);
		btnStop.setToolTipText(StringConstants.DIA_TIP_STOP);
		btnStop.setEnabled(false);
		btnStop.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stopObjectInspectorAction();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}

	@SuppressWarnings("unchecked")
	private void createColumns(TableViewer viewer, TableColumnLayout tableColumnLayout) {
		TableViewerColumn keyColumn = new TableViewerColumn(viewer, SWT.NONE);
		keyColumn.getColumn().setWidth(100);
		keyColumn.getColumn().setText(StringConstants.DIA_COL_NAME);
		keyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Entry) {
					Entry<String, String> entry = (Entry<String, String>) element;
					if (entry.getKey() != null) {
						return entry.getKey().toString();
					}
				}
				return "";
			}
		});

		TableViewerColumn valueColumn = new TableViewerColumn(viewer, SWT.NONE);
		valueColumn.getColumn().setWidth(200);
		valueColumn.getColumn().setText(StringConstants.DIA_COL_VALUE);
		valueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Entry) {
					Entry<String, String> entry = (Entry<String, String>) element;
					if (entry.getValue() != null) {
						return entry.getValue().toString();
					}
				}
				return "";
			}
		});

		valueColumn.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Entry && value instanceof String) {
					Entry<String, String> entry = (Entry<String, String>) element;
					entry.setValue(String.valueOf(value));
					attributesTableViewer.refresh(element);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Entry) {
					Entry<String, String> entry = (Entry<String, String>) element;
					if (entry.getValue() != null) {
						return entry.getValue().toString();
					}
				}
				return "";
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				if (element instanceof Entry) {
					return new TextCellEditor(attributesTableViewer.getTable());
				}
				return null;
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof Entry) {
					return true;
				}
				return false;
			}
		});

		tableColumnLayout.setColumnData(keyColumn.getColumn(), new ColumnWeightData(20, 100, true));
		tableColumnLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(80, 200, true));

	}

	@Override
	protected void handleShellCloseEvent() {
		super.handleShellCloseEvent();
		dispose();
	}
	
	private void addElement(MobileElement element, FolderEntity parentFolder) throws Exception {
		if (elementTreeViewer.getChecked(element) || elementTreeViewer.getGrayed(element)) {
			WebElementEntity convertedElement = MobileElementUtil.convertElementToWebElementEntity(element, null, parentFolder); 
			ObjectRepositoryController.getInstance().importWebElement(convertedElement , parentFolder);
		}
		for (MobileElement childElement : element.getChildrenElement()) {
			addElement(childElement, parentFolder);
		}
	}

	public void dispose() {
		eventBroker.unsubscribe(this);
		isDisposed = true;
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(DIALOG_TITLE);
	}

	@Override
	public void handleEvent(Event event) {
	}

	//Should to allow edit Object name when user select an Object
	private void refreshAttributesTable(MobileElement selectedElement) {
		if (selectedElement != null) {
			attributesTableViewer.setInput(new ArrayList<Entry<String, String>>(selectedElement.getAttributes().entrySet()));
		} else {
			attributesTableViewer.setInput(Collections.emptyList());
		}
		attributesTableViewer.refresh();
	}

    // Highlight Selected object on captured screenshot
	private void highLightObject(MobileElement selectedElement){
		if (selectedElement != null) {
			// Get object's location and object's size
            if(selectedElement.getAttributes().get(GUIObject.X) != null 
            		&& selectedElement.getAttributes().get(GUIObject.Y) != null 
            		&& selectedElement.getAttributes().get(GUIObject.WIDTH) != null
            		&& selectedElement.getAttributes().get(GUIObject.HEIGHT) != null){

            	Map<String, Object> datas = new HashMap<String, Object>(); 
            	datas.put("selected_object", selectedElement);
            	eventBroker.send(EventConstants.OBJECT_SPY_MOBILE_HIGHLIGHT, datas);
            }
        }
	}
	
	private void captureObjectAction() throws Exception {
		//Reopen Device View Dialog if it has not opened
		eventBroker.send(EventConstants.OBJECT_SPY_ENSURE_DEVICE_VIEW_DIALOG, "");
		
		appRootElement = inspectorController.getMobileObjectRoot();
		//Root element should be named as .apk file name
		appRootElement.setName(FilenameUtils.getName(txtAppFile.getText()));
		elementTreeViewer.setInput(new Object[]{appRootElement});
		elementTreeViewer.refresh();
		 //Render Screenshot
        String imgPath = inspectorController.captureScreenshot();
        File imgFile = new File(imgPath);
        if (imgFile.exists()) {
        	Map<String, Object> images = new HashMap<String, Object>(); 
        	images.put("real_image_file_path", imgFile);
        	images.put("appium_screen_object", appRootElement);
        	eventBroker.send(EventConstants.OBJECT_SPY_MOBILE_SCREEN_CAPTURE, images);
        }
	}
	
	public boolean isDisposed() {
		return isDisposed;
	}

	public FolderEntity getParentFolder() {
		return parentFolder;
	}

	public void setParentFolder(FolderEntity parentFolder) {
		this.parentFolder = parentFolder;
	}

    private void startObjectInspectorAction() {
    	//Validate all required informations are filled
    	if (validateData()) {                
            if(cbbAppType.getSelectionIndex() == 0){
                lblStatus.setText(StringConstants.DIA_LBL_STATUS_APP_STARTING);
                btnStart.setEnabled(false);
                this.getShell().getDisplay().asyncExec(new Runnable() {						
					@Override
					public void run() {
						try {
	                        //Start application using MobileDriver
	                    	String deviceName = cbbDevices.getItem(cbbDevices.getSelectionIndex());
	                        boolean result = inspectorController.startMobileApp(inspectorController.getDeviceId(deviceName), txtAppFile.getText(), false);
	                        if (result) {
	                            //Enable more feature if start application successful
	                            btnAdd.setEnabled(true);
	                            btnCapture.setEnabled(true);
	                            btnStop.setEnabled(true);
	                            lblStatus.setText(StringConstants.DIA_LBL_STATUS_APP_STARTED);
	                        } else {
	                            //Enable start button and show error dialog if application cannot start
	                            btnStart.setEnabled(true);
	                            lblStatus.setText("");
	                            MessageDialog.openError(Display.getCurrent().getActiveShell(), StringConstants.ERROR_TITLE, 
	                            		StringConstants.DIA_ERROR_MSG_CANNOT_START_APP_ON_CURRENT_DEVICE);
	                        }
	                    } catch (Exception ex) {
	                        btnStart.setEnabled(true);
	                        lblStatus.setText("");
	                        MessageDialog.openError(Display.getCurrent().getActiveShell(), StringConstants.ERROR_TITLE, ex.getMessage());
	                        logger.error(ex);
	                    }							
					}
				});                   
            }                
        }
    }
    
    private void stopObjectInspectorAction() {
        // Close application
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Quit Driver
                inspectorController.closeApp();
            }
        });
        thread.start();

        // Update UI
        lblStatus.setText("");
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        btnCapture.setEnabled(false);
        btnAdd.setEnabled(false);
        
        elementTreeViewer.setInput(new Object[]{});
		elementTreeViewer.refresh();
        
		eventBroker.send(EventConstants.OBJECT_SPY_CLOSE_MOBILE_APP, "");

		dispose();		
    }
    
    private boolean validateData(){
        if (cbbDevices.getSelectionIndex() < 0) {
        	MessageDialog.openError(Display.getCurrent().getActiveShell(), StringConstants.ERROR_TITLE, 
        			StringConstants.DIA_ERROR_MSG_PLS_CONNECT_AND_SELECT_DEVICE);
            return false;
        }
        if (cbbAppType.getSelectionIndex() < 0) {
        	MessageDialog.openError(Display.getCurrent().getActiveShell(), StringConstants.ERROR_TITLE, 
        			StringConstants.DIA_ERROR_MSG_PLS_SELECT_APP_TYPE);
            return false;
        }
        else if(cbbAppType.getSelectionIndex() == 1){
        	MessageDialog.openError(Display.getCurrent().getActiveShell(), StringConstants.ERROR_TITLE, 
        			StringConstants.DIA_ERROR_MSG_HAVE_NOT_SUPPORT_MOBILE_WEB);
            return false;
        }
        return true;
    }
    
    @Override
	protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.MODELESS| SWT.BORDER | SWT.TITLE);
	    setBlockOnOpen(false);
	}
    
    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        if (id == IDialogConstants.CANCEL_ID || id == IDialogConstants.OK_ID)
            return null;
        return super.createButton(parent, id, label, defaultButton);
    }
    
    @Override
    public boolean close(){
    	stopObjectInspectorAction();
    	return super.close();
    } 
    
    private FolderEntity findSelectedFolderInExplorer() throws Exception {
		Object[] selectedObjects = (Object[]) selectionService.getSelection(IdConstants.EXPLORER_PART_ID);
		if(selectedObjects != null && selectedObjects.length > 0){
			ITreeEntity parentTreeEntity = getParentTreeEntity(selectedObjects);
			if (parentTreeEntity != null && parentTreeEntity.getObject() instanceof FolderEntity) {
				FolderEntity parentFolder = (FolderEntity) parentTreeEntity.getObject();
				return parentFolder;
			}
		}
		return null;
	}
    
    private ITreeEntity getParentTreeEntity(Object[] selectedObjects) throws Exception {
		for (Object object : selectedObjects) {
			if (object instanceof ITreeEntity) {
				if (((ITreeEntity) object).getObject() instanceof FolderEntity) {
					FolderEntity folder = (FolderEntity) ((ITreeEntity) object).getObject();
					if (folder.getFolderType() == FolderType.WEBELEMENT) {
						return (ITreeEntity) object;
					}
				} else if (((ITreeEntity) object).getObject() instanceof WebElementEntity) {
					return (ITreeEntity) ((ITreeEntity) object).getParent();
				}
			}
		}
		return null;
	}
}
