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

public class UserInGroupFilter extends UserFilter {
	private static final long serialVersionUID = 1L;

	private final String group;

	public UserInGroupFilter(String group) {
		assert (group != null);

		this.group = group;
	}

	public String getGroup() {
		return group;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.model.filter.UserFilter#copy()
	 */
	UserFilter copy() {
		return new UserInGroupFilter(this.group);
	}
}