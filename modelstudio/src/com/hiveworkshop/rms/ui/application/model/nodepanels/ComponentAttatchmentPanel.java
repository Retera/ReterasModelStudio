package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.Attachment;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;

import javax.swing.*;
import java.util.Objects;

public class ComponentAttatchmentPanel extends ComponentIdObjectPanel<Attachment> {
	private final EditorHelpers.FloatEditor visibilityPanel;
	private final TwiTextField pathField;

	public ComponentAttatchmentPanel(ModelHandler modelHandler) {
		super(modelHandler);
		pathField = new TwiTextField(24, this::texturePathField);
		topPanel.add(new JLabel("Path"), "split 2");
		topPanel.add(pathField, "wrap");
		visibilityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_VISIBILITY, null);
		topPanel.add(visibilityPanel.getFlagPanel(), "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		pathField.setText(idObject.getPath());
		visibilityPanel.update(idObject, 1f);
	}
	private void texturePathField(String newPath) {
		newPath = "".equals(newPath) ? null : newPath;
		if (!Objects.equals(newPath, idObject.getPath())) {
			undoManager.pushAction(new ConsumerAction<>(idObject::setPath, newPath, idObject.getPath(), "Change Attachment Path").redo());
		}
	}
}
