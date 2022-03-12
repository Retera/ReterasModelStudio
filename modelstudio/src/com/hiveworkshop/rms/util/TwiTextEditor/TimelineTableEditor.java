package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeInterpTypeAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryMapAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.ValueParserUtil;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.*;
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
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

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
		super(sequence + " (" + sequence.getLength() + ") ", new JPanel(new MigLayout("fill, gap 0, ins 0", "", "[grow][]")));
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

		getEditorPanel();
	}

	public TimelineTableEditor(AnimFlag<T> animFlag, Sequence sequence, Function<String, T> parseFunction, T defaultValue, AnimatedNode node, ModelHandler modelHandler){
		super(sequence + " (" + sequence.getLength() + ") ", new JPanel(new MigLayout("fill, gap 0, ins 0", "", "[grow][]")));
		this.animFlag = animFlag;
		this.sequence = sequence;
		this.modelHandler = modelHandler;
		this.parseFunction = parseFunction;
		this.node = node;
		this.defaultValue = defaultValue;

		keyframeTable = new JTable();
		dataModel = new TwiTableModel<>(animFlag, sequence, defaultValue, null);
		keyframeTable.setModel(dataModel);
		keyframeTable.setDefaultEditor(defaultValue.getClass(), new TwiTableCellEditor<>(parseFunction, defaultValue));
		TwiTableDefaultRenderer renderer = new TwiTableDefaultRenderer(sequence);
		keyframeTable.setDefaultRenderer(Integer.class, renderer);
		keyframeTable.setDefaultRenderer(String.class, renderer);


//		editorPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		keyframeTable.addMouseListener(getMouseAdapter());
		keyframeTable.addKeyListener(getKeyAdapter());

		columnSizes.put(-1, keyframeTable.getRowHeight());
		columnSizes.put(0, keyframeTable.getRowHeight()*4);


		setColumnSizes();
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
			orgTimes.add((int) dataModel.getTimeAt(row));
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

	public JPanel getEditorPanel(){
		JScrollPane scrollPane = new JScrollPane(keyframeTable);
//		JPanel mainPanel = new JPanel(new MigLayout("fill, gap 0, ins 0", "", "[grow][]"));
		Dimension suitableSize = ScreenInfo.getSuitableSize(700, 300, 0.6);
		scrollPane.setPreferredSize(suitableSize);
		JPanel mainPanel = getCollapsableContentPanel();
		mainPanel.add(scrollPane, "spanx, growx, growy, wrap");

		JPanel buttonPanel = new JPanel(new MigLayout("", "", ""));
		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> apply());
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> reset());
		JButton okButton = new JButton("sout editor text");
		okButton.addActionListener(e -> readEditorPane(keyframeTable));
		JButton validateButton = new JButton("Validate");
		JButton linearize = new JButton("linearize");
		linearize.addActionListener(e -> linearize());
		JButton unLinearize = new JButton("unlinearize");
		unLinearize.addActionListener(e -> unLinearize());
		JButton addButton = new JButton("Add");
		addButton.addActionListener(e -> addEntry(animFlag.size(sequence)-1));
//		validateButton.addActionListener(e -> stringToEntryMap(editorPane.getText(), animFlag.tans(), parseFunction));

		buttonPanel.add(addButton, "");
//		buttonPanel.add(applyButton, "");
//		buttonPanel.add(resetButton, "");
//		buttonPanel.add(linearize, "");
//		buttonPanel.add(unLinearize, "");

		mainPanel.add(buttonPanel, "");
//		return mainPanel;



		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				System.out.println("Resized! height: " + getHeight());
				setPrefSize(suitableSize, scrollPane);
			}
		});
		keyframeTable.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				System.out.println("Resized! KF, height: " + getHeight());
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

	private void apply() {
		TreeMap<Integer, Entry<T>> newEntryMap = stringToEntryMap(keyframeTable.toString(), animFlag.tans(), parseFunction);
		SetFlagEntryMapAction<T> action = new SetFlagEntryMapAction<>(animFlag, sequence, newEntryMap, changeListener);
		if(modelHandler != null){
			modelHandler.getUndoManager().pushAction(action.redo());
		} else {
			// Temp for testing
			action.redo();
		}
	}

	private void linearize(){
		ChangeInterpTypeAction<T> action = new ChangeInterpTypeAction<>(animFlag, InterpolationType.LINEAR, changeListener);
//		editorPane.getDefaultRenderer(defalutValue.getClass()).
		if(modelHandler != null){
			modelHandler.getUndoManager().pushAction(action.redo());
		} else {
			// Temp for testing
			action.redo();
		}
		dataModel.fireTableStructureChanged();
		setColumnSizes();
	}
	private void unLinearize(){
		ChangeInterpTypeAction<T> action = new ChangeInterpTypeAction<>(animFlag, InterpolationType.HERMITE, changeListener);
		if(modelHandler != null){
			modelHandler.getUndoManager().pushAction(action.redo());
		} else {
			// Temp for testing
			action.redo();
		}
		dataModel.fireTableStructureChanged();
		setColumnSizes();
	}

	private void reset() {
		TreeMap<Integer, Entry<T>> entryMap = animFlag.getEntryMap(sequence);
//		editorPane.setText(entryMapToString(entryMap, animFlag.tans()));
	}

	private void showPopup(){
		JPanel mainPanel = getEditorPanel();

		mainPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		JFrame ugg = FramePopup.show(mainPanel, null, node.getName() + " - [" + animFlag.getName() + "] " + sequence + " (" + sequence.getLength() + ")");

	}

	public static void main(String[] args){
		Vec3AnimFlag animFlag = new Vec3AnimFlag("Translation");
		Sequence sequence = new Animation("Stand", 0, 2000);
		animFlag.setEntryMap(sequence, stringToEntryMap(getText(), false, Vec3::parseVec3));
		animFlag.setInterpType(InterpolationType.HERMITE);

		Helper helper = new Helper("Mesh03");

//		TimelineTableEditor<Vec3> textEditor = new TimelineTableEditor<>(animFlag, sequence, s -> Vec3.parseVec3(ValueParserUtil.getString(3,s)), new Vec3(0,0,0), helper, null);
		TimelineTableEditor<Vec3> textEditor = new TimelineTableEditor<>(sequence, s -> Vec3.parseVec3(ValueParserUtil.getString(3,s)), new Vec3(0,0,0), null);
		textEditor.setNode(helper, animFlag);
		textEditor.showPopup();
	}

	private static void readEditorPane(JTable editorPane){
		System.out.println(editorPane);
	}

	private static <Q> String entryMapToString(TreeMap<Integer, Entry<Q>> entryMap, boolean tans){
		StringBuilder stringBuilder = new StringBuilder();
		int timeStringL = ("" + entryMap.lastKey()).length();
		for(Entry<Q> entry : entryMap.values()){
			stringBuilder.append(StringPadder.padStringStart("" + entry.getTime(), " ", timeStringL)).append(": ");
			stringBuilder.append(entry.getValue()).append(",\n");
			if (tans) {
				stringBuilder.append("\tInTan  ").append(entry.getInTan()).append(",\n");
				stringBuilder.append("\tOutTan ").append(entry.getOutTan()).append(",\n");
			}
		}
		return stringBuilder.toString();
	}

	private static  <Q> TreeMap<Integer, Entry<Q>> stringToEntryMap(String text, boolean tans, Function<String, Q> parseFunction){
		TreeMap<Integer, Entry<Q>> entryMap = new TreeMap<>();
		String[] entries = text.split("\n+[\\s]*(?=\\d)");

		String timeAndValue = "[\\s\\S]*\\d+:.*\\d[\\s\\S]*";

		String regex = "[ \\t]*\\d+:" +
				"([\\w]*[ \t]*" +
					"[{\\[(]?" +
						"([ \t]*[eE\\d-., \t]+,?[ \t]*)+" +
					"[}\\])]?" +
				"[ \\t]*,?[ \\t]*\\n?)+";
		Pattern pattern = Pattern.compile(regex);
		System.out.println("turning " + entries.length + " string entries into entry map");
		for(String sEntry : entries){
//			System.out.println("sEntry!");
//			if(sEntry.matches(timeAndValue)){
			if(sEntry.matches(timeAndValue) && pattern.matcher(sEntry).matches()){
//			if(pattern.matcher(sEntry).matches()){
//				System.out.println("matches!");
				String[] eLines = sEntry.split("\n");
				String[] time_value = eLines[0].strip().split(":");
				if (time_value.length>1){
					int time = Integer.parseInt("0" + time_value[0].replaceAll("\\D", ""));
					Q value = parseFunction.apply(ValueParserUtil.getString(4, time_value[1].replaceAll("[^-\\d,.eE]", "")));
					Entry<Q> entry = new Entry<>(time, value);
					if(tans){
//						System.out.println("tans");
						entry.unLinearize();
						if(eLines.length>2){
							entry.setInTan(parseFunction.apply(eLines[1].replaceAll("[^-\\d,.eE]", "")));
							entry.setOutTan(parseFunction.apply(eLines[2].replaceAll("[^-\\d,.eE]", "")));
						}
					}
					entryMap.put(entry.time, entry);
				}
			}
		}

		System.out.println("made EntryMap with: " + entryMap.size() + " entries");

		return entryMap;
	}

	private static void regexShits() {
		//		String regex = "\\s*\\d+:(\\{?[-\\d.,e\\s]+}?\\s*,?\\s*\n?){1,3}";
//		String regex = "[ \\t]*\\d+:([\\w]*\\s*\\{?[-\\d.,eE\\s]+}?\\s*,?[ \\t]*\\n?)+";
		String timePart = "[ \\t]*\\d+:";
		String maybeInOutTan = "[\\w]*";
		String maybeStartBracket = "[{[(]?";
		String maybeEndBracket = "[}//])]?";
		String bracketWContent3 = "[{\\[(]?(-?\\d*.?\\d+[eE]?(-?\\d+)?,?[ \t]*)+[}//])]?";
		String bracketWContent3a = "[{[(]?([ \t]*-?\\d*.?\\d+[eE]?(-?\\d+)?,?[ \t]*)+[}\\])]?";
		String bracketWContent2 = maybeStartBracket + "(-?\\d*.?\\d+[eE]?(-?\\d+)?,?[ \t]*)+" + maybeEndBracket;
		String bracketWContent = "\\{?[-\\d.,eE \t]+}?";
		String afterBracket = "[ \\t]*,?[ \\t]*\\n?";
		String numberSection = "(" + maybeInOutTan + "[ \t]*" + bracketWContent3a + afterBracket + ")+";
		String a = "";
//		String regex = timePart + "([\\w]*[ \\t]*\\{?[-\\d.,eE \t]+}?[ \\t]*,?[ \\t]*\\n?)+";
//		String regex = timePart + numberSection;

		String regex = "[ \\t]*\\d+:" +
				"([\\w]*[ \t]*" +
				"[{\\[(]?" +
//						"([ \t]*-?\\d*\\.?\\d+[eE]?(-?\\d+)?,?[ \t]*)+" +
//						"([ \t]*[eE\\d-., \t]*\\d[eE\\d-., \t]*,?[ \t]*)+" +
				"([ \t]*[eE\\d-., \t]+,?[ \t]*)+" +
				"[}\\])]?" +
				"[ \\t]*,?[ \\t]*\\n?)+";
	}

	private static String getText() {
		return "" +
				"    Rotation 84 {\n" +
				"        Linear,\n" +
				"        0: { 0, 0, 0, 1 },\n" +
				"        167: { 0.002586, 0.013252, -0.011348, 0.999844 },\n" +
				"        300: { 0.004215, 0.0216, -0.018498, 0.999586 },\n" +
				"        433: { 0.005045, 0.025855, -0.022141, 0.999407 },\n" +
				"        533: { 0.004961, 0.025425, -0.021774, 0.999427 },\n" +
				"        633: { 0.00437, 0.022395, -0.019178, 0.999555 },\n" +
				"        767: { 0.002994, 0.015342, -0.013138, 0.999791 },\n" +
				"        1133: { -0.002211, -0.011333, 0.009705, 0.999886 },\n" +
				"        1267: { -0.003836, -0.019656, 0.016833, 0.999657 },\n" +
				"        1400: { -0.004898, -0.025101, 0.021496, 0.999441 },\n" +
				"        1500: { -0.005167, -0.026481, 0.022678, 0.999378 },\n" +
				"        1600: { -0.004886, -0.025041, 0.021445, 0.999444 },\n" +
				"        1700: { -0.004118, -0.021101, 0.018071, 0.999605 },\n" +
				"        1833: { -0.002526, -0.012948, 0.011088, 0.999851 },\n" +
				"        2000: { 0, 0, 0, 1 },\n" +
				"        2033: { 0, 0, 0, 1 },\n" +
				"        2200: { 0.002586, 0.013252, -0.011348, 0.999844 },\n" +
				"        2333: { 0.004215, 0.0216, -0.018498, 0.999586 },\n" +
				"        2467: { 0.005045, 0.025855, -0.022141, 0.999407 },\n" +
				"        2567: { 0.004961, 0.025425, -0.021774, 0.999427 },\n" +
				"        2667: { 0.00437, 0.022395, -0.019178, 0.999555 },\n" +
				"        2800: { 0.002994, 0.015342, -0.013138, 0.999791 },\n" +
				"        3167: { -0.002211, -0.011333, 0.009705, 0.999886 },\n" +
				"        3300: { -0.003836, -0.019656, 0.016833, 0.999657 },\n" +
				"        3433: { -0.004898, -0.025101, 0.021496, 0.999441 },\n" +
				"        3533: { -0.005167, -0.026481, 0.022678, 0.999378 },\n" +
				"        3633: { -0.004886, -0.025041, 0.021445, 0.999444 },\n" +
				"        3733: { -0.004118, -0.021101, 0.018071, 0.999605 },\n" +
				"        3867: { -0.002526, -0.012948, 0.011088, 0.999851 },\n" +
				"        4033: { 0, 0, 0, 1 },\n" +
				"        4067: { 0, 0, 0, 1 },\n" +
				"        4300: { 0.002684, 0.013758, -0.011782, 0.999832 },\n" +
				"        4500: { 0.004404, 0.022572, -0.01933, 0.999548 },\n" +
				"        4633: { 0.005002, 0.025636, -0.021954, 0.999417 },\n" +
				"        4800: { 0.004982, 0.025529, -0.021862, 0.999422 },\n" +
				"        4967: { 0.004406, 0.022581, -0.019338, 0.999548 },\n" +
				"        5167: { 0.003191, 0.016352, -0.014003, 0.999763 },\n" +
				"        5800: { -0.002238, -0.01147, 0.009823, 0.999883 },\n" +
				"        6000: { -0.003732, -0.019126, 0.016379, 0.999675 },\n" +
				"        6200: { -0.004775, -0.024471, 0.020957, 0.999469 },\n" +
				"        6400: { -0.005167, -0.026481, 0.022678, 0.999378 },\n" +
				"        6533: { -0.004887, -0.025046, 0.021449, 0.999444 },\n" +
				"        6700: { -0.003866, -0.019811, 0.016966, 0.999652 },\n" +
				"        6867: { -0.002298, -0.011779, 0.010087, 0.999877 },\n" +
				"        7067: { 0, 0, 0, 1 },\n" +
				"        7100: { 0, 0, 0, 1 },\n" +
				"        7367: { 0.002742, 0.014053, -0.012035, 0.999825 },\n" +
				"        7567: { 0.004319, 0.022135, -0.018956, 0.999565 },\n" +
				"        7733: { 0.005016, 0.025703, -0.022012, 0.999414 },\n" +
				"        7900: { 0.004964, 0.025438, -0.021785, 0.999426 },\n" +
				"        8067: { 0.004294, 0.022006, -0.018845, 0.999571 },\n" +
				"        8233: { 0.003165, 0.016221, -0.013891, 0.999766 },\n" +
				"        8800: { -0.002114, -0.010836, 0.009279, 0.999895 },\n" +
				"        9000: { -0.003746, -0.019197, 0.01644, 0.999673 },\n" +
				"        9200: { -0.004844, -0.024824, 0.021259, 0.999454 },\n" +
				"        9367: { -0.005167, -0.026481, 0.022678, 0.999378 },\n" +
				"        9500: { -0.004929, -0.025258, 0.021631, 0.999434 },\n" +
				"        9667: { -0.004055, -0.02078, 0.017795, 0.999617 },\n" +
				"        9867: { -0.002394, -0.012271, 0.010509, 0.999866 },\n" +
				"        10100: { 0, 0, 0, 1 },\n" +
				"        10133: { 0, 0, 0, 1 },\n" +
				"        10233: { 0.000621, 0.003186, -0.002729, 0.99999 },\n" +
				"        10333: { 0.000565, 0.002899, -0.002482, 0.999992 },\n" +
				"        11300: { -0.003186, -0.016329, 0.013984, 0.999763 },\n" +
				"        11633: { -0.003791, -0.019426, 0.016636, 0.999665 },\n" +
				"        11667: { -0.002475, -0.012684, 0.010862, 0.999857 },\n" +
				"        11700: { 0.000994, 0.005094, -0.004362, 0.999977 },\n" +
				"        11733: { 0.005897, 0.030222, -0.025882, 0.99919 },\n" +
				"        11800: { 0.017107, 0.087661, -0.075071, 0.99317 },\n" +
				"        11833: { 0.021968, 0.112571, -0.096404, 0.988711 },\n" +
				"        11867: { 0.025392, 0.130117, -0.11143, 0.984889 },\n" +
				"        11900: { 0.026687, 0.136754, -0.117113, 0.983295 },\n" +
				"        12533: { 0.026687, 0.136754, -0.117113, 0.983295 },\n" +
				"        12567: { 0.026452, 0.135549, -0.116082, 0.983591 },\n" +
				"        12600: { 0.025781, 0.132114, -0.11314, 0.984418 },\n" +
				"        12633: { 0.02473, 0.126725, -0.108525, 0.985672 },\n" +
				"        12700: { 0.021695, 0.111173, -0.095206, 0.988992 },\n" +
				"        12767: { 0.017774, 0.091079, -0.077998, 0.992625 },\n" +
				"        12933: { 0.006955, 0.03564, -0.030521, 0.998874 },\n" +
				"        13000: { 0.003386, 0.017353, -0.014861, 0.999733 },\n" +
				"        13033: { 0.001987, 0.010186, -0.008723, 0.999908 },\n" +
				"        13067: { 0.00092, 0.004716, -0.004038, 0.99998 },\n" +
				"        13100: { 0, 0.001226, -0.00105, 0.999998 },\n" +
				"        13133: { 0, 0, 0, 1 },\n" +
				"    }";
	}
}
