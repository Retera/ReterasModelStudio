package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

public class ObjectDataTableModel implements TableModel {
	private final MutableGameObject gameObject;
	private final List<EditableOnscreenObjectField> fields;
	private final Set<TableModelListener> tableModelListeners;
	private boolean displayAsRawData;
	private final ObjectData metaData;
	private final Runnable runOnIsCustomUnitStateChange;

	public ObjectDataTableModel(final MutableGameObject gameObject, final ObjectData metaData,
			final EditorFieldBuilder editorFieldBuilder, final boolean displayAsRawData,
			final Runnable runOnIsCustomUnitStateChange) {
		this.gameObject = gameObject;
		this.metaData = metaData;
		this.displayAsRawData = displayAsRawData;
		this.runOnIsCustomUnitStateChange = runOnIsCustomUnitStateChange;
		tableModelListeners = new LinkedHashSet<>();
		if (gameObject != null) {
			fields = editorFieldBuilder.buildFields(metaData, gameObject);
			fields.sort((o1, o2) -> {
				final int o1Level = o1.getLevel();
				final int o2Level = o2.getLevel();
				if (o1.isShowingLevelDisplay() && !o2.isShowingLevelDisplay()) {
					return 1;
				}
				if (!o1.isShowingLevelDisplay() && o2.isShowingLevelDisplay()) {
					return -1;
				}
				final int sortNameComparison = o1.getSortName(gameObject).compareTo(o2.getSortName(gameObject));
				if (sortNameComparison != 0) {
					return sortNameComparison;
				}
				return Integer.compare(o1Level, o2Level);
			});
		} else {
			fields = new ArrayList<>();
		}
	}

	public void setDisplayAsRawData(final boolean displayAsRawData) {
		this.displayAsRawData = displayAsRawData;
		for (final TableModelListener listener : tableModelListeners) {
			listener.tableChanged(new TableModelEvent(this, 0, Integer.MAX_VALUE, 0));
		}
	}

	@Override
	public int getRowCount() {
		return fields.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	public boolean hasEditedValue(final int rowIndex) {
		if (gameObject == null) {
			return false;
		}
		return fields.get(rowIndex).hasEditedValue(gameObject);
	}

	@Override
	public String getColumnName(final int columnIndex) {
		return switch (columnIndex) {
			case 0 -> WEString.getString("WESTRING_UE_FIELDNAME");
			case 1 -> WEString.getString("WESTRING_UE_FIELDVALUE");
			default -> WEString.getString("WESTRING_UNKNOWN");
		};
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		if (columnIndex == 0) {
			return String.class;
		}
		return Object.class;
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (gameObject == null) {
			return 0;
		}
		if (columnIndex == 0) {
			if (displayAsRawData) {
				return fields.get(rowIndex).getRawDataName();
			} else {
				return fields.get(rowIndex).getDisplayName(gameObject);
			}
		}
		return fields.get(rowIndex).getValue(gameObject);
	}

	public String getFieldRawDataName(final int rowIndex) {
		return fields.get(rowIndex).getRawDataName();
	}

	public int getFieldLevel(final int rowIndex) {
		return fields.get(rowIndex).getLevel();
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {

	}

	public void doPopupAt(final Component parent, final int rowIndex, final boolean isHoldingShift) {
		if (gameObject == null) {
			return;
		}
		final boolean hadBeenEdited = gameObject.hasEditorData();
		final EditableOnscreenObjectField field = fields.get(rowIndex);
		if (field.popupEditor(gameObject, parent, displayAsRawData, isHoldingShift)) {
			for (final TableModelListener listener : tableModelListeners) {
				listener.tableChanged(new TableModelEvent(this, rowIndex, rowIndex, 1));
			}
		}
		if (gameObject.hasEditorData() != hadBeenEdited) {
			runOnIsCustomUnitStateChange.run();
		}
	}

	@Override
	public void addTableModelListener(final TableModelListener l) {
		tableModelListeners.add(l);
	}

	@Override
	public void removeTableModelListener(final TableModelListener l) {
		tableModelListeners.remove(l);
	}

}
