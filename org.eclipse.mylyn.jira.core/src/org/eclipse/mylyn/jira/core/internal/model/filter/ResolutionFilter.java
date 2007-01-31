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

import org.eclipse.mylar.jira.core.internal.model.Resolution;

/**
 * Filter for restricting issues by their resolution. If you are looking for
 * unresolved issues, don't specify and resolutions. If you want all issues
 * regardless of their resolution omit this filter.
 */
public class ResolutionFilter implements Filter, Serializable {
	private static final long serialVersionUID = 1L;

	private final Resolution[] resolutions;

	public ResolutionFilter(Resolution[] resolutions) {
		assert (resolutions != null);

		this.resolutions = resolutions;
	}

	public Resolution[] getResolutions() {
		return this.resolutions;
	}

	public boolean isUnresolved() {
		return resolutions.length == 0;
	}

	ResolutionFilter copy() {
		return new ResolutionFilter(this.resolutions);
	}
}