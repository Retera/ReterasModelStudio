package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.List;

public class AnimationViewer extends JPanel {
	private EditableModel model;
	private final EditableModel blank = new EditableModel();
	private final DefaultComboBoxModel<Animation> animations;
	private final JComboBox<Animation> animationBox;
	private final boolean allowUnanimated;
	private TimeEnvironmentImpl renderEnv;
	private PerspectiveViewport perspectiveViewport;

	public AnimationViewer(ProgramPreferences programPreferences, boolean allowUnanimated) {
		this.allowUnanimated = allowUnanimated;

		getModelViewport();
		JPanel viewportPanel = new JPanel(new BorderLayout());
		setLayout(new MigLayout());

		viewportPanel.add(perspectiveViewport, BorderLayout.CENTER);
		add(viewportPanel, "wrap");

		animations = new DefaultComboBoxModel<>();

		animationBox = new JComboBox<>(animations);
		animationBox.setRenderer(getBoxRenderer());
		animationBox.addActionListener(e -> setAnimation());

		add(animationBox);
		setModel(blank);

	}

	private void setAnimation() {
		if (renderEnv != null) {
			renderEnv.setSequence((Animation) animationBox.getSelectedItem());
			renderEnv.setAnimationTime(0);
		}
	}

	private BasicComboBoxRenderer getBoxRenderer() {
		return new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index,
			                                              boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, value == null
						? "(Unanimated)" : value, index, isSelected, cellHasFocus);
			}
		};
	}

	private void getModelViewport() {
		try {
			perspectiveViewport = new PerspectiveViewport();
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setModel(EditableModel model) {
		animations.removeAllElements();
		this.model = model;
		ModelView modelView = new ModelView(model);

		if (allowUnanimated || (this.model.getAnims().size() == 0)) {
			animations.addElement(null);
		}
		for (Animation animation : this.model.getAnims()) {
			animations.addElement(animation);
		}
		RenderModel renderModel = new RenderModel(this.model, modelView).setVetoOverrideParticles(true);
		renderEnv = renderModel.getTimeEnvironment();
		perspectiveViewport.setModel(modelView, renderModel, true);
		renderEnv.setAnimationTime(0);
		renderEnv.setLive(true);
		renderEnv.setLoopType(LoopType.ALWAYS_LOOP);
		reload();
	}

	public void setTitle(String title) {
		setBorder(BorderFactory.createTitledBorder(title));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		perspectiveViewport.paint(perspectiveViewport.getGraphics());
	}

	public void reloadAllTextures() {
		perspectiveViewport.reloadAllTextures();
	}

	public void reload() {
		Animation selectedItem = (Animation) animationBox.getSelectedItem();
		animations.removeAllElements();

		List<Animation> anims = model.getAnims();
		if (allowUnanimated || (anims.size() == 0)) {
			animations.addElement(null);
		}
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
