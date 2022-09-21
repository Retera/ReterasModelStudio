package com.hiveworkshop.rms.parsers.slk;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DataTable extends ObjectData {
	private Map<StringKey, Element> dataTable = new LinkedHashMap<>();

	public DataTable() {

	}

	public void loadStuff(String[] sklDatafiles, String[] txtFiles, boolean canProduce) {
		try {
			for (String sklData : sklDatafiles) {
				System.out.println("2reading dataTableSLK: " + sklData);
				new ReadSLK().readSLK(this, GameDataFileSystem.getDefault().getResourceAsStream(sklData));
			}
			for (String txt : txtFiles) {
				DataTableUtils.readTXT(this, txt, canProduce);
			}
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}
	}

	@Override
	public Set<String> keySet() {
		Set<String> outputKeySet = new HashSet<>();
		Set<StringKey> internalKeySet = dataTable.keySet();
		for (StringKey key : internalKeySet) {
			outputKeySet.add(key.getString());
		}
		return outputKeySet;
	}

	@Override
	public Element get(final String id) {
		return dataTable.get(new StringKey(id));
	}
	public Element getElementWithField(final String id, final String field) {
		final Element element = get(id);
//		if ((element != null)){
//			System.out.println("Found element for ID '" + id + "' in table '" + this + "', had field '" + field + "':" + (element.hasField(field)));
//		}
		if ((element != null) && element.hasField(field)) {
			return element;
		}
		return null;
	}

	@Override
	public void setValue(final String id, final String field, final String value) {
		get(id).setField(field, value);
	}

	public void put(final String id, final Element e) {
		dataTable.put(new StringKey(id), e);
	}

	public void put(final String id) {
		dataTable.put(new StringKey(id), new Element(id, this));
	}
	public Element computeIfAbsent(final String id) {
		return dataTable.computeIfAbsent(new StringKey(id), k -> new Element(id, this));
	}

	public void put(final Element e) {
		if(e != null){
			dataTable.put(new StringKey(e.getId()), e);
		}
	}

//	public Unit getFallyWorker() {
//		return dataTable.get("h02Z");
//	}
//	public Unit getFallyWorker2() {
//		return dataTable.get("h03P");
//	}
//	public Unit getTribeWorker() {
//		return dataTable.get("opeo");
//	}
//	public Unit getTideWorker() {
//		return dataTable.get("ewsp");
//	}
//	public Unit getVoidWorker() {
//		return dataTable.get("e007");
//	}
//	public Unit getElfWorker() {
//		return dataTable.get("e000");
//	}
//	public Unit getHumanWorker() {
//		return dataTable.get("h001");
//	}
//	public Unit getOrcWorker() {
//		return dataTable.get("o000");
//	}
//	public Unit getUndeadWorker() {
//		return dataTable.get("u001");
//	}
//
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
//		 List<Unit> abils = table.getTideWorker().abilities();
//		System.out.println(abils);
//		for( Unit abil: abils ) {
//			System.out.println(abil.getUnitId());
//		}
//	}
}
