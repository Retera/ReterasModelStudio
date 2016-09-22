package swife;

import java.awt.Color;

public class Preferences {
	private Color viewportBackgroundColor;
	private Color viewportGridColor;
	private Color viewportHighlightGridColor;

	public Color getViewportBackgroundColor() {
		return viewportBackgroundColor;
	}
	public void setViewportBackgroundColor(final Color viewportBackgroundColor) {
		this.viewportBackgroundColor = viewportBackgroundColor;
	}
	public Color getViewportGridColor() {
		return viewportGridColor;
	}
	public void setViewportGridColor(final Color viewportGridColor) {
		this.viewportGridColor = viewportGridColor;
	}
	public Color getViewportHighlightGridColor() {
		return viewportHighlightGridColor;
	}
	public void setViewportHighlightGridColor(final Color viewportHighlightGridColor) {
		this.viewportHighlightGridColor = viewportHighlightGridColor;
	}
}
