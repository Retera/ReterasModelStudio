package com.requestin8r.src.units;

import java.awt.Color;
import java.awt.Dimension;
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

import com.requestin8r.src.WEString;

public class UnitOptionPanel extends JPanel implements ActionListener {
	public static final String TILESETS = "ABKYXJDCIFLWNOZGVQ";
	
	UnitDataTable unitData = UnitDataTable.get();
	Unit selection = null;
	
	JComboBox<String> raceBox, meleeBox, tilesetBox, levelBox;//playerBox,
//	DefaultComboBoxModel<String> playerBoxModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> raceBoxModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> raceBoxModelNeutral = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> meleeBoxModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> tilesetBoxModel = new DefaultComboBoxModel<String>();
	DefaultComboBoxModel<String> levelBoxModel = new DefaultComboBoxModel<String>();
	
	JLabel unitsLabel, heroesLabel, buildingsLabel, buildingsUprootedLabel, specialLabel;
	
	List<UnitButton> unitButtons = new ArrayList<UnitButton>();
	ButtonGroup buttonGroup = new ButtonGroup();
	
	JPanel buttonsPanel;
	JScrollPane buttonsScrollPane;
	
	ActionListener buttonListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			boolean found = false;
			for( UnitButton btn: unitButtons ) {
				if( e.getSource() == btn ) {
					//do stuff
//					btn.setBackground(Color.green);
					btn.setEnabled(false);
					found = true;
					String name = btn.getUnit().getName();

					String race = btn.getUnit().getField("race");
					boolean showLevel = true;
					for( int i = 0; i < 6; i++ ) {
						if( race.equals(raceKey(i)) ) {
							showLevel = false;
						}
					}
					if( showLevel ) {
						name += " - " + WEString.getString("WESTRING_LEVEL") + " " + btn.getUnit().getFieldValue("level");
					}//unit.getUnitId() + "<br>" + 
					unitsLabel.setText(WEString.getString("WESTRING_UNITS") + ": " + name);
					
					selection = btn.getUnit();
				}
				else {
					btn.setEnabled(true);
//					btn.setBackground(null);
				}
			}
			if( !found ) {
				selection = null;
				unitsLabel.setText(WEString.getString("WESTRING_UNITS") + ": " + WEString.getString("WESTRING_NONE_CAPS"));
			}
		}
	};
	public Unit getSelection() {
		return selection;
	}
	public UnitOptionPanel() {
		unitsLabel = new JLabel(WEString.getString("WESTRING_UNITS") + ": " + WEString.getString("WESTRING_NONE_CAPS"));
		heroesLabel = new JLabel(WEString.getString("WESTRING_UTYPE_HEROES"));
		buildingsLabel = new JLabel(WEString.getString("WESTRING_UTYPE_BUILDINGS"));
		buildingsUprootedLabel = new JLabel(WEString.getString("WESTRING_UTYPE_BUILDINGS_UPROOTED"));
		specialLabel = new JLabel(WEString.getString("WESTRING_UTYPE_SPECIAL"));

//		for( int i = 0; i < 12; i++ ) {
//			playerBoxModel.addElement(WEString.getString("WESTRING_PLAYER_" + String.format("%2d",i).replace(" ", "0")).replace("\"", ""));
//		}
//		playerBoxModel.addElement(WEString.getString("WESTRING_PLAYER_NA").replace("\"", ""));
//		playerBoxModel.addElement(WEString.getString("WESTRING_PLAYER_NP").replace("\"", ""));

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
		for( int i = 0; i <= 20; i++ )
			levelBoxModel.addElement(WEString.getString("WESTRING_LEVEL") + String.format(" %d",i));
		
		buttonsPanel = new JPanel();
		buttonsScrollPane = new JScrollPane(buttonsPanel);
		buttonsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

//		playerBox = new JComboBox<String>(playerBoxModel);
//		playerBox.addActionListener(this);
//		playerBox.setMaximumSize(new Dimension(10000,25));
		raceBox = new JComboBox<String>(raceBoxModel);
		raceBox.addActionListener(this);
		raceBox.setMaximumSize(new Dimension(10000,25));
		meleeBox = new JComboBox<String>(meleeBoxModel);
		meleeBox.addActionListener(this);
		meleeBox.setMaximumSize(new Dimension(10000,25));
		tilesetBox = new JComboBox<String>(tilesetBoxModel);
		tilesetBox.addActionListener(this);
		tilesetBox.setMaximumSize(new Dimension(10000,25));
		levelBox = new JComboBox<String>(levelBoxModel);
		levelBox.addActionListener(this);
		levelBox.setMaximumSize(new Dimension(10000,25));
		
		sortRaces();

		tilesetBox.setSelectedIndex(10);
	}
	
	private String raceKey() {
		if( raceBox.getModel() == raceBoxModel )
			switch (raceBox.getSelectedIndex() ) {
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
		else
			switch (raceBox.getSelectedIndex() ) {
			case -1:
				return "neutrals";
			case 0:
				return "neutrals";
			case 1:
				return "naga";
			}
		return "neutrals";
	}
	
	private String raceKey(int index) {
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
		List<Unit> units = new ArrayList<Unit>();
		List<Unit> heroes = new ArrayList<Unit>();
		List<Unit> buildings = new ArrayList<Unit>();
		List<Unit> buildingsUprooted = new ArrayList<Unit>();
		List<Unit> special = new ArrayList<Unit>();
		
		void sort() {
			Comparator<Unit> unitComp = new UnitComparator();
			
			Collections.sort(units, unitComp);
			Collections.sort(heroes, unitComp);
			Collections.sort(buildings, unitComp);
			Collections.sort(buildingsUprooted, unitComp);
			Collections.sort(special, unitComp);
		}
	}
	
	static Map<String,RaceData> sortedRaces;
	
	public void sortRaces() {
		if ( sortedRaces == null ) {
			sortedRaces = new HashMap<String,RaceData>();
			
			for( int i = 0; i < 6; i++ ) {
				sortedRaces.put(raceKey(i) + "melee", new RaceData());
				sortedRaces.put(raceKey(i) + "campaign", new RaceData());
				sortedRaces.put(raceKey(i) + "custom", new RaceData());
			}
			
			Unit root = unitData.get("Aroo");
			Unit rootAncientProtector = unitData.get("Aro2");
			Unit rootAncients = unitData.get("Aro1");
			for( String str: unitData.keySet() ) {
				str = str.toUpperCase();
				if( str.startsWith("B")
						|| str.startsWith("R")
						|| str.startsWith("A")
						|| str.startsWith("S") 
						|| str.startsWith("X") 
						|| str.startsWith("M") 
						|| str.startsWith("HERO") )
					continue;
				
				Unit unit = unitData.get(str);
				String raceKey = "neutrals";
				List<Unit> abilities = unit.abilities();
				boolean isCampaign = unit.getField("campaign").startsWith("1");
				boolean isCustom = !unit.getField("inEditor").startsWith("1");
				int sortGroupId = 0;
				
				for( int i = 0; i < 6; i++ ) {
					if( unit.getField("race").equals(raceKey(i)) ) {
						raceKey = raceKey(i);
					}
				}
				
				if( unit.getField("special").startsWith("1") ) {
					sortGroupId = 4;
				}
				else if( unit.getUnitId().length() > 1 && Character.isUpperCase(unit.getUnitId().charAt(0)) ) {
					sortGroupId = 1;
				}
				else if( abilities.contains(root) || abilities.contains(rootAncients) || abilities.contains(rootAncientProtector) ) {
					sortGroupId = 3;
				}
				else if( unit.getField("isbldg").startsWith("1") ) {
					sortGroupId = 2;
				}
				else {
					sortGroupId = 0;
				}
//				sortedRaces.get(raceKey(i) + "campaign").
				
				String storeKey = raceKey + (isCampaign ? "campaign" : "melee");
				if( isCustom ) {
					storeKey = raceKey + "custom";
				}
				
				switch(sortGroupId) {
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
			
			for( String str: sortedRaces.keySet() ) {
				RaceData race = sortedRaces.get(str);
				race.sort();
			}
		}
	}
	
	class UnitButton extends JButton {
		Unit unit;
		public UnitButton(Unit u) {
			super(u.getScaledIcon(0.5));
			this.unit = u;
			String uberTip = unit.getField("Ubertip");
			if( uberTip.length() < 1 ) {
				uberTip = unit.getField("UberTip");
			}
			if( uberTip.length() < 1 ) {
				uberTip = unit.getField("uberTip");
			}
			uberTip = uberTip.replace("|n","<br>");
			uberTip = uberTip.replace("|cffffcc00","");
			uberTip = uberTip.replace("|r","");
			
			String newUberTip = "";
			int depth = 0;
			for( int i = 0; i < uberTip.length(); i++ ) {
				char c = uberTip.charAt(i);
				if( c == '<' && uberTip.length() > i + 4 && uberTip.substring(i, i + 4).equals("<br>") ) {
					i += 3;
					depth = 0;
					newUberTip += "<br>";
				}
				else {
					if ( depth > 80 && c == ' ' ) {
						depth = 0;
						newUberTip += "<br>";
					}
					newUberTip += "" + c;
					depth++;
				}
			}
			
			uberTip = newUberTip;
			String name = unit.getName();
//			if( unit.getField("campaign").startsWith("1") && Character.isUpperCase(unit.getUnitId().charAt(0)) ) {
//				name = unit.getField("Propernames");
//				if( name.contains(",") ) {
//					name = name.split(",")[0];
//				}
//			}
			String race = unit.getField("race");
			boolean showLevel = true;
			for( int i = 0; i < 6; i++ ) {
				if( race.equals(raceKey(i)) ) {
					showLevel = false;
				}
			}
//			if( unit.getField("EditorSuffix").length() > 0 )
//				name += " " + unit.getField("EditorSuffix");
			if( showLevel ) {
				name += " - " + WEString.getString("WESTRING_LEVEL") + " " + unit.getFieldValue("level");
			}//unit.getUnitId() + "<br>" + 
			if( uberTip.length() > 0 )
				uberTip = "<html>" + name + "<br>--<br>" + uberTip + "</html>";
			else
				uberTip = name;
			this.setToolTipText(uberTip);
			buttonGroup.add(this);
			addActionListener(buttonListener);
			this.setDisabledIcon(unit.getScaledTintedIcon(Color.green, 0.5));
			setMargin(new Insets(0, 0, 0, 0));
		}
		public Unit getUnit() {
			return unit;
		}
	}
	
	boolean firstTime = true;
	public void relayout() {

		removeAll();
		buttonsPanel.removeAll();
		
//		if( playerBox.getSelectedIndex() > 11 ) {
//			if( raceBox.getModel() != raceBoxModelNeutral )
//				raceBox.setModel(raceBoxModelNeutral);
//		}
//		else {
//			if( raceBox.getModel() != raceBoxModel )
//				raceBox.setModel(raceBoxModel);
//		}
		
		String race = raceKey();
		String tileset = TILESETS.charAt(tilesetBox.getSelectedIndex()) + "";
		boolean isNeutral = race.equals("neutrals");
		boolean checkLevel = levelBox.getSelectedIndex() > 0 && isNeutral;
//		boolean isHostile = playerBox.getSelectedIndex() == 12 && !race.equals("naga");
//		boolean isPassive = playerBox.getSelectedIndex() == 13 && !race.equals("naga");

		buttonGroup.clearSelection();
		for( UnitButton ub: unitButtons ) {
			buttonGroup.remove(ub);
		}
		unitButtons.clear();
		
		RaceData data = sortedRaces.get(race + (meleeBox.getSelectedIndex() == 0 ? "melee" : "campaign"));
		if( meleeBox.getSelectedIndex() == 2 ) {
			data = sortedRaces.get(race + "custom");
		}
		
		GroupLayout layout = new GroupLayout(this);

		GroupLayout.Group superHorizontalGroup;
		GroupLayout.Group horizontalGroup;
		GroupLayout.Group verticalGroup;
		
		if( race.equals("neutrals") ) {
			layout.setHorizontalGroup(superHorizontalGroup = layout.createSequentialGroup()
					.addGap(12)
					.addGroup(layout.createParallelGroup()
//							.addComponent(playerBox)
							.addGroup(layout.createSequentialGroup()
									.addComponent(raceBox)
									.addGap(4)
									.addComponent(meleeBox)
									)
							.addGroup(layout.createSequentialGroup()
									.addComponent(tilesetBox)
									.addGap(4)
									.addComponent(levelBox)
									)
							.addComponent(unitsLabel)
							.addComponent(buttonsScrollPane)
							)
					);

			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(12)
//					.addComponent(playerBox)
//					.addGap(4)
					.addGroup(layout.createParallelGroup()
							.addComponent(raceBox)
							.addComponent(meleeBox)
							)
					.addGap(4)
					.addGroup(layout.createParallelGroup()
							.addComponent(tilesetBox)
							.addComponent(levelBox)
							)
					.addGap(4)
					.addComponent(unitsLabel)
					.addGap(4)
					.addComponent(buttonsScrollPane)
					.addGap(12)
					);
		}
		else {
			layout.setHorizontalGroup(superHorizontalGroup = layout.createSequentialGroup()
					.addGap(12)
					.addGroup(layout.createParallelGroup()
//							.addComponent(playerBox)
							.addGroup(layout.createSequentialGroup()
									.addComponent(raceBox)
									.addGap(4)
									.addComponent(meleeBox)
									)
							.addComponent(unitsLabel)
							.addComponent(buttonsScrollPane)
							)
					);

			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(12)
//					.addComponent(playerBox)
//					.addGap(4)
					.addGroup(layout.createParallelGroup()
							.addComponent(raceBox)
							.addComponent(meleeBox)
							)
					.addGap(4)
					.addComponent(unitsLabel)
					.addGap(4)
					.addComponent(buttonsScrollPane)
					.addGap(12)
					);
		}
		
		int rowLength = Math.max(1, (buttonsScrollPane.getWidth()) / 32 - 1);//(getWidth() - 24) / 32;
		if( firstTime ) {
			rowLength = 7;
			firstTime = false;
		}
		
		GroupLayout layout2 = new GroupLayout(buttonsPanel);
		horizontalGroup = layout2.createParallelGroup();
		verticalGroup = layout2.createSequentialGroup();
		
		layout2.setVerticalGroup(verticalGroup);
		layout2.setHorizontalGroup(horizontalGroup);
		System.err.println(tileset);
		
		
		GroupLayout.Group lastVertGroup = null;
		GroupLayout.Group lastHorizGroup = null;
		int i = 0;
		for( Unit unit: data.units ) {
//			System.err.println(unit.getField("tilesets"));
			if( isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_") ) {
//				System.err.println(unit.getField("Name") + " failed for tilset");
				continue;
			}
			if( checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
//				System.err.println(unit.getField("Name") + " failed for level");
				continue;
			}
//			if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
//			if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
			if( i % rowLength == 0 ) {
				if( lastVertGroup != null && lastHorizGroup != null ) {
					horizontalGroup.addGroup(lastHorizGroup);
					verticalGroup.addGroup(lastVertGroup);
				}
				lastVertGroup = layout2.createParallelGroup();
				lastHorizGroup = layout2.createSequentialGroup();
			}
			UnitButton myButton = new UnitButton(unit);
			unitButtons.add(myButton);
			lastVertGroup.addComponent(myButton);
			lastHorizGroup.addComponent(myButton);
			i++;
		}
		if( lastVertGroup != null && lastHorizGroup != null ) {
			horizontalGroup.addGroup(lastHorizGroup);
			verticalGroup.addGroup(lastVertGroup);
		}

		boolean good = false;
		for( Unit unit: data.heroes ) {
			if( isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_") ) {
//				System.err.println(unit.getField("Name") + " failed for tilset");
				continue;
			}
			if( checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
//				System.err.println(unit.getField("Name") + " failed for level");
				continue;
			}
//			if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
//			if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
			good = true;
			break;
		}
		if( data.heroes.size() > 0 && good ) {
			verticalGroup.addGap(12);
			verticalGroup.addComponent(heroesLabel);
			horizontalGroup.addComponent(heroesLabel);
			verticalGroup.addGap(4);
			
			lastVertGroup = null;
			lastHorizGroup = null;
			i = 0;
			for( Unit unit: data.heroes ) {
				if( isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_") ) {
//					System.err.println(unit.getField("Name") + " failed for tilset");
					continue;
				}
				if( checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
//					System.err.println(unit.getField("Name") + " failed for level");
					continue;
				}
//				if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
//					continue;
//				}
//				if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
//					continue;
//				}
				if( i % rowLength == 0 ) {
					if( lastVertGroup != null && lastHorizGroup != null ) {
						horizontalGroup.addGroup(lastHorizGroup);
						verticalGroup.addGroup(lastVertGroup);
					}
					lastVertGroup = layout2.createParallelGroup();
					lastHorizGroup = layout2.createSequentialGroup();
				}
				UnitButton myButton = new UnitButton(unit);
				unitButtons.add(myButton);
				lastVertGroup.addComponent(myButton);
				lastHorizGroup.addComponent(myButton);
				i++;
			}
			if( lastVertGroup != null && lastHorizGroup != null ) {
				horizontalGroup.addGroup(lastHorizGroup);
				verticalGroup.addGroup(lastVertGroup);
			}
		}
		
		good = false;
		for( Unit unit: data.buildings ) {
			if( isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_") ) {
				continue;
			}
			if( checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
				continue;
			}
//			if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
//			if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
//			if( isNeutral && !isPassive && !isHostile ) {
//				continue;
//			}
			good = true;
			break;
		}
		if( data.buildings.size() > 0 && good ) {
			verticalGroup.addGap(12);
			verticalGroup.addComponent(buildingsLabel);
			horizontalGroup.addComponent(buildingsLabel);
			verticalGroup.addGap(4);
			
			lastVertGroup = null;
			lastHorizGroup = null;
			i = 0;
			for( Unit unit: data.buildings ) {
				if( isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_") ) {
//					System.err.println(unit.getField("Name") + " failed for tilset");
					continue;
				}
				if( checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
//					System.err.println(unit.getField("Name") + " failed for level");
					continue;
				}
//				if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
//					continue;
//				}
//				if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
//					continue;
//				}
//				if( isNeutral && !isPassive && !isHostile ) {
//					continue;
//				}
				if( i % rowLength == 0 ) {
					if( lastVertGroup != null && lastHorizGroup != null ) {
						horizontalGroup.addGroup(lastHorizGroup);
						verticalGroup.addGroup(lastVertGroup);
					}
					lastVertGroup = layout2.createParallelGroup();
					lastHorizGroup = layout2.createSequentialGroup();
				}
				UnitButton myButton = new UnitButton(unit);
				unitButtons.add(myButton);
				lastVertGroup.addComponent(myButton);
				lastHorizGroup.addComponent(myButton);
				i++;
			}
			if( lastVertGroup != null && lastHorizGroup != null ) {
				horizontalGroup.addGroup(lastHorizGroup);
				verticalGroup.addGroup(lastVertGroup);
			}
		}
		
		good = false;
		for( Unit unit: data.buildingsUprooted ) {
			if( isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_") ) {
//				System.err.println(unit.getField("Name") + " failed for tilset");
				continue;
			}
			if( checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
//				System.err.println(unit.getField("Name") + " failed for level");
				continue;
			}
//			if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
//			if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
//			if( isNeutral && !isPassive && !isHostile ) {
//				continue;
//			}
			good = true;
			break;
		}
		if( data.buildingsUprooted.size() > 0 && good ) {
			verticalGroup.addGap(12);
			verticalGroup.addComponent(buildingsUprootedLabel);
			horizontalGroup.addComponent(buildingsUprootedLabel);
			verticalGroup.addGap(4);
			
			lastVertGroup = null;
			lastHorizGroup = null;
			i = 0;
			for( Unit unit: data.buildingsUprooted ) {
				if( isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_") ) {
//					System.err.println(unit.getField("Name") + " failed for tilset");
					continue;
				}
				if( checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
//					System.err.println(unit.getField("Name") + " failed for level");
					continue;
				}
//				if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
//					continue;
//				}
//				if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
//					continue;
//				}
//				if( isNeutral && !isPassive && !isHostile ) {
//					continue;
//				}
				if( i % rowLength == 0 ) {
					if( lastVertGroup != null && lastHorizGroup != null ) {
						horizontalGroup.addGroup(lastHorizGroup);
						verticalGroup.addGroup(lastVertGroup);
					}
					lastVertGroup = layout2.createParallelGroup();
					lastHorizGroup = layout2.createSequentialGroup();
				}
				UnitButton myButton = new UnitButton(unit);
				unitButtons.add(myButton);
				lastVertGroup.addComponent(myButton);
				lastHorizGroup.addComponent(myButton);
				i++;
			}
			if( lastVertGroup != null && lastHorizGroup != null ) {
				horizontalGroup.addGroup(lastHorizGroup);
				verticalGroup.addGroup(lastVertGroup);
			}
		}
		
		good = false;
		for( Unit unit: data.special ) {
			if( isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_") ) {
				continue;
			}
			if( checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
				continue;
			}
//			if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
//			if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
//				continue;
//			}
			good = true;
			break;
		}
		if( data.special.size() > 0 && good ) {
			verticalGroup.addGap(12);
			verticalGroup.addComponent(specialLabel);
			horizontalGroup.addComponent(specialLabel);
			verticalGroup.addGap(4);
			
			lastVertGroup = null;
			lastHorizGroup = null;
			i = 0;
			for( Unit unit: data.special ) {
				if( isNeutral && !unit.getField("tilesets").contains(tileset) && !unit.getField("tilesets").contains("*") && !unit.getField("tilesets").contains("_") ) {
//					System.err.println(unit.getField("Name") + " failed for tilset");
					continue;
				}
				if( checkLevel && unit.getFieldValue("level") != levelBox.getSelectedIndex() - 1) {
//					System.err.println(unit.getField("Name") + " failed for level");
					continue;
				}
//				if( isPassive && unit.getField("hostilePal").startsWith("1") ) {
//					continue;
//				}
//				if( isHostile && !unit.getField("hostilePal").startsWith("1") ) {
//					continue;
//				}
				if( i % rowLength == 0 ) {
					if( lastVertGroup != null && lastHorizGroup != null ) {
						horizontalGroup.addGroup(lastHorizGroup);
						verticalGroup.addGroup(lastVertGroup);
					}
					lastVertGroup = layout2.createParallelGroup();
					lastHorizGroup = layout2.createSequentialGroup();
				}
				UnitButton myButton = new UnitButton(unit);
				unitButtons.add(myButton);
				lastVertGroup.addComponent(myButton);
				lastHorizGroup.addComponent(myButton);
				i++;
			}
			if( lastVertGroup != null && lastHorizGroup != null ) {
				horizontalGroup.addGroup(lastHorizGroup);
				verticalGroup.addGroup(lastVertGroup);
			}
		}
		
		superHorizontalGroup.addGap(12);
		//verticalGroup.addGap(12);
		
		buttonsPanel.setLayout(layout2);
		
		setLayout(layout);
		
		revalidate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		relayout();
	}
}
