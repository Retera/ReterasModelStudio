package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ParticleEmitterPopcorn;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ExportInternal;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentPopcornPanel extends ComponentIdObjectPanel<ParticleEmitterPopcorn> {
	private final ComponentEditorTextField popcornPathField;
	JPanel visGuidPanel;
	private EditorHelpers.FloatEditor alphaPanel;
	private EditorHelpers.FloatEditor lifeSpanPanel;
	private EditorHelpers.FloatEditor emissionRatePanel;
	private EditorHelpers.FloatEditor speedPanel;
	private EditorHelpers.FloatEditor visPanel;
	private EditorHelpers.ColorEditor colorPanel;


	public ComponentPopcornPanel(ModelHandler modelHandler) {
		super(modelHandler);

		popcornPathField = new ComponentEditorTextField(24, this::texturePathField);
		topPanel.add(popcornPathField, "");
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(e -> export());
		topPanel.add(exportButton, "wrap");

		visGuidPanel = new JPanel(new MigLayout("gap 0", "[]8[]"));
		topPanel.add(visGuidPanel, "wrap");
		topPanel.add(valuePanelsPanel(), "wrap");
	}

	private JPanel valuePanelsPanel() {
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0, fill"));
		lifeSpanPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_LIFE_SPAN, this::setLifeSpan);
		emissionRatePanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_EMISSION_RATE, this::setEmissionRate);
		speedPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_SPEED, this::setInitVelocity);
		alphaPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_ALPHA, this::setAlpha);
		visPanel = new EditorHelpers.FloatEditor(modelHandler, "Visibility", null);

		colorPanel = new EditorHelpers.ColorEditor(modelHandler, MdlUtils.TOKEN_COLOR, this::setColor);


		panel.add(lifeSpanPanel.getFlagPanel(), "wrap");
		panel.add(emissionRatePanel.getFlagPanel(), "wrap");
		panel.add(speedPanel.getFlagPanel(), "wrap");
		panel.add(alphaPanel.getFlagPanel(), "wrap");
		panel.add(colorPanel.getFlagPanel(), "wrap");
		panel.add(visPanel.getFlagPanel(), "wrap");

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

		lifeSpanPanel.update(idObject, idObject.getLifeSpan());
		emissionRatePanel.update(idObject, idObject.getEmissionRate());
		speedPanel.update(idObject, idObject.getInitVelocity());
		alphaPanel.update(idObject, idObject.getAlpha());
		visPanel.update(idObject, 1f);
		colorPanel.update(idObject, idObject.getColor());
	}

	private JPanel updateAnimVisGuidPanel() {
		visGuidPanel.removeAll();

		JLabel alwaysLabel = new JLabel("Always");
		alwaysLabel.setToolTipText("Value to use when \"on\" or \"off\" isn't specified (\"none\" in chosen)");
		visGuidPanel.add(alwaysLabel);
		JButton always = new JButton(idObject.getAlwaysState().name());
		always.addActionListener(e -> setAlwaysState(always));
		visGuidPanel.add(always, "wrap, gapbottom 5, growx");

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
				setAnimState(animation, parent, state);
			});
			popupMenu.add(menuItem);
		}
		popupMenu.show(parent, parent.getWidth(), 0);
		return ParticleEmitterPopcorn.State.none;
	}

	private void setAnimState(Animation animation, JButton parent, ParticleEmitterPopcorn.State state) {
		ParticleEmitterPopcorn.State orgState = idObject.getAnimVisState(animation);
		if(state != orgState){
			undoManager.pushAction(new ConsumerAction<>(s -> idObject.setAnimVisState(animation, s), state, orgState, "").redo());
			parent.setText(state.name());
		}
	}

	private ParticleEmitterPopcorn.State setAlwaysState(JButton parent) {
		JPopupMenu popupMenu = new JPopupMenu();
		ParticleEmitterPopcorn.State orgState = idObject.getAlwaysState();
		for (ParticleEmitterPopcorn.State state : ParticleEmitterPopcorn.State.values()) {
			JMenuItem menuItem = new JMenuItem(state.name());
			menuItem.addActionListener(e -> {
				if(state != orgState){
					undoManager.pushAction(new ConsumerAction<>(s -> idObject.setAlwaysState(s), state, orgState, "").redo());
					parent.setText(state.name());
				}
			});
			popupMenu.add(menuItem);
		}
		popupMenu.show(parent, parent.getWidth(), 0);
		return ParticleEmitterPopcorn.State.none;
	}

	private void setLifeSpan(float value){
		if(idObject.getLifeSpan() != value){
			undoManager.pushAction(new ConsumerAction<>(idObject::setLifeSpan, value, idObject.getLifeSpan(), "LifeSpan").redo());
		}
	}
	private void setEmissionRate(float value){
		if(idObject.getEmissionRate() != value){
			undoManager.pushAction(new ConsumerAction<>(idObject::setEmissionRate, value, idObject.getEmissionRate(), "EmissionRate").redo());
		}
	}
	private void setInitVelocity(float value){
		if(idObject.getInitVelocity() != value){
			undoManager.pushAction(new ConsumerAction<>(idObject::setInitVelocity, value, idObject.getInitVelocity(), "InitVelocity").redo());
		}
	}
	private void setAlpha(float value){
		if(idObject.getAlpha() != value){
			undoManager.pushAction(new ConsumerAction<>(idObject::setAlpha, value, idObject.getAlpha(), "Alpha").redo());
		}
	}
	private void setColor(Vec3 color){
		if(!idObject.getColor().equalLocs(color)){
			undoManager.pushAction(new ConsumerAction<>(idObject::setColor, color, idObject.getColor(), "Color").redo());
		}
	}

	private void export(){
		String particlePath = idObject.getPath();
		if(!particlePath.isEmpty()){
			ExportInternal.exportInternalFile3(particlePath, "Popcorn", this);
		}
	}
}
