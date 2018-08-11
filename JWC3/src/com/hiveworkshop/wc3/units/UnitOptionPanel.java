package com.hiveworkshop.wc3.units;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.hiveworkshop.wc3.resources.WEString;

public class UnitOptionPanel extends JPanel implements ActionListener {
	public static final String TILESETS = "ABKYXJDCIFLWNOZGVQ";

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

	ActionListener buttonListener = new ActionListener() {
		@Override
		public void actionPerformed(final ActionEvent e) {
			boolean found = false;
			for (final UnitButton btn : unitButtons) {
				if (e.getSource() == btn) {
					// do stuff
					// btn.setBackground(Color.green);
					btn.setEnabled(false);
					found = true;
					String name = btn.getUnit().getName();

					final String race = btn.getUnit().getField("race");
					boolean showLevel = true;
					for (int i = 0; i < 6; i++) {
						if (race.equals(raceKey(i))) {
							showLevel = false;
						}
					}
					if (showLevel) {
						name += " - " + WEString.getString("WESTRING_LEVEL") + " "
								+ btn.getUnit().getFieldValue("level");
					} // unit.getUnitId() + "<br>" +
					unitsLabel.setText(WEString.getString("WESTRING_UNITS") + ": " + name);

					selection = btn.getUnit();
				} else {
					btn.setEnabled(true);
					// btn.setBackground(null);
				}
			}
			if (!found) {
				selection = null;
				unitsLabel.setText(
						WEString.getString("WESTRING_UNITS") + ": " + WEString.getString("WESTRING_NONE_CAPS"));
			}
		}
	};

	private final int borderGapAmount;

	private final boolean verticalStyle;

	public GameObject getSelection() {
		return selection;
	}

	public UnitOptionPanel(final ObjectData dataTable, final ObjectData abilityData) {
		this(dataTable, abilityData, 12, false, false);
	}

	public UnitOptionPanel(final ObjectData dataTable, final ObjectData abilityData, final int borderGapAmount,
			final boolean hideBorder, final boolean verticalStyle) {
		unitData = dataTable;
		this.abilityData = abilityData;
		this.borderGapAmount = borderGapAmount;
		this.verticalStyle = verticalStyle;
		unitsLabel = new JLabel(WEString.getString("WESTRING_UNITS") + ": " + WEString.getString("WESTRING_NONE_CAPS"));
		heroesLabel = new JLabel(WEString.getString("WESTRING_UTYPE_HEROES"));
		buildingsLabel = new JLabel(WEString.getString("WESTRING_UTYPE_BUILDINGS"));
		buildingsUprootedLabel = new JLabel(WEString.getString("WESTRING_UTYPE_BUILDINGS_UPROOTED"));
		specialLabel = new JLabel(WEString.getString("WESTRING_UTYPE_SPECIAL"));

		// for( int i = 0; i < 12; i++ ) {
		// playerBoxModel.addElement(WEString.getString("WESTRING_PLAYER_" +
		// String.format("%2d",i).replace(" ", "0")).replace("\"", ""));
		// }
		// playerBoxModel.addElement(WEString.getString("WESTRING_PLAYER_NA").replace("\"",
		// ""));
		// playerBoxModel.addElement(WEString.getString("WESTRING_PLAYER_NP").replace("\"",
		// ""));

		raceBoxModel.addElement(WEString.getString("WESTRING_RACE_HUMAN"));
		raceBoxModel.addElement(WEString.getString("WESTRING_RACE_ORC"));
		raceBoxModel.addElement(WEString.getString("WESTRING_RACE_UNDEAD"));
		raceBoxModel.addElement(WEString.getString("WESTRING_RACE_NIGHTELF"));
		raceBoxModel.addElement(WEString.getString("WESTRING_RACE_NEUTRAL"));
		raceBoxModel.addElement(WEString.getString("WESTRING_RACE_NEUTRAL_NAGA"));

		raceBoxModelNeutral.addElement(WEString.getString("WESTRING_RACE_NEUTRAL"));
		raceBoxModelNeutral.addElement(WEString.getString("WESTRING_RACE_NEUTRAL_NAGA"));

		meleeBoxModel.addElement(WEString.getString("WESTRING_MELEE"));
		meleeBoxModel.addElement(WEString.getString("WESTRING_CAMPAIGN"));
		meleeBoxModel.addElement(WEString.getString("WESTRING_CUSTOM"));
		meleeBoxModel.addElement(WEString.getString("WESTRING_ITEMSTATUS_HIDDEN").replace("\"", ""));

		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_ASHENVALE"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_BARRENS"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_BLACKCITADEL"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_CITYSCAPE"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_DALARAN"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_DALARANRUINS"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_DUNGEON"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_FELWOOD"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_ICECROWN"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_LORDAERON_FALL"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_LORDAERON_SUMMER"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_LORDAERON_WINTER"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_NORTHREND"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_OUTLAND"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_RUINS"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_DUNGEON2"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_VILLAGE"));
		tilesetBoxModel.addElement(WEString.getString("WESTRING_LOCALE_VILLAGEFALL"));

		levelBoxModel.addElement(WEString.getString("WESTRING_ANYLEVEL"));
		for (int i = 0; i <= 20; i++) {
			levelBoxModel.addElement(WEString.getString("WESTRING_LEVEL") + String.format(" %d", i));
		}

		buttonsPanel = new JPanel();
		buttonsScrollPane = new JScrollPane(buttonsPanel);
		buttonsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		if (hideBorder) {
			buttonsScrollPane.setBorder(null);
		}

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

	private String raceKey() {
		if (raceBox.getModel() == raceBoxModel) {
			switch (raceBox.getSelectedIndex()) {
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
				return "neutrals";
			case 5:
				return "naga";
			}
		} else {
			switch (raceBox.getSelectedIndex()) {
			case -1:
				return "neutrals";
			case 0:
				return "neutrals";
			case 1:
				return "naga";
			}
		}
		return "neutrals";
	}

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
			return "neutrals";
		case 5:
			return "naga";
		}
		return "neutrals";
	}

	class RaceData {
		List<GameObject> units = new ArrayList<>();
		List<GameObject> heroes = new ArrayList<>();
		List<GameObject> buildings = new ArrayList<>();
		List<GameObject> buildingsUprooted = new ArrayList<>();
		List<GameObject> special = new ArrayList<>();

		void sort() {
			final Comparator<GameObject> unitComp = new GameObjectComparator();

			Collections.sort(units, unitComp);
			Collections.sort(heroes, unitComp);
			Collections.sort(buildings, unitComp);
			Collections.sort(buildingsUprooted, unitComp);
			Collections.sort(special, unitComp);
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
				if (strUpper.startsWith("B") || strUpper.startsWith("R") || strUpper.startsWith("A")
						|| strUpper.startsWith("S") || strUpper.startsWith("X") || strUpper.startsWith("M")
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
				} else if (abilities.contains(root) || abilities.contains(rootAncients)
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

	class UnitButton extends JButton {
		GameObject unit;

		public UnitButton(final GameObject u) {
			super(u.getScaledIcon(0.5));
			setFocusable(false);
			this.unit = u;
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

			String newUberTip = "";
			int depth = 0;
			for (int i = 0; i < uberTip.length(); i++) {
				final char c = uberTip.charAt(i);
				if (c == '<' && uberTip.length() > i + 4 && uberTip.substring(i, i + 4).equals("<br>")) {
					i += 3;
					depth = 0;
					newUberTip += "<br>";
				} else {
					if (depth > 80 && c == ' ') {
						depth = 0;
						newUberTip += "<br>";
					}
					newUberTip += "" + c;
					depth++;
				}
			}

			uberTip = newUberTip;
			String name = unit.getName();
			// if( unit.getField("campaign").startsWith("1") &&
			// Character.isUpperCase(unit.getUnitId().charAt(0)) ) {
			// name = unit.getField("Propernames");
			// if( name.contains(",") ) {
			// name = name.split(",")[0];
			// }
			// }
			final String race = unit.getField("race");
			boolean showLevel = true;
			for (int i = 0; i < 6; i++) {
				if (race.equals(raceKey(i))) {
					showLevel = false;
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
			this.setToolTipText(uberTip);
			buttonGroup.add(this);
			addActionListener(buttonListener);
			this.setDisabledIcon(unit.getScaledTintedIcon(Color.green, 0.5));
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

	boolean firstTime = true;

	public void relayout() {

		removeAll();
		buttonsPanel.removeAll();

		// if( playerBox.getSelectedIndex() > 11 ) {
		// if( raceBox.getModel() != raceBoxModelNeutral )
		// raceBox.setModel(raceBoxModelNeutral);
		// }
		// else {
		// if( raceBox.getModel() != raceBoxModel )
		// raceBox.setModel(raceBoxModel);
		// }

		final String race = raceKey();
		final String tileset = TILESETS.charAt(tilesetBox.getSelectedIndex()) + "";
		final boolean isNeutral = race.equals("neutrals");
		final boolean checkLevel = levelBox.getSelectedIndex() > 0 && isNeutral;
		// boolean isHostile = playerBox.getSelectedIndex() == 12 &&
		// !race.equals("naga");
		// boolean isPassive = playerBox.getSelectedIndex() == 13 &&
		// !race.equals("naga");

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

		final GroupLayout layout = new GroupLayout(this);

		GroupLayout.Group superHorizontalGroup;
		GroupLayout.Group horizontalGroup;
		GroupLayout.Group verticalGroup;

		final boolean neutrals = race.equals("neutrals");
		tilesetBox.setVisible(neutrals);
		levelBox.setVisible(neutrals);
		if (verticalStyle) {
			layout.setHorizontalGroup(superHorizontalGroup = layout.createSequentialGroup().addGap(borderGapAmount)
					.addGroup(layout.createParallelGroup()
							// .addComponent(playerBox)
							.addGroup(layout.createSequentialGroup().addComponent(raceBox).addGap(4)
									.addComponent(meleeBox).addGap(4).addComponent(tilesetBox).addGap(4)
									.addComponent(levelBox))
							.addComponent(unitsLabel).addComponent(buttonsScrollPane))
					.addGap(borderGapAmount));

			layout.setVerticalGroup(layout.createSequentialGroup().addGap(borderGapAmount)
					// .addComponent(playerBox)
					// .addGap(4)
					.addGroup(layout.createParallelGroup().addComponent(raceBox).addComponent(meleeBox)
							.addComponent(tilesetBox).addComponent(levelBox))
					.addGap(4).addComponent(unitsLabel).addGap(4).addComponent(buttonsScrollPane)
					.addGap(borderGapAmount));
		} else if (neutrals) {
			layout.setHorizontalGroup(superHorizontalGroup = layout.createSequentialGroup().addGap(borderGapAmount)
					.addGroup(layout.createParallelGroup()
							// .addComponent(playerBox)
							.addGroup(layout.createSequentialGroup().addComponent(raceBox).addGap(4)
									.addComponent(meleeBox))
							.addGroup(layout.createSequentialGroup().addComponent(tilesetBox).addGap(4)
									.addComponent(levelBox))
							.addComponent(unitsLabel).addComponent(buttonsScrollPane))
					.addGap(borderGapAmount));

			layout.setVerticalGroup(layout.createSequentialGroup().addGap(borderGapAmount)
					// .addComponent(playerBox)
					// .addGap(4)
					.addGroup(layout.createParallelGroup().addComponent(raceBox).addComponent(meleeBox)).addGap(4)
					.addGroup(layout.createParallelGroup().addComponent(tilesetBox).addComponent(levelBox)).addGap(4)
					.addComponent(unitsLabel).addGap(4).addComponent(buttonsScrollPane).addGap(borderGapAmount));
		} else {
			layout.setHorizontalGroup(superHorizontalGroup = layout.createSequentialGroup().addGap(borderGapAmount)
					.addGroup(layout.createParallelGroup()
							// .addComponent(playerBox)
							.addGroup(layout.createSequentialGroup().addComponent(raceBox).addGap(4)
									.addComponent(meleeBox))
							.addComponent(unitsLabel).addComponent(buttonsScrollPane))
					.addGap(borderGapAmount));

			layout.setVerticalGroup(layout.createSequentialGroup().addGap(borderGapAmount)
					// .addComponent(playerBox)
					// .addGap(4)
					.addGroup(layout.createParallelGroup().addComponent(raceBox).addComponent(meleeBox)).addGap(4)
					.addComponent(unitsLabel).addGap(4).addComponent(buttonsScrollPane).addGap(borderGapAmount));
		}

		int rowLength = Math.max(1, (buttonsScrollPane.getWidth()) / 32);// (getWidth()
		// -
		// 24)
		// /
		// 32;
		if (firstTime) {
			rowLength = 7;
			firstTime = false;
		}

		final GroupLayout layout2 = new GroupLayout(buttonsPanel);
		horizontalGroup = layout2.createParallelGroup();
		verticalGroup = layout2.createSequentialGroup();

		layout2.setVerticalGroup(verticalGroup);
		layout2.setHorizontalGroup(horizontalGroup);

		GroupLayout.Group lastVertGroup = null;
		GroupLayout.Group lastHorizGroup = null;
		int i = 0;
		for (final GameObject unit : data.units) {
			// System.err.println(unit.getField("tilesets"));
			if (isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*")
					&& !unit.getField("tilesets").contains("_")) {
				// System.err.println(unit.getField("Name") + " failed for
				// tilset");
				continue;
			}
			if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
				// System.err.println(unit.getField("Name") + " failed for
				// level");
				continue;
			}
			// if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			// if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			if (i % rowLength == 0) {
				if (lastVertGroup != null && lastHorizGroup != null) {
					horizontalGroup.addGroup(lastHorizGroup);
					verticalGroup.addGroup(lastVertGroup);
				}
				lastVertGroup = layout2.createParallelGroup();
				lastHorizGroup = layout2.createSequentialGroup();
			}
			final UnitButton myButton = new UnitButton(unit);
			unitButtons.add(myButton);
			lastVertGroup.addComponent(myButton);
			lastHorizGroup.addComponent(myButton);
			i++;
		}
		if (lastVertGroup != null && lastHorizGroup != null) {
			horizontalGroup.addGroup(lastHorizGroup);
			verticalGroup.addGroup(lastVertGroup);
		}

		boolean good = false;
		for (final GameObject unit : data.heroes) {
			if (isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*")
					&& !unit.getField("tilesets").contains("_")) {
				// System.err.println(unit.getField("Name") + " failed for
				// tilset");
				continue;
			}
			if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
				// System.err.println(unit.getField("Name") + " failed for
				// level");
				continue;
			}
			// if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			// if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			good = true;
			break;
		}
		if (data.heroes.size() > 0 && good) {
			verticalGroup.addGap(12);
			verticalGroup.addComponent(heroesLabel);
			horizontalGroup.addComponent(heroesLabel);
			verticalGroup.addGap(4);

			lastVertGroup = null;
			lastHorizGroup = null;
			i = 0;
			for (final GameObject unit : data.heroes) {
				if (isNeutral && !unit.getField("tilesets").contains(tileset)
						&& !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_")) {
					// System.err.println(unit.getField("Name") + " failed for
					// tilset");
					continue;
				}
				if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
					// System.err.println(unit.getField("Name") + " failed for
					// level");
					continue;
				}
				// if( isPassive && unit.getField("hostilePal").startsWith("1")
				// ) {
				// continue;
				// }
				// if( isHostile && !unit.getField("hostilePal").startsWith("1")
				// ) {
				// continue;
				// }
				if (i % rowLength == 0) {
					if (lastVertGroup != null && lastHorizGroup != null) {
						horizontalGroup.addGroup(lastHorizGroup);
						verticalGroup.addGroup(lastVertGroup);
					}
					lastVertGroup = layout2.createParallelGroup();
					lastHorizGroup = layout2.createSequentialGroup();
				}
				final UnitButton myButton = new UnitButton(unit);
				unitButtons.add(myButton);
				lastVertGroup.addComponent(myButton);
				lastHorizGroup.addComponent(myButton);
				i++;
			}
			if (lastVertGroup != null && lastHorizGroup != null) {
				horizontalGroup.addGroup(lastHorizGroup);
				verticalGroup.addGroup(lastVertGroup);
			}
		}

		good = false;
		for (final GameObject unit : data.buildings) {
			if (isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*")
					&& !unit.getField("tilesets").contains("_")) {
				continue;
			}
			if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
				continue;
			}
			// if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			// if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			// if( isNeutral && !isPassive && !isHostile ) {
			// continue;
			// }
			good = true;
			break;
		}
		if (data.buildings.size() > 0 && good) {
			verticalGroup.addGap(12);
			verticalGroup.addComponent(buildingsLabel);
			horizontalGroup.addComponent(buildingsLabel);
			verticalGroup.addGap(4);

			lastVertGroup = null;
			lastHorizGroup = null;
			i = 0;
			for (final GameObject unit : data.buildings) {
				if (isNeutral && !unit.getField("tilesets").contains(tileset)
						&& !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_")) {
					// System.err.println(unit.getField("Name") + " failed for
					// tilset");
					continue;
				}
				if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
					// System.err.println(unit.getField("Name") + " failed for
					// level");
					continue;
				}
				// if( isPassive && unit.getField("hostilePal").startsWith("1")
				// ) {
				// continue;
				// }
				// if( isHostile && !unit.getField("hostilePal").startsWith("1")
				// ) {
				// continue;
				// }
				// if( isNeutral && !isPassive && !isHostile ) {
				// continue;
				// }
				if (i % rowLength == 0) {
					if (lastVertGroup != null && lastHorizGroup != null) {
						horizontalGroup.addGroup(lastHorizGroup);
						verticalGroup.addGroup(lastVertGroup);
					}
					lastVertGroup = layout2.createParallelGroup();
					lastHorizGroup = layout2.createSequentialGroup();
				}
				final UnitButton myButton = new UnitButton(unit);
				unitButtons.add(myButton);
				lastVertGroup.addComponent(myButton);
				lastHorizGroup.addComponent(myButton);
				i++;
			}
			if (lastVertGroup != null && lastHorizGroup != null) {
				horizontalGroup.addGroup(lastHorizGroup);
				verticalGroup.addGroup(lastVertGroup);
			}
		}

		good = false;
		for (final GameObject unit : data.buildingsUprooted) {
			if (isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*")
					&& !unit.getField("tilesets").contains("_")) {
				// System.err.println(unit.getField("Name") + " failed for
				// tilset");
				continue;
			}
			if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
				// System.err.println(unit.getField("Name") + " failed for
				// level");
				continue;
			}
			// if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			// if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			// if( isNeutral && !isPassive && !isHostile ) {
			// continue;
			// }
			good = true;
			break;
		}
		if (data.buildingsUprooted.size() > 0 && good) {
			verticalGroup.addGap(12);
			verticalGroup.addComponent(buildingsUprootedLabel);
			horizontalGroup.addComponent(buildingsUprootedLabel);
			verticalGroup.addGap(4);

			lastVertGroup = null;
			lastHorizGroup = null;
			i = 0;
			for (final GameObject unit : data.buildingsUprooted) {
				if (isNeutral && !unit.getField("tilesets").contains(tileset)
						&& !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_")) {
					// System.err.println(unit.getField("Name") + " failed for
					// tilset");
					continue;
				}
				if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
					// System.err.println(unit.getField("Name") + " failed for
					// level");
					continue;
				}
				// if( isPassive && unit.getField("hostilePal").startsWith("1")
				// ) {
				// continue;
				// }
				// if( isHostile && !unit.getField("hostilePal").startsWith("1")
				// ) {
				// continue;
				// }
				// if( isNeutral && !isPassive && !isHostile ) {
				// continue;
				// }
				if (i % rowLength == 0) {
					if (lastVertGroup != null && lastHorizGroup != null) {
						horizontalGroup.addGroup(lastHorizGroup);
						verticalGroup.addGroup(lastVertGroup);
					}
					lastVertGroup = layout2.createParallelGroup();
					lastHorizGroup = layout2.createSequentialGroup();
				}
				final UnitButton myButton = new UnitButton(unit);
				unitButtons.add(myButton);
				lastVertGroup.addComponent(myButton);
				lastHorizGroup.addComponent(myButton);
				i++;
			}
			if (lastVertGroup != null && lastHorizGroup != null) {
				horizontalGroup.addGroup(lastHorizGroup);
				verticalGroup.addGroup(lastVertGroup);
			}
		}

		good = false;
		for (final GameObject unit : data.special) {
			if (isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*")
					&& !unit.getField("tilesets").contains("_")) {
				continue;
			}
			if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
				continue;
			}
			// if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			// if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
			// continue;
			// }
			good = true;
			break;
		}
		if (data.special.size() > 0 && good) {
			verticalGroup.addGap(12);
			verticalGroup.addComponent(specialLabel);
			horizontalGroup.addComponent(specialLabel);
			verticalGroup.addGap(4);

			lastVertGroup = null;
			lastHorizGroup = null;
			i = 0;
			for (final GameObject unit : data.special) {
				if (isNeutral && !unit.getField("tilesets").contains(tileset)
						&& !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_")) {
					// System.err.println(unit.getField("Name") + " failed for
					// tilset");
					continue;
				}
				if (checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
					// System.err.println(unit.getField("Name") + " failed for
					// level");
					continue;
				}
				// if( isPassive && unit.getField("hostilePal").startsWith("1")
				// ) {
				// continue;
				// }
				// if( isHostile && !unit.getField("hostilePal").startsWith("1")
				// ) {
				// continue;
				// }
				if (i % rowLength == 0) {
					if (lastVertGroup != null && lastHorizGroup != null) {
						horizontalGroup.addGroup(lastHorizGroup);
						verticalGroup.addGroup(lastVertGroup);
					}
					lastVertGroup = layout2.createParallelGroup();
					lastHorizGroup = layout2.createSequentialGroup();
				}
				final UnitButton myButton = new UnitButton(unit);
				unitButtons.add(myButton);
				lastVertGroup.addComponent(myButton);
				lastHorizGroup.addComponent(myButton);
				i++;
			}
			if (lastVertGroup != null && lastHorizGroup != null) {
				horizontalGroup.addGroup(lastHorizGroup);
				verticalGroup.addGroup(lastVertGroup);
			}
		}

		superHorizontalGroup.addGap(12);
		// verticalGroup.addGap(12);

		buttonsPanel.setLayout(layout2);

		setLayout(layout);

		revalidate();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		relayout();
	}
}
