/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedRepository;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleRepositoriesContentProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleRepositoriesLabelProvider;
import com.atlassian.connector.eclipse.ui.team.CustomChangeSetLogEntry;
import com.atlassian.connector.eclipse.ui.team.CustomRepository;
import com.atlassian.connector.eclipse.ui.team.TeamUiUtils;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.navigator.ResourceComparator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Page for selecting changeset for the new review
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleAddChangesetsPage extends WizardPage {

	private static final String SELECT_MAPPING = "Select Mapping";

	private class ChangesetLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element == null) {
				return null;
			}
			if (element instanceof CustomRepository) {
				return CommonImages.getImage(CrucibleImages.REPOSITORY);
			} else if (element instanceof CustomChangeSetLogEntry) {
				return CommonImages.getImage(CrucibleImages.CHANGESET);
			} else if (element instanceof String) {
				return CommonImages.getImage(CrucibleImages.FILE);
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element == null) {
				return "";
			}
			if (element instanceof CustomRepository) {
				return ((CustomRepository) element).getUrl();
			} else if (element instanceof CustomChangeSetLogEntry) {
				CustomChangeSetLogEntry logEntry = (CustomChangeSetLogEntry) element;
				return logEntry.getRevision() + " [" + logEntry.getAuthor() + "] - " + logEntry.getComment();
			} else if (element instanceof String) {
				return (String) element;
			}
			return "";
		}
	}

	private class ChangesetContentProvider implements ITreeContentProvider {

		private Map<CustomRepository, SortedSet<CustomChangeSetLogEntry>> logEntries;

		public Object[] getChildren(Object parentElement) {
			if (logEntries == null || parentElement == null) {
				return new Object[0];
			}
			if (parentElement instanceof CustomRepository) {
				//root, repository URLs
				return logEntries.get(parentElement).toArray();
			} else if (parentElement instanceof CustomChangeSetLogEntry) {
				//changeset files
				return ((CustomChangeSetLogEntry) parentElement).getChangedFiles();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if (logEntries == null) {
				return null;
			}
			if (element instanceof Map || element instanceof CustomRepository) {
				//root, repository URLs
				return null;
			} else if (element instanceof CustomChangeSetLogEntry) {
				//changeset elements
				return ((CustomChangeSetLogEntry) element).getRepository();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			if (logEntries == null) {
				return false;
			}
			if (element instanceof Map) {
				//root, repository URLs
				return logEntries.size() > 0;
			} else if (element instanceof CustomRepository) {
				//change sets for a repository
				return logEntries.get(element).size() > 0;
			} else if (element instanceof CustomChangeSetLogEntry) {
				//changeset elements
				return ((CustomChangeSetLogEntry) element).getChangedFiles().length > 0;
			}
			return false;
		}

		/**
		 * @return array of map keys (Repository URLs)
		 */
		public Object[] getElements(Object inputElement) {
			if (logEntries == null) {
				return new Object[0];
			}
			//repositories 
			return logEntries.keySet().toArray();
		}

		public void dispose() {
			// ignore

		}

		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput instanceof Map) {
				logEntries = (Map<CustomRepository, SortedSet<CustomChangeSetLogEntry>>) newInput;
			}
		}

	}

	private final Map<CustomRepository, SortedSet<CustomChangeSetLogEntry>> availableLogEntries;

	private final Map<CustomRepository, SortedSet<CustomChangeSetLogEntry>> selectedLogEntries;

	private TreeViewer availableTreeViewer;

	private TreeViewer selectedTreeViewer;

	private TableViewer repositoriesMappingViewer;

	private final TaskRepository taskRepository;

	private Set<CrucibleCachedRepository> cachedRepositories;

	private final Map<CustomRepository, CrucibleCachedRepository> repositoryMappings;

	private final Map<CustomRepository, ComboViewer> mappingCombos;

	public CrucibleAddChangesetsPage(TaskRepository repository) {
		super("crucibleChangesets"); //$NON-NLS-1$
		setTitle("Add Changesets to Review");
		setDescription("Please select the changesets that should be included in the review.");
		this.availableLogEntries = new HashMap<CustomRepository, SortedSet<CustomChangeSetLogEntry>>();
		this.selectedLogEntries = new HashMap<CustomRepository, SortedSet<CustomChangeSetLogEntry>>();
		this.repositoryMappings = new HashMap<CustomRepository, CrucibleCachedRepository>();
		this.mappingCombos = new HashMap<CustomRepository, ComboViewer>();
		this.taskRepository = repository;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select files from your workspace:");

		new Label(composite, SWT.NONE).setText("");

		new Label(composite, SWT.NONE).setText("Files selected for the review:");

		createLeftViewer(composite);

		GridDataFactory.fillDefaults().applyTo(label);

		createButtonComp(composite);

		createRightViewer(composite);

		createRepositoryMappingComp(composite);

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	private void createRepositoryMappingComp(Composite composite) {
		final Composite mappingComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(mappingComposite);
		mappingComposite.setLayout(GridLayoutFactory.fillDefaults().create());

		final Table table = new Table(mappingComposite, SWT.BORDER);
		table.setHeaderVisible(true);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		repositoriesMappingViewer = new TableViewer(table);
		repositoriesMappingViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return selectedLogEntries.keySet().toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		final TableViewerColumn column1 = new TableViewerColumn(repositoriesMappingViewer, SWT.NONE);
		column1.getColumn().setText("Local Repository");
		column1.getColumn().setWidth(100);
		column1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CustomRepository) {
					return ((CustomRepository) element).getUrl();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof CustomRepository) {
					return CommonImages.getImage(CrucibleImages.REPOSITORY);
				}
				return null;
			}
		});
		repositoriesMappingViewer.setInput(selectedLogEntries);

		final TableViewerColumn column2 = new TableViewerColumn(repositoriesMappingViewer, SWT.NONE);
		column2.getColumn().setText("Crucible Repository");
		column2.getColumn().setWidth(100);
		column2.setLabelProvider(new ColumnLabelProvider());

		mappingComposite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle area = mappingComposite.getClientArea();
				Point size = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				ScrollBar vBar = table.getVerticalBar();
				int width = area.width - table.computeTrim(0, 0, 0, 0).width - vBar.getSize().x;
				if (size.y > area.height + table.getHeaderHeight()) {
					// Subtract the scrollbar width from the total column width
					// if a vertical scrollbar will be required
					Point vBarSize = vBar.getSize();
					width -= vBarSize.x;
				}
				Point oldSize = table.getSize();
				if (oldSize.x > area.width) {
					// table is getting smaller so make the columns 
					// smaller first and then resize the table to
					// match the client area width
					column2.getColumn().setWidth(150);
					column1.getColumn().setWidth(width - column2.getColumn().getWidth());
					table.setSize(area.width, area.height);
				} else {
					// table is getting bigger so make the table 
					// bigger first and then make the columns wider
					// to match the client area width
					table.setSize(area.width, area.height);
					column2.getColumn().setWidth(150);
					column1.getColumn().setWidth(width - column2.getColumn().getWidth());
				}
			}
		});
	}

	@Override
	public boolean isPageComplete() {
		// ignore
		return super.isPageComplete();
	}

	private void updateCombos() {
		// ignore
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (cachedRepositories == null) {
					cachedRepositories = CrucibleUiUtil.getCachedRepositories(taskRepository);
				}
				TableItem[] items = repositoriesMappingViewer.getTable().getItems();
				for (int i = 0; i < items.length; i++) {
					final CustomRepository customRepository = (CustomRepository) repositoriesMappingViewer.getElementAt(i);
					TableEditor editor = new TableEditor(repositoriesMappingViewer.getTable());
					final CCombo combo = new CCombo(repositoriesMappingViewer.getTable(), SWT.NONE);
					combo.setText(SELECT_MAPPING);
					combo.setEditable(false);
					final ComboViewer comboViewer = new ComboViewer(combo);
					mappingCombos.put(customRepository, comboViewer);
					comboViewer.setContentProvider(new CrucibleRepositoriesContentProvider());
					comboViewer.setLabelProvider(new CrucibleRepositoriesLabelProvider());
					comboViewer.setInput(cachedRepositories);
					comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
						public void selectionChanged(SelectionChangedEvent event) {
							if (comboViewer.getSelection() instanceof IStructuredSelection) {
								Object selected = ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
								if (cachedRepositories.contains(selected)) {
									repositoryMappings.put(customRepository, (CrucibleCachedRepository) selected);
								}
							}
							validatePage();
						}
					});
					editor.grabHorizontal = true;
					editor.setEditor(combo, items[i], 1);
				}
			}
		});
	}

	/*
	 * checks if page is complete updates the buttons
	 */
	private void validatePage() {
		setErrorMessage(null);

		//check if all custom repositories are mapped to crucible repositories
		boolean allFine = true;
		for (CustomRepository customRepository : repositoryMappings.keySet()) {
			if (repositoryMappings.get(customRepository) == null) {
				setErrorMessage("One or more local repositories are not mapped to Crucible repositories.");
				allFine = false;
			}
			break;
		}

		setPageComplete(allFine);

		getContainer().updateButtons();
	}

	private void createLeftViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.MULTI | SWT.BORDER);
		availableTreeViewer = new TreeViewer(tree);

		GridDataFactory.fillDefaults().grab(true, true).span(1, 2).hint(300, SWT.DEFAULT).applyTo(tree);
		availableTreeViewer.setLabelProvider(new ChangesetLabelProvider());
		availableTreeViewer.setContentProvider(new ChangesetContentProvider());
		availableTreeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
		availableTreeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		final Menu contextMenuSource = new Menu(getShell(), SWT.POP_UP);
		tree.setMenu(contextMenuSource);
		final MenuItem addFile = new MenuItem(contextMenuSource, SWT.PUSH);
		addFile.setText("Add");
		addFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addChangesets();
			}
		});
		final MenuItem getNextRevisions = new MenuItem(contextMenuSource, SWT.PUSH);
		getNextRevisions.setText("Get 10 more Revisions");
		getNextRevisions.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeSelection selection = getTreeSelection(availableTreeViewer);
				if (selection != null) {
					Iterator<Object> iterator = (selection).iterator();
					while (iterator.hasNext()) {
						Object element = iterator.next();
						if (element instanceof CustomChangeSetLogEntry) {
							CustomRepository customRepository = ((CustomChangeSetLogEntry) element).getRepository();
							SortedSet<CustomChangeSetLogEntry> logEntries = availableLogEntries.get(customRepository);
							int currentNumberOfEntries = logEntries == null ? 0 : logEntries.size();
							updateChangesets(customRepository.getUrl(), currentNumberOfEntries + 10);
						}
					}
				} else {
					//update all
					for (CustomRepository customRepository : availableLogEntries.keySet()) {
						SortedSet<CustomChangeSetLogEntry> logEntries = availableLogEntries.get(customRepository);
						int currentNumberOfEntries = logEntries == null ? 0 : logEntries.size();
						updateChangesets(customRepository.getUrl(), currentNumberOfEntries + 10);
					}
				}
			}
		});
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean hasSelection = validateTreeSelection(availableTreeViewer);
				addFile.setEnabled(hasSelection);
				getNextRevisions.setEnabled(hasSelection);
			}
		});
	}

	private void createButtonComp(Composite composite) {
		Composite buttonComp = new Composite(composite, SWT.NONE);
		buttonComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
		GridDataFactory.fillDefaults().grab(false, true).span(1, 2).applyTo(buttonComp);

		Button addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setText("Add -->");
		addButton.setToolTipText("Add all selected changesets");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addChangesets();
			}
		});
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(addButton);

		Button removeButton = new Button(buttonComp, SWT.PUSH);
		removeButton.setText("<-- Remove");
		removeButton.setToolTipText("Remove all selected changesets");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeChangesets();
			}

		});
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(removeButton);
	}

	private void createRightViewer(Composite composite) {
		Tree tree = new Tree(composite, SWT.MULTI | SWT.BORDER);
		selectedTreeViewer = new TreeViewer(tree);

		GridDataFactory.fillDefaults().grab(true, true).hint(300, SWT.DEFAULT).applyTo(tree);
		selectedTreeViewer.setLabelProvider(new ChangesetLabelProvider());
		selectedTreeViewer.setContentProvider(new ChangesetContentProvider());
		selectedTreeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
		selectedTreeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		final Menu contextMenuSource = new Menu(getShell(), SWT.POP_UP);
		tree.setMenu(contextMenuSource);
		MenuItem addFile = new MenuItem(contextMenuSource, SWT.PUSH);
		addFile.setText("Remove");
		addFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeChangesets();
			}
		});

		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateTreeSelection(selectedTreeViewer);
			}
		});
	}

	private void updateChangesets(final String repositoryUrl, final int numberToRetrieve) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					Map<CustomRepository, SortedSet<CustomChangeSetLogEntry>> retrieved = TeamUiUtils.getAllChangesets(
							repositoryUrl, numberToRetrieve, monitor);
					for (CustomRepository customRepository : retrieved.keySet()) {
						if (retrieved.get(customRepository) == null) {
							continue;
						}
						if (availableLogEntries.containsKey(customRepository)
								&& availableLogEntries.get(customRepository) != null) {
							availableLogEntries.get(customRepository).addAll(retrieved.get(customRepository));
						} else {
							availableLogEntries.put(customRepository, retrieved.get(customRepository));
						}
					}
				} catch (CoreException e) {
					setErrorMessage(e.getStatus().getMessage());
					StatusHandler.log(e.getStatus());
				}
			}
		};
		try {
			setErrorMessage(null);
			getContainer().run(true, true, runnable);
		} catch (Exception e) {
			setErrorMessage("Could not retrieve revisions for selected file. See error log for details");
			StatusHandler.log(new Status(IStatus.WARNING, CrucibleUiPlugin.PLUGIN_ID, "Failed to retrieve revisions", e));
		}
		if (availableLogEntries != null) {
			availableTreeViewer.setInput(availableLogEntries);
		}
		validatePage();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					updateChangesets(null, 10);
				}
			});
		}
	}

	private void addChangesets() {
		TreeSelection selection = getTreeSelection(availableTreeViewer);
		addOrRemoveChangesets(selection, true);
	}

	private void removeChangesets() {
		TreeSelection selection = getTreeSelection(selectedTreeViewer);
		addOrRemoveChangesets(selection, false);
	}

	@SuppressWarnings("unchecked")
	private void addOrRemoveChangesets(TreeSelection selection, boolean add) {
		if (selection != null) {
			Iterator<Object> iterator = (selection).iterator();
			Set<CustomRepository> expandedRepositories = new HashSet<CustomRepository>();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof CustomChangeSetLogEntry) {
					CustomRepository customRepository = ((CustomChangeSetLogEntry) element).getRepository();
					SortedSet<CustomChangeSetLogEntry> changesets = selectedLogEntries.get(customRepository);
					if (changesets == null) {
						changesets = new TreeSet<CustomChangeSetLogEntry>();
					}
					if (add) {
						changesets.add((CustomChangeSetLogEntry) element);
					} else {
						changesets.remove(element);
					}
					if (changesets.size() > 0) {
						selectedLogEntries.put(customRepository, changesets);
						if (!repositoryMappings.containsKey(customRepository)) {
							repositoryMappings.put(customRepository, null);
						}
					} else {
						selectedLogEntries.remove(customRepository);
						//if its the last of that repo, remove it from mapping
						if (!selectedLogEntries.containsKey(customRepository)) {
							repositoryMappings.remove(customRepository);
							ComboViewer viewer = mappingCombos.remove(customRepository);
							if (viewer != null) {
								viewer.getCCombo().dispose();
							}
						}
					}
					expandedRepositories.add(customRepository);
				}
			}
			selectedTreeViewer.setInput(selectedLogEntries);
			repositoriesMappingViewer.setInput(selectedLogEntries);
			updateCombos();
			selectedTreeViewer.setExpandedElements(expandedRepositories.toArray());
		}
		validatePage();
	}

	@SuppressWarnings("unchecked")
	private boolean validateTreeSelection(TreeViewer treeViewer) {
		TreeSelection selection = getTreeSelection(treeViewer);
		if (selection != null) {
			ArrayList<TreePath> validSelections = new ArrayList<TreePath>();
			Iterator<Object> iterator = (selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof CustomChangeSetLogEntry) {
					validSelections.add((selection).getPathsFor(element)[0]);
				}
			}
			//set new selection
			ISelection newSelection = new TreeSelection(validSelections.toArray(new TreePath[validSelections.size()]),
					(selection).getElementComparer());
			treeViewer.setSelection(newSelection);
			return !newSelection.isEmpty();
		}
		treeViewer.setSelection(new TreeSelection());
		return (!treeViewer.getSelection().isEmpty());
	}

	private TreeSelection getTreeSelection(TreeViewer treeViewer) {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof TreeSelection) {
			return (TreeSelection) selection;
		}
		return null;
	}

	public Map<CustomRepository, SortedSet<CustomChangeSetLogEntry>> getSelectedLogEntries() {
		return selectedLogEntries;
	}

	public Map<CustomRepository, CrucibleCachedRepository> getRepositoryMappings() {
		return repositoryMappings;
	}
}
