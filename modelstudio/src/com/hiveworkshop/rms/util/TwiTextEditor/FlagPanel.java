package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeInterpTypeAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveAnimFlagAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.Function;

public class FlagPanel<T> extends CollapsablePanel {
	private MultiAnimPanel<T> multiAnimPanel;
	private JPanel topPanel;
	private JComponent staticComponent;
	private String title;
	private Function<String, T> parseFunction;
	private T defaultValue;
	private ModelHandler modelHandler;
	private AnimatedNode node;
	private AnimFlag<T> animFlag;
	private T staticValue;
	private ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;

	public FlagPanel(String title, Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler){
		super(title, new JPanel(new MigLayout("gap 0, ins 0, hidemode 3")));
		this.title = title;
		this.parseFunction = parseFunction;
		this.defaultValue = defaultValue;
		this.modelHandler = modelHandler;
		topPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		getCollapsableContentPanel().add(topPanel, "wrap");
		multiAnimPanel = new MultiAnimPanel<>(parseFunction, defaultValue, modelHandler);
		getCollapsableContentPanel().add(multiAnimPanel);
	}

	public FlagPanel<T> update(AnimatedNode node, AnimFlag<T> animFlag, T staticValue){
		this.node = node;
		this.animFlag = animFlag;
		this.staticValue = staticValue;
		topPanel.removeAll();
		if(animFlag != null){
			topPanel.add(dynamicPanel());
			multiAnimPanel.setNode(node, animFlag);
			if (animFlag.hasGlobalSeq()) {
				setTitle(title + " (GlobalSeq: " + animFlag.getGlobalSeq() + ")");
			} else {
				setTitle(title);
			}
			multiAnimPanel.setVisible(true);
		} else {
			topPanel.add(staticPanel());
			multiAnimPanel.setVisible(false);
		}
		return this;
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
		JPanel panel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[]"));
		panel.add(new JLabel("Dynamic"));
		JButton button = new JButton("Make Static");
		button.addActionListener(e -> makeStatic());
		panel.add(button, "right, wrap");
		TwiComboBox<InterpolationType> comboBox = new TwiComboBox<>(InterpolationType.values(), InterpolationType.DONT_INTERP);
		comboBox.setSelectedItem(animFlag.getInterpolationType());
		comboBox.addOnSelectItemListener(this::setInterpolationType);
		panel.add(comboBox);
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

	private void makeDynamic(){
		AnimFlag<T> animFlag = AnimFlagUtils.createNewAnimFlag(defaultValue, title);
		UndoAction action = new AddAnimFlagAction<>(node, animFlag, changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}
}
