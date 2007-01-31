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
package org.eclipse.mylar.jira.core.internal.service.web.rss;

import java.text.SimpleDateFormat;

import org.eclipse.mylar.jira.core.internal.model.Component;
import org.eclipse.mylar.jira.core.internal.model.IssueType;
import org.eclipse.mylar.jira.core.internal.model.Priority;
import org.eclipse.mylar.jira.core.internal.model.Resolution;
import org.eclipse.mylar.jira.core.internal.model.Status;
import org.eclipse.mylar.jira.core.internal.model.Version;
import org.eclipse.mylar.jira.core.internal.model.filter.ComponentFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.ContentFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.CurrentUserFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.DateFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.DateRangeFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.EstimateVsActualFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.FilterDefinition;
import org.eclipse.mylar.jira.core.internal.model.filter.IssueTypeFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.NobodyFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.Order;
import org.eclipse.mylar.jira.core.internal.model.filter.PriorityFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.ProjectFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.RelativeDateRangeFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.ResolutionFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.SpecificUserFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.StatusFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.UserFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.UserInGroupFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.VersionFilter;

class RSSFilterConverter {

	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy"); //$NON-NLS-1$

	String convert(FilterDefinition filterDefinition) {
		StringBuffer buffer = new StringBuffer();

		if (filterDefinition.getProjectFilter() != null) {
			buffer.append('&').append(convertProjectFilter(filterDefinition.getProjectFilter()));
			if (filterDefinition.getComponentFilter() != null) {
				buffer.append('&').append(convertComponentFilter(filterDefinition.getComponentFilter()));
			}

			if (filterDefinition.getFixForVersionFilter() != null) {
				String versionFilterString = convertVersionFilter("fixfor", filterDefinition.getFixForVersionFilter()); //$NON-NLS-1$
				if (versionFilterString.length() > 0) {
					buffer.append('&').append(versionFilterString);
				}
			}

			if (filterDefinition.getReportedInVersionFilter() != null) {
				String versionFilterString = convertVersionFilter(
						"version", filterDefinition.getReportedInVersionFilter()); //$NON-NLS-1$
				if (versionFilterString.length() > 0) {
					buffer.append('&').append(versionFilterString);
				}
			}

			// TODO add custom filters here

		}

		if (filterDefinition.getContentFilter() != null) {
			buffer.append('&').append(convertContentFilter(filterDefinition.getContentFilter()));
		}

		if (filterDefinition.getIssueTypeFilter() != null) {
			buffer.append('&').append(convertIssueTypeFilter(filterDefinition.getIssueTypeFilter()));
		}

		if (filterDefinition.getAssignedToFilter() != null) {
			buffer.append('&').append(convertAssignedToFilter(filterDefinition.getAssignedToFilter()));
		}

		if (filterDefinition.getPriorityFilter() != null) {
			buffer.append('&').append(convertPriorityFilter(filterDefinition.getPriorityFilter()));
		}

		if (filterDefinition.getStatusFilter() != null) {
			buffer.append('&').append(convertStatusFilter(filterDefinition.getStatusFilter()));
		}

		if (filterDefinition.getResolutionFilter() != null) {
			buffer.append('&').append(convertResolutionFilter(filterDefinition.getResolutionFilter()));
		}

		if (filterDefinition.getReportedByFilter() != null) {
			buffer.append('&').append(convertReportedByFilter(filterDefinition.getReportedByFilter()));
		}

		if (filterDefinition.getEstimateVsActualFilter() != null) {
			buffer.append('&').append(convertEstimateVsActualFilter(filterDefinition.getEstimateVsActualFilter()));
		}

		if (filterDefinition.getCreatedDateFilter() != null) {
			buffer.append(convertCreatedDateFilter(filterDefinition.getCreatedDateFilter()));
		}

		if (filterDefinition.getUpdatedDateFilter() != null) {
			buffer.append(convertUpdatedDateFilter(filterDefinition.getUpdatedDateFilter()));
		}

		if (filterDefinition.getDueDateFilter() != null) {
			buffer.append(convertDueDateFilter(filterDefinition.getDueDateFilter()));
		}

		if (filterDefinition.getOrdering() != null) {
			buffer.append(convertOrdering(filterDefinition.getOrdering()));
		}

		if (buffer.length() > 0) {
			// Trim off the leading & if there is one
			return buffer.substring(1);
		}

		return ""; //$NON-NLS-1$
	}

	protected String convertProjectFilter(ProjectFilter projectFilter) {
		return new StringBuffer().append("pid=").append(projectFilter.getProject().getId()) //$NON-NLS-1$
				.toString();
	}

	protected String convertContentFilter(ContentFilter contentFilter) {
		return new StringBuffer().append("query=").append(contentFilter.getQueryString()) //$NON-NLS-1$
				.append("&summary=").append(contentFilter.isSearchingSummary()) //$NON-NLS-1$
				.append("&description=").append(contentFilter.isSearchingDescription()) //$NON-NLS-1$
				.append("&body=").append(contentFilter.isSearchingComments()) //$NON-NLS-1$
				.append("&environment=").append(contentFilter.isSearchingEnvironment()) //$NON-NLS-1$
				.toString();
	}

	protected String convertIssueTypeFilter(IssueTypeFilter issueTypeFilter) {
		StringBuffer buffer = new StringBuffer();

		if (issueTypeFilter.isStandardTypes()) {
			buffer.append("type=").append("-2"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (issueTypeFilter.isSubTaskTypes()) {
			buffer.append("type=").append("-3"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (issueTypeFilter.getIsueTypes() != null) {
			IssueType[] issueTypes = issueTypeFilter.getIsueTypes();
			if (issueTypes.length == 0) {
				return ""; //$NON-NLS-1$
			}

			buffer.append("type=").append(issueTypes[0].getId()); //$NON-NLS-1$

			for (int i = 1; i < issueTypes.length; i++) {
				buffer.append('&').append("type=").append(issueTypes[i].getId()); //$NON-NLS-1$
			}
		}
		return buffer.toString();
	}

	protected String convertReportedByFilter(UserFilter reportedByFilter) {
		StringBuffer buffer = new StringBuffer();
		if (reportedByFilter instanceof NobodyFilter) {
			return "reporterSelect=unassigned"; //$NON-NLS-1$
		} else if (reportedByFilter instanceof SpecificUserFilter) {
			buffer.append("reporterSelect=specificuser&reporter=") //$NON-NLS-1$
					.append(((SpecificUserFilter) reportedByFilter).getUser());
		} else if (reportedByFilter instanceof UserInGroupFilter) {
			buffer.append("reporterSelect=specificgroup&reporter=") //$NON-NLS-1$
					.append(((UserInGroupFilter) reportedByFilter).getGroup());
		} else if (reportedByFilter instanceof CurrentUserFilter) {
			return "reporterSelect=issue_current_user"; //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertAssignedToFilter(UserFilter assignedToFilter) {
		StringBuffer buffer = new StringBuffer();
		if (assignedToFilter instanceof NobodyFilter) {
			buffer.append("assigneeSelect=unassigned"); //$NON-NLS-1$
		} else if (assignedToFilter instanceof SpecificUserFilter) {
			buffer.append("assigneeSelect=specificuser&assignee=") //$NON-NLS-1$
					.append(((SpecificUserFilter) assignedToFilter).getUser());
		} else if (assignedToFilter instanceof UserInGroupFilter) {
			buffer.append("assigneeSelect=specificgroup&assignee=") //$NON-NLS-1$
					.append(((UserInGroupFilter) assignedToFilter).getGroup());
		} else if (assignedToFilter instanceof CurrentUserFilter) {
			return "assigneeSelect=issue_current_user"; //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertPriorityFilter(PriorityFilter priorityFilter) {
		StringBuffer buffer = new StringBuffer();
		Priority[] priorities = priorityFilter.getPriorities();
		if (priorities.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append("priorityIds=").append(priorities[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < priorities.length; i++) {
			buffer.append("&priorityIds=").append(priorities[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertStatusFilter(StatusFilter statusFilter) {
		StringBuffer buffer = new StringBuffer();
		Status[] statuses = statusFilter.getStatuses();
		if (statuses.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append("statusIds=").append(statuses[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < statuses.length; i++) {
			buffer.append("&statusIds=").append(statuses[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertResolutionFilter(ResolutionFilter resolutionFilter) {
		if (resolutionFilter.isUnresolved()) {
			return "resolutionIds=-1"; //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		Resolution[] resolution = resolutionFilter.getResolutions();
		if (resolution.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append("resolutionIds=").append(resolution[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < resolution.length; i++) {
			buffer.append("&resolutionIds=").append(resolution[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertComponentFilter(ComponentFilter componentFilter) {
		if (componentFilter.hasNoComponent()) {
			return "component=-1"; //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		Component[] components = componentFilter.getComponents();
		if (components.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append("component=").append(components[0].getId()); //$NON-NLS-1$

		for (int i = 1; i < components.length; i++) {
			buffer.append("&component=").append(components[i].getId()); //$NON-NLS-1$
		}

		return buffer.toString();
	}

	protected String convertVersionFilter(String param, VersionFilter versionFilter) {
		// Versions can only be either released or unreleased. If both are
		// selected, search in all versions
		if (versionFilter.isReleasedVersions() && versionFilter.isUnreleasedVersions()) {
			return ""; //$NON-NLS-1$
		}

		if (versionFilter.hasNoVersion()) {
			return param + "=-1"; //$NON-NLS-1$
		}
		if (versionFilter.isUnreleasedVersions()) {
			return param + "=-2"; //$NON-NLS-1$
		}
		if (versionFilter.isReleasedVersions()) {
			return param + "=-3"; //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		Version[] versions = versionFilter.getVersions();
		if (versions.length == 0) {
			return ""; //$NON-NLS-1$
		}

		buffer.append(param).append("=") //$NON-NLS-1$
				.append(versions[0].getId());

		for (int i = 1; i < versions.length; i++) {
			buffer.append("&") //$NON-NLS-1$
					.append(param).append("=") //$NON-NLS-1$
					.append(versions[i].getId());
		}

		return buffer.toString();
	}

	protected String convertEstimateVsActualFilter(EstimateVsActualFilter filter) {
		StringBuffer buffer = new StringBuffer();
		float min = filter.getMinVariation();
		float max = filter.getMaxVariation();

		if (min != 0L) {
			buffer.append("minRatioLimit=").append(min); //$NON-NLS-1$
		}

		if (max != 0L) {
			if (buffer.length() > 0) {
				buffer.append('&');
			}
			buffer.append("maxRatioLimit=").append(max); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	protected String convertDueDateFilter(DateFilter dueDateFilter) {
		StringBuffer buffer = new StringBuffer();

		if (dueDateFilter instanceof DateRangeFilter) {
			DateRangeFilter filter = (DateRangeFilter) dueDateFilter;
			if (filter.getFromDate() != null) {
				buffer.append("&duedateAfter=").append(DATE_FORMAT.format(filter.getFromDate())); //$NON-NLS-1$
			}

			if (filter.getToDate() != null) {
				buffer.append("&duedateBefore=").append(DATE_FORMAT.format(filter.getToDate())); //$NON-NLS-1$
			}

		} else if (dueDateFilter instanceof RelativeDateRangeFilter) {
			RelativeDateRangeFilter relativeFilter = ((RelativeDateRangeFilter) dueDateFilter);
			if (relativeFilter.previousMilliseconds() != 0L) {
				buffer.append("&duedatePrevious=").append(relativeFilter.previousMilliseconds()); //$NON-NLS-1$
			}

			if (relativeFilter.nextMilliseconds() != 0L) {
				buffer.append("&duedateNext=").append(relativeFilter.nextMilliseconds()); //$NON-NLS-1$
			}
		}

		return buffer.toString();
	}

	protected String convertUpdatedDateFilter(DateFilter updatedDateFilter) {
		StringBuffer buffer = new StringBuffer();

		if (updatedDateFilter instanceof DateRangeFilter) {
			DateRangeFilter filter = (DateRangeFilter) updatedDateFilter;
			if (filter.getFromDate() != null) {
				buffer.append("&updatedAfter=").append(DATE_FORMAT.format(filter.getFromDate())); //$NON-NLS-1$
			}

			if (filter.getToDate() != null) {
				buffer.append("&updatedBefore=").append(DATE_FORMAT.format(filter.getToDate())); //$NON-NLS-1$
			}

		} else if (updatedDateFilter instanceof RelativeDateRangeFilter) {
			RelativeDateRangeFilter relativeFilter = ((RelativeDateRangeFilter) updatedDateFilter);
			if (relativeFilter.previousMilliseconds() != 0L) {
				buffer.append("&updatedPrevious=").append(relativeFilter.previousMilliseconds()); //$NON-NLS-1$
			}
		}

		return buffer.toString();
	}

	protected String convertCreatedDateFilter(DateFilter createdDateFilter) {
		StringBuffer buffer = new StringBuffer();

		if (createdDateFilter instanceof DateRangeFilter) {
			DateRangeFilter filter = (DateRangeFilter) createdDateFilter;
			if (filter.getFromDate() != null) {
				buffer.append("&createdAfter=").append(DATE_FORMAT.format(filter.getFromDate())); //$NON-NLS-1$
			}

			if (filter.getToDate() != null) {
				buffer.append("&createdBefore=").append(DATE_FORMAT.format(filter.getToDate())); //$NON-NLS-1$
			}

		} else if (createdDateFilter instanceof RelativeDateRangeFilter) {
			RelativeDateRangeFilter relativeFilter = ((RelativeDateRangeFilter) createdDateFilter);
			if (relativeFilter.previousMilliseconds() != 0L) {
				buffer.append("&createdPrevious=").append(relativeFilter.previousMilliseconds()); //$NON-NLS-1$
			}
		}

		return buffer.toString();
	}

	protected String convertOrdering(Order[] ordering) {
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < ordering.length; i++) {
			Order order = ordering[i];
			String fieldName = getNameFromField(order.getField());
			if (fieldName == null) {
				continue;
			}
			buffer.append("&sorter/field=").append(fieldName) //$NON-NLS-1$
					.append("&sorter/order=").append(order.isAscending() ? "ASC" : "DESC"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return buffer.toString();
	}

	// TODO there should be an easier way of doing this
	// Would it be so bad to have the field name in the field?
	protected String getNameFromField(Order.Field field) {
		if (Order.Field.ISSUE_TYPE == field) {
			return "issuetype"; //$NON-NLS-1$
		} else if (Order.Field.ISSUE_KEY == field) {
			return "issuekey"; //$NON-NLS-1$
		} else if (Order.Field.SUMMARY == field) {
			return "summary"; //$NON-NLS-1$
		} else if (Order.Field.ASSIGNEE == field) {
			return "assignee"; //$NON-NLS-1$
		} else if (Order.Field.REPORTER == field) {
			return "reporter"; //$NON-NLS-1$
		} else if (Order.Field.PRIORITY == field) {
			return "priority"; //$NON-NLS-1$
		} else if (Order.Field.STATUS == field) {
			return "status"; //$NON-NLS-1$
		} else if (Order.Field.RESOLUTION == field) {
			return "resolution"; //$NON-NLS-1$
		} else if (Order.Field.CREATED == field) {
			return "created"; //$NON-NLS-1$
		} else if (Order.Field.UPDATED == field) {
			return "updated"; //$NON-NLS-1$
		} else if (Order.Field.DUE_DATE == field) {
			return "duedate"; //$NON-NLS-1$
		}

		return null;
	}

}