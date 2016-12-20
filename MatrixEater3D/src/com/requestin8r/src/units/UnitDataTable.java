package com.requestin8r.src.units;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.matrixeater.src.ExceptionPopup;
import com.matrixeater.src.MPQHandler;


public class UnitDataTable {
	static UnitDataTable theTable;
	static UnitDataTable spawnTable;
	static UnitDataTable ginterTable;
	static UnitDataTable buffTable;
	static UnitDataTable itemTable;
	static UnitDataTable theTableDestructibles;
	static UnitDataTable theTableDoodads;
	public static UnitDataTable get() {
		if( theTable == null ) {
			theTable = new UnitDataTable();
			theTable.loadDefaults();
		}
		return theTable;
	}
	public static UnitDataTable getDoodads() {
		if( theTableDoodads == null ) {
			theTableDoodads = new UnitDataTable();
			theTableDoodads.loadDoodads();
		}
		return theTableDoodads;
	}
	public static UnitDataTable getDestructables() {
		if( theTableDestructibles == null ) {
			theTableDestructibles = new UnitDataTable();
			theTableDestructibles.loadDestructibles();
		}
		return theTableDestructibles;
	}
	public static UnitDataTable getItems() {
		if( itemTable == null ) {
			itemTable = new UnitDataTable();
			itemTable.loadItems();
		}
		return itemTable;
	}
	public static UnitDataTable getBuffs() {
		if( buffTable == null ) {
			buffTable = new UnitDataTable();
			buffTable.loadBuffs();
		}
		return buffTable;
	}
	public static UnitDataTable getSpawns() {
		if( spawnTable == null ) {
			spawnTable = new UnitDataTable();
			spawnTable.loadSpawns();
		}
		return spawnTable;
	}
	public static UnitDataTable getGinters() {
		if( ginterTable == null ) {
			ginterTable = new UnitDataTable();
			ginterTable.loadGinters();
		}
		return ginterTable;
	}
	
	Map<String,Unit> dataTable = new LinkedHashMap<String,Unit>();
	
	public UnitDataTable() {
		
	}
	
	public Set<String> keySet() {
		return dataTable.keySet();
	}
	
	public void loadDestructibles() {
		readSLK(MPQHandler.get().getGameFile("Units\\DestructableData.slk"));
	}
	
	public void loadDoodads() {
		readSLK(MPQHandler.get().getGameFile("Doodads\\Doodads.slk"));
	}
	
	public void loadItems() {
		readSLK(MPQHandler.get().getGameFile("Units\\ItemData.slk"));
		readTXT(MPQHandler.get().getGameFile("Units\\ItemFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\ItemStrings.txt"));
	}
	
	public void loadBuffs() {
		readSLK(MPQHandler.get().getGameFile("Units\\AbilityBuffData.slk"));
		readTXT(MPQHandler.get().getGameFile("Units\\CampaignAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\CampaignAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\CommonAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\CommonAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\HumanAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\HumanAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NeutralAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NeutralAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NightElfAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NightElfAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\OrcAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\OrcAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\UndeadAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\UndeadAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\ItemAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\ItemAbilityStrings.txt"));
	}
	
	public void loadSpawns() {
		readSLK(MPQHandler.get().getGameFile("Splats\\SpawnData.slk"));
	}
	
	public void loadGinters() {
		readTXT(MPQHandler.get().getGameFile("UI\\war3skins.txt"), true);
	}
	
	public void loadDefaults() {
		readSLK(MPQHandler.get().getGameFile("Units\\UnitUI.slk"));
		readSLK(MPQHandler.get().getGameFile("Units\\AbilityData.slk"));
//		readSLK(MPQHandler.get().getGameFile("Units\\AbilityBuffData.slk"));
		readSLK(MPQHandler.get().getGameFile("Units\\UnitData.slk"));
		readSLK(MPQHandler.get().getGameFile("Units\\UnitAbilities.slk"));
		readSLK(MPQHandler.get().getGameFile("Units\\UnitBalance.slk"));
		readSLK(MPQHandler.get().getGameFile("Units\\UnitWeapons.slk"));
		readSLK(MPQHandler.get().getGameFile("Units\\UpgradeData.slk"));
		readTXT(MPQHandler.get().getGameFile("Units\\CampaignUnitFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\CampaignUnitStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\HumanUnitFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\HumanUnitStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NeutralUnitFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NeutralUnitStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NightElfUnitFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NightElfUnitStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\OrcUnitFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\OrcUnitStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\UndeadUnitFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\UndeadUnitStrings.txt"));
		

		readTXT(MPQHandler.get().getGameFile("Units\\CampaignUpgradeFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\CampaignUpgradeStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\HumanUpgradeFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\HumanUpgradeStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NeutralUpgradeFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NeutralUpgradeStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NightElfUpgradeFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NightElfUpgradeStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\OrcUpgradeFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\OrcUpgradeStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\UndeadUpgradeFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\UndeadUpgradeStrings.txt"));

		readTXT(MPQHandler.get().getGameFile("Units\\CampaignAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\CampaignAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\CommonAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\CommonAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\HumanAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\HumanAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NeutralAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NeutralAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NightElfAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\NightElfAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\OrcAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\OrcAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\UndeadAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\UndeadAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\ItemAbilityFunc.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\ItemAbilityStrings.txt"));
		readTXT(MPQHandler.get().getGameFile("Units\\ItemStrings.txt"));
		//readTXT(MPQHandler.get().getGameFile("war3mapMisc.txt"));

		
		//Specific data edits for tech browser
//		Unit castleAge = dataTable.get("R035");
//		castleAge.setField("Name", "Castle Age");
//		get("h030").addResearches("R03Y");
//		
////		getVoidWorker().addToList("n03A","Builds");
//		Unit empoweredNetherling = get("n03J");
//		empoweredNetherling.setField("Upgrade",empoweredNetherling.getField("Upgrade").replace("n03A","null"));
//		get("h02Y").setField("Name", "Shrine of the Æther");
//		get("h02H").setField("Name", "Æthergate");
//		get("h02G").setField("Name", "Vault of the Æther");
//		get("h02V").setField("Name", "Ætherstorm Tower");
		
//		//Loading of data
//		for( String unitid: dataTable.keySet() ) {
//			Unit u = dataTable.get(unitid);
////			u.setField("Parents",u.getField("Requires"));
//			for( Unit req: u.requires() ) {
//				u.addParent(req.getUnitId());
//			}
//		}
//		
//		for( String unitid: dataTable.keySet() ) {
//			Unit u = dataTable.get(unitid);
//			for( Unit upgrade: u.upgrades() ) {
//				upgrade.addParent(u.getUnitId());
//			}
//		}
//		
//		for( String unitid: dataTable.keySet() ) {
//			Unit u = dataTable.get(unitid);
//			for( Unit upgrade: u.researches() ) {
//				upgrade.addParent(u.getUnitId());
//			}
//		}
//		
//		//Now calculate Children values
//		
//
//		
//		for( String unitid: dataTable.keySet() ) {
//			Unit u = dataTable.get(unitid);
//			for( Unit upgrade: u.parents() ) {
//				upgrade.addChild(u.getUnitId());
//			}
//			for( Unit upgrade: u.requires() ) {
//				upgrade.addRequiredBy(u.getUnitId());
//			}
//		}
	}
	
//	public void updateListWithLevels(List<Unit> list, List<Integer> levels) {
//		for( int i = 0; i < levels.size() && i < list.size(); i++ ) {
//			int level = levels.get(i);
//			if( level == 2 && list.get(i).equals(get("R035")) ) {
//				list.set(i, get("R03Y"));
//				//Enforce that "Level 2 Castle Age" is considered
//				// to be "Golden Age"
//			}
//		}
//	}
	
	public void readTXT(File f) {
		readTXT(f, false);
	}
	public void readTXT(File f, boolean canProduce) {
		try {
			readTXT(new FileInputStream(f), canProduce);
		}
		catch (IOException e)
		{
			ExceptionPopup.display(e);
		}
	}
	
	public void readSLK(File f) {
		try {
			readSLK(new FileInputStream(f));
		}
		catch (IOException e)
		{
			ExceptionPopup.display(e);
		}
	}
	
	public void readTXT(InputStream txt, boolean canProduce) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(txt));
		
		String input = "";
		Unit currentUnit = null;
		while( (input = reader.readLine()) != null ) {
			if( input.startsWith("//") ) {
				continue;
			}
			if( input.contains("[") && input.contains("]") ) {
				int start = input.indexOf("[") + 1;
				int end = input.indexOf("]");
				String newKey = input.substring(start,end);
				String newKeyBase = newKey;
				currentUnit = dataTable.get(newKey.toLowerCase());
				if( currentUnit == null) {
//					currentUnit = dataTable.get(newKey.charAt(0) + "" + Character.toUpperCase(newKey.charAt(1)) + newKey.substring(2));
//					if( currentUnit == null ) {
//						currentUnit = dataTable.get(newKey.charAt(0) + "" + Character.toLowerCase(newKey.charAt(1)) + newKey.substring(2));
//						if( currentUnit == null ) {
//							currentUnit = dataTable.get(newKeyBase.substring(0,3) + Character.toUpperCase(newKeyBase.charAt(3)));
//							if( currentUnit == null ) {
								currentUnit = new Unit(newKey,this);
								if( canProduce ) {
									currentUnit = new LMUnit(newKey,this);
									dataTable.put(newKey.toLowerCase(), currentUnit);
								}
//								currentUnit.setField("fromTXT", "1");
//							}
//						}
//					}
				}
			}
			else if( input.contains("=") ) {
				int eIndex = input.indexOf("=");
				String fieldValue = input.substring(eIndex+1);
				if( fieldValue.length() > 1
						&& fieldValue.startsWith("\"")
						&& fieldValue.endsWith("\"") ) {
					fieldValue = fieldValue.substring(1,fieldValue.length() - 1);
				}
				currentUnit.setField(input.substring(0,eIndex), fieldValue);
			}
		}
		
		reader.close();
	}
	

	public void readSLK(InputStream txt) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(txt));
		
		String input = "";
		Unit currentUnit = null;
		input = reader.readLine();
		if( !input.contains("ID") ) {
			System.err.println("Formatting of SLK is unusual.");
		}
		input = reader.readLine();
		while( input.startsWith("P;") || input.startsWith("F;") ) {
			input = reader.readLine();
		}
		int yIndex = input.indexOf("Y")+1;
		int xIndex = input.indexOf("X")+1;
		int colCount = 0;
		int rowCount = 0;
		boolean flipMode = false;
		if( xIndex > yIndex ) {
			colCount = Integer.parseInt(input.substring(xIndex,input.lastIndexOf(";")));
			rowCount = Integer.parseInt(input.substring(yIndex,xIndex-2));
		}
		else {
			rowCount = Integer.parseInt(input.substring(yIndex,input.lastIndexOf(";")));
			colCount = Integer.parseInt(input.substring(xIndex,yIndex-2));
			flipMode = true;
		}
		int rowStartCount = 0;
		String [] dataNames = new String[colCount];
//		for( int i = 0; i < colCount && rowStartCount <= 1; i++ ) {
//			input = reader.readLine();
//			dataNames[i] = input.substring(input.indexOf("\"")+1,
//					input.lastIndexOf("\""));
//		}
//		
		int col = 0;
		while( (input = reader.readLine()) != null ) {
			if( input.startsWith("O;") ) {
				continue;
			}
			if( input.contains("X1;") ) {
				rowStartCount++;
				col = 0;
			}
			else
			{
				col++;
			}
			if( rowStartCount <= 1 ) {
				int subXIndex = input.indexOf("X");
				int eIndex = input.indexOf("K");
				if( flipMode && input.contains("Y") ) {
					eIndex = Math.min(input.indexOf("Y"),eIndex);
				}
				int fieldId = Integer.parseInt(input.substring(subXIndex+1,
						eIndex-1));
				
				dataNames[fieldId-1] = input.substring(input.indexOf("\"")+1,
						input.lastIndexOf("\""));
				continue;
			}
			if( rowStartCount == 2)
			System.out.println(Arrays.toString(dataNames));
			if( input.contains("X1;") ) {
				int start = input.indexOf("\"") + 1;
				int end = input.lastIndexOf("\"");
				if( start-1 != end ) {
					String newKey = input.substring(start,end);
					currentUnit = dataTable.get(newKey.toLowerCase());
					if( currentUnit == null) {
						currentUnit = new Unit(newKey,this);
						dataTable.put(newKey.toLowerCase(), currentUnit);
					}
				}
			}
			else if( input.contains("K") ) {
				int subXIndex = input.indexOf("X");
				int eIndex = input.indexOf("K");
				if( flipMode && input.contains("Y") ) {
					eIndex = Math.min(input.indexOf("Y"),eIndex);
				}
				int fieldId = Integer.parseInt(input.substring(subXIndex+1,
						eIndex-1));
				String fieldValue = input.substring(eIndex+1);
				if( fieldValue.length() > 1
						&& fieldValue.startsWith("\"")
						&& fieldValue.endsWith("\"") ) {
					fieldValue = fieldValue.substring(1,fieldValue.length() - 1);
				}
				currentUnit.setField(dataNames[fieldId-1], fieldValue);
			}
		}
		
		reader.close();
	}
	
	public Unit get(String id) {
		return dataTable.get(id.toLowerCase());
	}

//	public Unit getFallyWorker() {
//		return dataTable.get("h02Z");
//	}
//	
//	public Unit getFallyWorker2() {
//		return dataTable.get("h03P");
//	}
//	
//	public Unit getTribeWorker() {
//		return dataTable.get("opeo");
//	}
//	
//	public Unit getTideWorker() {
//		return dataTable.get("ewsp");
//	}
//	
//	public Unit getVoidWorker() {
//		return dataTable.get("e007");
//	}
//	
//	public Unit getElfWorker() {
//		return dataTable.get("e000");
//	}
//	
//	public Unit getHumanWorker() {
//		return dataTable.get("h001");
//	}
//	
//	public Unit getOrcWorker() {
//		return dataTable.get("o000");
//	}
//	
//	public Unit getUndeadWorker() {
//		return dataTable.get("u001");
//	}
	
//	public static void main(String [] args) {
//		UnitDataTable table = new UnitDataTable();
//		table.loadDefaults();
//		Unit villager = table.get("h02Z");
//		System.out.println(villager.getField("Name")+ " can build: ");
//		System.out.println(villager.builds());
//		
//		System.out.println();
//
//		Unit townSquare = table.get("owtw");
//		System.out.println(townSquare.getField("Name")+ " trains: ");
//		System.out.println(townSquare.trains());
//		
//		System.out.println(townSquare.getField("Name")+ " upgrades: ");
//		System.out.println(townSquare.upgrades());
//		
//		System.out.println(townSquare.getField("Name")+ " researches: ");
//		System.out.println(townSquare.researches());
//		
//		System.out.println(townSquare.getField("Name")+ " stats: ");
//		for( String field: townSquare.fields.keySet() ) {
//			System.out.println(field +": "+townSquare.getField(field));
//		}
////		System.out.println(townSquare.getField("goldcost"));
////		System.out.println(townSquare.getField("lumbercost"));
////		System.out.println(townSquare.getField("fmade"));
////		System.out.println(townSquare.getField("fmade"));
//		
//		List<Unit> abils = table.getTideWorker().abilities();
//		System.out.println(abils);
//		for( Unit abil: abils ) {
//			System.out.println(abil.getUnitId());
//		}
//	}
}
