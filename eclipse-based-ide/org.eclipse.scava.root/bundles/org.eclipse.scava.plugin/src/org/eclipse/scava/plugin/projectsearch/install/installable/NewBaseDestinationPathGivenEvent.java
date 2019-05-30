/*********************************************************************
* Copyright c 2017 FrontEndART Software Ltd.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse PublicLicense 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/

package org.eclipse.scava.plugin.projectsearch.install.installable;

import org.eclipse.scava.plugin.mvc.controller.Controller;
import org.eclipse.scava.plugin.mvc.event.routed.RoutedEvent;

public class NewBaseDestinationPathGivenEvent extends RoutedEvent {
	private final String path;

	public NewBaseDestinationPathGivenEvent(Controller source, String path) {
		super(source);
		this.path = path;
	}

	public String getPath() {
		return path;
	}
}