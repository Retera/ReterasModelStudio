package com.matrixeater.imp;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.EditableModel;

public class FaceEffectsAreNotModdableGUI extends JPanel {
	public FaceEffectsAreNotModdableGUI(final EditableModel model) {
		final JLabel audioFileLabel = new JLabel("Audio File:");
		final JTextField audioFilePath = new JTextField();
		final JButton audioFileBrowseBtn = new JButton("Browse");
		final JLabel baseSequenceLabel = new JLabel("Base Sequence:");
		final DefaultComboBoxModel<Animation> animations = new DefaultComboBoxModel<>();
		final JComboBox<Animation> baseSequenceChooser = new JComboBox<>();
	}
}
