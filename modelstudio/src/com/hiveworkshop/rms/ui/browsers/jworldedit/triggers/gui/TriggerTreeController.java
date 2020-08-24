package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;

public interface TriggerTreeController {
	Trigger createTrigger(TriggerCategory triggerCategory);

	Trigger createTriggerComment(TriggerCategory triggerCategory);

	TriggerCategory createCategory();

	void renameTrigger(Trigger trigger, String name);

	void moveTrigger(Trigger trigger, TriggerCategory triggerCategory, int index);

	void moveCategory(TriggerCategory triggerCategory, int index);

	void toggleCategoryIsComment(TriggerCategory triggerCategory);

	void renameCategory(TriggerCategory trigger, String name);

	void deleteTrigger(Trigger trigger);

	void deleteCategory(TriggerCategory category);
}
