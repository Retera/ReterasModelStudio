package com.hiveworkshop.rms.ui.gui.modeledit.components.material;

import javax.swing.table.AbstractTableModel;

import com.hiveworkshop.rms.editor.model.AnimFlag;

public class FloatTrackTableModel extends AbstractTableModel {
	private AnimFlag track;

	public FloatTrackTableModel(final AnimFlag track) {
		this.track = track;
	}

	@Override
	public String getColumnName(final int column) {
		if ((track != null) && track.tans()) {
			switch (column) {
			case 0:
				return "Keyframe";
			case 1:
				return "Value";
			case 2:
				return "InTan";
			case 3:
				return "OutTan";
			case 4:
				return "Delete";
			}
		} else {
			switch (column) {
			case 0:
				return "Keyframe";
			case 1:
				return "Value";
			case 2:
				return "Delete";
			}
		}
		return null;
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		if ((track != null) && track.tans()) {
			switch (columnIndex) {
			case 0:
				return Float.class;
			case 1:
				return Float.class;
			case 2:
				return Float.class;
			case 3:
				return Float.class;
			case 4:
				return String.class;
			}
		} else {
			switch (columnIndex) {
			case 0:
				return Float.class;
			case 1:
				return Float.class;
			case 2:
				return String.class;
			}
		}
		return super.getColumnClass(columnIndex);
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
			switch (columnIndex) {
			case 0:
				return track.getTimes().get(rowIndex);
			case 1:
				return track.getValues().get(rowIndex);
			case 2:
				return track.getInTans().get(rowIndex);
			case 3:
				return track.getOutTans().get(rowIndex);
			case 4:
				return "Delete";
			}
		} else {
			switch (columnIndex) {
			case 0:
				return track.getTimes().get(rowIndex);
			case 1:
				return track.getValues().get(rowIndex);
			case 2:
				return "Delete";
			}
		}
		return null;
	}

	public void setTrack(final AnimFlag track) {
		this.track = track;
		fireTableDataChanged();
	}
}
