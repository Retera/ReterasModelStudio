package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.awt.*;

public class OpenInternalObject extends ActionFunction {
	public OpenInternalObject(){
		super(TextKey.OBJECT_EDITOR, () -> fetchObject(), "control O");
	}



	public static void fetchObject() {
		MutableGameObject objectFetched = ImportFileActions.fetchObject();
		if (objectFetched != null) {

			String filepath = ImportFileActions.convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
			String fieldAsString = objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0);
			Image scaledInstance = BLPHandler.getGameTex(fieldAsString).getScaledInstance(16, 16, Image.SCALE_FAST);
			ImageIcon icon = new ImageIcon(scaledInstance);

			InternalFileLoader.loadFromStream(filepath, icon);
		}
	}
}
