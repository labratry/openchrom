/*******************************************************************************
 * Copyright (c) 2021, 2024 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Matthias Mailänder - initial API and implementation
 *******************************************************************************/
package net.openchrom.msd.converter.supplier.gaml.converter;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.chemclipse.converter.core.AbstractFileContentMatcher;
import org.eclipse.chemclipse.converter.core.IFileContentMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import net.openchrom.xxd.converter.supplier.gaml.io.Reader;
import net.openchrom.xxd.converter.supplier.gaml.v120.model.GAML;
import net.openchrom.xxd.converter.supplier.gaml.v120.model.ObjectFactory;
import net.openchrom.xxd.converter.supplier.gaml.v120.model.Technique;
import net.openchrom.xxd.converter.supplier.gaml.v120.model.Trace;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

public class FileContentMatcher extends AbstractFileContentMatcher implements IFileContentMatcher {

	@Override
	public boolean checkFileFormat(File file) {

		boolean isValidFormat = false;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList nodeList = document.getElementsByTagName(Reader.NODE_GAML);
			//
			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			GAML gaml = (GAML)unmarshaller.unmarshal(nodeList.item(0));
			boolean chromatography = false;
			boolean msdetector = false;
			for(Trace trace : gaml.getExperiment().get(0).getTrace()) {
				if(trace.getTechnique() == Technique.CHROM) {
					chromatography = true;
				} else if(trace.getTechnique() == Technique.MS) {
					msdetector = true;
				}
			}
			if(chromatography && msdetector) {
				isValidFormat = true;
			}
		} catch(Exception e) {
			// fail silently
		}
		return isValidFormat;
	}
}
