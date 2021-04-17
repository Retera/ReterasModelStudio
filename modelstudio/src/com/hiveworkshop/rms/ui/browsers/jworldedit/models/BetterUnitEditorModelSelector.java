package com.hiveworkshop.rms.ui.browsers.jworldedit.models;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData.WarcraftObject;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.UnitComparator;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BetterUnitEditorModelSelector extends JSplitPane implements TreeSelectionListener {
	MutableGameObject currentUnit = null;
	UnitEditorTreeModel model;

	JLabel debugLabel = new JLabel("debug");

	EditableModel mdl = new EditableModel();
	// MDL mdl;
	ModelView modelDisp = new ModelViewManager(mdl);
	PerspDisplayPanel modelPanel;
	DefaultTableModel tableModel;
	DefaultMutableTreeNode defaultSelection = null;
	JScrollPane treePane;
	private final MutableObjectData unitData;
	private final UnitEditorTree tree;

	public BetterUnitEditorModelSelector(final MutableObjectData unitData,
										 final UnitEditorSettings unitEditorSettings) {
		this.unitData = unitData;
		tree = new UnitEditorTree(unitData, new UnitTabTreeBrowserBuilder(), unitEditorSettings, WorldEditorDataType.UNITS);

		setLeftComponent(treePane = new JScrollPane(tree));
		final JPanel temp = new JPanel();
		temp.add(debugLabel);

		modelPanel = new PerspDisplayPanel("blank", modelDisp, new ProgramPreferences());
		fillTable();

		setRightComponent(modelPanel);

		tree.addTreeSelectionListener(this);
		treePane.setPreferredSize(new Dimension(350, 600));
		modelPanel.setPreferredSize(new Dimension(800, 600));
		if (defaultSelection != null) {
			tree.getSelectionModel().setSelectionPath(getPath(defaultSelection));
		}
	}

	public void fillTable() {
		if (currentUnit == null) {
			tree.selectFirstUnit();
			currentUnit = tree.getSelectedGameObject();
		}
		if (currentUnit != null) {

		} else {
			return;
		}

		String filepath = currentUnit.getFieldAsString(UnitFields.MODEL_FILE, 0);

		ModelView modelDisp = null;
		try {
			if (filepath.endsWith(".mdl")) {
				filepath = filepath.replace(".mdl", ".mdx");
			} else if (!filepath.endsWith(".mdx")) {
				filepath = filepath.concat(".mdx");
			}
			try (InputStream reader = GameDataFileSystem.getDefault().getResourceAsStream(filepath)) {
				mdl = new EditableModel(MdxUtils.loadMdlx(reader));
				modelDisp = new ModelViewManager(mdl);
				modelPanel.setViewport(modelDisp);
				modelPanel.setTitle(currentUnit.getName());
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (final Exception exc) {
			exc.printStackTrace();
			// bad model!
			JOptionPane.showMessageDialog(getParent(),
					"The chosen model could not be used.",
					"Program Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	static class UnitEditorTreeModel extends DefaultTreeModel {
		public UnitEditorTreeModel(final DefaultMutableTreeNode root) {
			super(root);
		}
	}

	public void loadRaceData(final DefaultMutableTreeNode folder, final RaceData data) {
		addDataToFolder(folder, "WESTRING_UNITS", data.units);
		addDataToFolder(folder, "WESTRING_UTYPE_BUILDINGS", data.buildings);
		addDataToFolder(folder, "WESTRING_UTYPE_HEROES", data.heroes);
		addDataToFolder(folder, "WESTRING_UTYPE_SPECIAL", data.special);
	}

	private void addDataToFolder(DefaultMutableTreeNode folder, String weType, List<WarcraftObject> objects) {
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(WEString.getString(weType));
		for (final WarcraftObject u : objects) {
			node.add(new DefaultMutableTreeNode(u));
		}
		if (objects.size() > 0) {
			folder.add(node);
			if (defaultSelection == null) {
				defaultSelection = node.getFirstLeaf();
			}
		}
	}

	static class RaceData {
		List<WarcraftObject> units = new ArrayList<>();
		List<WarcraftObject> heroes = new ArrayList<>();
		List<WarcraftObject> buildings = new ArrayList<>();
		List<WarcraftObject> buildingsUprooted = new ArrayList<>();
		List<WarcraftObject> special = new ArrayList<>();

		void sort() {
			final Comparator<WarcraftObject> unitComp = new UnitComparator();

			units.sort(unitComp);
			heroes.sort(unitComp);
			buildings.sort(unitComp);
			buildingsUprooted.sort(unitComp);
			special.sort(unitComp);
		}
	}

	@Override
	public void valueChanged(final TreeSelectionEvent e) {
		final DefaultMutableTreeNode o = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
		if (o.getUserObject() instanceof MutableGameObject) {
			final MutableGameObject obj = (MutableGameObject) o.getUserObject();
			debugLabel.setText(obj.getName());
			// System.out.println(obj.getId());
			currentUnit = obj;
			fillTable();
		}
	}

	public static TreePath getPath(TreeNode treeNode) {
		final List<Object> nodes = new ArrayList<>();
		if (treeNode != null) {
			nodes.add(treeNode);
			treeNode = treeNode.getParent();
			while (treeNode != null) {
				nodes.add(0, treeNode);
				treeNode = treeNode.getParent();
			}
		}

		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
	}

	public MutableGameObject getSelection() {
		return currentUnit;
	}
}
