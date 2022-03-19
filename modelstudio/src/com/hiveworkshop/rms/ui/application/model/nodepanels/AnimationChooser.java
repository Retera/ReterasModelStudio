package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.viewer.PreviewPanel;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

public class AnimationChooser {

	public AnimationChooser(EditableModel model) {


	}

	public JComboBox<Animation> getAnimationChooser(EditableModel model, DefaultComboBoxModel<Animation> animations, PreviewPanel previewPanel) {
		JComboBox<Animation> animationBox = new JComboBox<>(animations);
		// this prototype is to work around a weird bug where some components take for ever to load
		animationBox.setPrototypeDisplayValue(new Animation("Stand and work for me", 0, 1));
		animationBox.setRenderer(getComboBoxRenderer(model));
//		animationBox.addActionListener(e -> playSelectedAnimation(previewPanel));

		animationBox.setMaximumSize(new Dimension(99999999, 35));
		animationBox.setFocusable(true);
//		animationBox.addMouseWheelListener(this::changeAnimation);
		return animationBox;
	}

	private BasicComboBoxRenderer getComboBoxRenderer(EditableModel model) {
		return new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index,
			                                              boolean isSelected, boolean cellHasFocus) {
				Object display = value == null ? "(Unanimated)" : value;
				if (value != null && model != null) {
					display = "(" + model.getAnims().indexOf(value) + ") " + display;
				}
				return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
			}
		};
	}
}
