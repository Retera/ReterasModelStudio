package com.matrixeater.src.viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.lwjgl.LWJGLException;

import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
import com.hiveworkshop.wc3.gui.modeledit.PerspectiveViewport;
import com.hiveworkshop.wc3.mdl.Animation;

public class AnimationViewer extends JPanel {
	private final MDLDisplay mdlDisp;
	private PerspectiveViewport perspectiveViewport;

	public AnimationViewer(final MDLDisplay mdlDisp) {
		this.mdlDisp = mdlDisp;
		try {
			perspectiveViewport = new PerspectiveViewport(mdlDisp);
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
		} catch (final LWJGLException e) {
			throw new RuntimeException(e);
		}
		add(perspectiveViewport);
		final DefaultComboBoxModel<Animation> animations = new DefaultComboBoxModel<>();
		for (final Animation animation : mdlDisp.getMDL().getAnims()) {
			animations.addElement(animation);
		}
		final JComboBox<Animation> animationBox = new JComboBox<Animation>(animations);
		animationBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		add(animationBox);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(
				layout.createParallelGroup().addComponent(perspectiveViewport).addComponent(animationBox));
		layout.setVerticalGroup(
				layout.createSequentialGroup().addComponent(perspectiveViewport).addComponent(animationBox));
		setLayout(layout);
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		perspectiveViewport.repaint();
	}
}
