package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ParticleEmitterPopcorn;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Vec3;
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


	public ComponentPopcornPanel(ModelHandler modelHandler) {
		super(modelHandler);

		popcornPathField = new ComponentEditorTextField(24, this::texturePathField);
		topPanel.add(popcornPathField, "wrap");

		visGuidPanel = new JPanel(new MigLayout("gap 0", "[]8[]"));
		topPanel.add(visGuidPanel, "wrap");
		topPanel.add(valuePanelsPanel(), "wrap");
	}

	private JPanel valuePanelsPanel() {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0, fill"));
		lifeSpanPanel = new FloatValuePanel(modelHandler, "LifeSpan");
		emissionRatePanel = new FloatValuePanel(modelHandler, "EmissionRate");
		speedPanel = new FloatValuePanel(modelHandler, "Speed");
		alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA);
		visPanel = new FloatValuePanel(modelHandler, "Visibility");

		colorPanel = new ColorValuePanel(modelHandler, MdlUtils.TOKEN_COLOR);

		return panel;
	}

	private void texturePathField(String newPath) {
		idObject.setPath(newPath);
	}

	@Override
	public void updatePanels() {
		popcornPathField.reloadNewValue(idObject.getPath());
		idObject.updateAnimsVisMap(modelHandler.getModel().getAnims());
		updateAnimVisGuidPanel();

		lifeSpanPanel.reloadNewValue(idObject.getLifeSpan(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_LIFE_SPAN), idObject, MdlUtils.TOKEN_LIFE_SPAN, this::setLifeSpan);
		emissionRatePanel.reloadNewValue(idObject.getEmissionRate(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_EMISSION_RATE), idObject, MdlUtils.TOKEN_EMISSION_RATE, this::setEmissionRate);
		speedPanel.reloadNewValue(idObject.getInitVelocity(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_SPEED), idObject, MdlUtils.TOKEN_SPEED, this::setInitVelocity);
		alphaPanel.reloadNewValue(idObject.getAlpha(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_ALPHA), idObject, MdlUtils.TOKEN_ALPHA, this::setAlpha);
		visPanel.reloadNewValue(1f, (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_VISIBILITY), idObject, MdlUtils.TOKEN_VISIBILITY, null);
		colorPanel.reloadNewValue(idObject.getColor(), (Vec3AnimFlag) idObject.find(MdlUtils.TOKEN_COLOR), idObject, MdlUtils.TOKEN_COLOR, this::setColor);
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

	private void setLifeSpan(float value){
		if(idObject.getLifeSpan() != value){
//			undoManager.pushAction(new UndoAction().redo);
			idObject.setLifeSpan(value);
		}
	}
	private void setEmissionRate(float value){
		if(idObject.getEmissionRate() != value){
//			undoManager.pushAction(new UndoAction().redo);
			idObject.setEmissionRate(value);
		}
	}
	private void setInitVelocity(float value){
		if(idObject.getInitVelocity() != value){
//			undoManager.pushAction(new UndoAction().redo);
			idObject.setInitVelocity(value);
		}
	}
	private void setAlpha(float value){
		if(idObject.getAlpha() != value){
//			undoManager.pushAction(new UndoAction().redo);
			idObject.setAlpha(value);
		}
	}
	private void setColor(Vec3 color){
		if(!idObject.getColor().equalLocs(color)){
//			undoManager.pushAction(new UndoAction().redo);
			idObject.setColor(color);
		}
	}
}
