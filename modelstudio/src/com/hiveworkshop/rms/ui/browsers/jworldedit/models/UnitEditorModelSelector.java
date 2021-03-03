package com.hiveworkshop.rms.ui.browsers.jworldedit.models;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData.WarcraftData;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData.WarcraftObject;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.WarcraftObjectTreeCellRenderer;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.UnitComparator;
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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

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

		final DefaultMutableTreeNode standardUnitsFolder = new DefaultMutableTreeNode(WEString.getString("WESTRING_UE_STANDARDUNITS"));
		final DefaultMutableTreeNode customUnitsFolder = new DefaultMutableTreeNode(WEString.getString("WESTRING_UE_CUSTOMUNITS"));
		sortRaces();

		for (int i = 0; i < 7; i++) {
			final String race = raceName(i);
			final String raceKey = raceKey(i);
			final DefaultMutableTreeNode humanFolder = new DefaultMutableTreeNode(race);
			final DefaultMutableTreeNode humanMeleeFolder = new DefaultMutableTreeNode(WEString.getString("WESTRING_MELEE"));
			final DefaultMutableTreeNode humanCampFolder = new DefaultMutableTreeNode(WEString.getString("WESTRING_CAMPAIGN"));
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

		final JScrollPane treePane;
		setLeftComponent(treePane = new JScrollPane(tree));
		final JPanel temp = new JPanel();
		temp.add(debugLabel);

		// TODO null prefs
		modelPanel = new PerspDisplayPanel("blank", modelDisp, new ProgramPreferences());
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
		return switch (cat.toLowerCase()) {
			case "abil" -> WEString.getString("WESTRING_OE_CAT_ABILITIES").replace("&", "");
			case "art" -> WEString.getString("WESTRING_OE_CAT_ART").replace("&", "");
			case "combat" -> WEString.getString("WESTRING_OE_CAT_COMBAT").replace("&", "");
			case "data" -> WEString.getString("WESTRING_OE_CAT_DATA").replace("&", "");
			case "editor" -> WEString.getString("WESTRING_OE_CAT_EDITOR").replace("&", "");
			case "move" -> WEString.getString("WESTRING_OE_CAT_MOVEMENT").replace("&", "");
			case "path" -> WEString.getString("WESTRING_OE_CAT_PATHING").replace("&", "");
			case "sound" -> WEString.getString("WESTRING_OE_CAT_SOUND").replace("&", "");
			case "stats" -> WEString.getString("WESTRING_OE_CAT_STATS").replace("&", "");
			case "tech" -> WEString.getString("WESTRING_OE_CAT_TECHTREE").replace("&", "");
			case "text" -> WEString.getString("WESTRING_OE_CAT_TEXT").replace("&", "");
			default -> WEString.getString("WESTRING_UNKNOWN");
		};
	}

	public void fillTable() {
		if (currentUnit == null) {
			currentUnit = unitData.get("hpea");
		}
		if (currentUnit != null) {
			String filepath = currentUnit.getField("file");

			ModelView modelDisp = null;

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
				e.printStackTrace();
				// bad model!
				JOptionPane.showMessageDialog(getParent(), "The chosen model could not be used.", "Program Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	public void loadHotkeys() {
		final JRootPane root = getRootPane();
		getRootPane().getActionMap().put("displayAsRawData", new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				settings.setDisplayAsRawData(!settings.isDisplayAsRawData());
				final Enumeration<TreeNode> enumeration = UnitEditorModelSelector.this.root.breadthFirstEnumeration();
				while (enumeration.hasMoreElements()) {
					model.nodeChanged(enumeration.nextElement());
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

	}

	public void loadRaceData(final DefaultMutableTreeNode folder, final RaceData data) {
		fetchAndAddRaceData(folder, "WESTRING_UNITS", data.units);

		fetchAndAddRaceData(folder, "WESTRING_UTYPE_BUILDINGS", data.buildings);

		fetchAndAddRaceData(folder, "WESTRING_UTYPE_HEROES", data.heroes);

		fetchAndAddRaceData(folder, "WESTRING_UTYPE_SPECIAL", data.special);
	}

	private void fetchAndAddRaceData(DefaultMutableTreeNode folder, String weString, List<WarcraftObject> objects) {
		final DefaultMutableTreeNode units = new DefaultMutableTreeNode(WEString.getString(weString));
		for (final WarcraftObject u : objects) {
			units.add(new DefaultMutableTreeNode(u));
		}
		if (objects.size() > 0) {
			folder.add(units);
			if (defaultSelection == null) {
				defaultSelection = units.getFirstLeaf();
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

	static Map<String, RaceData> sortedRaces;

	private String raceKey(final int index) {
		return switch (index) {
			case -1, 0 -> "human";
			case 1 -> "orc";
			case 2 -> "undead";
			case 3 -> "nightelf";
			case 4 -> "naga";
			case 5 -> "hostiles";
			case 6 -> "passives";
			default -> "passives";
		};
	}

	private String raceName(final int index) {
		return switch (index) {
			case -1 -> WEString.getString("WESTRING_RACE_OTHER");
			case 0 -> WEString.getString("WESTRING_RACE_HUMAN");
			case 1 -> WEString.getString("WESTRING_RACE_ORC");
			case 2 -> WEString.getString("WESTRING_RACE_UNDEAD");
			case 3 -> WEString.getString("WESTRING_RACE_NIGHTELF");
			case 4 -> WEString.getString("WESTRING_RACE_NEUTRAL_NAGA");
			case 5 -> WEString.getString("WESTRING_NEUTRAL_HOSTILE");
			case 6 -> WEString.getString("WESTRING_NEUTRAL_PASSIVE");
			default -> WEString.getString("WESTRING_RACE_NEUTRALS");
		};
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
				String raceKey = "passives";
				final String abilities = unit.getField("abilList");
				final boolean isCampaign = unit.getField("campaign").startsWith("1");
				// boolean isCustom = !unit.getField("inEditor").startsWith("1");

				for (int i = 0; i < 6; i++) {
					if (unit.getField("race").equals(raceKey(i))) {
						raceKey = raceKey(i);
					}
				}
				if (raceKey.equals("passives") && (unit.getFieldValue("hostilePal") > 0)) {
					raceKey = "hostiles";
				}

				int sortGroupId = 0;
				if (unit.getField("special").startsWith("1")) {
					sortGroupId = 4;
				} else if ((unit.getId().length() > 1) && Character.isUpperCase(unit.getId().charAt(0))) {
					sortGroupId = 1;
				} else if (abilities.contains("Aroo") || abilities.contains("Aro2") || abilities.contains("Aro1")) {
					sortGroupId = 3;
				} else if (unit.getField("isbldg").startsWith("1")) {
					sortGroupId = 2;
				}
				// sortedRaces.get(raceKey(i) + "campaign").

				final String storeKey = raceKey + (isCampaign ? "campaign" : "melee");
				// if( isCustom ) {
				// storeKey = raceKey + "custom";
				// }

				switch (sortGroupId) {
					case 0 -> sortedRaces.get(storeKey).units.add(unit);
					case 1 -> sortedRaces.get(storeKey).heroes.add(unit);
					case 2 -> sortedRaces.get(storeKey).buildings.add(unit);
					case 3 -> {
						sortedRaces.get(storeKey).buildingsUprooted.add(unit);
						sortedRaces.get(storeKey).buildings.add(unit);
					}
					case 4 -> sortedRaces.get(storeKey).special.add(unit);
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
