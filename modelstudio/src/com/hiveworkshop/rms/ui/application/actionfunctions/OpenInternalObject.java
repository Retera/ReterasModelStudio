package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.awt.*;

public class OpenInternalObject extends ActionFunction {
	public OpenInternalObject(){
		super(TextKey.OBJECT_EDITOR, () -> fetchObject(), "control O");
	}



	public static void fetchObject() {
		MutableObjectData.MutableGameObject objectFetched = ImportFileActions.fetchObject();
		if (objectFetched != null) {

			String filepath = ImportFileActions.convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
			ImageIcon icon = new ImageIcon(BLPHandler.get().getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0)).getScaledInstance(16, 16, Image.SCALE_FAST));

			InternalFileLoader.loadFromStream(filepath, icon);
		}
	}
}
