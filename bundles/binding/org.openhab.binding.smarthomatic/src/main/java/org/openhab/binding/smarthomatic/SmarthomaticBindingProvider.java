/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smarthomatic;

import org.openhab.core.binding.BindingProvider;
import org.openhab.core.items.Item;

/**
 * @author arohde
 * @since 0.1.0
 */
public interface SmarthomaticBindingProvider extends BindingProvider {

	public int getMessageGroupId(String itemName);

	public int getMessageId(String itemName);

	public int getDeviceId(String itemName);

	public int getMessagePartId(String itemName);

	public Item getItem(String itemName);

	public String getConfigParam(String itemName, String paramName);

}
