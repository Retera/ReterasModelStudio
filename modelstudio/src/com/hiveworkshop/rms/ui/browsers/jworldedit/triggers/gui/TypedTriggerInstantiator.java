package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.Trigger;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerCategory;

public enum TypedTriggerInstantiator {
	TRIGGER() {
		@Override
		public Trigger create(final TriggerTreeController controller, final TriggerCategory category) {
			return controller.createTrigger(category);
		}
	},
	COMMENT() {
		@Override
		public Trigger create(final TriggerTreeController controller, final TriggerCategory category) {
			return controller.createTriggerComment(category);
		}
	};

	public abstract Trigger create(TriggerTreeController controller, TriggerCategory category);
}
