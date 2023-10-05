package com.hiveworkshop.rms.util.TwiTreeStuff;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.Consumer;

public class TwiTreeMouseAdapter extends MouseAdapter {
	private final Consumer<Boolean> expansionPropagationKeyDown;
	private Consumer<MouseEvent> mouseClickedConsumer;
	private Consumer<MouseEvent> mouseEnteredConsumer;
	private Consumer<MouseEvent> mouseExitedConsumer;
	private Consumer<MouseEvent> mousePressedConsumer;
	private Consumer<MouseEvent> mouseReleasedConsumer;
	private Consumer<MouseEvent> mouseMovedConsumer;
	private Consumer<MouseEvent> mouseWheelMovedConsumer;
	private Consumer<MouseEvent> mouseDraggedConsumer;

	public TwiTreeMouseAdapter(Consumer<Boolean> expansionPropagationKeyDown) {
		if (expansionPropagationKeyDown != null) {
			this.expansionPropagationKeyDown = expansionPropagationKeyDown;
		} else {
			this.expansionPropagationKeyDown = b -> {};
		}
	}

	private void onMouseEvent(final MouseEvent e, Consumer<MouseEvent> consumer) {
		if (consumer != null) {
			consumer.accept(e);
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
//		System.out.println("[ModelTreeMouseAdapter] mouseClicked");
		expansionPropagationKeyDown.accept(e.isControlDown());
		super.mouseClicked(e);
		if (mouseClickedConsumer != null) {
			mouseClickedConsumer.accept(e);
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
//		System.out.println("[ModelTreeMouseAdapter] mouseEntered");
		expansionPropagationKeyDown.accept(e.isControlDown());
		super.mouseEntered(e);
		if (mouseEnteredConsumer != null) {
			mouseEnteredConsumer.accept(e);
		}
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		expansionPropagationKeyDown.accept(e.isControlDown());
//		System.out.println("[ModelTreeMouseAdapter] mouseExited");
		super.mouseExited(e);
		if (mouseExitedConsumer != null) {
			mouseExitedConsumer.accept(e);
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {
//		System.out.println("[ModelTreeMouseAdapter] mousePressed: " + e);
		expansionPropagationKeyDown.accept(e.isControlDown());
		super.mousePressed(e);
		if (mousePressedConsumer != null) {
			mousePressedConsumer.accept(e);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		expansionPropagationKeyDown.accept(e.isControlDown());
		super.mouseReleased(e);
		if (mouseReleasedConsumer != null) {
			mouseReleasedConsumer.accept(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		expansionPropagationKeyDown.accept(e.isControlDown());
		super.mouseMoved(e);
		if (mouseMovedConsumer != null) {
			mouseMovedConsumer.accept(e);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		expansionPropagationKeyDown.accept(e.isControlDown());
		super.mouseWheelMoved(e);
		if (mouseWheelMovedConsumer != null) {
			mouseWheelMovedConsumer.accept(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		expansionPropagationKeyDown.accept(e.isControlDown());
		super.mouseDragged(e);
		if (mouseDraggedConsumer != null) {
			mouseDraggedConsumer.accept(e);
		}
	}

	public TwiTreeMouseAdapter setMouseClickedConsumer(Consumer<MouseEvent> mouseClickedConsumer) {
		this.mouseClickedConsumer = mouseClickedConsumer;
		return this;
	}

	public TwiTreeMouseAdapter setMouseEnteredConsumer(Consumer<MouseEvent> mouseEnteredConsumer) {
		this.mouseEnteredConsumer = mouseEnteredConsumer;
		return this;
	}

	public TwiTreeMouseAdapter setMouseExitedConsumer(Consumer<MouseEvent> mouseExitedConsumer) {
		this.mouseExitedConsumer = mouseExitedConsumer;
		return this;
	}

	public TwiTreeMouseAdapter setMousePressedConsumer(Consumer<MouseEvent> mousePressedConsumer) {
		this.mousePressedConsumer = mousePressedConsumer;
		return this;
	}

	public TwiTreeMouseAdapter setMouseReleasedConsumer(Consumer<MouseEvent> mouseReleasedConsumer) {
		this.mouseReleasedConsumer = mouseReleasedConsumer;
		return this;
	}

	public TwiTreeMouseAdapter setMouseMovedConsumer(Consumer<MouseEvent> mouseMovedConsumer) {
		this.mouseMovedConsumer = mouseMovedConsumer;
		return this;
	}

	public TwiTreeMouseAdapter setMouseWheelMovedConsumer(Consumer<MouseEvent> mouseWheelMovedConsumer) {
		this.mouseWheelMovedConsumer = mouseWheelMovedConsumer;
		return this;
	}

	public TwiTreeMouseAdapter setMouseDraggedConsumer(Consumer<MouseEvent> mouseDraggedConsumer) {
		this.mouseDraggedConsumer = mouseDraggedConsumer;
		return this;
	}
}
