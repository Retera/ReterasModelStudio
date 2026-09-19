package com.hiveworkshop.wc3.gui.modeledit.componenttree;

/**
 * Lets the Model tab ask the surrounding window to show a component somewhere
 * else: in the Edit views (Outliner + viewports) or in the Tracks view.
 */
public interface ModelComponentNavigationListener {
	void openInEditor(Object component);

	void openInTracks(Object component);

	ModelComponentNavigationListener NONE = new ModelComponentNavigationListener() {
		@Override
		public void openInEditor(final Object component) {
		}

		@Override
		public void openInTracks(final Object component) {
		}
	};
}
