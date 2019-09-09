package com.matrixeater.hacks.blessing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class BlessingWindow extends JPanel {

	private static final String CHOOSE_YOUR_BLESSING = "Choose your Blessing";
	private Font font;
	private Font subtitleFont;
	private Font titleFont;
	private final List<Blessing> possibleBlessings = Arrays.asList(new Blessing("Gonk", "Loa of the Pack"),
			new Blessing("Pa'ku", "Loa of the Winds"), new Blessing("Rezan", "Loa of Kings"),
			new Blessing("Bwonsamdi", "Loa of Graves"));
	private BorderedBox borderedBox;
	private BufferedImage optionsMenuBorder;
	private final Rectangle windowBounds;

	private final List<FrameHandle> frameHandles = new ArrayList<>();
	private BorderedBox tooltipBox;
	private BufferedImage stonesIcon;
	private Font tooltipFont;
	private BufferedImage tooltipBackground;

	private BlessingWindow(final Rectangle windowBounds) {
		this.windowBounds = windowBounds;
		setBackground(Color.BLACK);

		try {
			font = Font.createFont(Font.TRUETYPE_FONT, MpqCodebase.get().getResourceAsStream("fonts\\frizqt__.ttf"))
					.deriveFont(22f);
			subtitleFont = font.deriveFont(14f);
			tooltipFont = font.deriveFont(18f);
			titleFont = font.deriveFont(36f);
			optionsMenuBorder = BLPHandler.get().getGameTex("ui\\widgets\\escmenu\\orc\\orc-options-menu-border.blp");
			borderedBox = new BorderedBox(windowBounds, optionsMenuBorder);

			frameHandles.add(borderedBox);
			frameHandles.add(new BlessingItem(new Rectangle(windowBounds.x + 80, windowBounds.y + 350, 64, 64),
					BLPHandler.get().getGameTex("replaceabletextures\\commandbuttons\\btnberserkfortrolls.blp"),
					"Feral Swipes", 50,
					"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Donec odio. Quisque volutpat mattis eros. Nullam malesuada erat ut turpis. Suspendisse urna nibh"));
			frameHandles.add(new BlessingItem(new Rectangle(windowBounds.x + 150, windowBounds.y + 350, 64, 64),
					BLPHandler.get().getGameTex("replaceabletextures\\commandbuttons\\btnsacrificialdagger.blp"),
					"Troll Daggers", 75, "Increases the damage of all poison based skills and abilities."));
			frameHandles.add(new BlessingItem(new Rectangle(windowBounds.x + 220, windowBounds.y + 350, 64, 64),
					BLPHandler.get().getGameTex("replaceabletextures\\commandbuttons\\btncloudoffog.blp"),
					"Mystic Fogs", 65,
					"Provides an AoE around all allies structures that prevents the casting of enemy spells."));
			frameHandles.add(new BlessingItem(new Rectangle(windowBounds.x + 820, windowBounds.y + 350, 64, 64),
					BLPHandler.get().getGameTex("replaceabletextures\\commandbuttons\\btnsunderingblades.blp"),
					"Shield Smash", 65, "Causes all troll towers to do Chaos damage which ignores enemy armor."));
			frameHandles.add(new BlessingItem(new Rectangle(windowBounds.x + 890, windowBounds.y + 350, 64, 64),
					BLPHandler.get().getGameTex("replaceabletextures\\commandbuttons\\btnhelmutpurple.blp"),
					"Crown of Kings", 65, "Provides a +5 attribute bonus to all Troll heroes."));
			tooltipBox = new BorderedBox(new Rectangle(384, 128),
					BLPHandler.get().getGameTex("ui\\widgets\\tooltips\\human\\human-tooltip-border.blp"));
			stonesIcon = BLPHandler.get().getGameTex("ui\\widgets\\tooltips\\human\\tooltipstonesicon.blp");
			tooltipBackground = BLPHandler.get()
					.getGameTex("ui\\widgets\\tooltips\\human\\human-tooltip-background.blp");
		} catch (final FontFormatException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);

		final Graphics2D g2 = (Graphics2D) g;
		final RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHints(rh);

		borderedBox.paint(g);
		for (final FrameHandle frameHandle : frameHandles) {
			frameHandle.paint(g);
		}

		final int screenWidth = windowBounds.width;
		final int distanceBetween = screenWidth / possibleBlessings.size();
		final int halfDist = distanceBetween / 2;

		g.setFont(titleFont);
		int stringWidth = g.getFontMetrics().stringWidth(CHOOSE_YOUR_BLESSING);
		g.setColor(new Color(0xffffcc00));
		g.drawString(CHOOSE_YOUR_BLESSING, windowBounds.x + ((screenWidth - stringWidth) / 2), windowBounds.y + 170);

		g.setColor(Color.WHITE);

		for (int i = 0; i < possibleBlessings.size(); i++) {
			final Blessing possibleBlessing = possibleBlessings.get(i);

			g.setFont(font);
			final String loaName = possibleBlessing.getLoaName();
			stringWidth = g.getFontMetrics().stringWidth(loaName);
			g.drawString(loaName, (windowBounds.x + (halfDist + (distanceBetween * i))) - (stringWidth / 2),
					windowBounds.y + 270);

			g.setFont(subtitleFont);
			final String loaSubtitle = possibleBlessing.getLoaSubtitle();
			stringWidth = g.getFontMetrics().stringWidth(loaSubtitle);
			g.drawString(loaSubtitle, (windowBounds.x + (halfDist + (distanceBetween * i))) - (stringWidth / 2),
					windowBounds.y + 300);
		}

		for (final FrameHandle frame : frameHandles) {
			final Point mousePosition = getMousePosition();
			final Rectangle frameBounds = frame.getBounds();
			if (frameBounds.contains(mousePosition)) {
				// then show tooltip, if exists
				if (frame.getTooltip() != null) {
					g.setFont(tooltipFont);
					tooltipBox.setLocation(frameBounds.x + frameBounds.width + tooltipBox.getBorderBoxSize(),
							frameBounds.y);
					final Rectangle tooltipBounds = tooltipBox.getBounds();
					final int insetSize = tooltipBox.getBorderBoxSize() / 4;
					g.drawImage(tooltipBackground, tooltipBounds.x + insetSize, tooltipBounds.y + insetSize,
							tooltipBounds.width - (insetSize * 2), tooltipBounds.height - (insetSize * 2), null);
					tooltipBox.paint(g);
					g.drawString(frame.getTooltip(), tooltipBounds.x + 16,
							tooltipBounds.y + 16 + g.getFontMetrics().getAscent());
					final int stonesIconWidth = 24;
					final int stonesIconHeight = 24;
					final int stonesIconTopmostY = tooltipBounds.y + 16 + g.getFontMetrics().getAscent() + 8;
					g.drawImage(stonesIcon, tooltipBounds.x + 16, stonesIconTopmostY, stonesIconWidth, stonesIconHeight,
							null);
					g.setColor(Color.MAGENTA);
					g.drawString(Integer.toString(frame.getCost()), tooltipBounds.x + 16 + stonesIconWidth + 8,
							((stonesIconTopmostY + (stonesIconHeight / 2))
									- ((g.getFontMetrics().getAscent() + g.getFontMetrics().getDescent()) / 2))
									+ g.getFontMetrics().getAscent());
					final String ubertip = frame.getUbertip();
					final String[] words = ubertip.split(" ");
					int ubertipLineIndex = 0;
					final StringBuilder stringBuilder = new StringBuilder();
					final FontMetrics fontMetrics = g.getFontMetrics();
					int lineWidth = 0;
					g.setColor(Color.WHITE);
					for (final String word : words) {
						int additionalWordWidth = 0;
						if (stringBuilder.length() > 0) {
							additionalWordWidth += fontMetrics.charWidth(' ');
						}
						final int wordWidth = fontMetrics.stringWidth(word);
						additionalWordWidth += wordWidth;
						if ((lineWidth + additionalWordWidth) > (tooltipBounds.width - 32)) {
							final String textLine = stringBuilder.toString();
							g.drawString(textLine, tooltipBounds.x + 16, stonesIconTopmostY + stonesIconHeight
									+ (ubertipLineIndex * fontMetrics.getHeight()) + fontMetrics.getHeight());

							ubertipLineIndex++;
							lineWidth = wordWidth;
							stringBuilder.setLength(0);
						} else {
							lineWidth += additionalWordWidth;
							if (stringBuilder.length() > 0) {
								stringBuilder.append(' ');
							}
						}
						stringBuilder.append(word);
					}
					if (stringBuilder.length() > 0) {
						g.drawString(stringBuilder.toString(), tooltipBounds.x + 16,
								stonesIconTopmostY + stonesIconHeight + (ubertipLineIndex * fontMetrics.getHeight())
										+ fontMetrics.getHeight());
						ubertipLineIndex++;
					}
					tooltipBox.getBounds().height = (stonesIconTopmostY - tooltipBounds.y) + stonesIconHeight
							+ ((fontMetrics.getHeight() * ubertipLineIndex) + 16);
				}
			}
		}

		repaint();
	}

	public static void main(final String[] args) {
		final JFrame game = new JFrame("The Game");
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Rectangle windowBounds = new Rectangle(0, 0, screenSize.width, screenSize.height);
		final int borderBoxWidth = 64 * 24;
		final int borderBoxHeight = 64 * 16;
		final Rectangle borderedBoxBounds = new Rectangle((windowBounds.width - borderBoxWidth) / 2,
				(windowBounds.height - borderBoxHeight) / 2, borderBoxWidth, borderBoxHeight);
		game.setContentPane(new BlessingWindow(borderedBoxBounds));
		game.setBounds(windowBounds);
		game.setUndecorated(true);
		game.setLocation(0, 0);
		game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		game.setVisible(true);

	}

}
