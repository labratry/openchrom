/*******************************************************************************
 * Copyright (c) 2021, 2022 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Matthias Mailänder - initial API and implementation
 *******************************************************************************/
package net.openchrom.nmr.converter.supplier.nmrml.internal.v100.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuantifiedCompoundListType", propOrder = {"quantifiedCompound"})
public class QuantifiedCompoundListType {

	@XmlElement(required = true)
	protected List<QuantifiedCompoundType> quantifiedCompound;

	public List<QuantifiedCompoundType> getQuantifiedCompound() {

		if(quantifiedCompound == null) {
			quantifiedCompound = new ArrayList<QuantifiedCompoundType>();
		}
		return this.quantifiedCompound;
	}
}
