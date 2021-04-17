package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.EditorFieldBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.ObjectDataTableModel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.FieldPopupUtils;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectDataChangeListener;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.TreeNodeLinkerFromModel;
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
	private final JPanel searchPanel;
	private final JTextField findTextField;
	private final JCheckBox caseSens;
	private final JScrollPane treeScrollPane;
	private final Set<String> lastSelectedFields = new HashSet<>();

	public UnitEditorPanel(
			final MutableObjectData unitData,
			final DataTable unitMetaData,
			final EditorFieldBuilder editorFieldBuilder,
			final ObjectTabTreeBrowserBuilder objectTabTreeBrowserBuilder,
			final WorldEditorDataType dataType,
			final EditorTabCustomToolbarButtonData editorTabCustomToolbarButtonData,
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
		setLeftComponent(treeScrollPane);
		// temp.setBackground(Color.blue);

		table = new JTable();
		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
		table.addMouseListener(tableMouseListener());

		final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "EnterKeyPopupAction");
		table.getActionMap().put("EnterKeyPopupAction", enterKeyPopupAction(false));

		final KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(shiftEnter, "ShiftEnterKeyPopupAction");
		table.getActionMap().put("ShiftEnterKeyPopupAction", enterKeyPopupAction(true));

		table.addFocusListener(tableFocusListener());
		table.getTableHeader().setReorderingAllowed(false);

		final DefaultTableCellRenderer editHighlightingRenderer = getEditHighlightingRenderer();
		table.setDefaultRenderer(Object.class, editHighlightingRenderer);
		table.setDefaultRenderer(String.class, editHighlightingRenderer);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> getCurrentKeyboardEvent(ke));
		table.setShowGrid(false);

		final JTabbedPane splitWithBehaviorEditor = new JTabbedPane();
//		splitWithBehaviorEditor.addTab("Stats", new JScrollPane(table));

		class BehaviorTreeNode extends DefaultMutableTreeNode {
			private final ImageIcon icon;

			public BehaviorTreeNode(final ImageIcon icon, final String text) {
				super(text);
				this.icon = icon;
			}

			public ImageIcon getIcon() {
				return icon;
			}
		}

		final String filepath = "replaceabletextures\\commandbuttons\\btnstormbolt.blp";
		final BehaviorTreeNode behaviorRoot = new BehaviorTreeNode(niceIcon(filepath), "Storm Bolt");

		final BehaviorTreeNode localVarNode = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-scriptvariable.blp"), "Local Variables");
		behaviorRoot.add(localVarNode);
		localVarNode.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-setvariables.blp"), "LocalLevel"));
		localVarNode.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-setvariables.blp"), "LocalDuration"));
		localVarNode.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-setvariables.blp"), "LocalBuffType"));

		final BehaviorTreeNode actionsOnLearn = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"), "On Learn - Actions");
		behaviorRoot.add(actionsOnLearn);
		// (Ability: This_ability's Real Level Field Cooldown ('acdn'), of Level: level)
		// Greater than or equal to 10.00
		// (Ability: This_ability's Real Field: Missile Arc ('amac')) Greater than or
		// equal to 10.00
		// (Ability: This_ability's Integer Field: Missile Speed ('amsp')) Equal to 0

		actionsOnLearn.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-setvariables.blp"), "Set LocalLevel = (Level of (This ability) for (Triggering unit))"));
		actionsOnLearn.add(new BehaviorTreeNode(niceIcon("ui\\widgets\\tooltips\\human\\tooltipmanaicon.blp"), "Command Card - Add a (Unit target) command card icon for (This ability) using ((Ability: (This ability)'s Integer Field: Button Position - Vertex (X) ('abpx')), (Ability: (This ability)'s Integer Field: Button Position - Vertex (Y) ('abpy')))"));
		actionsOnLearn.add(new BehaviorTreeNode(niceIcon("ui\\widgets\\tooltips\\human\\tooltipmanaicon.blp"), "Command Card - Set the icon of (Last created Command Card Icon) to (Icon of (This ability))"));
		actionsOnLearn.add(new BehaviorTreeNode(niceIcon("ui\\widgets\\tooltips\\human\\tooltipmanaicon.blp"), "Command Card - Set the Mana Cost of (Last created Command Card Icon) to (Ability: (This ability)'s Integer Level Field Mana Cost ('amcs'), of Level: LocalLevel)"));
		actionsOnLearn.add(new BehaviorTreeNode(niceIcon("ui\\widgets\\tooltips\\human\\tooltipmanaicon.blp"), "Command Card - Set the Cooldown of (Last created Command Card Icon) to (Ability: (This ability)'s Real Level Field Cooldown ('acdn'), of Level: LocalLevel)"));

		final BehaviorTreeNode actionsOnCast = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"), "On Cast - Actions");
		behaviorRoot.add(actionsOnCast);


		String localLevelText = "Set LocalLevel = (Level of (This ability) for (Casting unit))";
		actionsOnCast.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-setvariables.blp"), localLevelText));

		String MissileCreateInitialText = "Missile - Create an initially unlaunched missile at (Position of (Casting unit)) with Z height 0.00 that will home in on (Target unit of ability being cast) above its head at Z height 0.00";
		actionsOnCast.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Missile.blp"), MissileCreateInitialText));

		String MissileChangeModelText = "Missile - Change the model file of (Last created missile) to be (Art path of (This ability) Missile Art (index 0))";
		actionsOnCast.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Missile.blp"), MissileChangeModelText));

		String MissileChangeOwnerText = "Missile - Change the owner of (Last created missile) to be (Owner of (Casting unit))";
		actionsOnCast.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Missile.blp"), MissileChangeOwnerText));

		String MissileLaunchText = "Missile - Launch (Last created missile) with a speed of (Ability: (This ability)'s Integer Field: Missile Speed ('amsp')) and arc of (Ability: (This ability)'s Real Field: Missile Arc ('amac'))";
		actionsOnCast.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Missile.blp"), MissileLaunchText));

		String unitCauseToDamageText = "Unit - Cause (Casting unit) to damage (Target unit of ability being cast), dealing (Ability: (This ability)'s Real Level Field Damage ('Htb1'), of Level: LocalLevel) damage of attack type Spells and damage type Vertex";
		actionsOnCast.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Ability.blp"), unitCauseToDamageText));

		String setLocalBuffTypeText = "Set LocalBuffType = (Ability: (This ability)'s Buff Level Field Buffs ('abuf'), of Level: LocalLevel)";
		actionsOnCast.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-setvariables.blp"), setLocalBuffTypeText));

		{

			final BehaviorTreeNode ifBlockStarter2 = new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Logical.blp"), "If (All Conditions are True) then do (Then Actions) else do (Else Actions)");
			actionsOnCast.add(ifBlockStarter2);

			final BehaviorTreeNode ifConditions2 = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggercondition.blp"), "If - Conditions");
			ifBlockStarter2.add(ifConditions2);
			ifConditions2.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Logical.blp"), "((Target unit of ability being cast) is A Hero) Equal to True"));
			// (Ability: This_ability's Real Level Field Duration - Vertex ('adur'), of
			// Level: level) Greater than or equal to 10.00

			final BehaviorTreeNode thenActions2 = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"), "Then - Actions");
			ifBlockStarter2.add(thenActions2);
			thenActions2.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-setvariables.blp"), "Set LocalDuration = (Ability: (This ability)'s Real Level Field Duration - Hero ('ahdu'), of Level: LocalLevel)"));

			final BehaviorTreeNode elseActions2 = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"), "Else - Actions");
			ifBlockStarter2.add(elseActions2);
			elseActions2.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-setvariables.blp"), "Set LocalDuration = (Ability: (This ability)'s Real Level Field Duration - Vertex ('adur'), of Level: LocalLevel)"));
		}

		final BehaviorTreeNode ifBlockStarter = new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Logical.blp"), "If (All Conditions are True) then do (Then Actions) else do (Else Actions)");
		actionsOnCast.add(ifBlockStarter);

		final BehaviorTreeNode ifConditions = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggercondition.blp"), "If - Conditions");
		ifBlockStarter.add(ifConditions);
		ifConditions.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Logical.blp"), "(Level of LocalBuffType for (Target unit of ability being cast)) Greater than 0"));

		final BehaviorTreeNode thenActions = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"), "Then - Actions");
		ifBlockStarter.add(thenActions);
		thenActions.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-CasterSystem.blp"), "Buff - Add LocalDuration to the duration for (Buff of (Target unit of ability being cast) of type LocalBuffType and ability type (This ability))"));

		final BehaviorTreeNode elseActions = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"), "Else - Actions");
		ifBlockStarter.add(elseActions);

		elseActions.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-CasterSystem.blp"), "Buff - Apply a new buff with Level: LocalLevel to (Target unit of ability being cast) of type LocalBuffType"));
		elseActions.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-CasterSystem.blp"), "Buff - Set the remaining duration for (Last applied buff) to LocalDuration"));

		final BehaviorTreeNode actionsOnBuffApplied = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"), "On Buff Applied - Actions");
		behaviorRoot.add(actionsOnBuffApplied);
		actionsOnBuffApplied.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-SetVariables.blp"), "Set LocalLevel = (Level of (This ability) for (Buffed unit))"));
		actionsOnBuffApplied.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Ability.blp"), "Stun (Buffed unit)"));

		final BehaviorTreeNode actionsOnBuffRemoved = new BehaviorTreeNode(niceIcon("replaceabletextures\\worldeditui\\editor-triggeraction.blp"), "On Buff Removed - Actions");
		behaviorRoot.add(actionsOnBuffRemoved);
		actionsOnBuffRemoved.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-SetVariables.blp"), "Set LocalLevel = (Level of (This ability) for (Buffed unit))"));
		actionsOnBuffRemoved.add(new BehaviorTreeNode(niceIcon("ReplaceableTextures\\WorldEditUI\\Actions-Ability.blp"), "Unstun (Buffed unit)"));

		final JTree behaviorTree = new JTree(behaviorRoot);
		behaviorTree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
			                                              final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
				final Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, sel,
						expanded, leaf, row, hasFocus);
				if (value instanceof BehaviorTreeNode) {
					setIcon(((BehaviorTreeNode) value).getIcon());
				}
				return treeCellRendererComponent;
			}
		});
		for (int i = 0; i < behaviorTree.getRowCount(); i++) {
			behaviorTree.expandRow(i);
		}
//		splitWithBehaviorEditor.addTab("Stats", new JScrollPane(table));
//		splitWithBehaviorEditor.addTab("Behavior", new JScrollPane(behaviorTree));
//		setRightComponent((splitWithBehaviorEditor));
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
				System.out.println("categoriesChanged(" + changedObject + ")");
				final UnitEditorTreeModel treeModel = tree.getModel();
				final TreeNode changedNode = treeModel.getNodeById(changedObject);
				if (changedNode != null) {
					final MutableTreeNode lastPathComponent = (MutableTreeNode) changedNode;
					treeModel.removeNodeFromParent(lastPathComponent);
					final DefaultMutableTreeNode newObjectNode = root.insertObjectInto(unitData.get(changedObject),
							new TreeNodeLinkerFromModel(treeModel));
					selectTreeNode(newObjectNode);
				} else {
					System.out.println("Changed node was not found");
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

	private AbstractAction enterKeyPopupAction(boolean b) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				final int rowIndex = table.getSelectedRow();
				if (dataModel != null) {
					dataModel.doPopupAt(UnitEditorPanel.this, rowIndex, b);
				}
			}
		};
	}

	private MouseListener tableMouseListener() {
		return new MouseListener() {
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
		};
	}

	private ImageIcon niceIcon(final String filepath) {
		BufferedImage gameTex = BLPHandler.get().getGameTex(filepath);
		if (gameTex == null) {
			gameTex = BLPHandler.get().getGameTex("Textures\\black32.blp");
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
				settings.isDisplayAsRawData(), () -> {
			final DefaultTreeModel treeModel = tree.getModel();

			if (currentUnitTreePath != null) {
				for (final Object untypedTreePathNode : currentUnitTreePath.getPath()) {
					treeModel.nodeChanged((TreeNode) untypedTreePathNode);
				}
			}
		});

		dataModel.addTableModelListener(e -> getTableModelListener());
		table.setModel(dataModel);

		dataModel.addTableModelListener(e -> getTableModelListener2());
		table.setAutoCreateColumnsFromModel(false);
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
						final int rowToSelect = table.convertRowIndexToView(i);
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
}
