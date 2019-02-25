package org.codechecker.eclipse.plugin.itemselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.codechecker.eclipse.plugin.utils.CheckerItem;
import org.codechecker.eclipse.plugin.utils.SimpleSelectionAdapter;
import org.codechecker.eclipse.plugin.utils.CheckerItem.LAST_ACTION;

public class CheckerView extends Dialog {

    private static final String DOUBLE_LEFT_IMAGE = "double_left.png";
    private static final String DOUBLE_RIGHT_IMAGE = "double_right.png";
    private static final String ARROW_LEFT_IMAGE = "arrow_left.png";
    private static final String ARROW_RIGHT_IMAGE = "arrow_right.png";

    private final List<CheckerItem> disabled;
    private final List<CheckerItem> enabled;
    private final ArrayList<CheckerItem> checkersList;

    Composite container;

    private Table disabledCheckersTable;
    private Table enabledCheckersTables;

    public CheckerView(Shell parentShell, ArrayList<CheckerItem> checkersList) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
        this.checkersList = new ArrayList<CheckerItem>();
        this.enabled = new ArrayList<CheckerItem>();
        this.disabled = new ArrayList<CheckerItem>();
        this.checkersList.addAll(checkersList);
    }

    public List<CheckerItem> getCheckersList() {
        ArrayList<CheckerItem> result = new ArrayList<>();
        for(CheckerItem it : enabled) {
            result.add(new CheckerItem(it.getText(), it.getLastAction()));
        }
        for(CheckerItem it : disabled) {
            result.add(new CheckerItem(it.getText(), it.getLastAction()));
        }
        Collections.sort(result);
        return result;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        container = new Composite(parent, SWT.NULL) {
            @Override
            public void setBounds(final int x, final int y, final int width, final int height) {
                super.setBounds(x, y, width, height);
                final Point itemsTableDefaultSize = disabledCheckersTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                final Point selectionTableDefaultSize = enabledCheckersTables.computeSize(SWT.DEFAULT, SWT.DEFAULT);

                int dTableSize = disabledCheckersTable.getSize().x;
                if (itemsTableDefaultSize.y > disabledCheckersTable.getSize().y) {
                    dTableSize -= disabledCheckersTable.getVerticalBar().getSize().x;
                }

                int eTableSize = enabledCheckersTables.getSize().x;
                if (selectionTableDefaultSize.y > enabledCheckersTables.getSize().y) {
                    eTableSize -= enabledCheckersTables.getVerticalBar().getSize().x;
                }
                disabledCheckersTable.getColumn(0).setWidth(dTableSize);
                enabledCheckersTables.getColumn(0).setWidth(eTableSize);
                disabledCheckersTable.getColumn(0).pack();
                enabledCheckersTables.getColumn(0).pack();
            }
        };
        container.setLayout(new GridLayout(3, true));
        container.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        createDisabledCheckersTable();
        createButtonSelectAll();
        createEnabledCheckersTable();
        createButtonSelect();
        createButtonDeselect();
        createButtonDeselectAll();

        setItems(checkersList);

        return container;
    }

    private void createDisabledCheckersTable() {
        this.disabledCheckersTable = createTable("Disabled checkers");
        this.disabledCheckersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(final MouseEvent event) {
                CheckerView.this.selectItem();
            }
        });
    }

    private Table createTable(String tableName) {
        final Table table = new Table(this.container, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        table.setLinesVisible(false);
        table.setHeaderVisible(true);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(tableName);
        final GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 4);
        gd.widthHint = 400;
        gd.heightHint = 300;
        table.setLayoutData(gd);
        new TableColumn(table, SWT.LEFT);
        table.setData(-1);
        return table;
    }

    private void createButtonSelectAll() {
        final Button buttonSelectAll = createButton(DOUBLE_RIGHT_IMAGE, true, GridData.END);
        buttonSelectAll.addSelectionListener(new SimpleSelectionAdapter() {

            @Override
            public void handle(final SelectionEvent e) {
                CheckerView.this.selectAll();
            }
        });
    }

    private void createEnabledCheckersTable() {
        enabledCheckersTables = createTable("Enabled checkers");
        enabledCheckersTables.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(final MouseEvent event) {
                CheckerView.this.deselectItem();
            }
        });
    }

    private void createButtonSelect() {
        final Button buttonSelect = createButton(ARROW_RIGHT_IMAGE, false, GridData.CENTER);
        buttonSelect.addSelectionListener(new SimpleSelectionAdapter() {
            @Override
            public void handle(final SelectionEvent e) {
                CheckerView.this.selectItem();
            }
        });
    }

    private void createButtonDeselect() {
        final Button buttonDeselect = createButton(ARROW_LEFT_IMAGE, false, GridData.CENTER);
        buttonDeselect.addSelectionListener(new SimpleSelectionAdapter() {
            @Override
            public void handle(final SelectionEvent e) {
                CheckerView.this.deselectItem();
            }
        });
    }

    private void createButtonDeselectAll() {
        final Button buttonDeselectAll = createButton(DOUBLE_LEFT_IMAGE, true, GridData.BEGINNING);
        buttonDeselectAll.addSelectionListener(new SimpleSelectionAdapter() {
            @Override
            public void handle(final SelectionEvent e) {
                CheckerView.this.deselectAll();
            }
        });
    }

    private Button createButton(final String fileName, final boolean verticalExpand, final int alignment) {
        final Button button = new Button(this.container, SWT.PUSH);
        final ClassLoader loader = CheckerView.class.getClassLoader();
        final Image image = new Image(this.container.getDisplay(), loader.getResourceAsStream("icons/" + fileName));
        button.setImage(image);
        final GridData gd = new GridData(GridData.FILL, alignment, false, verticalExpand);
        button.setLayoutData(gd);
        return button;
    }

    public void setItems(final List<CheckerItem> items) {
        if (items == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        disabled.clear();
        enabled.clear();
        for (final CheckerItem item : items) {
            if (item == null) {
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
            if(item.getLastAction() == LAST_ACTION.SELECTION) {
                enabled.add(new CheckerItem(item.getText(),item.getLastAction()));
            } else {
                disabled.add(new CheckerItem(item.getText(), item.getLastAction()));
            }
        }
        redrawTables();
    }

    private void redrawTables() {
        redrawTable(disabledCheckersTable, disabled);
        redrawTable(enabledCheckersTables, enabled);
        container.setRedraw(true);
        container.setBounds(container.getBounds());
    }

    private void redrawTable(final Table table, List<CheckerItem> checkers) {
        clean(table);
        fillData(table, checkers);
    }

    private void clean(final Table table) {
        if (table == null) {
            return;
        }

        for (final TableItem item : table.getItems()) {
            item.dispose();
        }
    }

    private void fillData(final Table table, List<CheckerItem> checkers) {
        Collections.sort(checkers, new Comparator<CheckerItem>() {
            @Override
            public int compare(CheckerItem o1, CheckerItem o2) {
                return o1.getText().compareTo(o2.getText());
            }
        });
        for (final CheckerItem item : checkers) {
            final TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setData(item);
            tableItem.setText(item.getText());
        }
        if (checkers.isEmpty()){
            final TableItem tableItem = new TableItem(table, SWT.NONE);
        }

    }

    protected void deselectAll() {
        disabled.addAll(enabled);
        final List<CheckerItem> deselectedItems = new ArrayList<CheckerItem>();
        for (final CheckerItem item : enabled) {
            item.setLastAction(LAST_ACTION.DESELECTION);
            deselectedItems.add(item);
        }
        enabled.clear();
        redrawTables();
    }

    protected void selectAll() {
        enabled.addAll(disabled);
        final List<CheckerItem> selectedItems = new ArrayList<CheckerItem>();
        for (final CheckerItem item : disabled) {
            item.setLastAction(LAST_ACTION.SELECTION);
            selectedItems.add(item);
        }
        disabled.clear();
        redrawTables();
    }

    protected void selectItem() {
        if (disabledCheckersTable.getSelectionCount() == 0) {
            return;
        }
        final List<CheckerItem> selectedItems = new ArrayList<CheckerItem>();
        for (final TableItem tableItem : disabledCheckersTable.getSelection()) {
            final CheckerItem item = (CheckerItem) tableItem.getData();
            item.setLastAction(LAST_ACTION.SELECTION);
            selectedItems.add(item);
            enabled.add(item);
            disabled.remove(item);
        }
        redrawTables();
    }

    protected void deselectItem() {
        if (enabledCheckersTables.getSelectionCount() == 0) {
            return;
        }
        final List<CheckerItem> deselectedItems = new ArrayList<CheckerItem>();
        for (final TableItem tableItem : enabledCheckersTables.getSelection()) {
            final CheckerItem item = (CheckerItem) tableItem.getData();
            item.setLastAction(LAST_ACTION.DESELECTION);
            deselectedItems.add(item);
            disabled.add(item);
            enabled.remove(item);
        }
        redrawTables();
    }

}