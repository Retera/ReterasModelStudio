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
	private ModelView modelView;
	final EditableModel blank = new EditableModel();
	private final DefaultComboBoxModel<Animation> animations;
	private final JComboBox<Animation> animationBox;
	private final boolean allowUnanimated;
	TimeEnvironmentImpl renderEnv;
	private PerspectiveViewport perspectiveViewport;

	public AnimationViewer(ModelView modelView, ProgramPreferences programPreferences, boolean allowUnanimated) {
		this.allowUnanimated = allowUnanimated;

		getModelViewport(modelView, programPreferences);
		JPanel viewportPanel = new JPanel(new BorderLayout());
		setLayout(new MigLayout());

		viewportPanel.add(perspectiveViewport, BorderLayout.CENTER);
		add(viewportPanel, "wrap");

		animations = new DefaultComboBoxModel<>();

		if (allowUnanimated || (this.modelView.getModel().getAnims().size() == 0)) {
			animations.addElement(null);
		}
		for (Animation animation : this.modelView.getModel().getAnims()) {
			animations.addElement(animation);
		}
		animationBox = new JComboBox<>(animations);
		animationBox.setRenderer(getBoxRenderer());
		animationBox.addActionListener(e -> renderEnv.setAnimation((Animation) animationBox.getSelectedItem()));

		add(animationBox);

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

	private void getModelViewport(ModelView modelView, ProgramPreferences programPreferences) {
		try {
			renderEnv = new TimeEnvironmentImpl();
			if (modelView == null) {
				this.modelView = new ModelView(blank, renderEnv);
			} else {
				this.modelView = modelView;
			}
			this.modelView.setVetoOverrideParticles(true);
			RenderModel renderModel = new RenderModel(this.modelView.getModel(), this.modelView, renderEnv);
			perspectiveViewport = new PerspectiveViewport(this.modelView, renderModel, renderEnv, true);
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
			renderEnv.setAnimationTime(0);
			renderEnv.setLive(true);
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setModel(ModelView modelView) {
		if (modelView == null) {
			modelView = new ModelView(blank, renderEnv);
		}
		this.modelView = modelView;
		perspectiveViewport.setModel(modelView);
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
//		boolean sawLast = selectedItem == null;
		List<Animation> anims = modelView.getModel().getAnims();
		if (allowUnanimated || (anims.size() == 0)) {
			animations.addElement(null);
		}
//		for (Animation animation : anims) {
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
