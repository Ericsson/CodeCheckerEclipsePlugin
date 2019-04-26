package org.codechecker.eclipse.plugin.views.config.filter;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;

import org.codechecker.eclipse.plugin.config.filter.Filter;
import org.codechecker.eclipse.plugin.config.filter.FilterConfiguration;
import org.codechecker.eclipse.plugin.config.filter.store.FilterStore;
import org.codechecker.eclipse.plugin.report.Severity;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;

public class FilterConfigurationDialog extends Dialog {

    private final FilterStore filterStore;
    FilterConfiguration currentConfiguration;
    private Combo filterName;
    private Button btnLinkToCurrent;
    private Table filterTable;
    private TableViewer tableViewer;

    /**
     * Create the dialog.
     */
    public FilterConfigurationDialog(Shell parentShell, FilterConfiguration currentConfiguration,
                                     String projectName) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
        this.currentConfiguration = currentConfiguration;
        this.filterStore = new FilterStore(projectName);
    }

    private void reloadFilterNameValues() {
        filterName.setItems(filterStore.getNames());
    }

    /**
     * Create contents of the dialog.
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FormLayout());

        btnLinkToCurrent = new Button(container, SWT.CHECK);
        FormData fd_btnLinkToCurrent = new FormData();
        fd_btnLinkToCurrent.top = new FormAttachment(0, 34);
        fd_btnLinkToCurrent.left = new FormAttachment(0, 10);
        btnLinkToCurrent.setLayoutData(fd_btnLinkToCurrent);
        btnLinkToCurrent.setText("Link to current editor by default");
        btnLinkToCurrent.setSelection(currentConfiguration.isLinkToCurrentEditorByDefalt());

        Label lblName = new Label(container, SWT.NONE);
        FormData fd_lblName = new FormData();
        fd_lblName.bottom = new FormAttachment(btnLinkToCurrent, -9);
        fd_lblName.left = new FormAttachment(0, 10);
        lblName.setLayoutData(fd_lblName);
        lblName.setText("Name:");
        /*
        Button btnSaveGlobal = new Button(container, SWT.NONE);
		btnSaveGlobal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		FormData fd_btnSaveGlobal = new FormData();
		fd_btnSaveGlobal.left = new FormAttachment(lblName, 158);
		fd_btnSaveGlobal.top = new FormAttachment(lblName, -5, SWT.TOP);
		btnSaveGlobal.setLayoutData(fd_btnSaveGlobal);
		btnSaveGlobal.setText("Save global filter");
		*/
        Button btnSaveProject = new Button(container, SWT.NONE);
        FormData fd_btnSaveProject = new FormData();
        fd_btnSaveProject.left = new FormAttachment(lblName, 158);
        fd_btnSaveProject.top = new FormAttachment(lblName, -5, SWT.TOP);
        btnSaveProject.setLayoutData(fd_btnSaveProject);
        btnSaveProject.setText("Save configuration");
        btnSaveProject.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!filterName.getText().isEmpty()) {
                    currentConfiguration.setName(filterName.getText());
                    currentConfiguration.setLinkToCurrentEditorByDefalt(btnLinkToCurrent
                            .getSelection());
                    filterStore.addFilterConfiguration(currentConfiguration);
                }
            }
        });

        Button btnNew = new Button(container, SWT.NONE);
        FormData fd_btnNew = new FormData();
        fd_btnNew.bottom = new FormAttachment(btnSaveProject, 0, SWT.BOTTOM);
        fd_btnNew.left = new FormAttachment(btnSaveProject, 6);
        btnNew.setLayoutData(fd_btnNew);
        btnNew.setText("New");
        btnSaveProject.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                currentConfiguration = new FilterConfiguration();
                filterName.setText("unnamed configuration");
                btnLinkToCurrent.setSelection(true);
            }
        });

        filterName = new Combo(container, SWT.NONE);
        FormData fd_filterName = new FormData();
        fd_filterName.right = new FormAttachment(btnLinkToCurrent, 0, SWT.RIGHT);
        fd_filterName.bottom = new FormAttachment(btnLinkToCurrent, -4);
        fd_filterName.left = new FormAttachment(lblName, 6);
        filterName.setLayoutData(fd_filterName);
        filterName.setText(currentConfiguration.getName());
        reloadFilterNameValues();
        /*
        Button btnDelete = new Button(container, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		FormData fd_btnDelete = new FormData();
		fd_btnDelete.top = new FormAttachment(lblName, -5, SWT.TOP);
		fd_btnDelete.left = new FormAttachment(btnLoad, 6);
		btnDelete.setLayoutData(fd_btnDelete);
		btnDelete.setText("Delete");
		*/
        tableViewer = new TableViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT
                .FULL_SELECTION);
        createColumns(tableViewer);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        filterTable = tableViewer.getTable();
        FormData fd_table = new FormData();
        fd_table.left = new FormAttachment(0, 10);
        fd_table.right = new FormAttachment(0, 865);
        fd_table.bottom = new FormAttachment(btnLinkToCurrent, 211, SWT.BOTTOM);
        fd_table.top = new FormAttachment(btnLinkToCurrent, 5);
        filterTable.setLayoutData(fd_table);

        Button btnAddFilter = new Button(container, SWT.NONE);
        btnAddFilter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                currentConfiguration.getFilters().add(new Filter());
                tableViewer.refresh();
            }
        });
        FormData fd_btnAddFilter = new FormData();
        fd_btnAddFilter.bottom = new FormAttachment(btnLinkToCurrent, 0, SWT.BOTTOM);
        fd_btnAddFilter.right = new FormAttachment(table, 0, SWT.RIGHT);
        btnAddFilter.setLayoutData(fd_btnAddFilter);
        btnAddFilter.setText("Add filter");

        Button btnDeleteSelectedFilters = new Button(container, SWT.NONE);
        btnDeleteSelectedFilters.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();

                for (Object o : selection.toArray()) {
                    currentConfiguration.getFilters().remove(o);
                }

                tableViewer.refresh();
            }
        });
        FormData fd_btnDeleteSelectedFilters = new FormData();
        fd_btnDeleteSelectedFilters.top = new FormAttachment(btnAddFilter, 0, SWT.TOP);
        fd_btnDeleteSelectedFilters.right = new FormAttachment(btnAddFilter, -6);
        btnDeleteSelectedFilters.setLayoutData(fd_btnDeleteSelectedFilters);
        btnDeleteSelectedFilters.setText("Delete selected filters");
        /*
        Button btnNew = new Button(container, SWT.NONE);
		FormData fd_btnNew = new FormData();
		fd_btnNew.bottom = new FormAttachment(btnSaveGlobal, 0, SWT.BOTTOM);
		fd_btnNew.left = new FormAttachment(btnDelete, 6);
		btnNew.setLayoutData(fd_btnNew);
		btnNew.setText("New");
		 */
        return container;
    }

    private void createColumns(TableViewer tableViewer) {
        // TODO Auto-generated method stub
        tableViewer.setContentProvider(new ListContentProvider());

        TableViewerColumn col1Name = new TableViewerColumn(tableViewer, SWT.NONE);
        col1Name.setEditingSupport(new FilepathEditingSupport(tableViewer));
        col1Name.getColumn().setText("Filepath");
        col1Name.getColumn().setWidth(200);
        col1Name.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Filter f = (Filter) element;
                return f.getFilepath();
            }
        });

        TableViewerColumn col2Name = new TableViewerColumn(tableViewer, SWT.NONE);
        col2Name.setEditingSupport(new CheckerIdEditingSupport(tableViewer));
        col2Name.getColumn().setText("Checker ID");
        col2Name.getColumn().setWidth(200);
        col2Name.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Filter f = (Filter) element;
                return f.getCheckerId();
            }
        });

        TableViewerColumn col3Name = new TableViewerColumn(tableViewer, SWT.NONE);
        col3Name.setEditingSupport(new SeverityEditingSupport(tableViewer));
        col3Name.getColumn().setText("Severity");
        col3Name.getColumn().setWidth(100);
        col3Name.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Filter f = (Filter) element;
                return f.getSeverity() == null ? null : f.getSeverity().toString();
            }
        });

        TableViewerColumn col4Name = new TableViewerColumn(tableViewer, SWT.NONE);
        col4Name.setEditingSupport(new BuildTargetEditingSupport(tableViewer));
        col4Name.getColumn().setText("Build target");
        col4Name.getColumn().setWidth(100);
        col4Name.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Filter f = (Filter) element;
                return f.getBuildTarget();
            }
        });

        TableViewerColumn col5Name = new TableViewerColumn(tableViewer, SWT.NONE);
        col5Name.setEditingSupport(new SuppressedEditingSupport(tableViewer));
        col5Name.getColumn().setText("Show suppressed");
        col5Name.getColumn().setWidth(50);
        col5Name.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                Filter f = (Filter) element;
                return f.isShowSuppressedErrors() ? "yes" : "no";
            }
        });

        TableViewerColumn col6Name = new TableViewerColumn(tableViewer, SWT.NONE);
        col6Name.setEditingSupport(new CheckerMsgEditingSupport(tableViewer));
        col6Name.getColumn().setText("Checker Msg");
        col6Name.getColumn().setWidth(200);
        col6Name.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Filter f = (Filter) element;
                return f.getCheckerMsg();
            }
        });


        tableViewer.setInput(currentConfiguration.getFilters());
    }

    /**
     * Create contents of the button bar.
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button button = createButton(parent, IDialogConstants.OK_ID, "Update", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(890, 360);
    }

    @Override
    protected void okPressed() {
        currentConfiguration.setName(filterName.getText());
        currentConfiguration.setLinkToCurrentEditorByDefalt(btnLinkToCurrent.getSelection());
        super.okPressed();
        // table is based on collection
    }

    public FilterConfiguration getCurrentConfiguration() {
        return currentConfiguration;
    }

    private static final class SeverityEditingSupport extends EditingSupport {

        private final TableViewer viewer;
        private final ComboBoxCellEditor editor;

        String[] displayValues;
        Severity[] severityValues;

        private SeverityEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
            displayValues = ArrayUtils.toArray("Any", "Style", "Low", "Medium", "High", "Critical");
            severityValues = ArrayUtils.toArray(Severity.ANY, Severity.STYLE, Severity.LOW,
                    Severity.MEDIUM, Severity.HIGH, Severity.CRITICAL);
            this.editor = new ComboBoxCellEditor(viewer.getTable(), displayValues, SWT.DROP_DOWN
                    | SWT.READ_ONLY);
        }

        protected boolean canEdit(Object element) {
            return true;
        }

        protected CellEditor getCellEditor(Object element) {
            editor.setValue(Arrays.asList(severityValues).indexOf(((Filter) element).getSeverity
                    ()));
            return editor;
        }

        protected Object getValue(Object element) {
            return Arrays.asList(severityValues).indexOf(((Filter) element).getSeverity());
        }

        protected void setValue(Object element, Object value) {
            Integer v = (Integer) value;
            ((Filter) element).setSeverity(severityValues[v]);
            viewer.update(element, null);
        }
    }

    private static final class SuppressedEditingSupport extends EditingSupport {

        private final TableViewer viewer;
        private final CellEditor editor;

        private SuppressedEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
            this.editor = new ComboBoxCellEditor(viewer.getTable(), ArrayUtils.toArray("yes",
                    "no"), SWT.DROP_DOWN | SWT.READ_ONLY);
        }

        protected boolean canEdit(Object element) {
            return true;
        }

        protected CellEditor getCellEditor(Object element) {
            editor.setValue(((Filter) element).isShowSuppressedErrors() ? 0 : 1);
            return editor;
        }

        protected Object getValue(Object element) {
            return ((Filter) element).isShowSuppressedErrors() ? 0 : 1;
        }

        protected void setValue(Object element, Object value) {
            Integer v = (Integer) value;
            ((Filter) element).setShowSuppressedErrors((int) value == 0);
            viewer.update(element, null);
        }
    }

    private static abstract class TextFieldEditingSupport extends EditingSupport {
        protected final TextCellEditor editor;
        protected final TableViewer viewer;

        private TextFieldEditingSupport(TableViewer viewer) {
            super(viewer);
            this.editor = new TextCellEditor(viewer.getTable());
            this.viewer = viewer;
        }

        protected boolean canEdit(Object element) {
            return true;
        }

        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

    }

    private static final class CheckerMsgEditingSupport extends TextFieldEditingSupport {
        private CheckerMsgEditingSupport(TableViewer viewer) {
            super(viewer);
        }

        protected Object getValue(Object element) {
            Filter f = (Filter) element;
            return f.getCheckerMsg();
        }

        protected void setValue(Object element, Object value) {
            Filter f = (Filter) element;
            f.setCheckerMsg((String) value);
            viewer.update(element, null);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            Filter f = (Filter) element;
            editor.setValue(f.getCheckerMsg());
            return super.getCellEditor(element);
        }
    }

    private static final class BuildTargetEditingSupport extends TextFieldEditingSupport {

        private BuildTargetEditingSupport(TableViewer viewer) {
            super(viewer);
        }

        protected Object getValue(Object element) {
            Filter f = (Filter) element;
            return f.getBuildTarget();
        }

        protected void setValue(Object element, Object value) {
            Filter f = (Filter) element;
            f.setBuildTarget((String) value);
            viewer.update(element, null);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            Filter f = (Filter) element;
            editor.setValue(f.getBuildTarget());
            return super.getCellEditor(element);
        }
    }

    private static final class CheckerIdEditingSupport extends TextFieldEditingSupport {

        private CheckerIdEditingSupport(TableViewer viewer) {
            super(viewer);
        }

        protected Object getValue(Object element) {
            Filter f = (Filter) element;
            return f.getCheckerId();
        }

        protected void setValue(Object element, Object value) {
            Filter f = (Filter) element;
            f.setCheckerId((String) value);
            viewer.update(element, null);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            Filter f = (Filter) element;
            editor.setValue(f.getCheckerId());
            return super.getCellEditor(element);
        }
    }

    private static final class FilepathEditingSupport extends TextFieldEditingSupport {

        private FilepathEditingSupport(TableViewer viewer) {
            super(viewer);
        }

        protected Object getValue(Object element) {
            Filter f = (Filter) element;
            return f.getFilepath();
        }

        protected void setValue(Object element, Object value) {
            Filter f = (Filter) element;
            f.setFilepath((String) value);
            viewer.update(element, null);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            Filter f = (Filter) element;
            editor.setValue(f.getFilepath());
            return super.getCellEditor(element);
        }
    }
}
