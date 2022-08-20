package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MultiAnimPanel<T> extends JPanel {
	private AnimFlag<T> animFlag;
	private final ModelHandler modelHandler;
	private final EditableModel model;
	private TimelineContainer node;
	private final Function<String, T> parseFunction;
	private final T defaultValue;
	private final Map<Sequence, TimelineTableEditor<T>> sequencePanelMap = new HashMap<>();
	private TableCellRenderer renderer;
	private TableCellEditor editor;

	public MultiAnimPanel(Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler){
		super(new MigLayout("fill, wrap 1"));
		this.modelHandler = modelHandler;
		this.model = modelHandler.getModel();
		this.parseFunction = parseFunction;
		this.defaultValue = defaultValue;
	}

	public MultiAnimPanel<T> setNode(TimelineContainer node, AnimFlag<T> animFlag){
		this.animFlag = animFlag;
		this.node = node;
		update();
		return this;
	}

	private void update(){
//		new Exception().printStackTrace();
		sequencePanelMap.keySet().removeIf(s -> !model.contains(s));
		for(int i = getComponentCount()-1; i>=0; i--){
			Component component = getComponent(i);
			if(component instanceof TimelineTableEditor){
				Sequence sequence = ((TimelineTableEditor<?>) component).getSequence();
				if (!model.contains(sequence) || !sequencePanelMap.containsKey(sequence)){
					remove(i);
					System.out.println("removed table for " + sequence);
				} else {
					component.setVisible(false);
				}
			}
		}


		List<Sequence> allSequences = model.getAllSequences();
		int compCount = 0;
		for (Sequence sequence : allSequences) {
			if (animFlag.hasSequence(sequence) || !animFlag.hasGlobalSeq() && sequence instanceof Animation) {
				TimelineTableEditor<T> tableEditor = sequencePanelMap.computeIfAbsent(sequence, k -> new TimelineTableEditor<>(sequence, parseFunction, defaultValue, modelHandler));
				tableEditor.setVisible(true);
				if(renderer != null){
					tableEditor.setDefaultRenderer(defaultValue.getClass(), renderer);
				}
				if(editor != null){
					tableEditor.setDefaultEditor(defaultValue.getClass(), editor);
				}
				tableEditor.setNode(node, animFlag);
				if (getComponentCount() <= compCount || this.getComponent(compCount) != tableEditor) {
					add(tableEditor, compCount);
				}
				compCount++;
			}
		}
	}

	protected void setTableRenderer(TableCellRenderer renderer){
		this.renderer = renderer;
	}
	protected void setTableEditor(TableCellEditor editor){
		this.editor = editor;
	}
}
