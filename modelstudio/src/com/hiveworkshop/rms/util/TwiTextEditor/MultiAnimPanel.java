package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MultiAnimPanel<T> extends JPanel {
	private AnimFlag<T> animFlag;
	private final ModelHandler modelHandler;
	private final EditableModel model;
	private AnimatedNode node;
	private final Function<String, T> parseFunction;
	private final T defaultValue;
	private final Map<Sequence, TimelineTableEditor<T>> sequencePanelMap = new HashMap<>();

	public MultiAnimPanel(Function<String, T> parseFunction, T defaultValue, ModelHandler modelHandler){
		super(new MigLayout("fill, wrap 1"));
		this.modelHandler = modelHandler;
		this.model = modelHandler.getModel();
		this.parseFunction = parseFunction;
		this.defaultValue = defaultValue;
	}

	public MultiAnimPanel<T> setNode(AnimatedNode node, AnimFlag<T> animFlag){
		this.animFlag = animFlag;
		this.node = node;
		update();
		return this;
	}

	private void update(){
		sequencePanelMap.keySet().removeIf(s -> !model.contains(s));
		for(int i = getComponentCount()-1; i>=0; i--){
			Component component = getComponent(i);
			if(component instanceof TimelineTableEditor){
				Sequence sequence = ((TimelineTableEditor<?>) component).getSequence();
				if (!model.contains(sequence)){
					remove(i);
				}
			}
		}

		List<Sequence> allSequences = model.getAllSequences();
		for (int i = 0; i < allSequences.size(); i++){
			Sequence sequence = allSequences.get(i);
			TimelineTableEditor<T> tableEditor = sequencePanelMap.computeIfAbsent(sequence, k -> new TimelineTableEditor<>(sequence, parseFunction, defaultValue, modelHandler));
			tableEditor.setNode(node, animFlag);
			if(getComponentCount() <= i || this.getComponent(i) != tableEditor){
//				add(tableEditor, "wrap", i);
				add(tableEditor, i);
			}
		}
	}
}
