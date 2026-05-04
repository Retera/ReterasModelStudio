package com.hiveworkshop.wc3.gui.modeledit.components.material;

import javax.swing.table.AbstractTableModel;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import hiveworkshop.localizationmanager.LocalizationManager;

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
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getcolumnname_keyframe");
			case 1:
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getcolumnname_value");
			case 2:
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getcolumnname_intan");
			case 3:
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getcolumnname_outtan");
			case 4:
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getcolumnname_delete");
			}
		} else {
			switch (column) {
			case 0:
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getcolumnname_keyframe");
			case 1:
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getcolumnname_value");
			case 2:
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getcolumnname_delete");
			}
		}
		return null;
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		if ((track != null) && track.tans()) {
			switch (columnIndex) {
			case 0:
				return Double.class;
			case 1:
				return Double.class;
			case 2:
				return Double.class;
			case 3:
				return Double.class;
			case 4:
				return String.class;
			}
		} else {
			switch (columnIndex) {
			case 0:
				return Double.class;
			case 1:
				return Double.class;
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
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getvalueat_delete");
			}
		} else {
			switch (columnIndex) {
			case 0:
				return track.getTimes().get(rowIndex);
			case 1:
				return track.getValues().get(rowIndex);
			case 2:
				return LocalizationManager.getInstance().get("string.floattracktablemodel_getvalueat_delete");
			}
		}
		return null;
	}

	public void setTrack(final AnimFlag track) {
		this.track = track;
		fireTableDataChanged();
	}
}
