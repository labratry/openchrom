/*******************************************************************************
 * Copyright (c) 2018, 2023 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 * Christoph Läubrich - settings provider
 *******************************************************************************/
package net.openchrom.xxd.process.supplier.templates.ui.swt;

import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.ADD_TOOLTIP;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.DIALOG_TITLE;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.EDIT_TOOLTIP;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.EXPORT_TITLE;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.IMAGE_ADJUST_POSITION;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.IMPORT_TITLE;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.MESSAGE_ADD;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.MESSAGE_EDIT;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.MESSAGE_EXPORT_FAILED;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.MESSAGE_EXPORT_SUCCESSFUL;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.MESSAGE_REMOVE;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.MESSAGE_REMOVE_ALL;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.REMOVE_ALL_TOOLTIP;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.REMOVE_TOOLTIP;
import static net.openchrom.xxd.process.supplier.templates.ui.fieldeditors.AbstractFieldEditor.TOOLTIP_ADJUST_POSITION;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.chemclipse.processing.core.ICategories;
import org.eclipse.chemclipse.processing.supplier.IProcessorPreferences;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImageProvider;
import org.eclipse.chemclipse.support.ui.events.IKeyEventProcessor;
import org.eclipse.chemclipse.support.ui.menu.ITableMenuEntry;
import org.eclipse.chemclipse.support.ui.swt.ExtendedTableViewer;
import org.eclipse.chemclipse.support.ui.swt.ITableSettings;
import org.eclipse.chemclipse.support.updates.IUpdateListener;
import org.eclipse.chemclipse.swt.ui.components.ISearchListener;
import org.eclipse.chemclipse.swt.ui.components.SearchSupportUI;
import org.eclipse.chemclipse.ux.extension.xxd.ui.methods.SettingsUIProvider;
import org.eclipse.chemclipse.ux.extension.xxd.ui.swt.IExtendedPartUI;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import net.openchrom.xxd.process.supplier.templates.model.AbstractSetting;
import net.openchrom.xxd.process.supplier.templates.model.AssignerStandard;
import net.openchrom.xxd.process.supplier.templates.model.AssignerStandards;
import net.openchrom.xxd.process.supplier.templates.preferences.PreferenceSupplier;
import net.openchrom.xxd.process.supplier.templates.settings.StandardsAssignerSettings;
import net.openchrom.xxd.process.supplier.templates.ui.validators.StandardsAssignerInputValidator;
import net.openchrom.xxd.process.supplier.templates.util.StandardsAssignerListUtil;

public class StandardsAssignerEditor implements SettingsUIProvider.SettingsUIControl, IExtendedPartUI {

	private AtomicReference<Button> buttonSearchControl = new AtomicReference<>();
	private AtomicReference<SearchSupportUI> toolbarSearch = new AtomicReference<>();
	private AtomicReference<Button> buttonAdjustControl = new AtomicReference<>();
	private AtomicReference<PositionAdjusterUI> toolbarAdjuster = new AtomicReference<>();
	private AtomicReference<StandardsAssignerListUI> listControl = new AtomicReference<>();
	//
	private Composite control;
	//
	private static final String DELETE = "Delete";
	//
	private Listener listener;
	private List<Button> buttons = new ArrayList<>();
	private Button buttonAdd;
	private Button buttonEdit;
	private Button buttonRemove;
	private Button buttonRemoveAll;
	private Button buttonImport;
	private Button buttonExport;
	//
	private AssignerStandards settings = new AssignerStandards();
	private IProcessorPreferences<StandardsAssignerSettings> preferences;

	public StandardsAssignerEditor(Composite parent, IProcessorPreferences<StandardsAssignerSettings> preferences, StandardsAssignerSettings settings) {

		this.preferences = preferences;
		if(settings != null) {
			this.settings.load(settings.getAssignerSettings());
		}
		//
		control = createControl(parent);
	}

	public void setSettings(AssignerStandards settings) {

		this.settings = settings;
		setInput();
	}

	@Override
	public void setEnabled(boolean enabled) {

		for(Button button : buttons) {
			button.setEnabled(enabled);
		}
		listControl.get().getControl().setEnabled(enabled);
	}

	@Override
	public IStatus validate() {

		return ValidationStatus.ok();
	}

	@Override
	public String getSettings() throws IOException {

		if(preferences != null) {
			StandardsAssignerSettings settingz = new StandardsAssignerSettings();
			settingz.setAssignerSettings(settings.save());
			return preferences.getSerialization().toString(settingz);
		}
		return "";
	}

	@Override
	public void addChangeListener(Listener listener) {

		this.listener = listener;
		//
		Table table = listControl.get().getTable();
		table.addListener(SWT.Selection, listener);
		table.addListener(SWT.KeyUp, listener);
		table.addListener(SWT.MouseUp, listener);
		table.addListener(SWT.MouseDoubleClick, listener);
		//
		buttonAdd.addListener(SWT.KeyUp, listener);
		buttonEdit.addListener(SWT.KeyUp, listener);
		buttonRemove.addListener(SWT.KeyUp, listener);
		buttonRemoveAll.addListener(SWT.KeyUp, listener);
		buttonImport.addListener(SWT.KeyUp, listener);
		buttonExport.addListener(SWT.KeyUp, listener);
	}

	@Override
	public Control getControl() {

		return control;
	}

	@Override
	public void restoreDefaults() {

		listControl.get().clear();
	}

	public void load(String entries) {

		settings.load(entries);
		setInput();
	}

	public String getValues() {

		return settings.save();
	}

	private Composite createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);
		//
		createToolbarMain(composite);
		createSearchSection(composite);
		createAdjustSection(composite);
		createTableSection(composite);
		//
		initialize();
		//
		return composite;
	}

	private void initialize() {

		enableToolbar(toolbarSearch, buttonSearchControl.get(), IMAGE_SEARCH, TOOLTIP_SEARCH, false);
		enableToolbar(toolbarAdjuster, buttonAdjustControl.get(), IMAGE_ADJUST_POSITION, TOOLTIP_ADJUST_POSITION, false);
		setInput();
	}

	private void createTableSection(Composite parent) {

		StandardsAssignerListUI standardsAssignerListUI = new StandardsAssignerListUI(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		Table table = standardsAssignerListUI.getTable();
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = 600;
		gridData.heightHint = 400;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);
		//
		table.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				/*
				 * Set the selected elements.
				 */
				List<AbstractSetting> settings = new ArrayList<>();
				List<?> objects = standardsAssignerListUI.getStructuredSelection().toList();
				//
				for(Object object : objects) {
					if(object instanceof AbstractSetting setting) {
						settings.add(setting);
					}
				}
				//
				toolbarAdjuster.get().setInput(settings);
			}
		});
		//
		standardsAssignerListUI.setEditEnabled(true);
		standardsAssignerListUI.setUpdateListener(new IUpdateListener() {

			@Override
			public void update() {

				setInput();
			}
		});
		//
		Shell shell = standardsAssignerListUI.getTable().getShell();
		ITableSettings tableSettings = standardsAssignerListUI.getTableSettings();
		addDeleteMenuEntry(shell, tableSettings);
		addKeyEventProcessors(shell, tableSettings);
		standardsAssignerListUI.applySettings(tableSettings);
		//
		listControl.set(standardsAssignerListUI);
	}

	private Button createButtonAdd(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText(ADD_TOOLTIP);
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_ADD, IApplicationImageProvider.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				InputDialog dialog = new InputDialog(e.display.getActiveShell(), DIALOG_TITLE, MESSAGE_ADD, StandardsAssignerListUtil.EXAMPLE_SINGLE, new StandardsAssignerInputValidator(settings.keySet()));
				if(IDialogConstants.OK_ID == dialog.open()) {
					String item = dialog.getValue();
					AssignerStandard setting = settings.extractSettingInstance(item);
					if(setting != null) {
						settings.add(setting);
						setInput();
					}
				}
			}
		});
		//
		return button;
	}

	private Button createButtonEdit(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText(EDIT_TOOLTIP);
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EDIT, IApplicationImageProvider.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				IStructuredSelection structuredSelection = (IStructuredSelection)listControl.get().getSelection();
				Object object = structuredSelection.getFirstElement();
				if(object instanceof AssignerStandard setting) {
					Set<String> keySetEdit = new HashSet<>();
					keySetEdit.addAll(settings.keySet());
					keySetEdit.remove(setting.getName());
					InputDialog dialog = new InputDialog(e.display.getActiveShell(), DIALOG_TITLE, MESSAGE_EDIT, settings.extractSetting(setting), new StandardsAssignerInputValidator(keySetEdit));
					if(IDialogConstants.OK_ID == dialog.open()) {
						String item = dialog.getValue();
						AssignerStandard settingNew = settings.extractSettingInstance(item);
						setting.copyFrom(settingNew);
						setInput();
					}
				}
			}
		});
		//
		return button;
	}

	private Button createButtonRemove(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText(REMOVE_TOOLTIP);
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_DELETE, IApplicationImageProvider.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				deleteItems(e.display.getActiveShell());
			}
		});
		//
		return button;
	}

	private Button createButtonRemoveAll(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText(REMOVE_ALL_TOOLTIP);
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_DELETE_ALL, IApplicationImageProvider.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if(MessageDialog.openQuestion(e.display.getActiveShell(), DIALOG_TITLE, MESSAGE_REMOVE_ALL)) {
					settings.clear();
					setInput();
				}
			}
		});
		//
		return button;
	}

	private Button createButtonImport(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText(IMPORT_TITLE);
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_IMPORT, IApplicationImageProvider.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fileDialog = new FileDialog(e.widget.getDisplay().getActiveShell(), SWT.READ_ONLY);
				fileDialog.setText(IMPORT_TITLE);
				fileDialog.setFilterExtensions(new String[]{AssignerStandards.FILTER_EXTENSION});
				fileDialog.setFilterNames(new String[]{AssignerStandards.FILTER_NAME});
				fileDialog.setFilterPath(PreferenceSupplier.getListPathImport());
				String path = fileDialog.open();
				if(path != null) {
					PreferenceSupplier.setListPathImport(fileDialog.getFilterPath());
					File file = new File(path);
					settings.importItems(file);
					setInput();
				}
			}
		});
		//
		return button;
	}

	private Button createButtonExport(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText(EXPORT_TITLE);
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EXPORT, IApplicationImageProvider.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fileDialog = new FileDialog(e.widget.getDisplay().getActiveShell(), SWT.SAVE);
				fileDialog.setOverwrite(true);
				fileDialog.setText(EXPORT_TITLE);
				fileDialog.setFilterExtensions(new String[]{AssignerStandards.FILTER_EXTENSION});
				fileDialog.setFilterNames(new String[]{AssignerStandards.FILTER_NAME});
				fileDialog.setFileName(AssignerStandards.FILE_NAME);
				fileDialog.setFilterPath(PreferenceSupplier.getListPathExport());
				String path = fileDialog.open();
				if(path != null) {
					PreferenceSupplier.setListPathExport(fileDialog.getFilterPath());
					File file = new File(path);
					if(settings.exportItems(file)) {
						MessageDialog.openInformation(e.display.getActiveShell(), EXPORT_TITLE, MESSAGE_EXPORT_SUCCESSFUL);
					} else {
						MessageDialog.openWarning(e.display.getActiveShell(), EXPORT_TITLE, MESSAGE_EXPORT_FAILED);
					}
				}
			}
		});
		//
		return button;
	}

	private void setInput() {

		toolbarAdjuster.get().setInput(settings);
		listControl.get().setInput(settings);
		//
		if(listener != null) {
			listener.handleEvent(new Event());
		}
	}

	private void createToolbarMain(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalAlignment = SWT.END;
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(8, false));
		//
		add(createButtonToggleSearch(composite));
		add(createButtonToggleAdjust(composite));
		add(buttonAdd = createButtonAdd(composite));
		add(buttonEdit = createButtonEdit(composite));
		add(buttonRemove = createButtonRemove(composite));
		add(buttonRemoveAll = createButtonRemoveAll(composite));
		add(buttonImport = createButtonImport(composite));
		add(buttonExport = createButtonExport(composite));
	}

	private Button createButtonToggleSearch(Composite parent) {

		Button button = createButtonToggleToolbar(parent, toolbarSearch, IMAGE_SEARCH, TOOLTIP_SEARCH);
		buttonSearchControl.set(button);
		//
		return button;
	}

	private Button createButtonToggleAdjust(Composite parent) {

		Button button = createButtonToggleToolbar(parent, toolbarAdjuster, IMAGE_ADJUST_POSITION, TOOLTIP_ADJUST_POSITION);
		buttonAdjustControl.set(button);
		//
		return button;
	}

	private void add(Button button) {

		buttons.add(button);
	}

	private void createSearchSection(Composite parent) {

		SearchSupportUI searchSupportUI = new SearchSupportUI(parent, SWT.NONE);
		searchSupportUI.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		searchSupportUI.setSearchListener(new ISearchListener() {

			@Override
			public void performSearch(String searchText, boolean caseSensitive) {

				listControl.get().setSearchText(searchText, caseSensitive);
			}
		});
		//
		toolbarSearch.set(searchSupportUI);
	}

	private void createAdjustSection(Composite parent) {

		PositionAdjusterUI positionAdjusterUI = new PositionAdjusterUI(parent, SWT.NONE);
		positionAdjusterUI.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//
		positionAdjusterUI.setUpdateListener(new IUpdateListener() {

			@Override
			public void update() {

				listControl.get().refresh();
			}
		});
		//
		toolbarAdjuster.set(positionAdjusterUI);
	}

	private void addDeleteMenuEntry(Shell shell, ITableSettings tableSettings) {

		tableSettings.addMenuEntry(new ITableMenuEntry() {

			@Override
			public String getName() {

				return DELETE;
			}

			@Override
			public String getCategory() {

				return ICategories.PEAK_IDENTIFIER;
			}

			@Override
			public void execute(ExtendedTableViewer extendedTableViewer) {

				deleteItems(shell);
			}
		});
	}

	private void addKeyEventProcessors(Shell shell, ITableSettings tableSettings) {

		tableSettings.addKeyEventProcessor(new IKeyEventProcessor() {

			@Override
			public void handleEvent(ExtendedTableViewer extendedTableViewer, KeyEvent e) {

				if(e.keyCode == SWT.DEL) {
					deleteItems(shell);
				}
			}
		});
	}

	private void deleteItems(Shell shell) {

		if(MessageDialog.openQuestion(shell, DIALOG_TITLE, MESSAGE_REMOVE)) {
			IStructuredSelection structuredSelection = (IStructuredSelection)listControl.get().getSelection();
			for(Object object : structuredSelection.toArray()) {
				if(object instanceof AssignerStandard setting) {
					settings.remove(setting);
				}
			}
			setInput();
		}
	}
}