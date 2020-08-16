package com.matrixeater.hacks;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class RigbornRotator extends JPanel {

	public RigbornRotator() {
		final JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft 3 Model", "mdl", "mdx"));
		jFileChooser.setAcceptAllFileFilterUsed(true);
		final JTextField inputField = new JTextField(45);
		final JLabel inputLabel = new JLabel("Input: ");
		final JButton inputButton = new JButton("Browse");
		inputButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int result = jFileChooser.showOpenDialog(RigbornRotator.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					if (jFileChooser.getSelectedFile() != null) {
						inputField.setText(jFileChooser.getSelectedFile().getPath());
					}
				}
			}
		});

		final JTextField outputField = new JTextField(45);
		final JLabel outputLabel = new JLabel("Output: ");
		final JButton outputButton = new JButton("Browse");
		outputButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int result = jFileChooser.showSaveDialog(RigbornRotator.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					if (jFileChooser.getSelectedFile() != null) {
						outputField.setText(jFileChooser.getSelectedFile().getPath());
					}
				}
			}
		});

		final JPanel rotatePanel = new JPanel();
		rotatePanel.setLayout(new GridLayout(4, 3));
		rotatePanel.setBorder(BorderFactory.createTitledBorder("Rotate"));
		final JLabel axisXLabel = new JLabel("Axis X:");
		rotatePanel.add(axisXLabel);
		final JSpinner axisXSpinner = new JSpinner(new SpinnerNumberModel(0.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1));
		rotatePanel.add(axisXSpinner);
		final JLabel axisYLabel = new JLabel("Axis Y:");
		rotatePanel.add(axisYLabel);
		final JSpinner axisYSpinner = new JSpinner(new SpinnerNumberModel(0.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1));
		rotatePanel.add(axisYSpinner);
		final JLabel axisZLabel = new JLabel("Axis Z:");
		rotatePanel.add(axisZLabel);
		final JSpinner axisZSpinner = new JSpinner(new SpinnerNumberModel(1.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1));
		rotatePanel.add(axisZSpinner);

		final JLabel angleLabel = new JLabel("Angle: ");
		rotatePanel.add(angleLabel);
		final JSpinner angleSpinner = new JSpinner(new SpinnerNumberModel(0.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1));
		rotatePanel.add(angleSpinner);

		final JButton generateButton = new JButton("Generate");
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					final EditableModel model = EditableModel.read(new File(inputField.getText()));
					final Helper rootRotation = new Helper("Bone_Rotation");
					rootRotation.setPivotPoint(new Vertex(0, 0, 0));
					final AnimFlag rotationAnimation = new AnimFlag("Rotation");
					rotationAnimation.setInterpType(InterpolationType.LINEAR);
					for (final Animation anim : model.getAnims()) {
						rotationAnimation.addKeyframe(anim.getIntervalStart(),
								new QuaternionRotation(
										new Vertex(((Number) axisXSpinner.getValue()).doubleValue(),
												((Number) axisYSpinner.getValue()).doubleValue(),
												((Number) axisZSpinner.getValue()).doubleValue()),
										Math.toRadians(((Number) angleSpinner.getValue()).doubleValue())));
					}

					for (final IdObject node : model.getIdObjects()) {
						if (node.getParent() == null) {
							node.setParent(rootRotation);
						}
					}
					rootRotation.add(rotationAnimation);

					model.add(rootRotation);

					model.printTo(new File(outputField.getText()));
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		});

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(16)
				.addGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()

						.addComponent(inputLabel).addComponent(inputField).addComponent(inputButton)

				).addGroup(layout.createSequentialGroup()

						.addComponent(outputLabel).addComponent(outputField).addComponent(outputButton)

				).addComponent(rotatePanel).addComponent(generateButton))

				.addGap(16));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(16).addGroup(layout.createParallelGroup()

				.addComponent(inputLabel).addComponent(inputField).addComponent(inputButton)

		).addGroup(layout.createParallelGroup()

				.addComponent(outputLabel).addComponent(outputField).addComponent(outputButton)

		).addComponent(rotatePanel).addComponent(generateButton)

				.addGap(16));

		setLayout(layout);
	}

	public static void asdf() {

		for (int ang = 0; ang < 36; ang++) {
			final InputStream footman = MpqCodebase.get()
					.getResourceAsStream("Units\\Human\\TheCaptain\\TheCaptain.mdx");
			try {
				final EditableModel model = new EditableModel(MdxUtils.loadModel(new BlizzardDataInputStream(footman)));

				final Helper rootRotation = new Helper("Bone_Rotation");
				rootRotation.setPivotPoint(new Vertex(0, 0, 0));
				final AnimFlag rotationAnimation = new AnimFlag("Rotation");
				rotationAnimation.setInterpType(InterpolationType.LINEAR);
				for (final Animation anim : model.getAnims()) {
					rotationAnimation.addKeyframe(anim.getIntervalStart(),
							new QuaternionRotation(new Vertex(0, 0, 1), ang * ((2 * Math.PI) / 36)));
				}

				for (final IdObject node : model.getIdObjects()) {
					if (node.getParent() == null) {
						node.setParent(rootRotation);
					}
				}
				rootRotation.add(rotationAnimation);

				model.add(rootRotation);

				model.printTo(new File(
						"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\CaptainOutput" + ang + ".mdx"));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(final String[] args) {
		final JFrame frame = new JFrame("Rigborn Rotator");
		frame.setContentPane(new RigbornRotator());
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
