package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;

import javax.swing.table.AbstractTableModel;

public class FloatTrackTableModel extends AbstractTableModel {
	private AnimFlag track;
	private Object[] lastButtons = {"X", null};
	private String[] lastButtonsTitle = {"", null};
	private Class<?>[] lastButtonsClazz = {Integer.class, null};
	private Class<?> valueClazz = Float.class;

	private Class<?>[] columnClassList = {Integer.class, String.class, getLbtClass(0), getLbtClass(1)};
//	private Class<?>[] columnTansClassList = {Integer.class, String.class, String.class, String.class, getLbtClass(0), getLbtClass(1)};

	public FloatTrackTableModel(final AnimFlag track) {
		this.track = track;
		setClassList();
	}

	@Override
	public String getColumnName(final int column) {
		if ((track != null) && track.tans()) {
			return switch (column) {
				case 0 -> "Keyframe";
				case 1 -> "Value";
				case 2 -> "InTan";
				case 3 -> "OutTan";
//				case 4 -> getLbt(0);
//				case 5 -> getLbt(1);
				case 4 -> lastButtonsTitle[0];
				case 5 -> lastButtonsTitle[1];
				default -> null;
			};
		} else {
			return switch (column) {
				case 0 -> "Keyframe";
				case 1 -> "Value";
//				case 2 -> getLbt(0);
//				case 3 -> getLbt(1);
				case 2 -> lastButtonsTitle[0];
				case 3 -> lastButtonsTitle[1];
				default -> null;
			};
		}
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		if (columnIndex < columnClassList.length) {
			return columnClassList[columnIndex];
		} else {
			return null;
		}
//		if ((track != null) && track.tans()) {
//			return switch (columnIndex) {
////				case 0, 1, 2, 3 -> Float.class;
////				case 4 -> JButton.class;
//				case 0 -> columnClassList[0];//Integer.class;
//				case 1 -> columnClassList[1];//String.class;
//				case 2 -> columnClassList[2];//String.class;
//				case 3 -> columnClassList[3];//String.class;
//				case 4 -> columnClassList[4];//lastButtonstitle.get(0).getClass();
//				case 5 -> columnClassList[5];//lastButtonstitle.get(1).getClass();
//				default -> super.getColumnClass(columnIndex);
//			};
//		} else {
//			return switch (columnIndex) {
////				case 0, 1 -> Float.class;
////				case 2 -> JButton.class;
//				case 0 -> columnClassList[0];//Integer.class;
//				case 1 -> columnClassList[1];//String.class;
//				case 2 -> columnClassList[2];//lastButtonstitle.get(0).getClass();
//				case 3 -> columnClassList[3];//lastButtonstitle.get(1).getClass();
//				default -> super.getColumnClass(columnIndex);
//			};
//		}
	}

	@Override
	public int getRowCount() {
		if (track == null) {
			return 0;
		}
		return track.size();
	}

	@Override
	public int getColumnCount() {
		int cols = columnClassList.length;
		if (lastButtons[1] == null) {
//			System.out.println("was null: " + lastButtons[1]);
			cols--;
		}
//		if (track == null) {
//			return 3;
//		}
//		return track.tans() ? 5 : 3;
//		System.out.println("cols: " + cols);
		return cols;
	}

	//⊠u22A0, ☒u2612, ☓u2613, ⛝u26DD, ╳u2573
	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (track == null) {
			return null;
		}
		if (track.tans()) {
			return switch (columnIndex) {
				case 0 -> track.getTimes().get(rowIndex);
				case 1 -> track.getValues().get(rowIndex);
				case 2 -> track.getInTans().get(rowIndex);
				case 3 -> track.getOutTans().get(rowIndex);
//				case 4 -> {JButton uggB = new JButton("X");
//				uggB.addActionListener(e -> ugg());
//				yield uggB;}
//				case 4 -> new JButton("X");
				case 4 -> lastButtons[0];
				case 5 -> lastButtons[1];
				default -> null;
			};
		} else {
			return switch (columnIndex) {
				case 0 -> track.getTimes().get(rowIndex);
				case 1 -> track.getValues().get(rowIndex);
//				case 2 ->  {JButton uggB = new JButton("X");
//					uggB.addActionListener(e -> ugg());
//					yield uggB;}
//				case 2 -> new JButton("X");
//				case 2 -> lastButtons[0];
//				case 3 -> lastButtons[1];
				case 2 -> getLbtValue(0, rowIndex);
				case 3 -> getLbtValue(1, rowIndex);
				default -> null;
			};
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (track.tans()) {
			return col < 4;
		} else {
			return col < 2;
		}
	}

//	@Override
//	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
////		System.out.println("value: " + aValue + ", row: " + rowIndex + ", col: " + columnIndex);
//	}

	public void setTrack(final AnimFlag track) {
		this.track = track;
		setClassList();
		fireTableDataChanged();
	}

//	private void ugg(){
//		System.out.println("X");
//	}

	public void addExtraColumn(String title, Object fill, Class<?> clazz) {
//		System.out.println("addExtraColumn");
		if (lastButtons[1] == null) {
//			System.out.println(fill);
			lastButtons[1] = lastButtons[0];
			lastButtonsTitle[1] = lastButtonsTitle[0];
			lastButtonsClazz[1] = lastButtonsClazz[0];
			lastButtons[0] = fill;
			lastButtonsTitle[0] = title;
			lastButtonsClazz[0] = clazz;
			setClassList();
//			System.out.println("did add!, [1]: " + lastButtons[1]);
			fireTableStructureChanged();
		}
	}

	public void removeExtraColumn() {
		if (lastButtons[1] != null) {
			lastButtons[0] = lastButtons[1];
			lastButtonsTitle[0] = lastButtonsTitle[1];
			lastButtonsClazz[0] = lastButtonsClazz[1];
			lastButtons[1] = null;
			lastButtonsTitle[1] = null;
			lastButtonsClazz[1] = null;
			setClassList();
		}
//		columnClassList = {Integer.class, String.class, getLbtClass(0), getLbtClass(1)};
//		columnTansClassList = {Integer.class, String.class, String.class, String.class, getLbtClass(0), getLbtClass(1)};
	}

	private void setClassList() {
		if ((track != null) && track.tans()) {
			columnClassList = new Class[] {Integer.class, valueClazz, valueClazz, valueClazz, getLbtClass(0), getLbtClass(1)};
		} else {
			columnClassList = new Class[] {Integer.class, valueClazz, getLbtClass(0), getLbtClass(1)};
		}
	}

//	public void setColumnClass(int column, Class<?> clazz){
//		if(column < columnClassList.length){
//			columnClassList[column] = clazz;
//		}
//	}

	public void setValueClass(Class<?> clazz) {
		valueClazz = clazz;
	}

	private String getLbt(int i) {
		return lastButtonsTitle[i];
	}

	private String getLbtValue(int i, int index) {
		if (lastButtons[i].getClass() == String.class) {
			return (String) lastButtons[i];
//		} else if(lastButtons[i].getClass() == ArrayList.class){
		} else if (lastButtons[i].getClass() == String[].class) {
//			List<String> buttonValues = (ArrayList<String>) lastButtons[i];
			String[] bv = (String[]) lastButtons[i];
			if (bv.length > index) {
				return bv[index];
			}
		}
		return "";
	}

	public void updateExtraButtonValues(Object values) {
		if (lastButtons[1] != null) {
			lastButtons[0] = values;
		}
	}

	private Class<?> getLbtClass(int i) {
//		System.out.println("class for " + i);
		return lastButtonsClazz[i];
//		if(columnClassList != null){
//			System.out.println("b1: " + lastButtons[1] + " tit: " + lastButtonstitle[1] + " cols: " + getColumnCount());
//		}
//		if (lastButtonstitle[i] == null){
//			return null;
//		}else {
//			return lastButtonstitle[i].getClass();
//		}
	}
}
