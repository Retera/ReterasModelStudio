package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.nodes.ChangeParticleTextureAction;
import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.tools.ParticleEditPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;

import javax.swing.*;

public class ComponentParticle2Panel extends ComponentIdObjectPanel<ParticleEmitter2> {

	private final EditorHelpers.FloatEditor widthPanel;
	private final EditorHelpers.FloatEditor lengthPanel;
	private final EditorHelpers.FloatEditor latitudePanel;
	private final EditorHelpers.FloatEditor variationPanel;
	private final EditorHelpers.FloatEditor speedPanel;
	private final EditorHelpers.FloatEditor gravityPanel;
	private final EditorHelpers.FloatEditor emissionPanel;
	private final EditorHelpers.FloatEditor lifeSpanPanel;
	private final EditorHelpers.FloatEditor visibilityPanel;
	private final IntEditorJSpinner replIdSpinner;

	private final TwiComboBox<Bitmap> textureChooser;

	public ComponentParticle2Panel(ModelHandler modelHandler) {
		super(modelHandler);

		textureChooser = new TwiComboBox<>(modelHandler.getModel().getTextures(), new Bitmap("", 1));
		textureChooser.setRenderer(new TextureListRenderer(model).setImageSize(64));
		textureChooser.addOnSelectItemListener(this::changeTexture);
		topPanel.add(textureChooser);

		JButton editParticle = new JButton("editParticle");
		editParticle.addActionListener(e -> viewParticlePanel());
		topPanel.add(editParticle, "spanx, growx, wrap");
		replIdSpinner = new IntEditorJSpinner(0, 0, this::setReplId);
		topPanel.add(new JLabel(MdlUtils.TOKEN_REPLACEABLE_ID + ": "), "spanx 2, split 2");
		topPanel.add(replIdSpinner, "wrap");

		widthPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_WIDTH, this::setWidth);
		lengthPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_LENGTH, this::setLength);
		latitudePanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_LATITUDE, this::setLatitude);
		variationPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_VARIATION, this::setVariation);
		speedPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_SPEED, this::setSpeed);
		gravityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_GRAVITY, this::setGravity);
		emissionPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_EMISSION_RATE, this::setEmissionRate);
		lifeSpanPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_LIFE_SPAN, this::setLifeSpan);
		visibilityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_VISIBILITY, null);
		topPanel.add(emissionPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(speedPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(gravityPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(variationPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(widthPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(lengthPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(lifeSpanPanel.getFlagPanel(), "spanx, growx, wrap");
		topPanel.add(latitudePanel.getFlagPanel(), "spanx, growx, wrap");
		add(visibilityPanel.getFlagPanel(), "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		replIdSpinner.reloadNewValue(idObject.getReplaceableId());
		widthPanel.update(idObject, (float) idObject.getWidth());
		lengthPanel.update(idObject, (float) idObject.getLength());
		latitudePanel.update(idObject, (float) idObject.getLatitude());
		variationPanel.update(idObject, (float) idObject.getVariation());
		speedPanel.update(idObject, (float) idObject.getSpeed());
		gravityPanel.update(idObject, (float) idObject.getGravity());
		emissionPanel.update(idObject, (float) idObject.getEmissionRate());
		lifeSpanPanel.update(idObject, (float) idObject.getLifeSpan());
		visibilityPanel.update(idObject, 1f);
		textureChooser.setSelectedItem(idObject.getTexture());
	}

	private void viewParticlePanel() {
		ParticleEditPanel panel = new ParticleEditPanel(idObject);
		FramePopup.show(panel, null, "Editing " + idObject.getName());
	}

	private void changeTexture(Bitmap selected) {
		if (selected != idObject.getTexture()) {
			System.out.println("Chose texture!");
			undoManager.pushAction(new ChangeParticleTextureAction(idObject, selected, changeListener).redo());
		}
	}

	private void setWidth(float value){
		if(value != idObject.getWidth()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setWidth, (double) value, idObject.getWidth(), "Width").redo());
		}
	}

	private void setReplId(int value){
		if(value != idObject.getReplaceableId()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setReplaceableId, value, idObject.getReplaceableId(), "ReplacebleId").redo());
		}
	}

	private void setLength(float value){
		if(value != idObject.getLength()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setLength, (double) value, idObject.getLength(), "Length").redo());
		}
	}

	private void setLatitude(float value){
		if(value != idObject.getLatitude()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setLatitude, (double) value, idObject.getLatitude(), "Latitude").redo());
		}
	}

	private void setVariation(float value){
		if(value != idObject.getVariation()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setVariation, (double) value, idObject.getVariation(), "Variation").redo());
		}
	}

	private void setSpeed(float value){
		if(value != idObject.getSpeed()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setSpeed, (double) value, idObject.getSpeed(), "Speed").redo());
		}
	}

	private void setGravity(float value){
		if(value != idObject.getGravity()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setGravity, (double) value, idObject.getGravity(), "Gravity").redo());
		}
	}

	private void setEmissionRate(float value){
		if(value != idObject.getEmissionRate()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setEmissionRate, (double) value, idObject.getEmissionRate(), "EmissionRate").redo());
		}
	}
	private void setLifeSpan(float value){
		if(value != idObject.getLifeSpan()){
			undoManager.pushAction(new ConsumerAction<>(idObject::setLifeSpan, (double) value, idObject.getLifeSpan(), "LifeSpan").redo());
		}
	}
}
