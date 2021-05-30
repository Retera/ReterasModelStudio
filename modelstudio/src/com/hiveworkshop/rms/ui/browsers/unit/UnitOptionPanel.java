package com.hiveworkshop.rms.ui.browsers.unit;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.parsers.slk.GameObjectComparator;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

public class UnitOptionPanel extends JPanel implements ActionListener {
	public static final String TILESETS = "ABKYXJDCIFLWNOZGVQ";
	public static final int ICON_SIZE = 32;

	ObjectData unitData;
	final ObjectData abilityData;
	GameObject selection = null;

	JComboBox<String> raceBox, meleeBox, tilesetBox, levelBox;// playerBox,
	// DefaultComboBoxModel<String> playerBoxModel = new
	// DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> raceBoxModel = new DefaultComboBoxModel<>();
	DefaultComboBoxModel<String> raceBoxModelNeutral = new DefaultComboBoxModel<>();
	DefaultComboBoxModel<String> meleeBoxModel = new DefaultComboBoxModel<>();
	DefaultComboBoxModel<String> tilesetBoxModel = new DefaultComboBoxModel<>();
	DefaultComboBoxModel<String> levelBoxModel = new DefaultComboBoxModel<>();

	JLabel unitsLabel, heroesLabel, buildingsLabel, buildingsUprootedLabel, specialLabel;

	List<UnitButton> unitButtons = new ArrayList<>();
	ButtonGroup buttonGroup = new ButtonGroup();

	JPanel buttonsPanel;
	JScrollPane buttonsScrollPane;

	private final boolean verticalStyle;

	public GameObject getSelection() {
		return selection;
	}

	boolean firstTime = true;

	public UnitOptionPanel(final ObjectData dataTable, final ObjectData abilityData) {
		this(dataTable, abilityData, false, false);
	}

	public UnitOptionPanel(final ObjectData dataTable, final ObjectData abilityData, final boolean hideBorder, final boolean verticalStyle) {
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
		fillModel(raceBoxModel, raceStrings);

		String[] neutralRaceStrings = {"WESTRING_RACE_NEUTRAL", "WESTRING_RACE_NEUTRAL_NAGA"};
		fillModel(raceBoxModelNeutral, neutralRaceStrings);

		String[] meleeStrings = {"WESTRING_MELEE", "WESTRING_CAMPAIGN", "WESTRING_CUSTOM"};
		fillModel(meleeBoxModel, meleeStrings);
		meleeBoxModel.addElement(WEString.getString("WESTRING_ITEMSTATUS_HIDDEN").replace("\"", ""));

		String[] tilesetStrings = {"WESTRING_LOCALE_ASHENVALE", "WESTRING_LOCALE_BARRENS",
				"WESTRING_LOCALE_BLACKCITADEL", "WESTRING_LOCALE_CITYSCAPE",
				"WESTRING_LOCALE_DALARAN", "WESTRING_LOCALE_DALARANRUINS",
				"WESTRING_LOCALE_DUNGEON", "WESTRING_LOCALE_FELWOOD",
				"WESTRING_LOCALE_ICECROWN", "WESTRING_LOCALE_LORDAERON_FALL",
				"WESTRING_LOCALE_LORDAERON_SUMMER", "WESTRING_LOCALE_LORDAERON_WINTER",
				"WESTRING_LOCALE_NORTHREND", "WESTRING_LOCALE_OUTLAND",
				"WESTRING_LOCALE_RUINS", "WESTRING_LOCALE_DUNGEON2",
				"WESTRING_LOCALE_VILLAGE", "WESTRING_LOCALE_VILLAGEFALL"};
		fillModel(tilesetBoxModel, tilesetStrings);

		levelBoxModel.addElement(WEString.getString("WESTRING_ANYLEVEL"));
		for (int i = 0; i <= 20; i++) {
			levelBoxModel.addElement(WEString.getString("WESTRING_LEVEL") + String.format(" %d", i));
		}

		buttonsPanel = new JPanel(new MigLayout("ins 0, wrap 7"));
		buttonsScrollPane = new JScrollPane(buttonsPanel);
		buttonsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		buttonsScrollPane.setFocusable(true);
		buttonsScrollPane.setMaximumSize(ScreenInfo.getBigWindow());
//		if (hideBorder) {
//			buttonsScrollPane.setBorder(null);
//		}
		buttonsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		buttonsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// playerBox = new JComboBox<String>(playerBoxModel);
		// playerBox.addActionListener(this);
		// playerBox.setMaximumSize(new Dimension(10000,25));
		raceBox = new JComboBox<>(raceBoxModel);
		raceBox.addActionListener(this);
		raceBox.setMaximumSize(new Dimension(10000, 25));

		meleeBox = new JComboBox<>(meleeBoxModel);
		meleeBox.addActionListener(this);
		meleeBox.setMaximumSize(new Dimension(10000, 25));

		tilesetBox = new JComboBox<>(tilesetBoxModel);
		tilesetBox.addActionListener(this);
		tilesetBox.setMaximumSize(new Dimension(10000, 25));

		levelBox = new JComboBox<>(levelBoxModel);
		levelBox.addActionListener(this);
		levelBox.setMaximumSize(new Dimension(10000, 25));

		sortRaces();

		tilesetBox.setSelectedIndex(10);
	}

	private String raceKey(final int index) {
		return switch (index) {
			case -1, 0 -> "human";
			case 1 -> "orc";
			case 2 -> "undead";
			case 3 -> "nightelf";
			case 5 -> "naga";
			default -> "neutrals";
		};
	}

	static class RaceData {
		List<GameObject> units = new ArrayList<>();
		List<GameObject> heroes = new ArrayList<>();
		List<GameObject> buildings = new ArrayList<>();
		List<GameObject> buildingsUprooted = new ArrayList<>();
		List<GameObject> special = new ArrayList<>();

		void sort() {
			final Comparator<GameObject> unitComp = new GameObjectComparator();

			units.sort(unitComp);
			heroes.sort(unitComp);
			buildings.sort(unitComp);
			buildingsUprooted.sort(unitComp);
			special.sort(unitComp);
		}
	}

	static Map<String, RaceData> sortedRaces;

	public static void dropRaceCache() {
		sortedRaces = null;
	}

	public void sortRaces() {
		if (sortedRaces == null) {
			sortedRaces = new HashMap<>();

			for (int i = 0; i < 6; i++) {
				sortedRaces.put(raceKey(i) + "melee", new RaceData());
				sortedRaces.put(raceKey(i) + "campaign", new RaceData());
				sortedRaces.put(raceKey(i) + "custom", new RaceData());
				sortedRaces.put(raceKey(i) + "hidden", new RaceData());
			}

			final GameObject root = abilityData.get("Aroo");
			final GameObject rootAncientProtector = abilityData.get("Aro2");
			final GameObject rootAncients = abilityData.get("Aro1");
			for (final String str : unitData.keySet()) {
				final String strUpper = str.toUpperCase();
				if (strUpper.startsWith("B")
						|| strUpper.startsWith("R")
						|| strUpper.startsWith("A")
						|| strUpper.startsWith("S")
						|| strUpper.startsWith("X")
						|| strUpper.startsWith("M")
						|| strUpper.startsWith("HERO")) {
					continue;
				}

				final GameObject unit = unitData.get(str);
				String raceKey = "neutrals";
				final List<? extends GameObject> abilities = unit.getFieldAsList("abilList", abilityData);// .abilities();
				final boolean isCampaign = unit.getField("campaign").startsWith("1");
				final boolean isCustom = unit.getField("JWC3_IS_CUSTOM_UNIT").startsWith("1");
				final boolean isHidden = !unit.getField("inEditor").startsWith("1");
				int sortGroupId = 0;

				for (int i = 0; i < 6; i++) {
					if (unit.getField("race").equals(raceKey(i))) {
						raceKey = raceKey(i);
					}
				}

				if (unit.getField("special").startsWith("1")) {
					sortGroupId = 4;
				} else if (unit.getId().length() > 1 && Character.isUpperCase(unit.getId().charAt(0))) {
					sortGroupId = 1;
				} else if (abilities.contains(root)
						|| abilities.contains(rootAncients)
						|| abilities.contains(rootAncientProtector)) {
					sortGroupId = 3;
				} else if (unit.getField("isbldg").startsWith("1")) {
					sortGroupId = 2;
				} else {
					sortGroupId = 0;
				}
				// sortedRaces.get(raceKey(i) + "campaign").

				String storeKey = raceKey + (isCampaign ? "campaign" : "melee");
				if (isHidden) {
					storeKey = raceKey + "hidden";
				}
				if (isCustom) {
					storeKey = raceKey + "custom";
				}

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

	private String raceKey() {
		if (raceBox.getModel() == raceBoxModel) {
			return raceKey(raceBox.getSelectedIndex());
		} else if (raceBox.getSelectedIndex() == 1) {
			return "naga";
		}
		return "neutrals";
	}

	public void relayout() {

		removeAll();
		buttonsPanel.removeAll();

		final String race = raceKey();
		final String tileset = TILESETS.charAt(tilesetBox.getSelectedIndex()) + "";
		final boolean isNeutral = race.equals("neutrals");
		final boolean checkLevel = levelBox.getSelectedIndex() > 0 && isNeutral;

		buttonGroup.clearSelection();
		for (final UnitButton ub : unitButtons) {
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

		final boolean neutrals = race.equals("neutrals");
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

		fillWithButtons(tileset, isNeutral, checkLevel, data.units);
		addComponents(tileset, isNeutral, checkLevel, data.heroes, heroesLabel);
		addComponents(tileset, isNeutral, checkLevel, data.buildings, buildingsLabel);
		addComponents(tileset, isNeutral, checkLevel, data.buildingsUprooted, buildingsUprootedLabel);
		addComponents(tileset, isNeutral, checkLevel, data.special, specialLabel);

		revalidate();
	}

	public void addComponents(String tileset, boolean isNeutral, boolean checkLevel, List<GameObject> gameObjects, JLabel label) {
		boolean good = false;
		for (final GameObject unit : gameObjects) {
			if (isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_")) {
				// System.err.println(unit.getField("Name") + " failed for tilset");
				continue;
			}
			if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
				// System.err.println(unit.getField("Name") + " failed for level");
				continue;
			}
			// if( isPassive && unit.getField("hostilePal").startsWith("1") ) {continue;}
			// if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {continue;}
			good = true;
			break;
		}
		if (gameObjects.size() > 0 && good) {
			buttonsPanel.add(label, "newline 12, span, wrap 4");

			fillWithButtons(tileset, isNeutral, checkLevel, gameObjects);
		}
	}

	public void fillWithButtons(String tileset, boolean isNeutral, boolean checkLevel, List<GameObject> gameObjects) {

		for (final GameObject unit : gameObjects) {
			if (isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_")) {
				// System.err.println(unit.getField("Name") + " failed for tilset");
				continue;
			}
			if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
				// System.err.println(unit.getField("Name") + " failed for level");
				continue;
			}

			final UnitButton unitButton = new UnitButton(unit);
			unitButtons.add(unitButton);
			buttonsPanel.add(unitButton);
		}
	}

	public void unitChosen(UnitButton button) {
		for (final UnitButton btn : unitButtons) {
			btn.setEnabled(true);
		}
		if (unitButtons.contains(button)) {
			button.setEnabled(false);
			String name = button.getUnit().getName();

			final String race = button.getUnit().getField("race");
			boolean showLevel = true;
			for (int i = 0; i < 6; i++) {
				if (race.equals(raceKey(i))) {
					showLevel = false;
					break;
				}
			}
			if (showLevel) {
				name += " - " + WEString.getString("WESTRING_LEVEL") + " " + button.getUnit().getFieldValue("level");
			}
			unitsLabel.setText(WEString.getString("WESTRING_UNITS") + ": " + name);

			selection = button.getUnit();
		} else {
			selection = null;
			unitsLabel.setText(WEString.getString("WESTRING_UNITS") + ": " + WEString.getString("WESTRING_NONE_CAPS"));
		}
	}


	@Override
	public void actionPerformed(final ActionEvent e) {
		relayout();
	}

	private void fillModel(DefaultComboBoxModel<String> boxModel, String[] strings) {
		for (String string : strings) {
			boxModel.addElement(WEString.getString(string));
		}
	}

	class UnitButton extends JButton {
		GameObject unit;

		public UnitButton(final GameObject u) {
			super(u.getScaledIcon(ICON_SIZE));

			setFocusable(false);
			unit = u;
			String uberTip = unit.getField("Ubertip");
			if (uberTip.length() < 1) {
				uberTip = unit.getField("UberTip");
			}
			if (uberTip.length() < 1) {
				uberTip = unit.getField("uberTip");
			}
			uberTip = uberTip.replace("|n", "<br>");
			uberTip = uberTip.replace("|cffffcc00", "");
			uberTip = uberTip.replace("|r", "");

			StringBuilder newUberTip = new StringBuilder();
			int depth = 0;
			for (int i = 0; i < uberTip.length(); i++) {
				final char c = uberTip.charAt(i);
				if (c == '<' && uberTip.length() > i + 4 && uberTip.startsWith("<br>", i)) {
					i += 3;
					depth = 0;
					newUberTip.append("<br>");
				} else {
					if (depth > 80 && c == ' ') {
						depth = 0;
						newUberTip.append("<br>");
					}
					newUberTip.append(c);
					depth++;
				}
			}

			uberTip = newUberTip.toString();
			String name = unit.getName();
			// if( unit.getField("campaign").startsWith("1") && Character.isUpperCase(unit.getUnitId().charAt(0)) ) {
			// name = unit.getField("Propernames");
			// if( name.contains(",") ) {name = name.split(",")[0]; }}
			final String race = unit.getField("race");
			boolean showLevel = true;
			for (int i = 0; i < 6; i++) {
				if (race.equals(raceKey(i))) {
					showLevel = false;
					break;
				}
			}
			// if( unit.getField("EditorSuffix").length() > 0 )
			// name += " " + unit.getField("EditorSuffix");
			if (showLevel) {
				name += " - " + WEString.getString("WESTRING_LEVEL") + " " + unit.getFieldValue("level");
			} // unit.getUnitId() + "<br>" +
			if (uberTip.length() > 0) {
				uberTip = "<html>" + name + "<br>--<br>" + uberTip + "</html>";
			} else {
				uberTip = name;
			}
			setToolTipText(uberTip);
			buttonGroup.add(this);
			addActionListener(e -> unitChosen(this));
			setDisabledIcon(unit.getScaledTintedIcon(Color.green, ICON_SIZE));
			setMargin(new Insets(0, 0, 0, 0));
			setBorder(null);
		}

		public GameObject getUnit() {
			return unit;
		}

		@Override
		protected void paintComponent(final Graphics g) {
			if (!isEnabled()) {
				g.translate(1, 1);
			}
			super.paintComponent(g);
			if (!isEnabled()) {
				g.translate(-1, -1);
				final Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(Color.GRAY);
				for (int i = 0; i < 2; i++) {
					g2.setColor(g2.getColor().brighter());
					g2.draw3DRect(i, i, getWidth() - i * 2 - 1, getHeight() - i * 2 - 1, false);
				}
				g2.dispose();
			}
		}
	}
}
