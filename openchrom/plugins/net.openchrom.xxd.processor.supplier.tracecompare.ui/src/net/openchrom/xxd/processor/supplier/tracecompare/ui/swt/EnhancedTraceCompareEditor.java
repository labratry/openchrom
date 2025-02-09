/*******************************************************************************
 * Copyright (c) 2017, 2021 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.xxd.processor.supplier.tracecompare.ui.swt;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.ProcessingInfo;
import org.eclipse.chemclipse.processing.ui.support.ProcessingInfoPartSupport;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.support.ui.listener.AbstractControllerComposite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import net.openchrom.xxd.processor.supplier.tracecompare.model.IProcessorModel;
import net.openchrom.xxd.processor.supplier.tracecompare.model.IReferenceModel;
import net.openchrom.xxd.processor.supplier.tracecompare.model.ISampleModel;
import net.openchrom.xxd.processor.supplier.tracecompare.model.ITrackModel;
import net.openchrom.xxd.processor.supplier.tracecompare.ui.editors.EditorProcessor;
import net.openchrom.xxd.processor.supplier.tracecompare.ui.preferences.PreferencePage;

public class EnhancedTraceCompareEditor extends AbstractControllerComposite {

	private static final Logger logger = Logger.getLogger(EnhancedTraceCompareEditor.class);
	private static final String DESCRIPTION = "Trace Compare";
	private static final String EVALUATE_REFERENCE = "Evaluate Reference";
	//
	private EditorProcessor editorProcessor;
	//
	private Button buttonValidate;
	private Button buttonSettings;
	private Button buttonNext;
	private List<Button> buttons;
	//
	private TraceCompareEditorUI traceCompareEditorUI;

	public EnhancedTraceCompareEditor(Composite parent, int style) {

		super(parent, style);
		buttons = new ArrayList<Button>();
		createControl();
	}

	public void setEditorProcessor(EditorProcessor editorProcessor) {

		this.editorProcessor = editorProcessor;
		editorProcessor.setDirty(true);
		traceCompareEditorUI.update(editorProcessor);
	}

	@Override
	public boolean setFocus() {

		return super.setFocus();
	}

	@Override
	public void setStatus(boolean readOnly) {

		for(Button button : buttons) {
			button.setEnabled(false);
		}
		/*
		 * Defaults when editable.
		 */
		if(!readOnly) {
			buttonValidate.setEnabled(true);
			buttonNext.setEnabled(true);
		}
		buttonSettings.setEnabled(true);
	}

	private void createControl() {

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		 * Standards Table
		 */
		Composite chartComposite = new Composite(composite, SWT.NONE);
		chartComposite.setLayout(new GridLayout(1, true));
		chartComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		 * Trace Compare
		 */
		traceCompareEditorUI = new TraceCompareEditorUI(chartComposite, SWT.NONE);
		traceCompareEditorUI.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		 * Button Bar
		 */
		Composite compositeButtons = new Composite(composite, SWT.NONE);
		compositeButtons.setLayout(new GridLayout(1, true));
		compositeButtons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		//
		GridData gridDataButtons = new GridData(GridData.FILL_HORIZONTAL);
		gridDataButtons.minimumWidth = 150;
		//
		buttons.add(buttonValidate = createValidateButton(compositeButtons, gridDataButtons));
		buttons.add(buttonSettings = createSettingsButton(compositeButtons, gridDataButtons));
		buttons.add(buttonNext = createNextButton(compositeButtons, gridDataButtons));
		buttons.add(createSaveButton(compositeButtons, gridDataButtons));
	}

	private Button createValidateButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Validate");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_VALIDATE, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if(editorProcessor != null) {
					IProcessingInfo<?> processingInfo = new ProcessingInfo<>();
					//
					IProcessorModel processorModel = editorProcessor.getProcessorModel();
					Set<String> references = processorModel.getReferenceModels().keySet();
					for(String reference : references) {
						IReferenceModel referenceModel = processorModel.getReferenceModels().get(reference);
						for(ISampleModel sampleModel : referenceModel.getSampleModels().values()) {
							Set<Integer> tracks = sampleModel.getTrackModels().keySet();
							for(Integer track : tracks) {
								/*
								 * Check each model.
								 */
								ITrackModel trackModel = sampleModel.getTrackModels().get(track);
								if(trackModel.isSkipped()) {
									continue;
								}
								/*
								 * Mark non-evaluated samples.
								 */
								if(!trackModel.isEvaluated()) {
									processingInfo.addWarnMessage(EVALUATE_REFERENCE, reference + " > Track " + track);
								}
							}
						}
					}
					//
					if(!processingInfo.hasWarnMessages() && !processingInfo.hasErrorMessages()) {
						processingInfo.addInfoMessage(DESCRIPTION, "All traces have been evaluated successfully.");
					}
					//
					ProcessingInfoPartSupport.getInstance().update(processingInfo, true);
				}
			}
		});
		return button;
	}

	private Button createSettingsButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Settings");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CONFIGURE, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				IPreferencePage preferencePage = new PreferencePage();
				preferencePage.setTitle("TraceCompare Preferences");
				PreferenceManager preferenceManager = new PreferenceManager();
				preferenceManager.addToRoot(new PreferenceNode("1", preferencePage));
				//
				PreferenceDialog preferenceDialog = new PreferenceDialog(Display.getDefault().getActiveShell(), preferenceManager);
				preferenceDialog.create();
				preferenceDialog.setMessage("Settings");
				if(preferenceDialog.open() == Window.OK) {
					MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Settings", "The settings have been updated.");
				}
			}
		});
		return button;
	}

	private Button createNextButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Next");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_ARROW_FORWARD, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				fireUpdateNext();
			}
		});
		return button;
	}

	private Button createSaveButton(Composite parent, GridData gridData) {

		Shell shell = Display.getDefault().getActiveShell();
		//
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Save");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_SAVE, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ProgressMonitorDialog monitor = new ProgressMonitorDialog(shell);
				try {
					monitor.run(true, true, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {

									editorProcessor.doSave(monitor);
								}
							});
						}
					});
				} catch(InvocationTargetException e1) {
					logger.warn(e1);
				} catch(InterruptedException e1) {
					logger.warn(e1);
				}
			}
		});
		return button;
	}
}
