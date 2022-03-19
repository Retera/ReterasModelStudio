package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class TwiTablePane<T> extends JScrollPane {
	private final JTable keyframeTable;
	private FloatTrackTableModel<T> floatTrackTableModel;
	private AnimFlag<T> animFlag;
	private Sequence sequence;
	private final Dimension scrollPaneSize;
	private final Dimension scrollPrefPaneSize;
	private final Map<Integer, Integer> columnSizes = new HashMap<>();
	private final Function<String, T> parseFunction;
	private int selectNewIndex = -1;
	private Consumer<int[]> rowsDeleteListener;
	private Consumer<Integer> addListener;

	private final BiConsumer<Integer, Entry<T>> changeAction;

	public TwiTablePane(Function<String, T> parseFunction, BiConsumer<Integer, Entry<T>> changeAction){
		this.keyframeTable = new JTable();
		this.parseFunction = parseFunction;
		this.changeAction = changeAction;
		setViewportView(keyframeTable);

//		System.out.println("editor comp: " + keyframeTable.getEditorComponent());

		scrollPaneSize = ScreenInfo.getSuitableSize(700, 300, 0.6);
		setMaximumSize(scrollPaneSize);
		scrollPrefPaneSize = new Dimension(scrollPaneSize);

		keyframeTable.addMouseListener(getMouseAdapter());
		keyframeTable.addKeyListener(getKeyAdapter());
		keyframeTable.setDefaultEditor(Vec3.class, new TableTextEditor());
		keyframeTable.setDefaultEditor(String.class, new TableTextEditor());
		keyframeTable.setDefaultEditor(Float.class, new TableTextEditor());
		keyframeTable.setDefaultEditor(Integer.class, new TableTextEditor());

		columnSizes.put(-1, keyframeTable.getRowHeight());
		columnSizes.put(0, keyframeTable.getRowHeight()*4);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
//				System.out.println("height: " + getHeight());
				setPrefSize();
			}
		});
		keyframeTable.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
//				System.out.println("KF, height: " + getHeight());
				setPrefSize();
			}
		});


		keyframeTable.addPropertyChangeListener(this::propertyChanged);
	}

	public void setPrefSize() {
		int totTableHeight = keyframeTable.getHeight() + keyframeTable.getTableHeader().getHeight() + 2;
		scrollPrefPaneSize.height = Math.min(totTableHeight, scrollPaneSize.height);
		setPreferredSize(scrollPrefPaneSize);
	}

	private void propertyChanged(PropertyChangeEvent evt) {
//		System.out.println("table property changed: " + evt);
		Object oldValue = evt.getOldValue();
		if(oldValue instanceof TableTextEditor){
			int row = ((JTable)(evt.getSource())).getEditingRow();
			int col = ((JTable)(evt.getSource())).getEditingColumn();
			System.out.println("should save: " + ((TableTextEditor) evt.getOldValue()).getCellEditorValue());
			changeEntry(row, floatTrackTableModel.getColumnName(col), ((TableTextEditor) oldValue).getEditorValue());
		} else if(oldValue instanceof DefaultCellEditor && ((DefaultCellEditor) oldValue).getComponent() instanceof TwiComboBox){
			int row = ((JTable)(evt.getSource())).getEditingRow();
			int col = ((JTable)(evt.getSource())).getEditingColumn();
			int selectedIndex = ((TwiComboBox<?>) ((DefaultCellEditor) oldValue).getComponent()).getSelectedIndex();

//			changeEntry(row, floatTrackTableModel.getColumnName(col), "" + selectedIndex);

		}
	}

	protected void changeEntry(int row, String field, String val) {
		if (parseFunction != null && changeAction != null && animFlag != null && sequence != null) {
			int orgTime = (int) floatTrackTableModel.getValueAt(row, 0);
			Entry<T> newEntry = animFlag.getEntryAt(sequence, orgTime).deepCopy();

			switch (field) {
				case "Keyframe" -> newEntry.setTime(parseTime(val));
				case "Value" -> newEntry.setValue(parseFunction.apply(val));
				case "InTan" -> newEntry.setInTan(parseFunction.apply(val));
				case "OutTan" -> newEntry.setOutTan(parseFunction.apply(val));
			}

			changeAction.accept(orgTime, newEntry);
			setSelectNew(animFlag.getIndexOfTime(sequence, newEntry.time));
		}
	}

	private Integer parseTime(String val) {
		String intString = val.replaceAll("[^\\d]", "");//.split("\\.")[0];
		intString = intString.substring(0, Math.min(intString.length(), 10));

		return Integer.parseInt(intString);
	}

	public TwiTablePane<T> setSequence(AnimFlag<T> animFlag, Sequence sequence) {
		this.animFlag = animFlag;
		this.sequence = sequence;
		if(floatTrackTableModel == null){
			floatTrackTableModel = new FloatTrackTableModel<>(animFlag, sequence);
//			this.keyframeTable.setModel(floatTrackTableModel);
		} else {
			floatTrackTableModel.setTrack(animFlag, sequence);
		}
		this.keyframeTable.setModel(floatTrackTableModel);

		for (int i : columnSizes.keySet()) {
			int count = keyframeTable.getColumnCount();
			int columnIndex = (count + i) % count;
			TableColumn column = keyframeTable.getColumnModel().getColumn(columnIndex);
			if (column != null) {
				column.setMaxWidth(columnSizes.get(i));
				column.setPreferredWidth(columnSizes.get(i));
				column.setMinWidth(5);
			}
		}
		setPrefSize();
		doSelectNewIndex();
		return this;
	}

	public TwiTablePane<T> setAddListener(Consumer<Integer> addListener) {
		this.addListener = addListener;
		return this;
	}

	public TwiTablePane<T> setRowsDeleteListener(Consumer<int[]> rowsDeleteListener) {
		this.rowsDeleteListener = rowsDeleteListener;
		return this;
	}

	private MouseAdapter getMouseAdapter(){
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (keyframeTable.getSelectedColumn() == keyframeTable.getColumnCount()-1 && rowsDeleteListener != null){
					rowsDeleteListener.accept(keyframeTable.getSelectedRows());
				}
			}
		};
	}

	private KeyAdapter getKeyAdapter(){
		return new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
//				System.out.println("keyReleased: " + e.getKeyCode());

				if (e.getKeyCode() == KeyEvent.VK_ENTER && rowsDeleteListener != null
						&& keyframeTable.getSelectedColumn() == keyframeTable.getColumnCount()-1) {
					rowsDeleteListener.accept(keyframeTable.getSelectedRows());
				}
				if (e.getKeyCode() == KeyEvent.VK_DELETE && rowsDeleteListener != null) {
					rowsDeleteListener.accept(keyframeTable.getSelectedRows());
				}
				if (addListener != null && (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD)) {
					addListener.accept(keyframeTable.getSelectedRow());
				}
			}
		};
	}

	private void doSelectNewIndex() {
//		System.out.println("doSelectNewIndex, " + selectNewIndex);
		if (selectNewIndex != -1 && selectNewIndex < keyframeTable.getRowCount()) {
			System.out.println("selecting row " + selectNewIndex);
			keyframeTable.setRowSelectionInterval(selectNewIndex, selectNewIndex);
			selectNewIndex = -1;
		}
	}

	public TwiTablePane<T> setSelectNewIndex(int selectNewIndex) {
		this.selectNewIndex = selectNewIndex;
		return this;
	}
	public TwiTablePane<T> setSelectNew(int newIndex) {
		this.selectNewIndex = newIndex;
		doSelectNewIndex();
		return this;
	}


	public JTable getKeyframeTable() {
		return keyframeTable;
	}
	public TwiTablePane<T> setRenderer(Class<?> clazz, TableCellRenderer renderer){
		keyframeTable.setDefaultRenderer(clazz, renderer);
		return this;
	}

	public FloatTrackTableModel<T> getFloatTrackTableModel() {
		return floatTrackTableModel;
	}

	public int[] getSelectedRows(){
		return keyframeTable.getSelectedRows();
	}

	public int getSelectedRow(){
		return keyframeTable.getSelectedRow();
	}

	public int getSelectedColumn(){
		return keyframeTable.getSelectedColumn();
	}
	public int getColumnCount(){
		return keyframeTable.getColumnCount();
	}
	public int getRowCount(){
		return keyframeTable.getRowCount();
	}
	public String getColumnName(int column){
		return keyframeTable.getColumnName(column);
	}
	public int getEditingRow(){
		return keyframeTable.getEditingRow();
	}
	public int getEditingColumn(){
		return keyframeTable.getEditingColumn();
	}
	public Component getEditorComponent(){
		return keyframeTable.getEditorComponent();
	}
	public int getRowHeight(){
		return keyframeTable.getRowHeight();
	}
}
