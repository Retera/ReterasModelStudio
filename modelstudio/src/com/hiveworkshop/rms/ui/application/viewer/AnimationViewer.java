package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

public class AnimationViewer extends JPanel {
	private ModelView modelView;
	private final AnimatedPerspectiveViewport perspectiveViewport;
	private final DefaultComboBoxModel<Animation> animations;
	private final JComboBox<Animation> animationBox;
	private final boolean allowUnanimated;

	public AnimationViewer(final ModelView modelView, final ProgramPreferences programPreferences, final boolean allowUnanimated) {
		this.modelView = modelView;
		this.allowUnanimated = allowUnanimated;
		try {
			perspectiveViewport = new AnimatedPerspectiveViewport(modelView, programPreferences, true);
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
			perspectiveViewport.setAnimationTime(0);
			perspectiveViewport.setLive(true);
		} catch (final LWJGLException e) {
			throw new RuntimeException(e);
		}
		setLayout(new BorderLayout());
		add(perspectiveViewport, BorderLayout.CENTER);
		animations = new DefaultComboBoxModel<>();
		if (allowUnanimated || (modelView.getModel().getAnims().size() == 0)) {
			animations.addElement(null);
		}
		for (final Animation animation : modelView.getModel().getAnims()) {
			animations.addElement(animation);
		}
		animationBox = new JComboBox<>(animations);
		animationBox.setRenderer(new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList list, final Object value, final int index,
					final boolean isSelected, final boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, value == null
						? "(Unanimated)" : value, index, isSelected, cellHasFocus);
			}
		});
		animationBox.addActionListener(e -> perspectiveViewport.setAnimation((Animation) animationBox.getSelectedItem()));
		add(animationBox, BorderLayout.AFTER_LAST_LINE);

	}

	public void setModel(final ModelView modelView) {
		this.modelView = modelView;
		perspectiveViewport.setModel(modelView);
		reload();
	}

	public void setTitle(final String title) {
		setBorder(BorderFactory.createTitledBorder(title));
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		// perspectiveViewport.repaint();
		perspectiveViewport.paint(perspectiveViewport.getGraphics());
	}

	public void reloadAllTextures() {
		perspectiveViewport.reloadAllTextures();
	}

	public void reload() {
		final Animation selectedItem = (Animation) animationBox.getSelectedItem();
		animations.removeAllElements();
		boolean sawLast = selectedItem == null;
		if (allowUnanimated || (modelView.getModel().getAnims().size() == 0)) {
			animations.addElement(null);
		}
		for (final Animation animation : modelView.getModel().getAnims()) {
			animations.addElement(animation);
			if (animation == selectedItem) {
				sawLast = true;
			}
		}
		perspectiveViewport.reloadTextures();
		if (sawLast && ((selectedItem != null) || allowUnanimated)) {
			animationBox.setSelectedItem(selectedItem);
		} else if (!allowUnanimated && (modelView.getModel().getAnims().size() > 0)) {
			animationBox.setSelectedItem(modelView.getModel().getAnim(0));
		}
	}
}
