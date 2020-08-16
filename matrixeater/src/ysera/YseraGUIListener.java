package ysera;

import com.hiveworkshop.wc3.mdl.EditableModel;

public interface YseraGUIListener {
	void openModel(EditableModel model);

	// probably repaint
	void stateChanged();
}
