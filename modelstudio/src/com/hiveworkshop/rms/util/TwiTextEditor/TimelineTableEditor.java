package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryMapAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.TwiTextEditor.table.TwiTable;
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
	private TimelineContainer node;
	private final TwiTable keyframeTable;
	private TwiTableModel<T> dataModel;
	private final Function<String, T> parseFunction;
	private final T defaultValue;
	private final Map<Integer, Integer> columnSizes = new HashMap<>();
//	private final ModelStructureChangeListener changeListener = null;
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	private Runnable updateStuff;

	public TimelineTableEditor(Sequence sequence, Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler){
		super(sequence + " (" + sequence.getLength() + ") ", new JPanel(new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow][]")));
		this.sequence = sequence;
		this.modelHandler = modelHandler;
		this.parseFunction = parseFunction;
		this.defaultValue = defaultValue;

		keyframeTable = new TwiTable();
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

	public TimelineTableEditor<T> setNode(TimelineContainer node, AnimFlag<T> animFlag) {
		this.animFlag = animFlag;
		this.node = node;
		if(modelHandler != null){
			dataModel = new TwiTableModel<>(animFlag, sequence, defaultValue, modelHandler.getUndoManager());
		} else {
			dataModel = new TwiTableModel<>(animFlag, sequence, defaultValue, null);
		}
		keyframeTable.setModel(dataModel);
		keyframeTable.sizeColumnsToFit(1);
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
			if(0 <= newSelectedRow && newSelectedRow < keyframeTable.getRowCount()){
				keyframeTable.setRowSelectionInterval(newSelectedRow, newSelectedRow);
			}
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
//		Dimension suitableSize = ScreenInfo.getSuitableSize(250, 300, 0.6);
//		Dimension suitableSize2 = ScreenInfo.getSuitableSize(3000, 450, 0.6);
		Dimension suitableSize = ScreenInfo.getSuitableSize(700, 300, 0.6);
		Dimension suitableSize2 = ScreenInfo.getSuitableSize(750, 450, 0.6);
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

		updateStuff = () -> {
//			System.out.println("\nupdateStuff");
			mainPanel.setPreferredSize(suitableSize2);
//			int height1 = getHeight();
//			int spH1 = scrollPane.getHeight();
			SwingUtilities.invokeLater(() -> setPrefSize(suitableSize, scrollPane, mainPanel));
//			int height2 = getHeight();
//			int spH2 = scrollPane.getHeight();
//			System.out.println("Resized! us, height: " + height1 + " -> " + height2);
//			System.out.println("Resized! us, height: " + spH1 + " -> " + spH2);
//			mainPanel.setPreferredSize(suitableSize);
		};
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
//				updateStuff.run();
//				System.out.println("\nself listener");
//
////				int height1 = getHeight();
////				int spH1 = scrollPane.getHeight();
//				setPrefSize(suitableSize, scrollPane, mainPanel);
////				int height2 = getHeight();
////				int spH2 = scrollPane.getHeight();
////				System.out.println("Resized! height: " + height1 + " -> " + height2);
////				System.out.println("Resized! height: " + spH1 + " -> " + spH2);
			}
		});
		keyframeTable.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
//				System.out.println("\nKF listener");
//////				updateStuff.run();
//				System.out.println("height: " + getHeight());
//				System.out.println("SP-height: " + scrollPane.getHeight());
//				System.out.println("RowHeight: " + keyframeTable.getRowHeight());


//				int height1 = getHeight();
//				int spH1 = scrollPane.getHeight();
				if(scrollPane.getHeight() < keyframeTable.getRowHeight()*2){
//					mainPanel.setPreferredSize(suitableSize2);
					updateStuff.run();
					SwingUtilities.invokeLater(() -> {revalidate();repaint();});

				}

				setPrefSize(suitableSize, scrollPane, mainPanel);
//				SwingUtilities.invokeLater(() -> setPrefSize(suitableSize, scrollPane, mainPanel));

//				int height2 = getHeight();
//				int spH2 = scrollPane.getHeight();
//				System.out.println("Resized! KF, height: " + height1 + " -> " + height2);
//				System.out.println("Resized! KF, height: " + spH1 + " -> " + spH2);
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

	public void setPrefSize(Dimension suitableSize, JScrollPane scrollPane, JPanel panel) {
//		System.out.println("KF heignt: " + keyframeTable.getHeight());
//		System.out.println("Header heignt: " + keyframeTable.getTableHeader().getHeight());
		int totTableHeight = keyframeTable.getHeight() + keyframeTable.getTableHeader().getHeight() + 2;
		Dimension size = new Dimension(suitableSize);
		size.height = Math.min(totTableHeight, suitableSize.height);
		int sizeDiff = panel.getHeight() - scrollPane.getHeight();
		scrollPane.setPreferredSize(size);
		Dimension size2 = new Dimension(size.width, size.height + sizeDiff);
		panel.setPreferredSize(size2);
		panel.revalidate();
	}
	public void setPrefSize1(Dimension suitableSize, JScrollPane scrollPane) {
//		System.out.println("\nKF heignt: " + keyframeTable.getHeight());
//		System.out.println("Header heignt: " + keyframeTable.getTableHeader().getHeight());
		int totTableHeight = keyframeTable.getHeight() + keyframeTable.getTableHeader().getHeight() + 2;
		Dimension size = new Dimension(suitableSize);
		size.height = Math.min(totTableHeight, suitableSize.height);
		scrollPane.setPreferredSize(size);
	}
	public void setPrefSize1(Dimension suitableSize, JScrollPane scrollPane, JPanel panel) {
//		System.out.println("\nKF heignt: " + keyframeTable.getHeight());
//		System.out.println("Header heignt: " + keyframeTable.getTableHeader().getHeight());
		int totTableHeight = keyframeTable.getHeight() + keyframeTable.getTableHeader().getHeight() + 2;
		Dimension size = new Dimension(suitableSize);
		size.height = Math.min(totTableHeight, suitableSize.height);
		scrollPane.setPreferredSize(size);
	}
}
