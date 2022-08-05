package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryMapAction;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.ValueParserUtil;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.StringPadder;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

public class TimelineTextEditor<T> {

	private final AnimFlag<T> animFlag;
	private final Sequence sequence;
	private final ModelHandler modelHandler;
	private final TimelineContainer node;
	private final JEditorPane editorPane;
	private final Function<String, T> parseFunction;
	private final DocumentUndoManager undoManager;
	private final ModelStructureChangeListener changeListener;

	public TimelineTextEditor(AnimFlag<T> animFlag, Sequence sequence, Function<String, T> parseFunction, TimelineContainer node, ModelHandler modelHandler){
		this.animFlag = animFlag;
		this.sequence = sequence;
		this.modelHandler = modelHandler;
		this.parseFunction = parseFunction;
		this.node = node;

		editorPane = new JEditorPane();
		editorPane.setText(entryMapToString(animFlag.getEntryMap(sequence), animFlag.tans()));
		editorPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		if(modelHandler == null){
			undoManager = new DocumentUndoManager(true);
			changeListener = null;
		} else {
			undoManager = new DocumentUndoManager();
			changeListener = ModelStructureChangeListener.changeListener;
		}
		editorPane.getDocument().addUndoableEditListener(undoManager);
	}


	public JPanel getEditorPanel(){
		JScrollPane scrollPane = new JScrollPane(editorPane);
		JPanel mainPanel = new JPanel(new MigLayout("fill, gap 0, ins 0", "", "[grow][]"));
		mainPanel.add(scrollPane, "spanx, growx, growy, wrap");

		JPanel buttonPanel = new JPanel(new MigLayout("", "", ""));
		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> apply());
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> reset());

		buttonPanel.add(applyButton, "");
		buttonPanel.add(resetButton, "");

		mainPanel.add(buttonPanel, "");
		return mainPanel;
	}

	private void apply() {
		TreeMap<Integer, Entry<T>> newEntryMap = stringToEntryMap(editorPane.getText(), animFlag.tans(), parseFunction);
		SetFlagEntryMapAction<T> action = new SetFlagEntryMapAction<>(animFlag, sequence, newEntryMap, changeListener);
		if(modelHandler != null){
			modelHandler.getUndoManager().pushAction(action.redo());
		} else {
			// Temp for testing
			action.redo();
		}
	}

	private void reset() {
		TreeMap<Integer, Entry<T>> entryMap = animFlag.getEntryMap(sequence);
		editorPane.setText(entryMapToString(entryMap, animFlag.tans()));
	}

	public void show(){
		JPanel mainPanel = getEditorPanel();

		mainPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		String nameString = node instanceof Named ? ((Named) node).getName() : node.getClass().getSimpleName();
		JFrame ugg = FramePopup.show(mainPanel, null, nameString + " - [" + animFlag.getName() + "] " + sequence + " (" + sequence.getLength() + ")");

		ActionMap actionMap = new ActionMap();
		actionMap.put(undoManager.getRedoAction().toString(), undoManager.getRedoAction());
		actionMap.put(undoManager.getUndoAction().toString(), undoManager.getUndoAction());

		ugg.getRootPane().setActionMap(actionMap);


		InputMap inputMap = new InputMap();
		inputMap.put((KeyStroke) undoManager.getRedoAction().getValue(Action.ACCELERATOR_KEY), undoManager.getRedoAction().toString());
		inputMap.put((KeyStroke) undoManager.getUndoAction().getValue(Action.ACCELERATOR_KEY), undoManager.getUndoAction().toString());
		ugg.getRootPane().setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
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

	public static <Q> TreeMap<Integer, Entry<Q>> stringToEntryMap(String text, boolean tans, Function<String, Q> parseFunction){
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
			if(sEntry.matches(timeAndValue) && pattern.matcher(sEntry).matches()){
				String[] eLines = sEntry.split("\n");
				if(tans && eLines.length<3){
					eLines = sEntry.split("(,\\s*\\{)|([Ta][Aa][Nn])");
				}
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
}
