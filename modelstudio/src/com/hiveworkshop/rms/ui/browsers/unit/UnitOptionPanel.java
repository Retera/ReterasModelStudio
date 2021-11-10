package com.hiveworkshop.rms.ui.browsers.unit;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class UnitOptionPanel extends JPanel {
	public static final String TILESETS = "ABKYXJDCIFLWNOZGVQ";
	public static final int ICON_SIZE = 32;

	private final ObjectData unitData;
	private final ObjectData abilityData;
	private GameObject selection = null;
	String[] raceKeys = {"human", "orc", "undead", "nightelf", "neutrals", "naga"};

	private final JComboBox<String> raceBox;
	private final JComboBox<String> meleeBox;
	private final JComboBox<String> tilesetBox;
	private final JComboBox<String> levelBox;
	private final DefaultComboBoxModel<String> raceBoxModel;

//	private JComboBox<String> playerBox;
	// DefaultComboBoxModel<String> playerBoxModel = new
	// DefaultComboBoxModel<String>();

	private final JLabel unitsLabel;
	private final JLabel heroesLabel;
	private final JLabel buildingsLabel;
	private final JLabel buildingsUprootedLabel;
	private final JLabel specialLabel;

	private final List<UnitButton> unitButtons = new ArrayList<>();
	private final ButtonGroup buttonGroup = new ButtonGroup();

	private final JPanel buttonsPanel;
	private final JScrollPane buttonsScrollPane;

	private final boolean verticalStyle;
	private boolean firstTime = true;

	static Map<String, RaceData> sortedRaces;


	public GameObject getSelection() {
		return selection;
	}

	public UnitOptionPanel(ObjectData dataTable, ObjectData abilityData) {
		this(dataTable, abilityData, false, false);
	}

	public UnitOptionPanel(ObjectData dataTable, ObjectData abilityData, boolean hideBorder, boolean verticalStyle) {
		setLayout(new MigLayout("fill"));
		setMaximumSize(ScreenInfo.getBigWindow());
		unitData = dataTable;
		this.abilityData = abilityData;
		this.verticalStyle = verticalStyle;
		unitsLabel = new JLabel(WEString.getString("WESTRING_UNITS") + ": " + WEString.getString("WESTRING_NONE_CAPS"));
		heroesLabel = new JLabel(WEString.getString("WESTRING_UTYPE_HEROES"));
		buildingsLabel = new JLabel(WEString.getString("WESTRING_UTYPE_BUILDINGS"));
		buildingsUprootedLabel = new JLabel(WEString.getString("WESTRING_UTYPE_BUILDINGS_UPROOTED"));
		specialLabel = new JLabel(WEString.getString("WESTRING_UTYPE_SPECIAL"));

		String[] raceStrings = {"WESTRING_RACE_HUMAN", "WESTRING_RACE_ORC", "WESTRING_RACE_UNDEAD", "WESTRING_RACE_NIGHTELF", "WESTRING_RACE_NEUTRAL", "WESTRING_RACE_NEUTRAL_NAGA"};
		raceBoxModel = getBoxModelOf(raceStrings);

		String[] neutralRaceStrings = {"WESTRING_RACE_NEUTRAL", "WESTRING_RACE_NEUTRAL_NAGA"};
		DefaultComboBoxModel<String> raceBoxModelNeutral = getBoxModelOf(neutralRaceStrings);

		String[] meleeStrings = {"WESTRING_MELEE", "WESTRING_CAMPAIGN", "WESTRING_CUSTOM"};
		DefaultComboBoxModel<String> meleeBoxModel = getBoxModelOf(meleeStrings);
		meleeBoxModel.addElement(WEString.getString("WESTRING_ITEMSTATUS_HIDDEN").replace("\"", ""));


		DefaultComboBoxModel<String> tileSetBoxModel = getBoxModelOf(WE_LOC.values());

		DefaultComboBoxModel<String> levelBoxModel = new DefaultComboBoxModel<>();
		levelBoxModel.addElement(WEString.getString("WESTRING_ANYLEVEL"));
		for (int i = 0; i <= 20; i++) {
			levelBoxModel.addElement(WEString.getString("WESTRING_LEVEL") + String.format(" %d", i));
		}

		buttonsPanel = new JPanel(new MigLayout("ins 0, wrap 7"));
		buttonsScrollPane = getButtonsScrollPane();

		// playerBox = new JComboBox<String>(playerBoxModel);
		// playerBox.addActionListener(e -> relayout());
		// playerBox.setMaximumSize(new Dimension(10000,25));
		raceBox = new JComboBox<>(raceBoxModel);
		raceBox.addActionListener(e -> relayout());
		raceBox.setMaximumSize(new Dimension(10000, 25));

		meleeBox = new JComboBox<>(meleeBoxModel);
		meleeBox.addActionListener(e -> relayout());
		meleeBox.setMaximumSize(new Dimension(10000, 25));

		tilesetBox = new JComboBox<>(tileSetBoxModel);
		tilesetBox.addActionListener(e -> relayout());
		tilesetBox.setMaximumSize(new Dimension(10000, 25));

		levelBox = new JComboBox<>(levelBoxModel);
		levelBox.addActionListener(e -> relayout());
		levelBox.setMaximumSize(new Dimension(10000, 25));


		if (sortedRaces == null) {
			sortRaces();
		}

		tilesetBox.setSelectedIndex(10);
	}

	private JScrollPane getButtonsScrollPane() {
		JScrollPane buttonsScrollPane = new JScrollPane(buttonsPanel);
		buttonsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		buttonsScrollPane.setFocusable(true);
		buttonsScrollPane.setMaximumSize(ScreenInfo.getBigWindow());
//		if (hideBorder) {
//			buttonsScrollPane.setBorder(null);
//		}
		buttonsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		buttonsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		return buttonsScrollPane;
	}

	protected String raceKey(final int index) {
		String[] ugg = {"human", "orc", "undead", "nightelf", "neutrals", "naga"};
		return switch (index) {
			case -1, 0 -> "human";
			case 1 -> "orc";
			case 2 -> "undead";
			case 3 -> "nightelf";
			case 5 -> "naga";
			default -> "neutrals";
		};
	}

	protected boolean isShowLevel(String race) {
		return !Arrays.asList(raceKeys).contains(race);
//		for (String key : raceKeys) {
//			if (race.equals(key)) {
//				return false;
//			}
//		}
//		return true;
	}

	private String getRaceKey(GameObject unit) {
		String race = unit.getField("race");
		for (String key : raceKeys) {
			if (race.equals(key)) {
				return key;
			}
		}

		return "neutrals";
	}

	public static void dropRaceCache() {
		sortedRaces = null;
	}

	public void sortRaces() {
		sortedRaces = new HashMap<>();

//		for (int i = 0; i < 6; i++) {
//			String raceKey = raceKey(i);
//			sortedRaces.put(raceKey + "melee", new RaceData());
//			sortedRaces.put(raceKey + "campaign", new RaceData());
//			sortedRaces.put(raceKey + "custom", new RaceData());
//			sortedRaces.put(raceKey + "hidden", new RaceData());
//		}
		for (String raceKey : raceKeys) {
			sortedRaces.put(raceKey + "melee", new RaceData());
			sortedRaces.put(raceKey + "campaign", new RaceData());
			sortedRaces.put(raceKey + "custom", new RaceData());
			sortedRaces.put(raceKey + "hidden", new RaceData());
		}

		GameObject root = abilityData.get("Aroo");
		GameObject rootAncientProtector = abilityData.get("Aro2");
		GameObject rootAncients = abilityData.get("Aro1");

		for (String str : unitData.keySet()) {
			String strUpper = str.toUpperCase();
			if (strUpper.startsWith("B")
					|| strUpper.startsWith("R")
					|| strUpper.startsWith("A")
					|| strUpper.startsWith("S")
					|| strUpper.startsWith("X")
					|| strUpper.startsWith("M")
					|| strUpper.startsWith("HERO")) {
				continue;
			}

			GameObject unit = unitData.get(str);

			int sortGroupId = getSortGroupId(root, rootAncientProtector, rootAncients, unit);

			sortedRaces.get(getStoreKey(unit)).addUnitToCorrectList(unit, sortGroupId);
		}

//		for (String str : sortedRaces.keySet()) {
//			RaceData race = sortedRaces.get(str);
//			race.sort();
//		}
		for (RaceData race : sortedRaces.values()) {
			race.sort();
		}
	}

	private String getStoreKey(GameObject unit) {
		String raceKey = getRaceKey(unit);
		if (unit.getField("JWC3_IS_CUSTOM_UNIT").startsWith("1")) {
			return raceKey + "custom";
		} else if (!unit.getField("inEditor").startsWith("1")) {
			return raceKey + "hidden";
		} else if (unit.getField("campaign").startsWith("1")) {
			return raceKey + "campaign";
		} else {
			return raceKey + "melee";
		}
	}

	private int getSortGroupId(GameObject root, GameObject rootAncientProtector, GameObject rootAncients, GameObject unit) {
		List<? extends GameObject> abilities = unit.getFieldAsList("abilList", abilityData);// .abilities();

		if (unit.getField("special").startsWith("1")) {
			return 4;
		} else if (unit.getId().length() > 1 && Character.isUpperCase(unit.getId().charAt(0))) {
			return 1;
		} else if (abilities.contains(root)
				|| abilities.contains(rootAncients)
				|| abilities.contains(rootAncientProtector)) {
			return 3;
		} else if (unit.getField("isbldg").startsWith("1")) {
			return 2;
		} else {
			return 0;
		}
	}

	private String raceKey() {
		int selectedIndex = raceBox.getSelectedIndex();
		if (raceBox.getModel() == raceBoxModel) {
			if (0 <= selectedIndex && selectedIndex < raceKeys.length) {
				return raceKeys[selectedIndex];

			} else {
				return raceKeys[0];
			}
		} else if (selectedIndex == 1) {
			return "naga";
		}
		return "neutrals";
	}

	public void relayout() {

		removeAll();
		buttonsPanel.removeAll();

		String race = raceKey();
		String tileSet = TILESETS.charAt(tilesetBox.getSelectedIndex()) + "";
		boolean isNeutral = race.equals("neutrals");
		boolean checkLevel = levelBox.getSelectedIndex() > 0 && isNeutral;

		buttonGroup.clearSelection();
		for (UnitButton ub : unitButtons) {
			buttonGroup.remove(ub);
		}
		unitButtons.clear();

		RaceData data = sortedRaces.get(race + (meleeBox.getSelectedIndex() == 0 ? "melee" : "campaign"));
		if (meleeBox.getSelectedIndex() == 2) {
			data = sortedRaces.get(race + "custom");
		}
		if (meleeBox.getSelectedIndex() == 3) {
			data = sortedRaces.get(race + "hidden");
		}

		boolean neutrals = race.equals("neutrals");
		tilesetBox.setVisible(neutrals);
		levelBox.setVisible(neutrals);
		add(raceBox, "growx, gapx 4, split 2, span");
		add(meleeBox, "growx, wrap");
		if (verticalStyle) {
			add(tilesetBox, "growx, gapx 4, split 2, span");
			add(levelBox, "growx, gapy 4, wrap");
		} else if (neutrals) {
			add(tilesetBox, "growx, gapx 4, split 2, span");
			add(levelBox, "growx, gapy 4, wrap");
		}

		add(unitsLabel, "growx, span, wrap");
		add(buttonsScrollPane, "grow, span");

		int scrollbarWith = buttonsScrollPane.getVerticalScrollBar().getWidth();
		System.out.println("scrollbarWith: " + scrollbarWith);
		int rowLength = Math.max(1, (buttonsScrollPane.getWidth() - scrollbarWith) / 32);
		if (firstTime) {
			rowLength = 8;
			firstTime = false;
		}
//		System.out.println(rowLength);

		buttonsPanel.setLayout(new MigLayout("wrap " + (rowLength - 1)));
		buttonsScrollPane.setMaximumSize(ScreenInfo.getBigWindow());

		fillWithButtons(tileSet, isNeutral, checkLevel, data.units);
		addComponents(tileSet, isNeutral, checkLevel, data.heroes, heroesLabel);
		addComponents(tileSet, isNeutral, checkLevel, data.buildings, buildingsLabel);
		addComponents(tileSet, isNeutral, checkLevel, data.buildingsUprooted, buildingsUprootedLabel);
		addComponents(tileSet, isNeutral, checkLevel, data.special, specialLabel);

		revalidate();
	}

	public void addComponents(String tileSet, boolean isNeutral, boolean checkLevel, List<GameObject> gameObjects, JLabel label) {
		for (GameObject unit : gameObjects) {
			if (isValid(tileSet, isNeutral, checkLevel, unit)) {
				buttonsPanel.add(label, "newline 12, span, wrap 4");
				break;
			}
//			else {
//				// System.err.println(unit.getField("Name") + " failed for tilset");
//				// System.err.println(unit.getField("Name") + " failed for level");
//			}
		}
		if (gameObjects.size() > 0) {
			fillWithButtons(tileSet, isNeutral, checkLevel, gameObjects);
		}
	}

	private boolean isValid(String tileSet, boolean isNeutral, boolean checkLevel, GameObject unit) {
		boolean b1 = isNeutral
				&& !unit.getField("tilesets").contains(tileSet)
				&& !unit.getField("tilesets").contains("*")
				&& !unit.getField("tilesets").contains("_");
		boolean level = checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1;
		return !(b1 || level);
	}

	public void fillWithButtons(String tileSet, boolean isNeutral, boolean checkLevel, List<GameObject> gameObjects) {

		for (GameObject unit : gameObjects) {
			if (isValid(tileSet, isNeutral, checkLevel, unit)) {
//				UnitButton unitButton1 = new UnitButton(this, unit);
				UnitButton unitButton = new UnitButton(this::unitChosen, isShowLevel(unit.getField("race")), unit);
				buttonGroup.add(unitButton);
				unitButtons.add(unitButton);
				buttonsPanel.add(unitButton);
			}
		}
	}

	public void unitChosen(UnitButton button) {
		for (UnitButton btn : unitButtons) {
			btn.setEnabled(true);
		}
		if (unitButtons.contains(button)) {
			button.setEnabled(false);
			String name = button.getUnit().getName();

			String race = button.getUnit().getField("race");
			if (isShowLevel(race)) {
				name += " - " + WEString.getString("WESTRING_LEVEL") + " " + button.getUnit().getFieldValue("level");
			}
			unitsLabel.setText(WEString.getString("WESTRING_UNITS") + ": " + name);

			selection = button.getUnit();
		} else {
			selection = null;
			unitsLabel.setText(WEString.getString("WESTRING_UNITS") + ": " + WEString.getString("WESTRING_NONE_CAPS"));
		}
	}


//	@Override
//	public void actionPerformed(ActionEvent e) {
//		relayout();
//	}

	private void fillModel(DefaultComboBoxModel<String> boxModel, String[] strings) {
		for (String string : strings) {
			boxModel.addElement(WEString.getString(string));
		}
//		for (WE_LOC loc : WE_LOC.values()) {
//			boxModel.addElement(WEString.getString(loc.getString()));
//		}
	}

	private DefaultComboBoxModel<String> getBoxModelOf(String[] strings) {
		DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<>();
		for (String string : strings) {
			boxModel.addElement(WEString.getString(string));
		}
		return boxModel;
	}
	private DefaultComboBoxModel<String> getBoxModelOf(Enum<?>[] e) {
		DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<>();
		for (Object o : e) {
			boxModel.addElement(WEString.getString(o.toString()));
		}
		return boxModel;
//		for (WE_LOC loc : WE_LOC.values()) {
//			boxModel.addElement(WEString.getString(loc.getString()));
//		}
	}
	private DefaultComboBoxModel fillModel1(DefaultComboBoxModel<String> boxModel, String[] strings) {
//		for (String string : strings) {
//			boxModel.addElement(WEString.getString(string));
//		}
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
		for (WE_LOC loc : WE_LOC.values()) {
			model.addElement(WEString.getString(loc.getString()));
		}
		return model;
	}

}
