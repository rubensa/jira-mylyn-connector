/*******************************************************************************
 * Copyright (c) 2005 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.jira.core.internal.model.filter;

import java.io.Serializable;

import org.eclipse.mylar.jira.core.internal.model.Status;

public class StatusFilter implements Filter, Serializable {
	private static final long serialVersionUID = 1L;

	private final Status[] statuses;

	public StatusFilter(Status[] statuses) {
		assert (statuses != null);
		assert (statuses.length > 0);

		this.statuses = statuses;
	}

	public Status[] getStatuses() {
		return this.statuses;
	}

	StatusFilter copy() {
		return new StatusFilter(this.statuses);
	}

}