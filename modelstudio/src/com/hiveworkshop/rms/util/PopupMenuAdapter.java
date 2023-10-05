package com.hiveworkshop.rms.util;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.function.Consumer;

public class PopupMenuAdapter implements PopupMenuListener {
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}

	public static PopupMenuAdapter onShowAdapter(Consumer<PopupMenuEvent> eventConsumer) {
		return new PopupMenuAdapter() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				eventConsumer.accept(e);
			}
		};
	}
	public static PopupMenuAdapter onShowAdapter(Runnable runOnShow) {
		return new PopupMenuAdapter() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				runOnShow.run();
			}
		};
	}

	public static PopupMenuAdapter onHideAdapter(Consumer<PopupMenuEvent> eventConsumer) {
		return new PopupMenuAdapter() {
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				eventConsumer.accept(e);
			}
		};
	}
	public static PopupMenuAdapter onHideAdapter(Runnable runOnHide) {
		return new PopupMenuAdapter() {
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				runOnHide.run();
			}
		};
	}
}
