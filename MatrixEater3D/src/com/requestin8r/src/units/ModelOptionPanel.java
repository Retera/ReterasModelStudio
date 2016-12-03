package com.requestin8r.src.units;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.matrixeater.src.MDL;
import com.matrixeater.src.MDLDisplay;
import com.matrixeater.src.MPQHandler;
import com.matrixeater.src.PerspDisplayPanel;
import com.requestin8r.src.WEString;

public class ModelOptionPanel extends JPanel {
	
	static class Model {
		String displayName;
		String filepath;
		
		@Override
		public String toString() {
			return displayName;
		}
	}
	static class ModelGroup {
		String name;
		List<Model> models = new ArrayList<Model>();
		public ModelGroup(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	static class ModelComparator implements Comparator<Model> {
		@Override
		public int compare(Model o1, Model o2) {
			return o1.displayName.compareToIgnoreCase(o2.displayName);
		}
		
	}
	static class NamedList<E> extends ArrayList<E> {
		String name;
		public NamedList(String name) {
			this.name = name;
		}
	}
	static List<ModelGroup> groups = new ArrayList<ModelGroup>();

	static UnitDataTable unitData = UnitDataTable.get();
	static UnitDataTable itemData = UnitDataTable.getItems();
	static UnitDataTable buffData = UnitDataTable.getBuffs();
	static UnitDataTable destData = UnitDataTable.getDestructables();
	static UnitDataTable doodData = UnitDataTable.getDoodads();
	static UnitDataTable spawnData = UnitDataTable.getSpawns();
	static UnitDataTable ginterData = UnitDataTable.getGinters();
	
	static boolean preloaded;
	static void preload() {
		if( preloaded ) {
			return;
		}
		else
			preloaded = true;
		// 11 ModelGroups:
		// - Units
		// - Units - Missiles
		// - Units - Special
		// - Items
		// - Abilities
		// - Buffs
		// - Destructibles
		// - Doodads
		// - Spawned Effects
		// - Game Interface
		
		//		WESTRING_OE_TYPECAT_UNIT=Units
		//		WESTRING_OE_TYPECAT_UNIT_MSSL=Units - Missiles
		//		WESTRING_OE_TYPECAT_UNIT_SPEC=Units - Special
		//		WESTRING_OE_TYPECAT_ITEM=Items
		//		WESTRING_OE_TYPECAT_ABIL=Abilities
		//		WESTRING_OE_TYPECAT_BUFF=Buffs
		//		WESTRING_OE_TYPECAT_UPGR=Upgrades
		//		WESTRING_OE_TYPECAT_DEST=Destructibles
//				WESTRING_OE_TYPECAT_DOOD=Doodads
//				WESTRING_OE_TYPECAT_SPWN=Spawned Effects
//				WESTRING_OE_TYPECAT_SKIN=Game Interface
//				WESTRING_OE_TYPECAT_XTRA=Extra
		
		
		// Preload "Units" modelGroup
		Map<String,NamedList<String>> unitsModelData = new HashMap<String,NamedList<String>>();
		Map<String,NamedList<String>> unitsMissileData = new HashMap<String,NamedList<String>>();
		Map<String,NamedList<String>> unitsSpecialData = new HashMap<String,NamedList<String>>();
		Map<String,NamedList<String>> abilityModelData = new HashMap<String,NamedList<String>>();
		Map<String,NamedList<String>> buffModelData = new HashMap<String,NamedList<String>>();
		Map<String,NamedList<String>> itemsModelData = new HashMap<String,NamedList<String>>();
		Map<String,NamedList<String>> destModelData = new HashMap<String,NamedList<String>>();
		Map<String,NamedList<String>> doodModelData = new HashMap<String,NamedList<String>>();
		Map<String,NamedList<String>> spawnModelData = new HashMap<String,NamedList<String>>();
		Map<String,NamedList<String>> ginterModelData = new HashMap<String,NamedList<String>>();
		
		//List<Unit> sortedUnitData = new ArrayList<Unit>();
		for( String str: unitData.keySet() ) {
			str = str.toUpperCase();
			if( str.startsWith("R") ) {
				continue;
			}
			else if ( (str.startsWith("A")
					|| str.startsWith("S")) ) {
				// ability
				Unit unit = unitData.get(str);
				String filepath = unit.getField("Areaeffectart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " +WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}
				filepath = unit.getField("areaeffectart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " +WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}
				filepath = unit.getField("AreaEffectart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " +WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}
				filepath = unit.getField("AreaEffectArt");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}
				filepath = unit.getField("AreaeffectArt");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase().toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase().toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_AREAEFFECT"));
				}
				
				filepath = unit.getField("CasterArt");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_CASTER"));
				}
				filepath = unit.getField("Casterart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_CASTER"));
				}
				filepath = unit.getField("casterart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_CASTER"));
				}
				
				filepath = unit.getField("EffectArt");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
				}
				filepath = unit.getField("Effectart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
				}
				filepath = unit.getField("effectart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
				}
				
				filepath = unit.getField("Missileart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
				}
				filepath = unit.getField("missileart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
				}
				filepath = unit.getField("MissileArt");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
				}
				
				filepath = unit.getField("SpecialArt");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
				}
				filepath = unit.getField("Specialart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
				}
				filepath = unit.getField("specialart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
				}

				filepath = unit.getField("TargetArt");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
				}
				filepath = unit.getField("Targetart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
				}
				filepath = unit.getField("targetart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						filepath = filepath.split(",")[0];
					}
					NamedList<String> unitList = abilityModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						abilityModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
				}
			}
			else if ( str.startsWith("B") 
					|| str.startsWith("X")
					|| str.startsWith("A")) {
				// BUFF
			}
			else {
				// UNIT
				Unit unit = unitData.get(str);
				String filepath = unit.getField("file");
				if( filepath.length() > 0 ) {
					NamedList<String> unitList = unitsModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						unitsModelData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName());
				}
				
				filepath = unit.getField("Missileart");
				if( filepath.length() > 0 ) {
					if( filepath.contains(",") ) {
						String[] filepaths = filepath.split(",");
						for( String fp: filepaths ) {
							NamedList<String> unitList = unitsMissileData.get(fp.toLowerCase());
							if( unitList == null ) {
								unitList = new NamedList<String>(filepath);
								unitsMissileData.put(fp.toLowerCase(), unitList);
							}
							unitList.add(unit.getName());
						}
					}
					else {
						NamedList<String> unitList = unitsMissileData.get(filepath.toLowerCase());
						if( unitList == null ) {
							unitList = new NamedList<String>(filepath);
							unitsMissileData.put(filepath.toLowerCase(), unitList);
						}
						unitList.add(unit.getName());
					}
				}
				
				filepath = unit.getField("Specialart");
				if( filepath.length() > 0 ) {
					NamedList<String> unitList = unitsSpecialData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						unitsSpecialData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
				}
				
				filepath = unit.getField("Targetart");
				if( filepath.length() > 0 ) {
					NamedList<String> unitList = unitsSpecialData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						unitsSpecialData.put(filepath.toLowerCase(), unitList);
					}
					unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
				}
			}
		}
		
		for( String str: buffData.keySet() ) {

			Unit unit = buffData.get(str);
			String filepath = unit.getField("EffectArt");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
			}
			filepath = unit.getField("Effectart");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
			}
			filepath = unit.getField("effectart");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_EFFECT"));
			}
			
			filepath = unit.getField("Missileart");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
			}
			filepath = unit.getField("MissileArt");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
			}
			filepath = unit.getField("missileart");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_MISSILE"));
			}
			
			filepath = unit.getField("SpecialArt");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
			}
			filepath = unit.getField("Specialart");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
			}
			filepath = unit.getField("specialart");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_SPECIAL"));
			}
			
			filepath = unit.getField("TargetArt");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
			}
			filepath = unit.getField("Targetart");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
			}
			filepath = unit.getField("targetart");
			if( filepath.length() > 0 ) {
				if( filepath.contains(",") ) {
					filepath = filepath.split(",")[0];
				}
				NamedList<String> unitList = buffModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					buffModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " " + WEString.getString("WESTRING_OE_TYPECAT_SUFFIX_TARGET"));
			}
		}

		for( String str: itemData.keySet() ) {
			// ITEMS
			Unit unit = itemData.get(str);
			String filepath = unit.getField("file");
			if( filepath.length() > 0 ) {
				NamedList<String> unitList = itemsModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					itemsModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName());
			}
		}

		for( String str: destData.keySet() ) {
			// ITEMS
			Unit unit = destData.get(str);
			String filepath = unit.getField("file");
			if( filepath.length() > 0 ) {
				NamedList<String> unitList = destModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					destModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " <Base>");
				
				int numVar = unit.getFieldValue("numVar");
				if( numVar > 1 ) {
					for( int i = 0; i < numVar; i++ ) {

						String filepath2 = filepath + i + ".mdl";
						if( filepath2.length() > 0 ) {
							NamedList<String> unitList2 = destModelData.get(filepath2.toLowerCase());
							if( unitList2 == null ) {
								unitList2 = new NamedList<String>(filepath2);
								destModelData.put(filepath2.toLowerCase(), unitList2);
							}
							unitList2.add(unit.getName() + " <" + WEString.getString("WESTRING_PREVIEWER_VAR") + " "+(i+1)+">");
						}
					}
				}
			}
		}

		for( String str: doodData.keySet() ) {
			// ITEMS
			Unit unit = doodData.get(str);
			String filepath = unit.getField("file");
			if( filepath.length() > 0 ) {
				NamedList<String> unitList = doodModelData.get(filepath.toLowerCase());
				if( unitList == null ) {
					unitList = new NamedList<String>(filepath);
					doodModelData.put(filepath.toLowerCase(), unitList);
				}
				unitList.add(unit.getName() + " <Base>");
				
				int numVar = unit.getFieldValue("numVar");
				if( numVar > 1 ) {
					for( int i = 0; i < numVar; i++ ) {

						String filepath2 = filepath + i + ".mdl";
						if( filepath2.length() > 0 ) {
							NamedList<String> unitList2 = doodModelData.get(filepath2.toLowerCase());
							if( unitList2 == null ) {
								unitList2 = new NamedList<String>(filepath2);
								doodModelData.put(filepath2.toLowerCase(), unitList2);
							}
							unitList2.add(unit.getName() + " <" + WEString.getString("WESTRING_PREVIEWER_VAR") + " "+(i+1)+">");
						}
					}
				}
			}
		}

		for( String str: spawnData.keySet() ) {
			if( !str.equals("init") ) {
				// ITEMS
				Unit unit = spawnData.get(str);
				String model = unit.getField("Model");
				if( model.equals("_") ) {
					continue;
				}
				String filepath = model;
				if( filepath.length() > 0 ) {
					NamedList<String> unitList = spawnModelData.get(filepath.toLowerCase());
					if( unitList == null ) {
						unitList = new NamedList<String>(filepath);
						spawnModelData.put(filepath.toLowerCase(), unitList);
					}
					if( model.contains("\\") ) {
						model = model.substring(model.lastIndexOf("\\") + 1);
					}
					if( model.contains(".") ) {
						model = model.substring(0, model.indexOf("."));
					}
					unitList.add(model);
				}
			}
		}
		
		for( String str: ginterData.keySet() ) {
			Unit race = ginterData.get(str);
//			System.err.println("Gintering unit " + str);
			for( String fieldName: race.fields.keySet() ) {
				String value = race.getField(fieldName);
				if( value.endsWith(".mdl") ) {

					String filepath = value;
					if( filepath.length() > 0 ) {
						NamedList<String> unitList = ginterModelData.get(filepath.toLowerCase());
						if( unitList == null ) {
							unitList = new NamedList<String>(filepath);
							ginterModelData.put(filepath.toLowerCase(), unitList);
						}
						unitList.add(fieldName + " (" + race.getUnitId() + ")");
					}
				}
			}
		}
		
		final int lengthCap = 120;
//		Collections.sort(sortedUnitData, new UnitComparator2());
//		for( Unit unit: sortedUnitData ) {
//		}
		ModelGroup units = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_UNIT"));
		for( String str: unitsModelData.keySet() ) {
			NamedList<String> unitList = unitsModelData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			units.models.add(nextModel);
		}
		Collections.sort(units.models, new ModelComparator());
		groups.add(units);

		ModelGroup unitsMissiles = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_UNIT_MSSL"));
		for( String str: unitsMissileData.keySet() ) {
			NamedList<String> unitList = unitsMissileData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			unitsMissiles.models.add(nextModel);
		}
		Collections.sort(unitsMissiles.models, new ModelComparator());
		groups.add(unitsMissiles);

		ModelGroup unitsSpecial = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_UNIT_SPEC"));
		for( String str: unitsSpecialData.keySet() ) {
			NamedList<String> unitList = unitsSpecialData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			unitsSpecial.models.add(nextModel);
		}
		Collections.sort(unitsSpecial.models, new ModelComparator());
		groups.add(unitsSpecial);

		ModelGroup items = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_ITEM"));
		for( String str: itemsModelData.keySet() ) {
			NamedList<String> unitList = itemsModelData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			items.models.add(nextModel);
		}
		Collections.sort(items.models, new ModelComparator());
		groups.add(items);

		ModelGroup abilities = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_ABIL"));
		for( String str: abilityModelData.keySet() ) {
			NamedList<String> unitList = abilityModelData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			abilities.models.add(nextModel);
		}
		Collections.sort(abilities.models, new ModelComparator());
		groups.add(abilities);

		ModelGroup buffs = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_BUFF"));
		for( String str: buffModelData.keySet() ) {
			NamedList<String> unitList = buffModelData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			buffs.models.add(nextModel);
		}
		Collections.sort(buffs.models, new ModelComparator());
		groups.add(buffs);

		ModelGroup destructibles = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_DEST"));
		for( String str: destModelData.keySet() ) {
			NamedList<String> unitList = destModelData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			destructibles.models.add(nextModel);
		}
		Collections.sort(destructibles.models, new ModelComparator());
		groups.add(destructibles);

		ModelGroup doodads = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_DOOD"));
		for( String str: doodModelData.keySet() ) {
			NamedList<String> unitList = doodModelData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			doodads.models.add(nextModel);
		}
		Collections.sort(doodads.models, new ModelComparator());
		groups.add(doodads);

		ModelGroup spawns = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_SPWN"));
		for( String str: spawnModelData.keySet() ) {
			NamedList<String> unitList = spawnModelData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			spawns.models.add(nextModel);
		}
		Collections.sort(spawns.models, new ModelComparator());
		groups.add(spawns);

		ModelGroup ginters = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_SKIN"));
		for( String str: ginterModelData.keySet() ) {
			NamedList<String> unitList = ginterModelData.get(str);
//			Collections.sort(unitList);
			String nameOutput = "";
			for( String unitName: unitList ) {
				if( nameOutput.length() > 0 ) {
					nameOutput += ", ";
				}
				if( nameOutput.length() + unitName.length() > lengthCap ) {
					nameOutput += "...";
					break;
				}
				else {
					nameOutput += unitName;
				}
			}
			Model nextModel = new Model();
			nextModel.displayName = nameOutput;
			nextModel.filepath = unitList.name;
			ginters.models.add(nextModel);
		}
		Collections.sort(ginters.models, new ModelComparator());
		groups.add(ginters);

		ModelGroup extra = new ModelGroup(WEString.getString("WESTRING_OE_TYPECAT_XTRA"));
		UnitDataTable worldEditData = new UnitDataTable();
		worldEditData.readTXT(MPQHandler.get().getGameFile("UI\\WorldEditData.txt"), true);
		Unit extraModels = worldEditData.get("ExtraModels");
		int emId = 0;
		while( extraModels.getField(String.format("%2d", emId).replace(" ", "0")).length() > 0 ) {
			String field = extraModels.getField(String.format("%2d", emId).replace(" ", "0"));
			String[] fieldParts = field.split(",");
			Model nextModel = new Model();
			nextModel.displayName = WEString.getString(fieldParts[2]);
			nextModel.filepath = fieldParts[1];
			extra.models.add(nextModel);
			
			emId ++;
		}
		Collections.sort(extra.models, new ModelComparator());
		groups.add(extra);
		
		for( Model model: extra.models ) {
			System.out.println(model);// + ": \"" + model.filepath + "\"");
		}
		
//		new JFrame().setVisible(true);
	}
	
	JComboBox<ModelGroup> groupBox;
	JComboBox<Model> modelBox;
	DefaultComboBoxModel<ModelGroup> groupsModel = new DefaultComboBoxModel<ModelGroup>();
	List<DefaultComboBoxModel<Model>> groupModels = new ArrayList<DefaultComboBoxModel<Model>>();
	
	PerspDisplayPanel viewer;
	
	final MDL blank = new MDL();
	final MDLDisplay blankDisp = new MDLDisplay(blank, null);
	public ModelOptionPanel() {
		preload();
		
		for( ModelGroup group: groups ) {
			groupsModel.addElement(group);
			DefaultComboBoxModel<Model> groupModel = new DefaultComboBoxModel<Model>();
			
			for( Model model: group.models ) {
				groupModel.addElement(model);
			}
			groupModels.add(groupModel);
		}
		groupBox = new JComboBox<ModelGroup>(groupsModel);
		modelBox = new JComboBox<Model>(groupModels.get(0));
		groupBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelBox.setModel(groupModels.get(groupBox.getSelectedIndex()));
				modelBox.setSelectedIndex(0);
			}
		});
		modelBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MDL toLoad = blank;
				MDLDisplay modelDisp;
				try {
					String filepath = ((Model)modelBox.getSelectedItem()).filepath;
					if( filepath.endsWith(".mdl") ) {
						filepath = filepath.replace(".mdl", ".mdx");
					}
					else if( !filepath.endsWith(".mdx") ) {
						filepath = filepath.concat(".mdx");
					}
					toLoad = MDL.read(MPQHandler.get().getGameFile(filepath));
					modelDisp = new MDLDisplay(toLoad, null);
				}
				catch (Exception exc) {
					exc.printStackTrace();
					//bad model!
					modelDisp = blankDisp;
				}
				
				viewer.setViewport(modelDisp);
				viewer.setTitle(toLoad.getName());
			}
		});

		groupBox.setMaximumRowCount(11);
		modelBox.setMaximumRowCount(36);

		groupBox.setMaximumSize(new Dimension(140, 25));
		modelBox.setMaximumSize(new Dimension(10000, 25));
		
		viewer = new PerspDisplayPanel("blank",  blankDisp);
		modelBox.setSelectedIndex(0);
		
		add(groupBox);
		add(modelBox);
		
		GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(8)
				.addComponent(viewer)
				.addGap(8)
				.addGroup(layout.createParallelGroup()
						.addComponent(groupBox)
						.addComponent(modelBox)
						)
				.addGap(8)
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(8)
				.addGroup(layout.createParallelGroup()
						.addComponent(viewer)
						.addGroup(layout.createSequentialGroup()
								.addComponent(groupBox)
								.addGap(4)
								.addComponent(modelBox)
								)
						)
				.addGap(8)
				);
		
		setLayout(layout);
	}
	public String getSelection() {
		if( modelBox.getSelectedItem() != null )
			return ((Model)modelBox.getSelectedItem()).filepath;
		else
			return null;
	}
	
//	public static void main(String[] args) {
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedLookAndFeelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		preload();
//		JOptionPane.showMessageDialog(null, new ModelOptionPanel(), "Choose Model", JOptionPane.PLAIN_MESSAGE);
//	}
}
