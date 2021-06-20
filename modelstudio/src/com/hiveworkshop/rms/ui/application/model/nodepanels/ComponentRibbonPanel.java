package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

public class ComponentRibbonPanel extends ComponentIdObjectPanel<RibbonEmitter> {

	private FloatValuePanel alphaPanel;


	public ComponentRibbonPanel(ModelHandler modelHandler, ModelStructureChangeListener changeListener) {
		super(modelHandler, changeListener);

		alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA, modelHandler.getUndoManager(), changeListener);
		add(alphaPanel, "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		alphaPanel.reloadNewValue((float) idObject.getAlpha(), (FloatAnimFlag) idObject.find(MdlUtils.TOKEN_ALPHA), idObject, MdlUtils.TOKEN_ALPHA, idObject::setAlpha);
	}
}
