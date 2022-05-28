package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.model.nodepanels.AnimationChooser;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;

public class AnimationViewer extends JPanel {
	private EditableModel model;
	private final EditableModel blank = new EditableModel();
	private final AnimationChooser animationChooser;
	private final boolean allowUnanimated;
	private PerspectiveViewport perspectiveViewport;

	public AnimationViewer(ProgramPreferences programPreferences, boolean allowUnanimated) {
		this.allowUnanimated = allowUnanimated;

		getModelViewport();
		JPanel viewportPanel = new JPanel(new BorderLayout());
		setLayout(new MigLayout());

		viewportPanel.add(perspectiveViewport, BorderLayout.CENTER);
		add(viewportPanel, "wrap");

		animationChooser = new AnimationChooser(true, false, true).setAllowUnanimated(allowUnanimated);

		add(animationChooser);
		setModel(blank);
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
		this.model = model;
		ModelView modelView = new ModelView(model);

		RenderModel renderModel = new RenderModel(this.model, modelView).setVetoOverrideParticles(true);
		renderModel.getTimeEnvironment().setLoopType(LoopType.ALWAYS_LOOP);

		animationChooser.setModel(model, renderModel);
		animationChooser.chooseSequence(ViewportHelpers.findDefaultAnimation(model));

		perspectiveViewport.setModel(modelView, renderModel, true);
		perspectiveViewport.reloadTextures();
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
		perspectiveViewport.reloadTextures();
	}
}
