/*******************************************************************************
 * Copyright (c) 2019, 2023 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.xxd.classifier.supplier.ratios.model;

import java.util.List;

import org.eclipse.chemclipse.chromatogram.xxd.classifier.result.ResultStatus;

import net.openchrom.xxd.classifier.supplier.ratios.model.time.TimeRatios;

import junit.framework.TestCase;

public class PeakRatioResult_2_Test extends TestCase {

	private PeakRatioResult peakRatioResult;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		peakRatioResult = new PeakRatioResult(ResultStatus.OK, "Test", new TimeRatios());
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public void test1() {

		assertNotNull(peakRatioResult.getPeakRatios());
	}

	public void test2() {

		List<? extends IPeakRatio> peakRatios = peakRatioResult.getPeakRatios();
		assertEquals(0, peakRatios.size());
	}
}