package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.ObjectDataTableModel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.FieldPopupUtils;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.builders.AbstractFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.War3ID;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class UnitEditorPanel extends JSplitPane implements TreeSelectionListener {
	private static final Object SHIFT_KEY_LOCK = new Object();
	private final MutableObjectData unitData;
	private final DataTable unitMetaData;
	private MutableGameObject currentUnit = null;
	private final UnitEditorSettings settings = new UnitEditorSettings();
	private final UnitEditorTree tree;
	private TopLevelCategoryFolder root;

	private final JTable table;
	private final AbstractFieldBuilder editorFieldBuilder;
	private boolean holdingShift = false;
	private ObjectDataTableModel dataModel;
	private TreePath currentUnitTreePath;
	private final EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData;
	private final Runnable customUnitPopupRunner;
	private final JPanel searchPanel;
	private JTextField findTextField;
	private JCheckBox caseSens;
	private final Set<String> lastSelectedFields = new HashSet<>();

	public UnitEditorPanel(
			final MutableObjectData unitData,
			final AbstractFieldBuilder editorFieldBuilder,
			final ObjectTabTreeBrowserBuilder objectTabTreeBrowserBuilder,
			final EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData,
			final Runnable customUnitPopupRunner) {

		this.editorTabCustomToolbarButtonData = editorTabCustomToolbarButtonData;
		this.customUnitPopupRunner = customUnitPopupRunner;

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.unitData = unitData;
		this.unitMetaData = unitData.getSourceSLKMetaData();
		this.editorFieldBuilder = editorFieldBuilder;

		System.out.println(unitData + ", " + objectTabTreeBrowserBuilder + ", " + settings + ", " + unitData.getWorldEditorDataType());
		tree = new UnitEditorTree(unitData, objectTabTreeBrowserBuilder, settings);
		root = tree.getRoot();
		JScrollPane treeScrollPane = new JScrollPane(tree);
		setLeftComponent(treeScrollPane);
		// temp.setBackground(Color.blue);

		table = getTable();

		final String filepath = "replaceabletextures\\commandbuttons\\btnstormbolt.blp";
		final BehaviorTreeNode behaviorRoot = new BehaviorTreeNode("Storm Bolt", niceIcon(filepath));

		behaviorRoot.add(getLocalVarNode());
		behaviorRoot.add(getActionsOnLearn());

		behaviorRoot.add(getActionsOnCast());
		behaviorRoot.add(getActionsOnBuffApplied());
		behaviorRoot.add(getActionsOnBuffRemoved());

		final JTree behaviorTree = new JTree(behaviorRoot);
		behaviorTree.setCellRenderer(getTreeCellRenderer());

		for (int i = 0; i < behaviorTree.getRowCount(); i++) {
			behaviorTree.expandRow(i);
		}

		final JTabbedPane splitWithBehaviorEditor = new JTabbedPane();
//		splitWithBehaviorEditor.addTab("Stats", new JScrollPane(table));
//		splitWithBehaviorEditor.addTab("Stats", new JScrollPane(table));
//		splitWithBehaviorEditor.addTab("Behavior", new JScrollPane(behaviorTree));
//		setRightComponent((splitWithBehaviorEditor));

		setRightComponent(new JScrollPane(table));

		tree.addTreeSelectionListener(this);
		System.out.println("UggaBugga!");
		tree.selectFirstUnit();
		System.out.println("UggaBugga2!");

		UnitEditorDataChangeListener objectDataChangeListener = new UnitEditorDataChangeListener(this, unitData);
		unitData.addChangeListener(objectDataChangeListener);
		setupCopyPaste(new ObjectTabTreeBrowserTransferHandler(unitData.getWorldEditorDataType()));

		searchPanel = getSearchPanel();
	}

	private JTable getTable() {
		JTable table = new JTable();
		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
		table.addMouseListener(tableMouseListener());

		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "EnterKeyPopupAction");
		table.getActionMap().put("EnterKeyPopupAction", enterKeyPopupAction(false));

		KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(shiftEnter, "ShiftEnterKeyPopupAction");
		table.getActionMap().put("ShiftEnterKeyPopupAction", enterKeyPopupAction(true));

		table.addFocusListener(tableFocusListener());
		table.getTableHeader().setReorderingAllowed(false);

		DefaultTableCellRenderer editHighlightingRenderer = getEditHighlightingRenderer();
		table.setDefaultRenderer(Object.class, editHighlightingRenderer);
		table.setDefaultRenderer(String.class, editHighlightingRenderer);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this::getCurrentKeyboardEvent);
		table.setShowGrid(false);
		return table;
	}

	private DefaultTreeCellRenderer getTreeCellRenderer() {
		return new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
			                                              boolean expanded, boolean leaf, int row, boolean hasFocus) {
				Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, sel,
						expanded, leaf, row, hasFocus);
				if (value instanceof BehaviorTreeNode) {
					setIcon(((BehaviorTreeNode) value).getIcon());
				}
				return treeCellRendererComponent;
			}
		};
	}

	private JPanel getSearchPanel() {
		JPanel searchPanel = new JPanel(new MigLayout());
		searchPanel.add(new JLabel(WEString.getString("WESTRING_FINDDLG_FIND")), "cell 0 0");

		findTextField = new JTextField(40);
		searchPanel.add(findTextField, "cell 1 0");

		caseSens = new JCheckBox(WEString.getString("WESTRING_FINDDLG_CASESENS"));
		searchPanel.add(caseSens, "cell 1 1");
		return searchPanel;
	}

	private BehaviorTreeNode getActionsOnCast() {
		String repTexWEui = "ReplaceableTextures\\WorldEditUI\\";

		final BehaviorTreeNode actionsOnCast = new BehaviorTreeNode("On Cast - Actions", niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"));

		String localLevelText = "Set LocalLevel = (Level of (This ability) for (Casting unit))";
		String MissileCreateInitialText = "Missile - Create an initially unlaunched missile at (Position of (Casting unit)) with Z height 0.00 that will home in on (Target unit of ability being cast) above its head at Z height 0.00";
		String MissileChangeModelText = "Missile - Change the model file of (Last created missile) to be (Art path of (This ability) Missile Art (index 0))";
		String MissileChangeOwnerText = "Missile - Change the owner of (Last created missile) to be (Owner of (Casting unit))";
		String MissileLaunchText = "Missile - Launch (Last created missile) with a speed of (Ability: (This ability)'s Integer Field: Missile Speed ('amsp')) and arc of (Ability: (This ability)'s Real Field: Missile Arc ('amac'))";
		String unitCauseToDamageText = "Unit - Cause (Casting unit) to damage (Target unit of ability being cast), dealing (Ability: (This ability)'s Real Level Field Damage ('Htb1'), of Level: LocalLevel) damage of attack type Spells and damage type Vertex";
		String setLocalBuffTypeText = "Set LocalBuffType = (Ability: (This ability)'s Buff Level Field Buffs ('abuf'), of Level: LocalLevel)";

		actionsOnCast.add(new BehaviorTreeNode(localLevelText, niceIcon(repTexWEui + "Actions-setvariables.blp")));
		actionsOnCast.add(new BehaviorTreeNode(MissileCreateInitialText, niceIcon(repTexWEui + "Actions-Missile.blp")));
		actionsOnCast.add(new BehaviorTreeNode(MissileChangeModelText, niceIcon(repTexWEui + "Actions-Missile.blp")));
		actionsOnCast.add(new BehaviorTreeNode(MissileChangeOwnerText, niceIcon(repTexWEui + "Actions-Missile.blp")));
		actionsOnCast.add(new BehaviorTreeNode(MissileLaunchText, niceIcon(repTexWEui + "Actions-Missile.blp")));
		actionsOnCast.add(new BehaviorTreeNode(unitCauseToDamageText, niceIcon(repTexWEui + "Actions-Ability.blp")));
		actionsOnCast.add(new BehaviorTreeNode(setLocalBuffTypeText, niceIcon(repTexWEui + "Actions-setvariables.blp")));

		{
			actionsOnCast.add(getIfBlockStarter2());
		}

		actionsOnCast.add(getIfBlockStarter());
		return actionsOnCast;
	}

	private BehaviorTreeNode getIfBlockStarter2() {
		String repTexWEui1 = "ReplaceableTextures\\WorldEditUI\\";
		String repTexWEui2 = "replaceabletextures\\worldeditui\\";

		String ifThenElse = "If (All Conditions are True) then do (Then Actions) else do (Else Actions)";
		String ifText = "If - Conditions";
		String thenText = "Then - Actions";
		String elseText = "Else - Actions";

		String locDurHero = "Set LocalDuration = (Ability: (This ability)'s Real Level Field Duration - Hero ('ahdu'), of Level: LocalLevel)";
		String locDurVert = "Set LocalDuration = (Ability: (This ability)'s Real Level Field Duration - Vertex ('adur'), of Level: LocalLevel)";
		String targetHero = "((Target unit of ability being cast) is A Hero) Equal to True";
		// (Ability: This_ability's Real Level Field Duration - Vertex ('adur'), of Level: level) Greater than or equal to 10.00

		BehaviorTreeNode ifBlockStarter2 = new BehaviorTreeNode(ifThenElse, niceIcon(repTexWEui1 + "Actions-Logical.blp"));

		BehaviorTreeNode ifConditions2 = new BehaviorTreeNode(ifText, niceIcon(repTexWEui2 + "editor-triggercondition.blp"));
		ifConditions2.add(new BehaviorTreeNode(targetHero, niceIcon(repTexWEui1 + "Actions-Logical.blp")));

		BehaviorTreeNode thenActions2 = new BehaviorTreeNode(thenText, niceIcon(repTexWEui2 + "editor-triggeraction.blp"));
		thenActions2.add(new BehaviorTreeNode(locDurHero, niceIcon(repTexWEui1 + "Actions-setvariables.blp")));

		BehaviorTreeNode elseActions2 = new BehaviorTreeNode(elseText, niceIcon(repTexWEui2 + "editor-triggeraction.blp"));
		elseActions2.add(new BehaviorTreeNode(locDurVert, niceIcon(repTexWEui1 + "Actions-setvariables.blp")));

		ifBlockStarter2.add(ifConditions2);
		ifBlockStarter2.add(thenActions2);
		ifBlockStarter2.add(elseActions2);
		return ifBlockStarter2;
	}

	private BehaviorTreeNode getIfBlockStarter() {
		final String repTexWEui1 = "ReplaceableTextures\\WorldEditUI\\";
		final String repTexWEui2 = "replaceabletextures\\worldeditui\\";

		final BehaviorTreeNode ifBlockStarter = new BehaviorTreeNode("If (All Conditions are True) then do (Then Actions) else do (Else Actions)", niceIcon(repTexWEui1 + "Actions-Logical.blp"));

		final BehaviorTreeNode ifConditions = new BehaviorTreeNode("If - Conditions", niceIcon(repTexWEui2 + "editor-triggercondition.blp"));
		ifConditions.add(new BehaviorTreeNode("(Level of LocalBuffType for (Target unit of ability being cast)) Greater than 0", niceIcon(repTexWEui1 + "Actions-Logical.blp")));

		final BehaviorTreeNode thenActions = new BehaviorTreeNode("Then - Actions", niceIcon(repTexWEui2 + "editor-triggeraction.blp"));
		thenActions.add(new BehaviorTreeNode("Buff - Add LocalDuration to the duration for (Buff of (Target unit of ability being cast) of type LocalBuffType and ability type (This ability))", niceIcon(repTexWEui1 + "Actions-CasterSystem.blp")));

		ifBlockStarter.add(ifConditions);
		ifBlockStarter.add(thenActions);
		ifBlockStarter.add(getElseActions());
		return ifBlockStarter;
	}

	private BehaviorTreeNode getElseActions() {
		final String repTexWEui = "ReplaceableTextures\\WorldEditUI\\";
		final BehaviorTreeNode elseActions = new BehaviorTreeNode("Else - Actions", niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"));
		elseActions.add(new BehaviorTreeNode("Buff - Apply a new buff with Level: LocalLevel to (Target unit of ability being cast) of type LocalBuffType", niceIcon(repTexWEui + "Actions-CasterSystem.blp")));
		elseActions.add(new BehaviorTreeNode("Buff - Set the remaining duration for (Last applied buff) to LocalDuration", niceIcon(repTexWEui + "Actions-CasterSystem.blp")));
		return elseActions;
	}

	private BehaviorTreeNode getActionsOnBuffRemoved() {
		final String repTexWEui = "ReplaceableTextures\\WorldEditUI\\";
		final BehaviorTreeNode actionsOnBuffRemoved = new BehaviorTreeNode("On Buff Removed - Actions", niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"));
		actionsOnBuffRemoved.add(new BehaviorTreeNode("Set LocalLevel = (Level of (This ability) for (Buffed unit))", niceIcon(repTexWEui + "Actions-SetVariables.blp")));
		actionsOnBuffRemoved.add(new BehaviorTreeNode("Unstun (Buffed unit)", niceIcon(repTexWEui + "Actions-Ability.blp")));
		return actionsOnBuffRemoved;
	}

	private BehaviorTreeNode getActionsOnBuffApplied() {
		final String repTexWEui = "ReplaceableTextures\\WorldEditUI\\";
		final BehaviorTreeNode actionsOnBuffApplied = new BehaviorTreeNode("On Buff Applied - Actions", niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"));
		actionsOnBuffApplied.add(new BehaviorTreeNode("Set LocalLevel = (Level of (This ability) for (Buffed unit))", niceIcon(repTexWEui + "Actions-SetVariables.blp")));
		actionsOnBuffApplied.add(new BehaviorTreeNode("Stun (Buffed unit)", niceIcon(repTexWEui + "Actions-Ability.blp")));
		return actionsOnBuffApplied;
	}

	private BehaviorTreeNode getActionsOnLearn() {
		final BehaviorTreeNode actionsOnLearn = new BehaviorTreeNode("On Learn - Actions", niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"));
		// (Ability: This_ability's Real Level Field Cooldown ('acdn'), of Level: level)
		// Greater than or equal to 10.00
		// (Ability: This_ability's Real Field: Missile Arc ('amac')) Greater than or
		// equal to 10.00
		// (Ability: This_ability's Integer Field: Missile Speed ('amsp')) Equal to 0

		actionsOnLearn.add(new BehaviorTreeNode("Set LocalLevel = (Level of (This ability) for (Triggering unit))", niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-setvariables.blp")));
		actionsOnLearn.add(new BehaviorTreeNode("Command Card - Add a (Unit target) command card icon for (This ability) using ((Ability: (This ability)'s Integer Field: Button Position - Vertex (X) ('abpx')), (Ability: (This ability)'s Integer Field: Button Position - Vertex (Y) ('abpy')))", niceIcon("ui\\widgets\\tooltips\\human\\tooltipmanaicon.blp")));
		actionsOnLearn.add(new BehaviorTreeNode("Command Card - Set the icon of (Last created Command Card Icon) to (Icon of (This ability))", niceIcon("ui\\widgets\\tooltips\\human\\tooltipmanaicon.blp")));
		actionsOnLearn.add(new BehaviorTreeNode("Command Card - Set the Mana Cost of (Last created Command Card Icon) to (Ability: (This ability)'s Integer Level Field Mana Cost ('amcs'), of Level: LocalLevel)", niceIcon("ui\\widgets\\tooltips\\human\\tooltipmanaicon.blp")));
		actionsOnLearn.add(new BehaviorTreeNode("Command Card - Set the Cooldown of (Last created Command Card Icon) to (Ability: (This ability)'s Real Level Field Cooldown ('acdn'), of Level: LocalLevel)", niceIcon("ui\\widgets\\tooltips\\human\\tooltipmanaicon.blp")));
		return actionsOnLearn;
	}

	private BehaviorTreeNode getLocalVarNode() {
		final BehaviorTreeNode localVarNode = new BehaviorTreeNode("Local Variables", niceIcon("replaceabletextures\\worldeditui\\editor-scriptvariable.blp"));
		final String repTexWEui = "ReplaceableTextures\\WorldEditUI\\";
		localVarNode.add(new BehaviorTreeNode("LocalLevel", niceIcon(repTexWEui + "Actions-setvariables.blp")));
		localVarNode.add(new BehaviorTreeNode("LocalDuration", niceIcon(repTexWEui + "Actions-setvariables.blp")));
		localVarNode.add(new BehaviorTreeNode("LocalBuffType", niceIcon(repTexWEui + "Actions-setvariables.blp")));
		return localVarNode;
	}


	private boolean getCurrentKeyboardEvent(KeyEvent ke) {
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

	private FocusListener tableFocusListener() {
		return new FocusListener() {
			@Override
			public void focusLost(final FocusEvent e) {
				table.repaint();
			}

			@Override
			public void focusGained(final FocusEvent e) {
				table.repaint();
			}
		};
	}

	private DefaultTableCellRenderer getEditHighlightingRenderer() {
		return new DefaultTableCellRenderer() {
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
				} else if ((dataModel != null) && dataModel.hasEditedValue(row)) {
					setForeground(settings.getEditedValueColor());
				} else {
					setForeground(null);
				}
				return this;
			}
		};
	}

	private AbstractAction enterKeyPopupAction(boolean isHoldingShift) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				showEnterPopup(isHoldingShift);
			}
		};
	}

	private void showEnterPopup(boolean isHoldingShift) {
		final int rowIndex = table.getSelectedRow();
		if (dataModel != null) {
			dataModel.doPopupAt(UnitEditorPanel.this, rowIndex, isHoldingShift);
		}
	}

	private MouseListener tableMouseListener() {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					// this might should use e.isShiftDown instead of synchronized hack?
					showEnterPopup(holdingShift);
				}
			}
		};
	}

	private ImageIcon niceIcon(final String filepath) {
		BufferedImage gameTex = BLPHandler.getGameTex(filepath);
		if (gameTex == null) {
			gameTex = BLPHandler.getGameTex("Textures\\black32.blp");
		}
		return new ImageIcon(IconUtils.worldEditStyleIcon(gameTex.getScaledInstance(16, 16, Image.SCALE_FAST)));
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

	protected void selectTreeNode(final TreeNode lastPathComponent) {
		final TreePath pathForNode = getPathForNode(lastPathComponent);
		tree.setSelectionPath(pathForNode);
		tree.scrollPathToVisible(pathForNode);
	}

	protected void addSelectedTreeNode(final TreeNode lastPathComponent) {
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
		return new TreePath(nodes.toArray());
	}

	public void selectUnit(final War3ID unitId) {
		final Enumeration<TreeNode> depthFirstEnumeration = root.depthFirstEnumeration();
		while (depthFirstEnumeration.hasMoreElements()) {
			final TreeNode nextElement = depthFirstEnumeration.nextElement();

			if (nextElement instanceof DefaultMutableTreeNode) {
				if (((DefaultMutableTreeNode) nextElement).getUserObject() instanceof MutableGameObject) {
					final MutableGameObject object = (MutableGameObject) ((DefaultMutableTreeNode) nextElement).getUserObject();

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
				settings.isDisplayAsRawData(), () -> changeCustomUnit());

		dataModel.addTableModelListener(e -> getTableModelListener());
		table.setModel(dataModel);

		dataModel.addTableModelListener(e -> getTableModelListener2());
		table.setAutoCreateColumnsFromModel(false);
	}

	private void changeCustomUnit() {
		final DefaultTreeModel treeModel = tree.getModel();

		if (currentUnitTreePath != null) {
			for (final Object untypedTreePathNode : currentUnitTreePath.getPath()) {
				treeModel.nodeChanged((TreeNode) untypedTreePathNode);
			}
		}
	}

	private void getTableModelListener2() {
		for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
			if (lastSelectedFields.contains(dataModel.getFieldRawDataName(rowIndex) + ":" + dataModel.getFieldLevel(rowIndex))) {
				table.addRowSelectionInterval(rowIndex, rowIndex);
			}
		}
	}

	private void getTableModelListener() {
		if (currentUnit != null) {
			lastSelectedFields.clear();
			if (dataModel != null) {
				for (final int rowIndex : table.getSelectedRows()) {
					lastSelectedFields.add(dataModel.getFieldRawDataName(rowIndex) + ":" + dataModel.getFieldLevel(rowIndex));
				}
			}
		}
	}

	public void toggleDisplayAsRawData() {
		settings.setDisplayAsRawData(!settings.isDisplayAsRawData());
		if (dataModel != null) {
			dataModel.setDisplayAsRawData(settings.isDisplayAsRawData());
		}
		refreshAllTreeNodes();
		repaint();
	}

	private void refreshAllTreeNodes() {
		final Enumeration<TreeNode> enumer = root.breadthFirstEnumeration();
		while (enumer.hasMoreElements()) {
			tree.getModel().nodeChanged(enumer.nextElement());
		}
	}

	@Override
	public void valueChanged(final TreeSelectionEvent e) {
		currentUnitTreePath = e.getNewLeadSelectionPath();
		if (currentUnitTreePath != null) {
			getTableModelListener();
			final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
			if (o.getUserObject() instanceof MutableGameObject) {
				currentUnit = (MutableGameObject) o.getUserObject();
			} else {
				currentUnit = null;
			}
			fillTable();
			getTableModelListener2();
		}
	}

	public void doSearchForUnit() {
		boolean tableHadFocus = table.hasFocus();
		final String title = WEString.getString("WESTRING_FINDDLG_TITLE");
		int result = FieldPopupUtils.showPopup(this, searchPanel, title, findTextField);
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

		lookThroughTable(text, caseSensitive, dataModel.getRowCount(), startIndex);

		if (startIndex > 0) {
			lookThroughTable(text, caseSensitive, startIndex, 0);
		}
	}

	private void lookThroughTable(String text, boolean caseSensitive, int startIndex, int i2) {
		for (int i = i2; i < startIndex; i++) {
			for (int j = 0; j < dataModel.getColumnCount(); j++) {
				final Object tableData = dataModel.getValueAt(i, j);
				String tableString = tableData.toString();

				if (!caseSensitive) {
					tableString = tableString.toLowerCase();

					if (tableString.contains(text)) {
						int rowToSelect = table.convertRowIndexToView(i);
						table.setRowSelectionInterval(rowToSelect, rowToSelect);
						table.scrollRectToVisible(table.getCellRect(rowToSelect, j, true));
						return;
					}
				}
			}
		}
	}

	public void loadHotkeys() {
		tree.loadHotkeys();
	}

	protected UnitEditorTreeModel getTreeModel() {
		return tree.getModel();
	}

	protected void setTreeSelectionPath(TreePath path) {
		tree.setSelectionPath(path);
	}

	protected TopLevelCategoryFolder getRoot() {
		return root;
	}
}
