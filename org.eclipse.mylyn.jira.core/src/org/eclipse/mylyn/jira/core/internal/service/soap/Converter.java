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
package org.eclipse.mylar.jira.core.internal.service.soap;

import org.eclipse.mylar.jira.core.internal.beans.RemoteComponent;
import org.eclipse.mylar.jira.core.internal.beans.RemoteFilter;
import org.eclipse.mylar.jira.core.internal.beans.RemoteGroup;
import org.eclipse.mylar.jira.core.internal.beans.RemoteIssue;
import org.eclipse.mylar.jira.core.internal.beans.RemoteIssueType;
import org.eclipse.mylar.jira.core.internal.beans.RemotePriority;
import org.eclipse.mylar.jira.core.internal.beans.RemoteProject;
import org.eclipse.mylar.jira.core.internal.beans.RemoteResolution;
import org.eclipse.mylar.jira.core.internal.beans.RemoteServerInfo;
import org.eclipse.mylar.jira.core.internal.beans.RemoteStatus;
import org.eclipse.mylar.jira.core.internal.beans.RemoteUser;
import org.eclipse.mylar.jira.core.internal.beans.RemoteVersion;
import org.eclipse.mylar.jira.core.internal.model.Component;
import org.eclipse.mylar.jira.core.internal.model.Group;
import org.eclipse.mylar.jira.core.internal.model.Issue;
import org.eclipse.mylar.jira.core.internal.model.IssueType;
import org.eclipse.mylar.jira.core.internal.model.NamedFilter;
import org.eclipse.mylar.jira.core.internal.model.Priority;
import org.eclipse.mylar.jira.core.internal.model.Project;
import org.eclipse.mylar.jira.core.internal.model.Resolution;
import org.eclipse.mylar.jira.core.internal.model.ServerInfo;
import org.eclipse.mylar.jira.core.internal.model.Status;
import org.eclipse.mylar.jira.core.internal.model.User;
import org.eclipse.mylar.jira.core.internal.model.Version;


class Converter {

	protected static Priority[] convert(RemotePriority[] remotePriorities) {
		Priority[] priorities = new Priority[remotePriorities.length];
		for (int i = 0; i < remotePriorities.length; i++) {
			priorities[i] = convert(remotePriorities[i]);
		}
		return priorities;
	}

	protected static Priority convert(RemotePriority remotePriority) {
		Priority priority = new Priority();
		priority.setColour(remotePriority.getColor());
		priority.setDescription(remotePriority.getDescription());
		priority.setIcon(remotePriority.getIcon());
		priority.setId(remotePriority.getId());
		priority.setName(remotePriority.getName());

		return priority;
	}

	protected static Status[] convert(RemoteStatus[] remoteStatuses) {
		Status[] statuses = new Status[remoteStatuses.length];
		for (int i = 0; i < remoteStatuses.length; i++) {
			statuses[i] = convert(remoteStatuses[i]);
		}
		return statuses;
	}

	protected static Status convert(RemoteStatus remoteStatus) {
		Status status = new Status();
		status.setDescription(remoteStatus.getDescription());
		status.setIcon(remoteStatus.getIcon());
		status.setId(remoteStatus.getId());
		status.setName(remoteStatus.getName());

		return status;
	}

	protected static Version[] convert(RemoteVersion[] remoteVersions) {
		Version[] versions = new Version[remoteVersions.length];
		for (int i = 0; i < remoteVersions.length; i++) {
			versions[i] = convert(remoteVersions[i]);
		}
		return versions;
	}

	protected static Version convert(RemoteVersion remoteVersion) {
		Version version = new Version();
		version.setArchived(remoteVersion.isArchived());
		version.setId(remoteVersion.getId());
		version.setName(remoteVersion.getName());
		version.setReleased(remoteVersion.isReleased());
		version
				.setReleaseDate(remoteVersion.getReleaseDate() != null ? remoteVersion.getReleaseDate().getTime()
						: null);
		version.setSequence(remoteVersion.getSequence().longValue());

		return version;
	}

	protected static Resolution[] convert(RemoteResolution[] remoteResolutions) {
		Resolution[] resolutions = new Resolution[remoteResolutions.length];
		for (int i = 0; i < remoteResolutions.length; i++) {
			resolutions[i] = convert(remoteResolutions[i]);
		}
		return resolutions;
	}

	protected static Resolution convert(RemoteResolution remoteResolution) {
		Resolution resolution = new Resolution();
		resolution.setDescription(remoteResolution.getDescription());
		resolution.setIcon(remoteResolution.getIcon());
		resolution.setId(remoteResolution.getId());
		resolution.setName(remoteResolution.getName());

		return resolution;
	}

	protected static IssueType[] convert(RemoteIssueType[] remoteIssueTypes) {
		IssueType[] issueTypes = new IssueType[remoteIssueTypes.length];
		for (int i = 0; i < remoteIssueTypes.length; i++) {
			issueTypes[i] = convert(remoteIssueTypes[i]);
		}
		return issueTypes;
	}

	protected static IssueType convert(RemoteIssueType remoteIssueType) {
		IssueType issueType = new IssueType();
		issueType.setDescription(remoteIssueType.getDescription());
		issueType.setIcon(remoteIssueType.getIcon());
		issueType.setId(remoteIssueType.getId());
		issueType.setName(remoteIssueType.getName());

		return issueType;
	}

	protected static Project[] convert(RemoteProject[] remoteProjects) {
		Project[] projects = new Project[remoteProjects.length];
		for (int i = 0; i < remoteProjects.length; i++) {
			projects[i] = convert(remoteProjects[i]);
		}
		return projects;
	}

	protected static Project convert(RemoteProject remoteProject) {
		Project project = new Project();
		project.setDescription(remoteProject.getDescription());
		project.setId(remoteProject.getId());
		project.setKey(remoteProject.getKey());
		project.setLead(remoteProject.getLead());
		project.setName(remoteProject.getName());
		project.setProjectUrl(remoteProject.getProjectUrl());
		project.setUrl(remoteProject.getUrl());

		return project;
	}

	protected static Component[] convert(RemoteComponent[] remoteComponents) {
		Component[] components = new Component[remoteComponents.length];
		for (int i = 0; i < remoteComponents.length; i++) {
			components[i] = convert(remoteComponents[i]);
		}
		return components;
	}

	protected static Component convert(RemoteComponent remoteComponent) {
		Component component = new Component();
		component.setId(remoteComponent.getId());
		component.setName(remoteComponent.getName());

		return component;
	}

	protected static User[] convert(RemoteUser[] remoteUsers) {
		User[] users = new User[remoteUsers.length];
		for (int i = 0; i < remoteUsers.length; i++) {
			users[i] = convert(remoteUsers[i]);
		}
		return users;
	}

	protected static User convert(RemoteUser remoteUser) {
		User user = new User();
		user.setEmail(remoteUser.getEmail());
		user.setFullName(remoteUser.getFullname());
		user.setName(remoteUser.getName());

		return user;
	}

	protected static Group[] convert(RemoteGroup[] remoteGroups) {
		Group[] groups = new Group[remoteGroups.length];
		for (int i = 0; i < remoteGroups.length; i++) {
			groups[i] = convert(remoteGroups[i]);
		}

		return groups;
	}

	protected static Group convert(RemoteGroup remoteGroup) {
		Group group = new Group();
		group.setName(remoteGroup.getName());
		group.setUsers(convert(remoteGroup.getUsers()));

		return group;
	}

	protected static ServerInfo convert(RemoteServerInfo remoteServerInfo) {
		ServerInfo serverInfo = new ServerInfo();
		serverInfo.setBaseUrl(remoteServerInfo.getBaseUrl());
		serverInfo.setBuildDate(remoteServerInfo.getBuildDate().getTime());
		serverInfo.setBuildNumber(remoteServerInfo.getBuildNumber());
		serverInfo.setEdition(remoteServerInfo.getEdition());
		serverInfo.setVersion(remoteServerInfo.getVersion());

		return serverInfo;
	}

	// TODO reconcile this
	protected static Issue convert(RemoteIssue remoteIssue) {
		Issue issue = new Issue();
		return issue;
	}

	public static NamedFilter[] convert(RemoteFilter[] savedFilters) {
		NamedFilter[] namedFilters = new NamedFilter[savedFilters.length];

		for (int i = 0; i < savedFilters.length; i++) {
			RemoteFilter savedFilter = savedFilters[i];
			NamedFilter filter = new NamedFilter();
			filter.setId(savedFilter.getId());
			filter.setName(savedFilter.getName());
			filter.setAuthor(savedFilter.getAuthor());
			filter.setDescription(savedFilter.getDescription());

			namedFilters[i] = filter;
		}

		return namedFilters;
	}
}