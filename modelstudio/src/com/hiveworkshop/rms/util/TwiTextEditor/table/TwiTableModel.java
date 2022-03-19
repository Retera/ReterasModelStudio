package com.hiveworkshop.rms.util.TwiTextEditor.table;

import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeFlagEntryAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class TwiTableModel<T> implements TableModel {

	private final AnimFlag<T> animFlag;
	private final Sequence sequence;
	private final T defaultValue;
	private final UndoManager undoManager;
	private final EventListenerList listenerList = new EventListenerList();
	private boolean doIndex = false;


	public TwiTableModel(AnimFlag<T> animFlag, Sequence sequence, T defaultValue, UndoManager undoManager){
		this.animFlag = animFlag;
		this.sequence = sequence;
		this.defaultValue = defaultValue;
		this.undoManager = undoManager;
	}

	@Override
	public int getRowCount() {
		return animFlag.size(sequence);
	}

	@Override
	public int getColumnCount() {
		return (doIndex ? 1 : 0) + 3 + (animFlag.tans() ? 2 : 0);
	}

	@Override
	public String getColumnName(int columnIndex) {
		return switch (getColumn(columnIndex)){
			case INDEX -> "";
			case TIME -> "Time";
			case VALUE -> "Value";
			case INTAN -> "InTan"; // InTan or delete button
			case OUTTAN -> "OutTan"; // OutTan or nothing
			case DELETE -> ""; // Delete button or nothing
			default -> null;
		};
	}
//	public String getColumnName(int columnIndex) {
//		return switch (getColumn(columnIndex)){
//			case INDEX -> "";
//			case TIME -> "Time";
//			case VALUE -> "Value";
//			case INTAN -> animFlag.tans() ? "InTan" : ""; // InTan or delete button
//			case OUTTAN -> animFlag.tans() ? "OutTan" : null; // OutTan or nothing
//			case DELETE -> animFlag.tans() ? "" : null; // Delete button or nothing
//			default -> null;
//		};
//	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return switch (getColumn(columnIndex)){
			case INDEX -> Integer.class;
			case TIME -> Integer.class;
			case VALUE -> defaultValue.getClass();
			case INTAN -> defaultValue.getClass() ; // InTan or delete button
			case OUTTAN -> defaultValue.getClass(); // OutTan or nothing
			case DELETE -> String.class; // Delete button or nothing
			default -> null;
		};
//		return switch (getColumn(columnIndex)){
//			case INDEX -> Integer.class;
//			case TIME -> Integer.class;
//			case VALUE -> defaultValue.getClass();
//			case INTAN -> animFlag.tans() ? defaultValue.getClass() : String.class; // InTan or delete button
//			case OUTTAN -> animFlag.tans() ? defaultValue.getClass() : null; // OutTan or nothing
//			case DELETE -> animFlag.tans() ? String.class : null; // Delete button or nothing
//			default -> null;
//		};
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
//		return 0 < columnIndex && 1 < getColumnCount()-columnIndex;
		return switch (getColumn(columnIndex)){
			case INDEX -> false;
			case TIME -> true;
			case VALUE -> true;
			case INTAN -> true; // InTan or delete button
			case OUTTAN -> true; // OutTan or nothing
			case DELETE -> false; // Delete button or nothing
			default -> false;
		};
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex<animFlag.size(sequence)){
			column column = getColumn(columnIndex);
			return getColValue(rowIndex, column);
//			return switch (getColumn(columnIndex)){
//				case INDEX -> rowIndex;
//				case TIME -> time;
//				case VALUE -> animFlag.getEntryAt(sequence, time).getValue();
//				case INTAN -> animFlag.tans() ? animFlag.getEntryAt(sequence, time).getInTan() : "X"; // InTan or delete button
//				case OUTTAN -> animFlag.tans() ? animFlag.getEntryAt(sequence, time).getOutTan() : null; // OutTan or nothing
//				case DELETE -> animFlag.tans() ? "X" : null; // OutTan or nothing
//				default -> null;
//			};
		}

		return null;
	}

	private Object getColValue(int rowIndex, column column) {
		int time = animFlag.getTimeFromIndex(sequence, rowIndex);
		return switch (column){
			case INDEX  -> rowIndex;
			case TIME   -> time;
			case VALUE  -> animFlag.getEntryAt(sequence, time).getValue();
			case INTAN  -> animFlag.getEntryAt(sequence, time).getInTan(); // InTan or delete button
			case OUTTAN -> animFlag.getEntryAt(sequence, time).getOutTan(); // OutTan or nothing
			case DELETE -> "X"; // OutTan or nothing
			default     -> null;
		};
	}

	public Integer getTimeAt(int rowIndex){
		return animFlag.getTimeFromIndex(sequence, rowIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(rowIndex<animFlag.size(sequence) && columnIndex<getColumnCount()){
			System.out.println("settin value at: [" + rowIndex + "," + columnIndex + "] to " + aValue);
			Entry<T> oldEntry = animFlag.getEntryAt(sequence, (Integer) getColValue(rowIndex, column.TIME));
			Entry<T> newEntry = oldEntry.deepCopy();
			if (getColumn(columnIndex) == column.TIME && aValue instanceof Integer || aValue.getClass() == defaultValue.getClass()){
				switch (getColumn(columnIndex)) {
					case TIME -> newEntry.setTime((Integer) aValue);
					case VALUE -> newEntry.setValue((T) aValue);
					case INTAN -> newEntry.setInTan((T) aValue);
					case OUTTAN -> newEntry.setOutTan((T) aValue);
				}
				if(!oldEntry.equals(newEntry)){
					if(undoManager != null){
						undoManager.pushAction(new ChangeFlagEntryAction<>(animFlag, newEntry, oldEntry, sequence, ModelStructureChangeListener.changeListener).redo());
					} else {
						new ChangeFlagEntryAction<>(animFlag, newEntry, oldEntry, sequence, null).redo();
					}
				}
			}
		}
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listenerList.remove(TableModelListener.class, l);
	}

	public void fireTableDataChanged() {
		fireTableChanged(new TableModelEvent(this));
	}
	public void fireTableStructureChanged() {
		fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	public void fireTableChanged(TableModelEvent e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==TableModelListener.class) {
				((TableModelListener)listeners[i+1]).tableChanged(e);
			}
		}
	}
	public void fireTableRowsInserted(int firstRow, int lastRow) {
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	private column getColumn(int columnIndex){
		if (!doIndex){
			return switch (columnIndex){
				case 0 ->column.TIME;
				case 1 ->column.VALUE;
				case 2 -> animFlag.tans() ? column.INTAN : column.DELETE;
				case 3 -> animFlag.tans() ? column.OUTTAN : null;
				case 4 -> animFlag.tans() ? column.DELETE : null;

				default -> column.INVALID;
			};
//			if(0 < columnIndex && columnIndex < column.values().length){
//				if(!animFlag.tans() && columnIndex == 3){
//					return column.DELETE;
//				} else if(!animFlag.tans() && columnIndex == 4){
//					return column.INVALID;
//				}
//				return column.values()[columnIndex+1];
//			}
		} else {
			if(0 <= columnIndex && columnIndex < column.values().length){
				if(!animFlag.tans() && columnIndex == 4){
					return column.DELETE;
				} else if(!animFlag.tans() && columnIndex == 5){
					return column.INVALID;
				}
				return column.values()[columnIndex];
			}
		}
		return column.INVALID;
	}

	private enum column{
		INDEX,
		TIME,
		VALUE,
		INTAN,
		OUTTAN,
		DELETE,
		EXTRA1,
		EXTRA2,
		INVALID,
	}
}
