package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryMapAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.TwiTextEditor.table.TwiTableCellEditor;
import com.hiveworkshop.rms.util.TwiTextEditor.table.TwiTableDefaultRenderer;
import com.hiveworkshop.rms.util.TwiTextEditor.table.TwiTableModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TimelineTableEditor<T> extends CollapsablePanel {

	private AnimFlag<T> animFlag;
	private final Sequence sequence;
	private final ModelHandler modelHandler;
	private AnimatedNode node;
	private final JTable keyframeTable;
	private TwiTableModel<T> dataModel;
	private final Function<String, T> parseFunction;
	private final T defaultValue;
	private final Map<Integer, Integer> columnSizes = new HashMap<>();
//	private final ModelStructureChangeListener changeListener = null;
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;

	public TimelineTableEditor(Sequence sequence, Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler){
		super(sequence + " (" + sequence.getLength() + ") ", new JPanel(new MigLayout("fill, gap 0, ins 0", "[grow]", "[][]")));
		this.sequence = sequence;
		this.modelHandler = modelHandler;
		this.parseFunction = parseFunction;
		this.defaultValue = defaultValue;

		keyframeTable = new JTable();
		keyframeTable.setDefaultEditor(defaultValue.getClass(), new TwiTableCellEditor<>(parseFunction, defaultValue));
		TwiTableDefaultRenderer renderer = new TwiTableDefaultRenderer(sequence);
		keyframeTable.setDefaultRenderer(Integer.class, renderer);
		keyframeTable.setDefaultRenderer(String.class, renderer);


//		editorPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		keyframeTable.addMouseListener(getMouseAdapter());
		keyframeTable.addKeyListener(getKeyAdapter());

		columnSizes.put(-1, keyframeTable.getRowHeight());
		columnSizes.put(0, keyframeTable.getRowHeight()*4);

		fillEditorPanel();
	}

	public TimelineTableEditor<T> setNode(AnimatedNode node, AnimFlag<T> animFlag) {
		this.animFlag = animFlag;
		this.node = node;
		dataModel = new TwiTableModel<>(animFlag, sequence, defaultValue, modelHandler.getUndoManager());
		keyframeTable.setModel(dataModel);
		setColumnSizes();
		setTitle(sequence + " (" + sequence.getLength() + ") " + animFlag.size(sequence) + " keyframes");
//		dataModel.fireTableStructureChanged();
		return this;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public TimelineTableEditor<T> setDefaultRenderer(Class<?> columnClass, TableCellRenderer renderer) {
		keyframeTable.setDefaultRenderer(columnClass, renderer);
		return this;
	}

	public TimelineTableEditor<T> setDefaultEditor(Class<?> columnClass, TableCellEditor editor) {
		keyframeTable.setDefaultEditor(columnClass, editor);
		return this;
	}

	private void setColumnSizes() {
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
	}

	private MouseAdapter getMouseAdapter(){
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (keyframeTable.getSelectedColumn() == keyframeTable.getColumnCount()-1){
					removeEntry(keyframeTable.getSelectedRows());
				}
			}
		};
	}

	private void removeEntry(int... rows) {
		ArrayList<Integer> orgTimes = new ArrayList<>();
		for (int row : rows){
			orgTimes.add(dataModel.getTimeAt(row));
		}
		if (!orgTimes.isEmpty()) {
			UndoAction action = new RemoveFlagEntryAction<>(animFlag, orgTimes, sequence, changeListener);
			if(modelHandler != null){
				modelHandler.getUndoManager().pushAction(action.redo());
			} else {
				// Temp for testing
				action.redo();
			}
			dataModel.fireTableDataChanged();

			int newSelectedRow = rows[0];
			if(newSelectedRow >= keyframeTable.getRowCount()){
				newSelectedRow -= 1;
			}
			keyframeTable.setRowSelectionInterval(newSelectedRow, newSelectedRow);
		}

	}
	private KeyAdapter getKeyAdapter(){
		return new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
//				System.out.println("keyReleased: " + e.getKeyCode());

				if (e.getKeyCode() == KeyEvent.VK_ENTER
						&& keyframeTable.getSelectedColumn() == keyframeTable.getColumnCount()-1) {
					removeEntry(keyframeTable.getSelectedRows());
				}
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeEntry(keyframeTable.getSelectedRows());
				}
				if ((e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD)) {
					addEntry(keyframeTable.getSelectedRow());
				}
			}
		};
	}

	private void addEntry(int row) {
		if (animFlag != null) {
			Entry<T> newEntry;
			if (animFlag.getEntryMap(sequence) == null || animFlag.getEntryMap(sequence).isEmpty()) {
				newEntry = new Entry<>(0, defaultValue);
				if (animFlag.tans()) {
					newEntry.unLinearize();
				}
			} else {
				int time = animFlag.getTimeFromIndex(sequence, row);
				newEntry = animFlag.getEntryAt(sequence, time).deepCopy();

				while (animFlag.hasEntryAt(sequence, newEntry.getTime())) {
					newEntry.setTime(newEntry.getTime() + 1);
				}
			}

			UndoAction action = new AddFlagEntryAction<>(animFlag, newEntry, sequence, changeListener);
			if(modelHandler != null){
				modelHandler.getUndoManager().pushAction(action.redo());
			} else {
				// Temp for testing
				action.redo();
			}
			dataModel.fireTableDataChanged();
			keyframeTable.setEditingRow(row+1);
//			keyframeTable.setRowSelectionInterval(row+1, row+1);

		}
	}

	private void editAsText(){
		TimelineTextEditor<T> editor = new TimelineTextEditor<>(animFlag, sequence, parseFunction, node, modelHandler);
		editor.show();
	}

	private void clearAnimation(){
		modelHandler.getUndoManager().pushAction(new RemoveFlagEntryMapAction<>(animFlag, sequence, changeListener).redo());
	}

	public JPanel fillEditorPanel(){
		JScrollPane scrollPane = new JScrollPane(keyframeTable);
		Dimension suitableSize = ScreenInfo.getSuitableSize(700, 300, 0.6);
		scrollPane.setPreferredSize(suitableSize);
		JPanel mainPanel = getCollapsableContentPanel();
		mainPanel.add(scrollPane, "spanx, growx, growy, wrap");

		JPanel buttonPanel = new JPanel(new MigLayout("fill", "", ""));

		JButton addButton = new JButton("Add");
		addButton.addActionListener(e -> addEntry(animFlag.size(sequence)-1));
		buttonPanel.add(addButton, "");

		JButton editAsTextButton = new JButton("Edit as text");
		editAsTextButton.addActionListener(e -> editAsText());
		buttonPanel.add(editAsTextButton, "right");

		JButton clearAnimationButton = new JButton("Clear Animation");
		clearAnimationButton.addActionListener(e -> clearAnimation());
		buttonPanel.add(clearAnimationButton, "right");

		mainPanel.add(buttonPanel, "growx");

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
//				System.out.println("Resized! height: " + getHeight());
				setPrefSize(suitableSize, scrollPane);
			}
		});
		keyframeTable.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
//				System.out.println("Resized! KF, height: " + getHeight());
				setPrefSize(suitableSize, scrollPane);
			}
		});
//		keyframeTable.addContainerListener(new ContainerAdapter() {
//			@Override
//			public void componentAdded(ContainerEvent e) {
//				super.componentAdded(e);
//				System.out.println("Added!");
//				setPrefSize(suitableSize, scrollPane);
//			}
//
//			@Override
//			public void componentRemoved(ContainerEvent e) {
//				super.componentRemoved(e);
//				System.out.println("Eemoved!");
//				setPrefSize(suitableSize, scrollPane);
//			}
//		});

		return this;
	}
	public void setPrefSize(Dimension suitableSize, JScrollPane scrollPane) {
		int totTableHeight = keyframeTable.getHeight() + keyframeTable.getTableHeader().getHeight() + 2;
		Dimension size = new Dimension(suitableSize);
		size.height = Math.min(totTableHeight, suitableSize.height);
		scrollPane.setPreferredSize(size);
	}
}
