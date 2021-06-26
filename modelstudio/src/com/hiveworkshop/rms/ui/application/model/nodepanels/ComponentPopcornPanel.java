package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ParticleEmitterPopcorn;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentPopcornPanel extends ComponentIdObjectPanel<ParticleEmitterPopcorn> {
	private final ComponentEditorTextField popcornPathField;
	JPanel visGuidPanel;
	private FloatValuePanel alphaPanel;
	private FloatValuePanel lifeSpanPanel;
	private FloatValuePanel emissionRatePanel;
	private FloatValuePanel speedPanel;
	private FloatValuePanel visPanel;
	private ColorValuePanel colorPanel;


	public ComponentPopcornPanel(ModelHandler modelHandler, ModelStructureChangeListener changeListener) {
		super(modelHandler, changeListener);

		popcornPathField = new ComponentEditorTextField(24);
		popcornPathField.addEditingStoppedListener(this::texturePathField);
		topPanel.add(popcornPathField, "wrap");

		visGuidPanel = new JPanel(new MigLayout("gap 0", "[]8[]"));
		topPanel.add(visGuidPanel, "wrap");
		topPanel.add(valuePanelsPanel(), "wrap");
	}

	private JPanel valuePanelsPanel() {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0, fill"));
		lifeSpanPanel = new FloatValuePanel(modelHandler, "LifeSpan", modelHandler.getUndoManager(), changeListener);
		emissionRatePanel = new FloatValuePanel(modelHandler, "EmissionRate", modelHandler.getUndoManager(), changeListener);
		speedPanel = new FloatValuePanel(modelHandler, "Speed", modelHandler.getUndoManager(), changeListener);
		alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA, modelHandler.getUndoManager(), changeListener);
		visPanel = new FloatValuePanel(modelHandler, "Visibility", modelHandler.getUndoManager(), changeListener);

		colorPanel = new ColorValuePanel(modelHandler, MdlUtils.TOKEN_COLOR, modelHandler.getUndoManager(), changeListener);

		return panel;
	}

	private void texturePathField() {
		idObject.setPath(popcornPathField.getText());
	}

	@Override
	public void updatePanels() {
		popcornPathField.reloadNewValue(idObject.getPath());
		idObject.updateAnimsVisMap(modelHandler.getModel().getAnims());
		updateAnimVisGuidPanel();

		lifeSpanPanel.reloadNewValue(idObject.getLifeSpan(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_LIFE_SPAN), idObject, MdlUtils.TOKEN_LIFE_SPAN, idObject::setLifeSpan);
		emissionRatePanel.reloadNewValue(idObject.getEmissionRate(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_EMISSION_RATE), idObject, MdlUtils.TOKEN_EMISSION_RATE, idObject::setEmissionRate);
		speedPanel.reloadNewValue(idObject.getInitVelocity(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_SPEED), idObject, MdlUtils.TOKEN_SPEED, idObject::setInitVelocity);
		alphaPanel.reloadNewValue(idObject.getAlpha(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_ALPHA), idObject, MdlUtils.TOKEN_ALPHA, idObject::setAlpha);
		visPanel.reloadNewValue(1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_VISIBILITY), idObject, MdlUtils.TOKEN_VISIBILITY, null);
		colorPanel.reloadNewValue(idObject.getColor(), (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_COLOR), idObject, MdlUtils.TOKEN_COLOR, idObject::setColor);
	}

	private JPanel updateAnimVisGuidPanel() {
		visGuidPanel.removeAll();
		for (Animation animation : modelHandler.getModel().getAnims()) {
			visGuidPanel.add(new JLabel(animation.getName()));
			JButton button = new JButton(idObject.getAnimVisState(animation).name());
			button.addActionListener(e -> setState(animation, button));
			visGuidPanel.add(button, "wrap, growx");
		}
		return visGuidPanel;
	}

	private ParticleEmitterPopcorn.State setState(Animation animation, JButton parent) {
		JPopupMenu popupMenu = new JPopupMenu();
		for (ParticleEmitterPopcorn.State state : ParticleEmitterPopcorn.State.values()) {
			JMenuItem menuItem = new JMenuItem(state.name());
			menuItem.addActionListener(e -> {
				idObject.setAnimVisState(animation, state);
				parent.setText(state.name());
			});
			popupMenu.add(menuItem);
		}
		popupMenu.show(parent, parent.getWidth(), 0);
		return ParticleEmitterPopcorn.State.none;
	}
}
