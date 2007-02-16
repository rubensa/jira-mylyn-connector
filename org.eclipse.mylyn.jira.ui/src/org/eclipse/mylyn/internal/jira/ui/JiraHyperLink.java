/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.internal.jira.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.TasksUiUtil;

/**
 * @author Eugene Kuleshov
 */
public class JiraHyperLink implements IHyperlink {

	private final IRegion region;

	private final TaskRepository repository;
	
	private final String id;

	private final String taskUrl;

	public JiraHyperLink(IRegion nlsKeyRegion, TaskRepository repository, String id, String taskUrl) {
		this.region = nlsKeyRegion;
		this.repository = repository;
		this.id = id;
		this.taskUrl = taskUrl;
	}

	public IRegion getHyperlinkRegion() {
		return region;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return "Open Task " + id;
	}

	public void open() {
		if (repository != null) {
			TasksUiUtil.openRepositoryTask(repository.getUrl(), id, taskUrl);
		} else {
			MessageDialog.openError(null, "Mylar Jira Connector",
					"Could not determine repository for report");
		}
	}

}