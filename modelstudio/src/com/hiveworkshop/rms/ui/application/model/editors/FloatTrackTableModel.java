package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;

import javax.swing.table.AbstractTableModel;

public class FloatTrackTableModel extends AbstractTableModel {
	private AnimFlag<?> track;
	private Object[] lastButtons = {"X", null};
	private String[] lastButtonsTitle = {"", null};
	private Class<?>[] lastButtonsClazz = {Integer.class, null};
	private Class<?> valueClazz = Float.class;

	private Class<?>[] columnClassList = {Integer.class, String.class, getLbtClass(0), getLbtClass(1)};

	public FloatTrackTableModel(final AnimFlag<?> track) {
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
				case 4 -> lastButtonsTitle[0];
				case 5 -> lastButtonsTitle[1];
				default -> null;
			};
		} else {
			return switch (column) {
				case 0 -> "Keyframe";
				case 1 -> "Value";
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
			cols--;
		}
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
				case 0 -> track.getTimeFromIndex(rowIndex);
				case 1 -> track.getValueFromIndex(rowIndex);
				case 2 -> track.getInTanFromIndex(rowIndex);
				case 3 -> track.getOutTanFromIndex(rowIndex);
				case 4 -> lastButtons[0];
				case 5 -> lastButtons[1];
				default -> null;
			};
		} else {
			return switch (columnIndex) {
				case 0 -> track.getTimeFromIndex(rowIndex);
				case 1 -> track.getValueFromIndex(rowIndex);
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

	public void setTrack(AnimFlag<?> track) {
		this.track = track;
		setClassList();
		fireTableDataChanged();
	}

	public void addExtraColumn(String title, Object fill, Class<?> clazz) {
		if (lastButtons[1] == null) {
			lastButtons[1] = lastButtons[0];
			lastButtonsTitle[1] = lastButtonsTitle[0];
			lastButtonsClazz[1] = lastButtonsClazz[0];
			lastButtons[0] = fill;
			lastButtonsTitle[0] = title;
			lastButtonsClazz[0] = clazz;
			setClassList();
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
	}

	private void setClassList() {
		if ((track != null) && track.tans()) {
			columnClassList = new Class[] {Integer.class, valueClazz, valueClazz, valueClazz, getLbtClass(0), getLbtClass(1)};
		} else {
			columnClassList = new Class[] {Integer.class, valueClazz, getLbtClass(0), getLbtClass(1)};
		}
	}

	public void setValueClass(Class<?> clazz) {
		valueClazz = clazz;
	}

	private String getLbt(int i) {
		return lastButtonsTitle[i];
	}

	private String getLbtValue(int i, int index) {
		if (lastButtons[i].getClass() == String.class) {
			return (String) lastButtons[i];
		} else if (lastButtons[i].getClass() == String[].class) {
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
		return lastButtonsClazz[i];
	}
}
