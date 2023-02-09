package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeInterpTypeAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.tools.GlobalSeqWizard;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.TimeLogger;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.TreeMap;
import java.util.function.Function;

public class FlagPanel<T> extends CollapsablePanel {
	private MultiAnimPanel<T> multiAnimPanel;
	private JPanel topPanel;
	private JComponent staticComponent;
	private String title;
	private String flagToken;
	private Function<String, T> parseFunction;
	private T defaultValue;
	private ModelHandler modelHandler;
	private TimelineContainer node;
	private AnimFlag<T> animFlag;
	private T staticValue;
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	TimeLogger timeLogger;

	public FlagPanel(String flagToken, Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler){
		this(flagToken, flagToken, parseFunction, defaultValue, modelHandler);
	}

//	public FlagPanel(String flagToken, String title, Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler){
//		this(flagToken, title, parseFunction, defaultValue, modelHandler, new TimeLogger().start());
//	}
//	public FlagPanel(String flagToken, String title, Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler, TimeLogger timeLogger){
//		super(title, new JPanel(new MigLayout("gap 0, ins 0, hidemode 3")));
//		timeLogger.log("Initiated Collapsable panel");
//		this.timeLogger = timeLogger;
//		this.flagToken = flagToken;
//		this.title = title;
//		this.parseFunction = parseFunction;
//		this.defaultValue = defaultValue;
//		this.modelHandler = modelHandler;
//		topPanel = new JPanel(new MigLayout("fill, gap 0, ins 0"));
//		timeLogger.log("created topPanel");
//		getCollapsableContentPanel().add(topPanel, "growx, wrap");
//		timeLogger.log("added topPanel");
//		multiAnimPanel = new MultiAnimPanel<>(parseFunction, defaultValue, modelHandler);
//		timeLogger.log("created multiAnimPanel");
//		getCollapsableContentPanel().add(multiAnimPanel);
//		timeLogger.log("added multiAnimPanel");
//		System.out.println("Flagpanel - " + title);
//		timeLogger.print();
//	}
	public FlagPanel(String flagToken, String title, Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler){
		super(title, new JPanel(new MigLayout("gap 0, ins 0, hidemode 3")));
		this.flagToken = flagToken;
		this.title = title;
		this.parseFunction = parseFunction;
		this.defaultValue = defaultValue;
		this.modelHandler = modelHandler;
		topPanel = new JPanel(new MigLayout("fill, gap 0, ins 0"));
		getCollapsableContentPanel().add(topPanel, "growx, wrap");
		multiAnimPanel = new MultiAnimPanel<>(parseFunction, defaultValue, modelHandler);
		getCollapsableContentPanel().add(multiAnimPanel);
	}

	public FlagPanel<T> update(TimelineContainer node, AnimFlag<T> animFlag, T staticValue){
//		timeLogger = new TimeLogger().start();
		this.node = node;
		this.animFlag = animFlag;
		this.staticValue = staticValue;
		topPanel.removeAll();
//		timeLogger.log("removed all");
		if(animFlag != null){
//			topPanel.add(dynamicPanel(), "grow");
			topPanel.add(dynamicPanel(), "growx, growy");
//			timeLogger.log("added dynamicPanel");
			multiAnimPanel.setNode(node, animFlag);
//			timeLogger.log("multiAnimPanel.setNode");
			if (animFlag.hasGlobalSeq()) {
				setTitle(title + " (GlobalSeq: " + animFlag.getGlobalSeq() + ")");
			} else {
				setTitle(title);
			}
//			timeLogger.log("set title");
			multiAnimPanel.setVisible(true);
//			timeLogger.log("multiAnimPanel.setVisible");
		} else {
			setTitle(title);
//			topPanel.add(staticPanel(), "grow");
			topPanel.add(staticPanel(), "growx, growy");
//			timeLogger.log("added static panel");
			multiAnimPanel.setVisible(false);
		}
//		System.out.println("Flagpanel - " + title + " for " + node);
//		timeLogger.print();
		return this;
	}
	public FlagPanel<T> update(TimelineContainer node, AnimFlag<T> animFlag){
//		timeLogger = new TimeLogger().start();
//		this.node = node;
//		this.animFlag = animFlag;
//		topPanel.removeAll();
//		timeLogger.log("removed all");
//		if(animFlag != null){
////			topPanel.add(dynamicPanel(), "grow");
//			topPanel.add(dynamicPanel(), "growx, growy");
//			multiAnimPanel.setNode(node, animFlag);
//			if (animFlag.hasGlobalSeq()) {
//				setTitle(title + " (GlobalSeq: " + animFlag.getGlobalSeq() + ")");
//			} else {
//				setTitle(title);
//			}
//			multiAnimPanel.setVisible(true);
//		} else {
//			setTitle(title);
////			topPanel.add(staticPanel(), "grow");
//			topPanel.add(staticPanel(), "growx, growy");
//			multiAnimPanel.setVisible(false);
//		}
//		return this;
		return update(node, animFlag, null);
	}

	public FlagPanel<T> setStaticComponent(JComponent staticComponent){
		this.staticComponent = staticComponent;
		return this;
	}
	private JPanel staticPanel(){
		JPanel panel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[]"));
		panel.add(new JLabel("Static"));
		JButton button = new JButton("Make Dynamic");
		button.addActionListener(e -> makeDynamic());
		panel.add(button, "right, wrap");
		if(staticComponent != null) {
			panel.add(staticComponent, "spanx");
		}
		return panel;
	}
	private JPanel dynamicPanel(){
		JPanel panel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[][]"));
		panel.add(new JLabel("Dynamic"));

		JButton button = new JButton("Make Static");
		button.addActionListener(e -> makeStatic());
		panel.add(button, "right, wrap");

		TwiComboBox<InterpolationType> comboBox = new TwiComboBox<>(InterpolationType.values(), InterpolationType.DONT_INTERP);
		comboBox.setSelectedItem(animFlag.getInterpolationType());
		comboBox.addOnSelectItemListener(this::setInterpolationType);
		panel.add(comboBox);

		if(!animFlag.hasGlobalSeq()){
			JButton button2 = new JButton("To GlobalSeq");
//		    button2.addActionListener(e -> turnIntoGlobalSeq());
//			button2.addActionListener(e -> GlobalSeqWizard.showPopup(modelHandler, animFlag, title + " to GlobalSeq", this));
			button2.addActionListener(e -> GlobalSeqWizard.showPopup(modelHandler, animFlag, title + " to GlobalSeq", button2));
			panel.add(button2, "right, wrap");
		} else {
			JButton button2 = new JButton("Edit GlobalSeq");
//			button2.addActionListener(e -> GlobalSeqWizard.showPopup(modelHandler, animFlag, "Edit " + title, this));
			button2.addActionListener(e -> GlobalSeqWizard.showPopup(modelHandler, animFlag, "Edit " + title, button2));
			panel.add(button2, "right, wrap");
		}
		return panel;
	}

	private void setInterpolationType(InterpolationType type){
		if(animFlag != null && type != animFlag.getInterpolationType()){
			UndoAction undoAction = new ChangeInterpTypeAction<>(animFlag, type, changeListener);
			modelHandler.getUndoManager().pushAction(undoAction.redo());
		}
	}

	private void makeStatic(){
		UndoAction action = new RemoveAnimFlagAction(node, animFlag, changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	private void turnIntoGlobalSeq(){
		if(animFlag != null){
			Sequence firstSequence = null;
			for(Sequence sequence : modelHandler.getModel().getAllSequences()){
				if(animFlag.hasSequence(sequence)){
					firstSequence = sequence;
					break;
				}
			}
			GlobalSeq globalSeq = new GlobalSeq(1000);
			TreeMap<Integer, Entry<T>> entryMap;
			if(firstSequence != null){
				globalSeq.setLength(firstSequence.getLength());
				entryMap = animFlag.getSequenceEntryMapCopy(firstSequence);
			} else {
				entryMap = new TreeMap<>();
			}
//			UndoAction addEntryMapAction = new AddFlagEntryMapAction<>(animFlag, globalSeq, entryMap, changeListener);
			if(!modelHandler.getModel().contains(globalSeq)){
				UndoAction addEntryMapAction = new AddFlagEntryMapAction<>(animFlag, globalSeq, entryMap, null);
				AddSequenceAction addSequenceAction = new AddSequenceAction(modelHandler.getModel(), globalSeq, null);

				CompoundAction action = new CompoundAction("Set to GlobalSeq", changeListener::animationParamsChanged, addSequenceAction, addEntryMapAction);
				modelHandler.getUndoManager().pushAction(action.redo());
			} else {

				UndoAction addEntryMapAction = new AddFlagEntryMapAction<>(animFlag, globalSeq, entryMap, changeListener);
				modelHandler.getUndoManager().pushAction(addEntryMapAction.redo());
			}
		}
	}

	private void makeDynamic(){
		AnimFlag<T> animFlag = AnimFlagUtils.createNewAnimFlag(defaultValue, flagToken);
		UndoAction action = new AddAnimFlagAction<>(node, animFlag, changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	public void setTableRenderer(TableCellRenderer renderer){
		multiAnimPanel.setTableRenderer(renderer);
	}
	public void setTableEditor(TableCellEditor editor){
		multiAnimPanel.setTableEditor(editor);
	}
}
