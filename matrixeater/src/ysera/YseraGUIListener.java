package ysera;

import com.hiveworkshop.wc3.mdl.MDL;

public interface YseraGUIListener {
	void openModel(MDL model);

	// probably repaint
	void stateChanged();
}
