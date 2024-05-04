package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeInterpTypeAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveAnimFlagAction;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.tools.GlobalSeqWizard;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.CollapsablePanel;
import com.hiveworkshop.rms.util.TimeLogger;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.function.Function;

public class FlagPanel<T> extends CollapsablePanel {
	private final MultiAnimPanel<T> multiAnimPanel;
	private final JPanel topPanel;
	private JComponent staticComponent;
	private final String title;
	private final String flagToken;
	private final Function<String, T> parseFunction;
	private final T defaultValue;
	private final ModelHandler modelHandler;
	private TimelineContainer node;
	private AnimFlag<T> animFlag;
	private T staticValue;
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	TimeLogger timeLogger;

	public FlagPanel(String flagToken, Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler) {
		this(flagToken, flagToken, parseFunction, defaultValue, modelHandler);
	}

	public FlagPanel(String flagToken, String title, Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler) {
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
	public FlagPanel(String flagToken, String title, Function<String, T> parseFunction, T defaultValue, String valueRegex, String weedingRegex, ModelHandler modelHandler) {
		super(title, new JPanel(new MigLayout("gap 0, ins 0, hidemode 3")));
		this.flagToken = flagToken;
		this.title = title;
		this.parseFunction = parseFunction;
		this.defaultValue = defaultValue;
		this.modelHandler = modelHandler;
		topPanel = new JPanel(new MigLayout("fill, gap 0, ins 0"));
		getCollapsableContentPanel().add(topPanel, "growx, wrap");
		multiAnimPanel = new MultiAnimPanel<>(parseFunction, defaultValue, valueRegex, weedingRegex, modelHandler);
		getCollapsableContentPanel().add(multiAnimPanel);
	}

	public FlagPanel<T> update(TimelineContainer node, AnimFlag<T> animFlag, T staticValue) {
//		timeLogger = new TimeLogger().start();
		this.node = node;
		this.animFlag = animFlag;
		this.staticValue = staticValue;
		topPanel.removeAll();
//		timeLogger.log("removed all");
		if (animFlag != null) {
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
	public FlagPanel<T> update(TimelineContainer node, AnimFlag<T> animFlag) {
		return update(node, animFlag, null);
	}

	public FlagPanel<T> setStaticComponent(JComponent staticComponent) {
		this.staticComponent = staticComponent;
		return this;
	}
	private JPanel staticPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[]"));
		panel.add(new JLabel("Static"));
		JButton button = new JButton("Make Dynamic");
		button.addActionListener(e -> makeDynamic());
		panel.add(button, "right, wrap");
		if (staticComponent != null) {
			panel.add(staticComponent, "spanx");
		}
		return panel;
	}
	private JPanel dynamicPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][][]", "[][]"));
		panel.add(new JLabel("Dynamic"));

		panel.add(Button.create("Make Static", e -> makeStatic()), "right, wrap");

		TwiComboBox<InterpolationType> comboBox = new TwiComboBox<>(InterpolationType.values(), InterpolationType.DONT_INTERP);
		comboBox.setSelectedItem(animFlag.getInterpolationType());
		comboBox.addOnSelectItemListener(this::setInterpolationType);
		panel.add(comboBox);

		if (!animFlag.hasGlobalSeq()) {
			panel.add(Button.create("To GlobalSeq", e -> GlobalSeqWizard.showPopup(modelHandler, animFlag, title + " to GlobalSeq", panel)), "right, wrap");
		} else {
			panel.add(Button.create("Edit GlobalSeq", e -> GlobalSeqWizard.showPopup(modelHandler, animFlag, "Edit " + title, panel)), "right, wrap");
		}
		return panel;
	}

	private void setInterpolationType(InterpolationType type) {
		if (animFlag != null && type != animFlag.getInterpolationType()) {
			UndoAction undoAction = new ChangeInterpTypeAction<>(animFlag, type, changeListener);
			modelHandler.getUndoManager().pushAction(undoAction.redo());
		}
	}

	private void makeStatic() {
		UndoAction action = new RemoveAnimFlagAction(node, animFlag, changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	private void makeDynamic() {
		AnimFlag<T> animFlag = AnimFlagUtils.createNewAnimFlag(defaultValue, flagToken);
		if (animFlag != null) {
			UndoAction action = new AddAnimFlagAction<>(node, animFlag, changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	public void setTableRenderer(TableCellRenderer renderer) {
		multiAnimPanel.setTableRenderer(renderer);
	}
	public void setTableEditor(TableCellEditor editor) {
		multiAnimPanel.setTableEditor(editor);
	}
}
