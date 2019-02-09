package com.hiveworkshop.wc3.jworldedit.objects;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.hiveworkshop.wc3.jworldedit.objects.better.EditorFieldBuilder;
import com.hiveworkshop.wc3.jworldedit.objects.better.ObjectDataTableModel;
import com.hiveworkshop.wc3.jworldedit.objects.better.fields.FieldPopupUtils;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.TreeNodeLinkerFromModel;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectDataChangeListener;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

import net.miginfocom.swing.MigLayout;

public class UnitEditorPanel extends JSplitPane implements TreeSelectionListener {
	private static final Object SHIFT_KEY_LOCK = new Object();
	private final MutableObjectData unitData;
	private final DataTable unitMetaData;
	MutableGameObject currentUnit = null;
	UnitEditorSettings settings = new UnitEditorSettings();
	UnitEditorTree tree;
	TopLevelCategoryFolder root;

	JTable table;
	private final EditorFieldBuilder editorFieldBuilder;
	private boolean holdingShift = false;
	private ObjectDataTableModel dataModel;
	private TreePath currentUnitTreePath;
	private final EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData;
	private final Runnable customUnitPopupRunner;
	private JPanel searchPanel;
	private JTextField findTextField;
	private JCheckBox caseSens;
	private JScrollPane treeScrollPane;
	private final Set<String> lastSelectedFields = new HashSet<>();

	public UnitEditorPanel(final MutableObjectData unitData, final DataTable unitMetaData,
			final EditorFieldBuilder editorFieldBuilder, final ObjectTabTreeBrowserBuilder objectTabTreeBrowserBuilder,
			final WorldEditorDataType dataType, final EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData,
			final Runnable customUnitPopupRunner) {
		this.editorTabCustomToolbarButtonData = editorTabCustomToolbarButtonData;
		this.customUnitPopupRunner = customUnitPopupRunner;
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.unitData = unitData;
		this.unitMetaData = unitMetaData;
		this.editorFieldBuilder = editorFieldBuilder;
		tree = new UnitEditorTree(unitData, objectTabTreeBrowserBuilder, settings, dataType);
		root = tree.getRoot();
		treeScrollPane = new JScrollPane(tree);
		this.setLeftComponent(treeScrollPane);
		// temp.setBackground(Color.blue);
		table = new JTable();
		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
		table.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(final MouseEvent e) {

			}

			@Override
			public void mousePressed(final MouseEvent e) {

			}

			@Override
			public void mouseExited(final MouseEvent e) {

			}

			@Override
			public void mouseEntered(final MouseEvent e) {

			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					final int rowIndex = table.getSelectedRow();
					if (dataModel != null) {
						dataModel.doPopupAt(UnitEditorPanel.this, rowIndex, holdingShift);
					}
				}
			}
		});
		final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "EnterKeyPopupAction");
		table.getActionMap().put("EnterKeyPopupAction", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				final int rowIndex = table.getSelectedRow();
				if (dataModel != null) {
					dataModel.doPopupAt(UnitEditorPanel.this, rowIndex, false);
				}
			}
		});
		final KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(shiftEnter, "ShiftEnterKeyPopupAction");
		table.getActionMap().put("ShiftEnterKeyPopupAction", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				final int rowIndex = table.getSelectedRow();
				if (dataModel != null) {
					dataModel.doPopupAt(UnitEditorPanel.this, rowIndex, true);
				}
			}
		});
		final DefaultTableCellRenderer editHighlightingRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value,
					final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				final boolean rowHasFocus = isSelected && table.hasFocus();
				setBackground(null);
				final Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
				if (isSelected) {
					if (rowHasFocus) {
						setForeground(settings.getSelectedValueColor());
					} else {
						setForeground(null);
						setBackground(settings.getSelectedUnfocusedValueColor());
					}
				} else if (dataModel != null && dataModel.hasEditedValue(row)) {
					setForeground(settings.getEditedValueColor());
				} else {
					setForeground(null);
				}
				return this;
			}
		};
		table.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(final FocusEvent e) {
				table.repaint();
			}

			@Override
			public void focusGained(final FocusEvent e) {
				table.repaint();
			}
		});
		table.getTableHeader().setReorderingAllowed(false);
		table.setDefaultRenderer(Object.class, editHighlightingRenderer);
		table.setDefaultRenderer(String.class, editHighlightingRenderer);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

			@Override
			public boolean dispatchKeyEvent(final KeyEvent ke) {
				synchronized (SHIFT_KEY_LOCK) {
					switch (ke.getID()) {
					case KeyEvent.KEY_PRESSED:
						if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
							holdingShift = true;
						}
						break;

					case KeyEvent.KEY_RELEASED:
						if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
							holdingShift = false;
						}
						break;
					}
					return false;
				}
			}
		});
		table.setShowGrid(false);

		setRightComponent(new JScrollPane(table));

		tree.addTreeSelectionListener(this);
		tree.selectFirstUnit();

		unitData.addChangeListener(new MutableObjectDataChangeListener() {
			@Override
			public void textChanged(final War3ID changedObject) {
				final UnitEditorTreeModel treeModel = tree.getModel();
				final TreeNode changedNode = treeModel.getNodeById(changedObject);
				if (changedNode != null) {
					final MutableTreeNode lastPathComponent = (MutableTreeNode) changedNode;
					treeModel.nodeChanged(lastPathComponent);
				}
			}

			@Override
			public void modelChanged(final War3ID changedObject) {

			}

			@Override
			public void iconsChanged(final War3ID changedObject) {
				final UnitEditorTreeModel treeModel = tree.getModel();
				final TreeNode changedNode = treeModel.getNodeById(changedObject);
				if (changedNode != null) {
					final MutableTreeNode lastPathComponent = (MutableTreeNode) changedNode;
					treeModel.nodeChanged(lastPathComponent);
				}
			}

			@Override
			public void fieldsChanged(final War3ID changedObject) {

			}

			@Override
			public void categoriesChanged(final War3ID changedObject) {
				final UnitEditorTreeModel treeModel = tree.getModel();
				final TreeNode changedNode = treeModel.getNodeById(changedObject);
				if (changedNode != null) {
					final MutableTreeNode lastPathComponent = (MutableTreeNode) changedNode;
					treeModel.removeNodeFromParent(lastPathComponent);
					final DefaultMutableTreeNode newObjectNode = root.insertObjectInto(unitData.get(changedObject),
							new TreeNodeLinkerFromModel(treeModel));
					selectTreeNode(newObjectNode);
				}
			}

			@Override
			public void objectCreated(final War3ID newObject) {
				final MutableGameObject mutableGameObject = unitData.get(newObject);
				final DefaultMutableTreeNode newTreeNode = root.insertObjectInto(mutableGameObject,
						new TreeNodeLinkerFromModel(tree.getModel()));
				TreeNode node = newTreeNode.getParent();
				while (node != null) {
					tree.getModel().nodeChanged(node);
					node = node.getParent();
				}
				selectTreeNode(newTreeNode);
			}

			@Override
			public void objectsCreated(final War3ID[] newObjects) {
				tree.setSelectionPath(null);
				for (final War3ID newObjectId : newObjects) {
					final MutableGameObject mutableGameObject = unitData.get(newObjectId);
					final DefaultMutableTreeNode newTreeNode = root.insertObjectInto(mutableGameObject,
							new TreeNodeLinkerFromModel(tree.getModel()));
					TreeNode node = newTreeNode.getParent();
					while (node != null) {
						tree.getModel().nodeChanged(node);
						node = node.getParent();
					}
					addSelectedTreeNode(newTreeNode);
				}
			}

			@Override
			public void objectRemoved(final War3ID removedId) {
				final UnitEditorTreeModel treeModel = tree.getModel();
				final MutableTreeNode changedNode = treeModel.getNodeById(removedId);
				if (changedNode != null) {
					treeModel.removeNodeFromParent(changedNode);
				}
			}

			@Override
			public void objectsRemoved(final War3ID[] removedIds) {
				final UnitEditorTreeModel treeModel = tree.getModel();
				for (final War3ID removedId : removedIds) {
					final MutableTreeNode changedNode = treeModel.getNodeById(removedId);
					if (changedNode != null) {
						treeModel.removeNodeFromParent(changedNode);
					}
				}

			}
		});
		// KeyEventDispatcher myKeyEventDispatcher = new DefaultFocusManager();
		// KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(myKeyEventDispatcher);
		setupCopyPaste(new ObjectTabTreeBrowserTransferHandler(dataType));

		searchPanel = new JPanel(new MigLayout());
		searchPanel.add(new JLabel(WEString.getString("WESTRING_FINDDLG_FIND")), "cell 0 0");
		findTextField = new JTextField(40);
		searchPanel.add(findTextField, "cell 1 0");
		caseSens = new JCheckBox(WEString.getString("WESTRING_FINDDLG_CASESENS"));
		searchPanel.add(caseSens, "cell 1 1");
	}

	public void reloadAllDataVerySlowly() {
		tree.reloadAllObjectDataVerySlowly();
		root = tree.getRoot();
	}

	public MutableObjectData getUnitData() {
		return unitData;
	}

	private void setupCopyPaste(final ObjectTabTreeBrowserTransferHandler treeTransferHandler) {
		tree.setTransferHandler(treeTransferHandler);
		final ActionMap map = tree.getActionMap();
		map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
		map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
	}

	public void runCustomUnitPopup() {
		customUnitPopupRunner.run();
	}

	private void selectTreeNode(final TreeNode lastPathComponent) {
		final TreePath pathForNode = getPathForNode(lastPathComponent);
		tree.setSelectionPath(pathForNode);
		tree.scrollPathToVisible(pathForNode);
	}

	private void addSelectedTreeNode(final TreeNode lastPathComponent) {
		final TreePath pathForNode = getPathForNode(lastPathComponent);
		tree.addSelectionPath(pathForNode);
		tree.scrollPathToVisible(pathForNode);
	}

	private TreePath getPathForNode(final TreeNode lastPathComponent) {
		final LinkedList<Object> nodes = new LinkedList<>();
		TreeNode currentNode = lastPathComponent;
		while (currentNode != null) {
			nodes.addFirst(currentNode);
			currentNode = currentNode.getParent();
		}
		final TreePath pathForNode = new TreePath(nodes.toArray());
		return pathForNode;
	}

	public void selectUnit(final War3ID unitId) {
		final Enumeration<TreeNode> depthFirstEnumeration = root.depthFirstEnumeration();
		while (depthFirstEnumeration.hasMoreElements()) {
			final TreeNode nextElement = depthFirstEnumeration.nextElement();
			if (nextElement instanceof DefaultMutableTreeNode) {
				if (((DefaultMutableTreeNode) nextElement).getUserObject() instanceof MutableGameObject) {
					final MutableGameObject object = (MutableGameObject) ((DefaultMutableTreeNode) nextElement)
							.getUserObject();
					if (object.getAlias().equals(unitId)) {
						selectTreeNode(nextElement);
						return;
					}
				}
			}
		}
	}

	public EditorTabCustomToolbarButtonData getEditorTabCustomToolbarButtonData() {
		return editorTabCustomToolbarButtonData;
	}

	public void fillTable() {
		dataModel = new ObjectDataTableModel(currentUnit, unitMetaData, editorFieldBuilder,
				settings.isDisplayAsRawData(), new Runnable() {
					@Override
					public void run() {
						final DefaultTreeModel treeModel = tree.getModel();
						if (currentUnitTreePath != null) {
							for (final Object untypedTreePathNode : currentUnitTreePath.getPath()) {
								treeModel.nodeChanged((TreeNode) untypedTreePathNode);
							}
						}

					}
				});
		table.setModel(dataModel);
		table.setAutoCreateColumnsFromModel(false);
	}

	public void toggleDisplayAsRawData() {
		settings.setDisplayAsRawData(!settings.isDisplayAsRawData());
		if (dataModel != null) {
			dataModel.setDisplayAsRawData(settings.isDisplayAsRawData());
		}
		refreshAllTreeNodes();
		// fillTable();
		repaint();
	}

	private void refreshAllTreeNodes() {
		final Enumeration<DefaultMutableTreeNode> enumer = UnitEditorPanel.this.root.breadthFirstEnumeration();
		while (enumer.hasMoreElements()) {
			tree.getModel().nodeChanged(enumer.nextElement());
		}
	}

	@Override
	public void valueChanged(final TreeSelectionEvent e) {
		currentUnitTreePath = e.getNewLeadSelectionPath();
		if (currentUnitTreePath != null) {
			if (currentUnit != null) {
				lastSelectedFields.clear();
				if (dataModel != null) {
					for (final int rowIndex : table.getSelectedRows()) {
						lastSelectedFields.add(dataModel.getFieldRawDataName(rowIndex));
					}
				}
			}
			final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
			if (o.getUserObject() instanceof MutableGameObject) {
				final MutableGameObject obj = (MutableGameObject) o.getUserObject();
				currentUnit = obj;
			} else {
				currentUnit = null;
			}
			fillTable();
			for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
				if (lastSelectedFields.contains(dataModel.getFieldRawDataName(rowIndex))) {
					table.addRowSelectionInterval(rowIndex, rowIndex);
				}
			}
		}
	}

	public static class DropLocation extends TransferHandler.DropLocation {
		protected DropLocation(final Point dropPoint) {
			super(dropPoint);
		}

	}

	public void doSearchForUnit() {
		final boolean tableHadFocus = table.hasFocus();
		final int result = FieldPopupUtils.showPopup(this, searchPanel, WEString.getString("WESTRING_FINDDLG_TITLE"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, findTextField);
		if (result == JOptionPane.OK_OPTION) {
			if (tableHadFocus) {
				findInTable(findTextField.getText(), settings.isDisplayAsRawData(), caseSens.isSelected());
			} else {
				tree.find(findTextField.getText(), settings.isDisplayAsRawData(), caseSens.isSelected());
			}
		}
	}

	public void doSearchFindNextUnit() {
		if (table.hasFocus()) {
			findInTable(findTextField.getText(), settings.isDisplayAsRawData(), caseSens.isSelected());
		} else {
			tree.find(findTextField.getText(), settings.isDisplayAsRawData(), caseSens.isSelected());
		}
	}

	private void findInTable(String text, final boolean displayAsRawData, final boolean caseSensitive) {
		if (!caseSensitive) {
			text = text.toLowerCase();
		}
		final int startIndex = table.getSelectedRow() + 1;
		for (int i = startIndex; i < dataModel.getRowCount(); i++) {
			for (int j = 0; j < dataModel.getColumnCount(); j++) {
				final Object tableData = dataModel.getValueAt(i, j);
				String tableString = tableData.toString();
				if (!caseSensitive) {
					tableString = tableString.toLowerCase();
					if (tableString.contains(text)) {
						final int rowToSelect = table.convertRowIndexToView(i);
						table.setRowSelectionInterval(rowToSelect, rowToSelect);
						table.scrollRectToVisible(table.getCellRect(rowToSelect, j, true));
						return;
					}
				}
			}
		}
		if (startIndex > 0) {
			for (int i = 0; i < startIndex; i++) {
				for (int j = 0; j < dataModel.getColumnCount(); j++) {
					final Object tableData = dataModel.getValueAt(i, j);
					String tableString = tableData.toString();
					if (!caseSensitive) {
						tableString = tableString.toLowerCase();
						if (tableString.contains(text)) {
							final int rowToSelect = table.convertRowIndexToView(i);
							table.setRowSelectionInterval(rowToSelect, rowToSelect);
							table.scrollRectToVisible(table.getCellRect(rowToSelect, j, true));
							return;
						}
					}
				}
			}
		}
	}

	public void loadHotkeys() {
		tree.loadHotkeys();
	}
}
