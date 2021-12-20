package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.War3ID;

public class OpenInternalDoodad extends ActionFunction {
	public OpenInternalDoodad(){
		super(TextKey.DOODAD_BROWSER, () -> fetchDoodad());
	}



	public static void fetchDoodad() {
//		MutableGameObject objectFetched = ImportFileActions.fetchDoodad();
//		if (objectFetched != null) {
//
//			String filepath = ImportFileActions.convertPathToMDX(getFilePath(objectFetched));
////			String fieldAsString = objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0);
////			Image scaledInstance = BLPHandler.getGameTex(fieldAsString).getScaledInstance(16, 16, Image.SCALE_FAST);
////			ImageIcon icon = new ImageIcon(scaledInstance);
////
////			InternalFileLoader.loadFromStream(filepath, icon);
//			InternalFileLoader.loadFromStream(filepath, null);
//		}
		EditableModel modelFetched = ImportFileActions.fetchDoodadModel();

		if (modelFetched != null) {
			ModelLoader.loadModel(true, true, ModelLoader.newTempModelPanel(null, modelFetched));
		}
	}


	private static String getFilePath(MutableGameObject obj){
//		MutableGameObject obj = (MutableGameObject) o.getUserObject();
		int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
		if (numberOfVariations > 1) {
			return obj.getFieldAsString(War3ID.fromString("dfil"), 0) + 0 + ".mdl";
//			for (int i = 0; i < numberOfVariations; i++) {
//				String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0) + i + ".mdl";
//				InternalFileLoader.loadMdxStream(obj, prePath, i == 0);
//			}
		} else {
			return obj.getFieldAsString(War3ID.fromString("dfil"), 0);
//			InternalFileLoader.loadMdxStream(obj, prePath, true);
		}
	}
}
