package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;

import javax.swing.table.AbstractTableModel;

public class FloatVecTrackTableModel extends AbstractTableModel {
	private AnimFlag track;

	public FloatVecTrackTableModel(final AnimFlag track) {
		this.track = track;
	}

	@Override
	public String getColumnName(final int column) {
		if ((track != null) && track.tans()) {
			return switch (column) {
				case 0 -> "Keyframe";
				case 1 -> "Value";
				case 2 -> "InTan";
				case 3 -> "OutTan";
				case 4 -> "";
				default -> null;
			};
		} else {
			return switch (column) {
				case 0 -> "Keyframe";
				case 1 -> "Value";
				case 2 -> "";
				default -> null;
			};
		}
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
//		System.out.println("class for column " + columnIndex);
		if ((track != null) && track.tans()) {
			return switch (columnIndex) {
//				case 0, 1, 2, 3 -> Float.class;
				case 0 -> Integer.class;
				case 1, 2, 3 -> String.class;
//				case 4 -> JButton.class;
				case 4 -> String.class;
				default -> super.getColumnClass(columnIndex);
			};
		} else {
			return switch (columnIndex) {
//				case 0 -> Float.class;
				case 0 -> Integer.class;
				case 1 -> String.class;
//				case 1 -> Vec3.class;
//				case 2 -> JButton.class;
				case 2 -> String.class;
				default -> super.getColumnClass(columnIndex);
			};
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
		if (track == null) {
			return 3;
		}
		return track.tans() ? 5 : 3;
	}

	//⊠u22A0, ☒u2612, ☓u2613, ⛝u26DD, ╳u2573
	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
//		System.out.println("Value at [" + rowIndex + ", " + columnIndex + "]");
		if (track == null) {
			return null;
		}
		if (track.tans()) {
			return switch (columnIndex) {
				case 0 -> track.getTimes().get(rowIndex);
				case 1 -> track.getValues().get(rowIndex);
				case 2 -> track.getInTans().get(rowIndex);
				case 3 -> track.getOutTans().get(rowIndex);
//				case 4 -> new JButton("X");
				case 4 -> "X";
				default -> null;
			};
		} else {
			return switch (columnIndex) {
				case 0 -> track.getTimes().get(rowIndex);
				case 1 -> track.getValues().get(rowIndex);
//				case 2 -> new JButton("X");
				case 2 -> "X";
//				case 2 -> "\uD83C\uDFA8"; // 🎨 \uD83C\uDFA8
				default -> null;
			};
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		System.out.println("editable? [" + row + ", " + col + "]");
		if (track.tans()) {
			System.out.println("<4");
			return col < 4;
		} else {
			System.out.println("<2");
			return col < 2;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
//		System.out.println("value: " + aValue + ", row: " + rowIndex + ", col: " + columnIndex);
	}

	public void setTrack(final AnimFlag track) {
		this.track = track;
		fireTableDataChanged();
	}
}
