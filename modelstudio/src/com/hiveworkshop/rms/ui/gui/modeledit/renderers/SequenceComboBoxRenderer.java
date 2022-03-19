package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

public class SequenceComboBoxRenderer extends BasicComboBoxRenderer {
	private ModelHandler modelHandler;

	public SequenceComboBoxRenderer() {
		super();
	}

	public SequenceComboBoxRenderer(ModelHandler modelHandler) {
		super();
		this.modelHandler = modelHandler;
	}

	public SequenceComboBoxRenderer setModelHandler(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		return this;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		String name = "";
		if (modelHandler != null) {
			if (value instanceof Animation) {
				name = "(" + modelHandler.getModel().getAnims().indexOf(value) + ") " + ((Animation) value).getName() + " (" + ((Animation) value).getLength() + ")";
			} else if (value instanceof GlobalSeq) {
				name = "GlobalSeq " + modelHandler.getModel().getGlobalSeqId((GlobalSeq) value);
			} else {
				name = "(Unanimated)";
			}
		}
//		Object display = value == null ? "(Unanimated)" : value;
//		if (value != null && modelHandler != null) {
//			display = "(" + modelHandler.getModel().getAnims().indexOf(value) + ") " + display;
//		}
		return super.getListCellRendererComponent(list, name, index, isSel, hasFoc);
	}

}
