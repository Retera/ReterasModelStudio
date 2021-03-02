package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.List;

public class AnimationViewer extends JPanel {
	private ModelView modelView;
	private final AnimatedPerspectiveViewport perspectiveViewport;
	private final DefaultComboBoxModel<Animation> animations;
	private final JComboBox<Animation> animationBox;
	private final boolean allowUnanimated;
	ComPerspRenderEnv renderEnv;

	public AnimationViewer(final ModelView modelView, final ProgramPreferences programPreferences, final boolean allowUnanimated) {
		this.modelView = modelView;
		this.allowUnanimated = allowUnanimated;
		try {
			renderEnv = new ComPerspRenderEnv();
			modelView.setVetoOverrideParticles(true);
			RenderModel renderModel = new RenderModel(modelView.getModel(), modelView);
			perspectiveViewport = new AnimatedPerspectiveViewport(modelView, renderModel, programPreferences, renderEnv, true);
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
			renderEnv.setAnimationTime(0);
			renderEnv.setLive(true);
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
		animationBox.addActionListener(e -> renderEnv.setAnimation((Animation) animationBox.getSelectedItem()));
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
//		boolean sawLast = selectedItem == null;
		List<Animation> anims = modelView.getModel().getAnims();
		if (allowUnanimated || (anims.size() == 0)) {
			animations.addElement(null);
		}
//		for (final Animation animation : anims) {
//			animations.addElement(animation);
//			if (animation == selectedItem) {
//				sawLast = true;
//			}
//		}
//		System.out.println("allow unanimated: " + allowUnanimated);
		animations.addAll(anims);
		boolean sawLast = (selectedItem == null || anims.contains(selectedItem));
		perspectiveViewport.reloadTextures();
		if (sawLast && ((selectedItem != null) || allowUnanimated)) {
			animationBox.setSelectedItem(selectedItem);
		} else if (!allowUnanimated && (anims.size() > 0)) {
			animationBox.setSelectedItem(anims.get(0));
		}
	}
}
