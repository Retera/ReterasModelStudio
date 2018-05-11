package com.hiveworkshop.wc3.jworldedit.models;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.hiveworkshop.wc3.gui.modeledit.PerspDisplayPanel;
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.wc3.jworldedit.objects.UnitTabTreeBrowserBuilder;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftObject;
import com.hiveworkshop.wc3.units.UnitComparator;
import com.hiveworkshop.wc3.units.fields.UnitFields;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;

import de.wc3data.stream.BlizzardDataInputStream;

public class BetterUnitEditorModelSelector extends JSplitPane implements TreeSelectionListener {
	MutableGameObject currentUnit = null;
	UnitEditorTreeModel model;

	JLabel debugLabel = new JLabel("debug");

	MDL mdl = new MDL();
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
		tree = new UnitEditorTree(unitData, new UnitTabTreeBrowserBuilder(), unitEditorSettings,
				WorldEditorDataType.UNITS);

		this.setLeftComponent(treePane = new JScrollPane(tree));
		final JPanel temp = new JPanel();
		temp.add(debugLabel);

		modelPanel = new PerspDisplayPanel("blank", modelDisp, null);
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
			try (BlizzardDataInputStream reader = new BlizzardDataInputStream(
					MpqCodebase.get().getResourceAsStream(filepath))) {
				mdl = MdxUtils.loadModel(reader).toMDL();
				modelDisp = new ModelViewManager(mdl);
				modelPanel.setViewport(modelDisp);
				modelPanel.setTitle(currentUnit.getName());
			} catch (final FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// loadFile(MPQHandler.get().getGameFile(filepath), true);
			// modelMenu.getAccessibleContext().setAccessibleDescription("Allows
			// the user to control which parts of the model are displayed for
			// editing.");
			// modelMenu.setEnabled(true);
			// modelDisp = new MDLDisplay(toLoad, null);
		} catch (final Exception exc) {
			exc.printStackTrace();
			// bad model!
			JOptionPane.showMessageDialog(getParent(), "The chosen model could not be used.", "Program Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	static class UnitEditorTreeModel extends DefaultTreeModel {
		public UnitEditorTreeModel(final DefaultMutableTreeNode root) {
			super(root);
		}

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

	}

	public void loadRaceData(final DefaultMutableTreeNode folder, final RaceData data) {
		final DefaultMutableTreeNode units = new DefaultMutableTreeNode(WEString.getString("WESTRING_UNITS"));
		final DefaultMutableTreeNode buildings = new DefaultMutableTreeNode(
				WEString.getString("WESTRING_UTYPE_BUILDINGS"));
		final DefaultMutableTreeNode heroes = new DefaultMutableTreeNode(WEString.getString("WESTRING_UTYPE_HEROES"));
		final DefaultMutableTreeNode special = new DefaultMutableTreeNode(WEString.getString("WESTRING_UTYPE_SPECIAL"));
		for (final WarcraftObject u : data.units) {
			DefaultMutableTreeNode node;
			units.add(node = new DefaultMutableTreeNode(u));
			if (defaultSelection == null) {
				defaultSelection = node;
			}
		}
		for (final WarcraftObject u : data.buildings) {
			buildings.add(new DefaultMutableTreeNode(u));
		}
		for (final WarcraftObject u : data.heroes) {
			heroes.add(new DefaultMutableTreeNode(u));
		}
		for (final WarcraftObject u : data.special) {
			special.add(new DefaultMutableTreeNode(u));
		}
		if (data.units.size() > 0) {
			folder.add(units);
		}
		if (data.buildings.size() > 0) {
			folder.add(buildings);
		}
		if (data.heroes.size() > 0) {
			folder.add(heroes);
		}
		if (data.special.size() > 0) {
			folder.add(special);
		}
	}

	class RaceData {
		List<WarcraftObject> units = new ArrayList<>();
		List<WarcraftObject> heroes = new ArrayList<>();
		List<WarcraftObject> buildings = new ArrayList<>();
		List<WarcraftObject> buildingsUprooted = new ArrayList<>();
		List<WarcraftObject> special = new ArrayList<>();

		void sort() {
			final Comparator<WarcraftObject> unitComp = new UnitComparator();

			Collections.sort(units, unitComp);
			Collections.sort(heroes, unitComp);
			Collections.sort(buildings, unitComp);
			Collections.sort(buildingsUprooted, unitComp);
			Collections.sort(special, unitComp);
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
