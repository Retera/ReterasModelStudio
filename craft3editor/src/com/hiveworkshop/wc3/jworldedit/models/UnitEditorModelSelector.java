package com.hiveworkshop.wc3.jworldedit.models;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.hiveworkshop.wc3.gui.modeledit.PerspDisplayPanel;
import com.hiveworkshop.wc3.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.wc3.jworldedit.objects.WarcraftObjectTreeCellRenderer;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftObject;
import com.hiveworkshop.wc3.units.UnitComparator;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;

import de.wc3data.stream.BlizzardDataInputStream;

@Deprecated
public class UnitEditorModelSelector extends JSplitPane implements TreeSelectionListener {
	WarcraftData unitData = StandardObjectData.getStandardUnits();
	GameObject currentUnit = null;
	DataTable unitMetaData = StandardObjectData.getStandardUnitMeta();
	UnitEditorSettings settings = new UnitEditorSettings();
	JTree tree;
	UnitEditorTreeModel model;
	DefaultMutableTreeNode root;

	JLabel debugLabel = new JLabel("debug");

	EditableModel mdl = new EditableModel();
	// MDL mdl;
	ModelView modelDisp = new ModelViewManager(mdl);
	PerspDisplayPanel modelPanel;
	DefaultTableModel tableModel;
	DefaultMutableTreeNode defaultSelection = null;

	public UnitEditorModelSelector() {
		root = new DefaultMutableTreeNode();
		// System.out.println(unitData.get("opeo").getName());
		// root.add(new DefaultMutableTreeNode(unitData.get("opeo")));
		// root.add(new DefaultMutableTreeNode(unitData.get("hpea")));
		// root.add(new DefaultMutableTreeNode(unitData.get("uaco")));
		final DefaultMutableTreeNode standardUnitsFolder = new DefaultMutableTreeNode(
				WEString.getString("WESTRING_UE_STANDARDUNITS"));
		final DefaultMutableTreeNode customUnitsFolder = new DefaultMutableTreeNode(
				WEString.getString("WESTRING_UE_CUSTOMUNITS"));
		sortRaces();
		for (int i = 0; i < 7; i++) {
			final String race = raceName(i);
			final String raceKey = raceKey(i);
			final DefaultMutableTreeNode humanFolder = new DefaultMutableTreeNode(race);
			final DefaultMutableTreeNode humanMeleeFolder = new DefaultMutableTreeNode(
					WEString.getString("WESTRING_MELEE"));
			final DefaultMutableTreeNode humanCampFolder = new DefaultMutableTreeNode(
					WEString.getString("WESTRING_CAMPAIGN"));
			final RaceData humanMData = sortedRaces.get(raceKey + "melee");
			final RaceData humanCData = sortedRaces.get(raceKey + "campaign");
			loadRaceData(humanMeleeFolder, humanMData);
			loadRaceData(humanCampFolder, humanCData);
			if (humanMeleeFolder.getLeafCount() > 1) {
				humanFolder.add(humanMeleeFolder);
			}
			if (humanCampFolder.getLeafCount() > 1) {
				humanFolder.add(humanCampFolder);
			}
			standardUnitsFolder.add(humanFolder);
		}
		root.add(standardUnitsFolder);
		root.add(customUnitsFolder);

		model = new UnitEditorTreeModel(root);
		tree = new JTree(root);
		tree.setModel(model);
		tree.setCellRenderer(new WarcraftObjectTreeCellRenderer(settings, WorldEditorDataType.UNITS));
		tree.setRootVisible(false);
		JScrollPane treePane;
		this.setLeftComponent(treePane = new JScrollPane(tree));
		final JPanel temp = new JPanel();
		// temp.setBackground(Color.blue);
		temp.add(debugLabel);

		// TODO null prefs
		modelPanel = new PerspDisplayPanel("blank", modelDisp, null, new RenderModel(modelDisp.getModel(), null));
		// table.setShowGrid(false);
		fillTable();

		setRightComponent(modelPanel);

		tree.addTreeSelectionListener(this);
		treePane.setPreferredSize(new Dimension(350, 600));
		modelPanel.setPreferredSize(new Dimension(800, 600));
		if (defaultSelection != null) {
			tree.getSelectionModel().setSelectionPath(getPath(defaultSelection));
		}

		// KeyEventDispatcher myKeyEventDispatcher = new DefaultFocusManager();
		// KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(myKeyEventDispatcher);
	}

	public static String categoryName(final String cat) {
		switch (cat.toLowerCase()) {
		case "abil":
			return WEString.getString("WESTRING_OE_CAT_ABILITIES").replace("&", "");
		case "art":
			return WEString.getString("WESTRING_OE_CAT_ART").replace("&", "");
		case "combat":
			return WEString.getString("WESTRING_OE_CAT_COMBAT").replace("&", "");
		case "data":
			return WEString.getString("WESTRING_OE_CAT_DATA").replace("&", "");
		case "editor":
			return WEString.getString("WESTRING_OE_CAT_EDITOR").replace("&", "");
		case "move":
			return WEString.getString("WESTRING_OE_CAT_MOVEMENT").replace("&", "");
		case "path":
			return WEString.getString("WESTRING_OE_CAT_PATHING").replace("&", "");
		case "sound":
			return WEString.getString("WESTRING_OE_CAT_SOUND").replace("&", "");
		case "stats":
			return WEString.getString("WESTRING_OE_CAT_STATS").replace("&", "");
		case "tech":
			return WEString.getString("WESTRING_OE_CAT_TECHTREE").replace("&", "");
		case "text":
			return WEString.getString("WESTRING_OE_CAT_TEXT").replace("&", "");
		}
		return WEString.getString("WESTRING_UNKNOWN");
	}

	public void fillTable() {
		if (currentUnit == null) {
			currentUnit = unitData.get("hpea");
		}
		if (currentUnit != null) {

		} else {
			return;
		}

		String filepath = currentUnit.getField("file");

		ModelView modelDisp = null;
		try {
			if (filepath.endsWith(".mdl")) {
				filepath = filepath.replace(".mdl", ".mdx");
			} else if (!filepath.endsWith(".mdx")) {
				filepath = filepath.concat(".mdx");
			}
			try (InputStream reader = MpqCodebase.get().getResourceAsStream(filepath)) {
				mdl = new EditableModel(MdxUtils.loadModel(reader));
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
		// Vector colNames = new Vector();
		// colNames.add("Name");
		// colNames.add("Value");
		// Vector fields = new Vector();
		// for( String fieldId: unitMetaData.keySet() ) {
		// Element field = unitMetaData.get(fieldId);
		// Vector fieldVect = new Vector();
		// String name = field.getField("field");
		// if( !settings.isDisplayAsRawData() ) {
		// name = categoryName(field.getField("category"))+" -
		// "+WEString.getString(field.getField("displayName"));
		// }
		//
		// fieldVect.add(name);
		// fieldVect.add(currentUnit.getField(field.getField("field")));
		// if( field.getFieldValue("useUnit") > 0
		// || (currentUnit.getFieldValue("isbldg") > 0 &&
		// field.getFieldValue("useBuilding") > 0)
		// || (Character.isUpperCase(currentUnit.getId().charAt(0)) &&
		// field.getFieldValue("useHero") > 0)
		// )
		// fields.add(fieldVect);
		// }
		// fields.sort(new Comparator<Vector>() {
		//
		// @Override
		// public int compare(Vector o1, Vector o2) {
		// return o1.get(0).toString().compareTo(o2.get(0).toString());
		// }
		//
		// });
		// if( tableModel == null ) {
		// tableModel = new DefaultTableModel(fields, colNames);
		// table.setModel(tableModel);
		// } else {
		// tableModel.setDataVector(fields, colNames);
		// }
	}

	public void loadHotkeys() {
		final JRootPane root = getRootPane();
		this.getRootPane().getActionMap().put("displayAsRawData", new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				settings.setDisplayAsRawData(!settings.isDisplayAsRawData());
				final Enumeration<TreeNode> enumer = UnitEditorModelSelector.this.root.breadthFirstEnumeration();
				while (enumer.hasMoreElements()) {
					model.nodeChanged(enumer.nextElement());
				}
				fillTable();
				repaint();
			}

		});
		root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("control D"),
				"displayAsRawData");

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

	static Map<String, RaceData> sortedRaces;

	private String raceKey(final int index) {
		switch (index) {
		case -1:
			return "human";
		case 0:
			return "human";
		case 1:
			return "orc";
		case 2:
			return "undead";
		case 3:
			return "nightelf";
		case 4:
			return "naga";
		case 5:
			return "hostiles";
		case 6:
			return "passives";
		}
		return "passives";
	}

	private String raceName(final int index) {
		switch (index) {
		case -1:
			return WEString.getString("WESTRING_RACE_OTHER");
		case 0:
			return WEString.getString("WESTRING_RACE_HUMAN");
		case 1:
			return WEString.getString("WESTRING_RACE_ORC");
		case 2:
			return WEString.getString("WESTRING_RACE_UNDEAD");
		case 3:
			return WEString.getString("WESTRING_RACE_NIGHTELF");
		case 4:
			return WEString.getString("WESTRING_RACE_NEUTRAL_NAGA");
		case 5:
			return WEString.getString("WESTRING_NEUTRAL_HOSTILE");
		case 6:
			return WEString.getString("WESTRING_NEUTRAL_PASSIVE");
		}
		return WEString.getString("WESTRING_RACE_NEUTRALS");
	}

	public void sortRaces() {
		if (sortedRaces == null) {
			sortedRaces = new HashMap<>();

			for (int i = 0; i < 7; i++) {
				sortedRaces.put(raceKey(i) + "melee", new RaceData());
				sortedRaces.put(raceKey(i) + "campaign", new RaceData());
				sortedRaces.put(raceKey(i) + "custom", new RaceData());
			}

			final WarcraftObject root = (WarcraftObject) unitData.get("Aroo");
			final WarcraftObject rootAncientProtector = (WarcraftObject) unitData.get("Aro2");
			final WarcraftObject rootAncients = (WarcraftObject) unitData.get("Aro1");
			for (String str : unitData.keySet()) {
				final String baseId = str;
				str = str.toUpperCase();
				if (str.startsWith("B") || str.startsWith("R") || str.startsWith("A") || str.startsWith("S")
						|| str.startsWith("X") || str.startsWith("M") || str.startsWith("HERO")) {
					continue;
				}

				final WarcraftObject unit = (WarcraftObject) unitData.get(baseId);
				// if( unit == null ) {
				// System.err.println(str);
				// continue;
				// }
				String raceKey = "passives";
				final String abilities = unit.getField("abilList");
				final boolean isCampaign = unit.getField("campaign").startsWith("1");
				// boolean isCustom =
				// !unit.getField("inEditor").startsWith("1");
				int sortGroupId = 0;

				for (int i = 0; i < 6; i++) {
					if (unit.getField("race").equals(raceKey(i))) {
						raceKey = raceKey(i);
					}
				}
				if (raceKey.equals("passives") && (unit.getFieldValue("hostilePal") > 0)) {
					raceKey = "hostiles";
				}

				if (unit.getField("special").startsWith("1")) {
					sortGroupId = 4;
				} else if ((unit.getId().length() > 1) && Character.isUpperCase(unit.getId().charAt(0))) {
					sortGroupId = 1;
				} else if (abilities.contains("Aroo") || abilities.contains("Aro2") || abilities.contains("Aro1")) {
					sortGroupId = 3;
				} else if (unit.getField("isbldg").startsWith("1")) {
					sortGroupId = 2;
				} else {
					sortGroupId = 0;
				}
				// sortedRaces.get(raceKey(i) + "campaign").

				final String storeKey = raceKey + (isCampaign ? "campaign" : "melee");
				// if( isCustom ) {
				// storeKey = raceKey + "custom";
				// }

				switch (sortGroupId) {
				case 0:
					sortedRaces.get(storeKey).units.add(unit);
					break;
				case 1:
					sortedRaces.get(storeKey).heroes.add(unit);
					break;
				case 2:
					sortedRaces.get(storeKey).buildings.add(unit);
					break;
				case 3:
					sortedRaces.get(storeKey).buildingsUprooted.add(unit);
					sortedRaces.get(storeKey).buildings.add(unit);
					break;
				case 4:
					sortedRaces.get(storeKey).special.add(unit);
					break;
				}
			}

			for (final String str : sortedRaces.keySet()) {
				final RaceData race = sortedRaces.get(str);
				race.sort();
			}
		}
	}

	@Override
	public void valueChanged(final TreeSelectionEvent e) {
		final DefaultMutableTreeNode o = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
		if (o.getUserObject() instanceof WarcraftObject) {
			final WarcraftObject obj = (WarcraftObject) o.getUserObject();
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

	public GameObject getSelection() {
		return currentUnit;
	}
}
