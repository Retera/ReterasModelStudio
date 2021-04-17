package com.hiveworkshop.rms.ui.preferences;

import javax.swing.*;
import java.awt.event.MouseEvent;

public enum MouseButtonPreference {
	RIGHT() {

		@Override
		public boolean isButton(final MouseEvent event) {
			return SwingUtilities.isRightMouseButton(event);
		}

	},
	LEFT() {

		@Override
		public boolean isButton(final MouseEvent event) {
			return SwingUtilities.isLeftMouseButton(event);
		}

	},
	MIDDLE() {

		@Override
		public boolean isButton(final MouseEvent event) {
			return SwingUtilities.isMiddleMouseButton(event);
		}

	};

	public abstract boolean isButton(MouseEvent event);
}
