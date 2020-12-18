package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.AnimFlag;

import javax.swing.table.AbstractTableModel;

public class FloatTrackTableModel extends AbstractTableModel {
	private AnimFlag track;

	public FloatTrackTableModel(final AnimFlag track) {
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
				case 4 -> "Delete";
				default -> null;
			};
		} else {
			return switch (column) {
				case 0 -> "Keyframe";
				case 1 -> "Value";
				case 2 -> "Delete";
				default -> null;
			};
		}
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		if ((track != null) && track.tans()) {
			return switch (columnIndex) {
				case 0, 1, 2, 3 -> Float.class;
				case 4 -> String.class;
				default -> super.getColumnClass(columnIndex);
			};
		} else {
			return switch (columnIndex) {
				case 0, 1 -> Float.class;
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
				case 4 -> "Delete";
				default -> null;
			};
		} else {
			return switch (columnIndex) {
				case 0 -> track.getTimes().get(rowIndex);
				case 1 -> track.getValues().get(rowIndex);
				case 2 -> "Delete";
				default -> null;
			};
		}
	}

	public void setTrack(final AnimFlag track) {
		this.track = track;
		fireTableDataChanged();
	}
}
