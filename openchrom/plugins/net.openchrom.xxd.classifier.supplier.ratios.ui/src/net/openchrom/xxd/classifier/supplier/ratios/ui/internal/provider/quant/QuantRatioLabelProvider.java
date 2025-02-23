/*******************************************************************************
 * Copyright (c) 2019, 2024 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 * Christoph Läubrich - Color-support
 *******************************************************************************/
package net.openchrom.xxd.classifier.supplier.ratios.ui.internal.provider.quant;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.eclipse.chemclipse.model.core.IChromatogramOverview;
import org.eclipse.chemclipse.model.core.IPeak;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.support.text.ValueFormat;
import org.eclipse.swt.graphics.Image;

import net.openchrom.xxd.classifier.supplier.ratios.model.quant.QuantRatio;
import net.openchrom.xxd.classifier.supplier.ratios.ui.internal.provider.AbstractTraceRatioLabelProvider;
import net.openchrom.xxd.classifier.supplier.ratios.ui.swt.DisplayOption;

public class QuantRatioLabelProvider extends AbstractTraceRatioLabelProvider {

	private DecimalFormat decimalFormat = ValueFormat.getDecimalFormatEnglish();
	private DisplayOption displayOption = DisplayOption.RESULTS;

	public QuantRatioLabelProvider() {

		this(DisplayOption.RESULTS);
	}

	public QuantRatioLabelProvider(DisplayOption displayOption) {

		super(DisplayOption.RESULTS.equals(displayOption) ? //
				Arrays.asList(QuantRatioResultTitles.INDEX_RESULTS_CONCENTRATION, QuantRatioResultTitles.INDEX_RESULTS_CONCENTRATION_UNIT) : //
				Arrays.asList() //
		);
		this.displayOption = displayOption;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		if(columnIndex == 0) {
			return getImage(element);
		} else {
			return null;
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {

		String text;
		switch(displayOption) {
			case RESULTS:
				text = getColumnTextResults(element, columnIndex);
				break;
			case SETTINGS:
				text = getColumnTextSettings(element, columnIndex);
				break;
			default:
				text = "";
				break;
		}
		return text;
	}

	private String getColumnTextSettings(Object element, int columnIndex) {

		String text = "";
		if(element instanceof QuantRatio ratio) {
			switch(columnIndex) {
				case 0:
					text = ratio.getName();
					break;
				case 1:
					text = ratio.getQuantitationName();
					break;
				case 2:
					text = decimalFormat.format(ratio.getExpectedConcentration());
					break;
				case 3:
					text = ratio.getConcentrationUnit();
					break;
				case 4:
					text = decimalFormat.format(ratio.getDeviationWarn());
					break;
				case 5:
					text = decimalFormat.format(ratio.getDeviationError());
					break;
			}
		}
		return text;
	}

	private String getColumnTextResults(Object element, int columnIndex) {

		String text = "";
		if(element instanceof QuantRatio ratio) {
			switch(columnIndex) {
				case 0:
					IPeak peak = ratio.getPeak();
					if(peak != null) {
						text = decimalFormat.format(peak.getPeakModel().getRetentionTimeAtPeakMaximum() / IChromatogramOverview.MINUTE_CORRELATION_FACTOR);
					} else {
						text = "--";
					}
					break;
				case 1:
					text = ratio.getName();
					break;
				case 2:
					text = ratio.getQuantitationName();
					break;
				case 3:
					text = decimalFormat.format(ratio.getExpectedConcentration());
					break;
				case 4:
					text = decimalFormat.format(ratio.getConcentration());
					break;
				case 5:
					text = ratio.getConcentrationUnit();
					break;
				case 6:
					text = decimalFormat.format(ratio.getDeviation());
					break;
				case 7:
					text = decimalFormat.format(ratio.getResponseFactor());
					break;
			}
		}
		return text;
	}

	@Override
	public Image getImage(Object element) {

		return ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CLASSIFIER, IApplicationImage.SIZE_16x16);
	}
}