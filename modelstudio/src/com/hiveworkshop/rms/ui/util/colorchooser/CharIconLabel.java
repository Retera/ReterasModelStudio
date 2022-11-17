package com.hiveworkshop.rms.ui.util.colorchooser;


import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Consumer;

public final class CharIconLabel extends JLabel {
	private boolean doPrint = false;
	private Color currentColor;
	private Color orgColor;
	private static final ColorChooserPopup COLOR_CHOOSER_POPUP = new ColorChooserPopup();
//	private ColorChooserIcon icon;
	private ImageIcon onIcon;
	private ImageIcon offIcon;
	private int arc = 18;
	private BufferedImage image;
	private int[] pixels;
	private RoundRectangle2D.Float roundRect;
	private Rectangle rectangle;
	private Color borderColor = Color.BLACK;
	private int size;
	private int imageCreationSize;
	private Consumer<Boolean> onOffConsumer;

	public CharIconLabel(final String text, int arc, final Color color, int size) {
		this(text, arc, color, size, null);
	}
	public CharIconLabel(final String text, int arc, final Color color, int size, Consumer<Boolean> onOffConsumer) {
		this.onOffConsumer = onOffConsumer;
		currentColor = color;
		orgColor = color;
		this.size = size;
		this.imageCreationSize = size;
		setArc(arc);
		this.setBackground(Color.red);
		pixels = new int[imageCreationSize*imageCreationSize];
		Arrays.fill(pixels, 0);
//		onIcon = new ImageIcon(paintImage(Color.BLACK, Color.GRAY, "\uD83D\uDC41"));
//		offIcon = new ImageIcon(paintImage(Color.GRAY, Color.DARK_GRAY, "\uD83D\uDC41"));
//		onIcon = new ImageIcon(paintImage(Color.BLACK, Color.GRAY, "\uD83E\uDD1A"));
//		offIcon = new ImageIcon(paintImage(Color.GRAY, Color.DARK_GRAY, "\uD83E\uDD1A"));
		onIcon = new ImageIcon(paintImage(Color.BLACK, Color.GRAY, text));
		offIcon = new ImageIcon(paintImage(Color.GRAY, Color.DARK_GRAY, text));
		setIcon(onIcon);
//		setText(text);


		setMinimumSize(new Dimension(size, size));
		setPreferredSize(new Dimension(size, size));
		setMaximumSize(new Dimension(size, size));
	}

	public CharIconLabel toggleOn(boolean on){
		if(on){
			setIcon(onIcon);
		} else {
			setIcon(offIcon);
		}
		consumeOnOff(on);
		return this;
	}
	public CharIconLabel toggleOn(){
		if(CharIconLabel.this.getIcon() == onIcon){
			setIcon(offIcon);
			consumeOnOff(false);
//			System.out.println("\t[CharIconLabel] off");
		} else {
			setIcon(onIcon);
//			System.out.println("\t[CharIconLabel] on");
			consumeOnOff(true);
		}
		return this;
	}
	public CharIconLabel setOn(){
		setIcon(onIcon);
		consumeOnOff(true);
		return this;
	}
	public CharIconLabel setOff(){
		setIcon(offIcon);
		consumeOnOff(false);
		return this;
	}
	private void consumeOnOff(boolean on){
		if(onOffConsumer != null){
			onOffConsumer.accept(on);
		}
	}

	public static void main(final String[] args) {
		JPanel panel = new JPanel(new MigLayout(""));
		// 👁🤚
		final CharIconLabel chooser1 = new CharIconLabel("\uD83D\uDC41", 18, Color.YELLOW,  18);
//		final CharIconLabel chooser1 = new CharIconLabel("V", 18, Color.YELLOW,  18);
		chooser1.addMouseListener(getMouseAdapter(chooser1));
		panel.add(chooser1);
		final CharIconLabel chooser2 = new CharIconLabel("\uD83E\uDD1A", 18, Color.YELLOW,  18);
//		final CharIconLabel chooser2 = new CharIconLabel("E", 18, Color.YELLOW,  18);
		chooser2.addMouseListener(getMouseAdapter(chooser2));
		panel.add(chooser2);
		panel.add(new JLabel("Geoset 1"));
//		JOptionPane.showMessageDialog(null, panel);


		JPanel panel1 = new JPanel(new MigLayout("fill", "[center]", "[center]"));
		panel1.add(panel);
		JFrame frame = new JFrame("Test labels");
		frame.setContentPane(panel1);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.addKeyListener(getKeyAdapter());
		frame.setSize(300, 100);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static MouseAdapter getMouseAdapter(CharIconLabel chooser) {
		return new MouseAdapter() {
			Integer threeDCameraSpinMouseEx = MouseEvent.BUTTON2_DOWN_MASK;
			Integer threeDCameraPanMouseEx = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK;
			Integer selectMouseButton = MouseEvent.BUTTON1_DOWN_MASK;
			Integer modifyMouseButton = MouseEvent.BUTTON3_DOWN_MASK;
			Integer addSelectModifier = MouseEvent.SHIFT_DOWN_MASK;
			Integer removeSelectModifier = MouseEvent.CTRL_DOWN_MASK;
			private Integer snapTransformModifier = MouseEvent.CTRL_DOWN_MASK;
			private Integer transformAxisLocModifier = MouseEvent.ALT_DOWN_MASK;
			private Integer transformPrecisionModifier = MouseEvent.SHIFT_DOWN_MASK;
//			private Integer transformPrecisionModifier = MouseEvent.NOBUTTON;
			boolean mousePressed;
			boolean mouseIn;
			Integer ugg = ~(0b1111111111);

			@Override
			public void mousePressed(MouseEvent e) {
				int modifiersEx = e.getModifiersEx();
//				chooser.print("[CharIconLabel] mousePressed: " + e);
//				chooser.print("[CharIconLabel] mousePressed, button: " + e.getButton());
//				chooser.print("[CharIconLabel] mousePressed, ModifiersEx: " + e.getModifiersEx());
//				chooser.print("[CharIconLabel] mousePressed, ModifiersExUGG: " + (e.getModifiersEx() & ugg));
				if (!mousePressed) {
					chooser.toggleOn();
				}

				int select_xor_MB = e.getModifiersEx() ^ selectMouseButton;
				boolean isSelect = select_xor_MB == 0 // no modifiers
						|| (select_xor_MB ^ addSelectModifier) == 0 // add selection modifier
						||  (select_xor_MB ^ removeSelectModifier) == 0; // remove selection modifier


				int eMB = modifiersEx & ugg;
				int modMB = modifyMouseButton & ugg;
				int edit_xor_MB = e.getModifiersEx() ^ modifyMouseButton;
				boolean edit = edit_xor_MB == 0 // no modifiers
						|| (edit_xor_MB ^ snapTransformModifier) == 0 // Rotation Angle Snap Modifier
						||  (edit_xor_MB ^ transformPrecisionModifier) == 0; // remove selection modifier
//				boolean cancel = (modifiersEx & modifyMouseButton) == modifyMouseButton;
//				boolean cancel = eMB != modMB;
				boolean cancel = !MouseEventHelpers.isSameMouseButton(e, modifyMouseButton);


				if(cancel){
					chooser.print("[CharIconLabel] cancel!");
				}
//				boolean startActivity = (modifyMouseButton & modifiersEx) > 0;
				boolean startActivity = MouseEventHelpers.matches(e, modifyMouseButton, snapTransformModifier, transformAxisLocModifier, transformPrecisionModifier);
				if(startActivity){
					chooser.print("[CharIconLabel] start!");
					if(MouseEventHelpers.hasModifier(e, snapTransformModifier)){
						chooser.print("\tsnap!");
					} else if(MouseEventHelpers.hasModifier(e, transformAxisLocModifier)){
						chooser.print("\tlock axis!");
					} else if(MouseEventHelpers.hasModifier(e, transformPrecisionModifier)){
						chooser.print("\tprecise!");
					}
				}

				int snapPrecise = snapTransformModifier | transformPrecisionModifier;
				int snapAxis = snapTransformModifier | transformAxisLocModifier;
				int axisPrecise = transformAxisLocModifier | transformPrecisionModifier;
				int snapAxisPrecise = snapTransformModifier | transformAxisLocModifier | transformPrecisionModifier;

				int match = MouseEventHelpers.getMatch(e, modifyMouseButton,
						snapTransformModifier,
						transformAxisLocModifier,
						transformPrecisionModifier,
						snapPrecise,
						snapAxis,
						axisPrecise,
						snapAxisPrecise
						);
				if(match != 0){
					chooser.print("[CharIconLabel] start!" + match);
					if(match == modifyMouseButton){
						chooser.print("\tnormal!");
					} else if(match == snapTransformModifier){
						chooser.print("\tsnap!");
					} else if(match == transformAxisLocModifier){
						chooser.print("\tlock axis!");
					} else if(match == transformPrecisionModifier){
						chooser.print("\tprecise!");
					} else if(match == snapPrecise){
						chooser.print("\tsnap Precise!");
					} else if(match == snapAxis){
						chooser.print("\tsnap Axis!");
					} else if(match == axisPrecise){
						chooser.print("\taxis Precise!");
					} else if(match == snapAxisPrecise){
						chooser.print("\tsnap Axis Precise!");
					}
				}


				super.mouseReleased(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
//				chooser.print("[CharIconLabel] mouseReleased: " + e);
//				chooser1.toggleOn();
				mousePressed = false;
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
				int mask = MouseEvent.BUTTON1_DOWN_MASK;
//				chooser.print("[CharIconLabel] mouseEntered: " + e);
				if ((e.getModifiersEx() & mask) == mask) {
					chooser.toggleOn();
				}
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(final MouseEvent e) {
//				int mask = MouseEvent.BUTTON1_DOWN_MASK;
//				chooser.print("[CharIconLabel] mouseExited: " + e);
//				if((e.getModifiersEx() & mask) == mask){
//					chooser1.toggleOn();
//				}
				super.mouseEntered(e);
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
//				int mask = MouseEvent.BUTTON1_DOWN_MASK;
//				chooser.print("[CharIconLabel] mouseDragged: " + e);
//				if((e.getModifiersEx() & mask) == mask){
//					chooser1.toggleOn();
//				}
				super.mouseEntered(e);
			}

		};
	}

	private static KeyAdapter getKeyAdapter(){
		return new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				super.keyTyped(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() != KeyEvent.VK_CONTROL && e.getKeyCode() != KeyEvent.VK_SHIFT){
					System.out.println("e: " + e);
					System.out.println("e.getKeyCode: " + e.getKeyCode());
					System.out.println("e.getKeyCode | ctrl: " + (e.getKeyCode() | KeyEvent.CTRL_DOWN_MASK));
					System.out.println("e.getExtendedKeyCode: " + e.getExtendedKeyCode());
					System.out.println("e.getModifiersEx: " + e.getModifiersEx());
					System.out.println("e.getID: " + e.getID());

					KeyStroke keyStrokeForEvent = KeyStroke.getKeyStrokeForEvent(e);
					System.out.println("\nkeyStrokeForEvent: " + keyStrokeForEvent);
					System.out.println("keyStrokeForEvent.getKeyCode: " + keyStrokeForEvent.getKeyCode());
					System.out.println("keyStrokeForEvent.getModifiers: " + keyStrokeForEvent.getModifiers());

					KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, KeyEvent.CTRL_DOWN_MASK);
					System.out.println("\nks: " + keyStroke);
					System.out.println("ks.getKeyCode: " + keyStroke.getKeyCode());
					System.out.println("ks.getModifiers: " + keyStroke.getModifiers());
				}
				super.keyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
			}
		};
	}


	public CharIconLabel setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
		return this;
	}

	public CharIconLabel setArc(int arc) {
		this.arc = arc;

//		int width = image.getWidth();
//		int height = image.getHeight();
		roundRect = new RoundRectangle2D.Float(0, 0, imageCreationSize-1, imageCreationSize-1, arc+1, arc+1);
		rectangle = new Rectangle(0, 0, imageCreationSize, imageCreationSize);
//		paintImage(currentColor, Color.GRAY, "");
		return this;
	}


	private BufferedImage paintImage(Color color, Color bgColor, String imageText) {

//		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		BufferedImage image = new BufferedImage(imageCreationSize, imageCreationSize, BufferedImage.TYPE_INT_ARGB);
		int width = image.getWidth();
		int height = image.getHeight();
		// set transparent
		image.setRGB(0,0, width, height, pixels, 0, width);

		Graphics2D g = (Graphics2D) image.getGraphics();
//		g.setStroke(new BasicStroke(2));

		// fill icon
		g.setColor(bgColor);
		g.fillRoundRect(0, 0, width-1, height-1, arc+1, arc+1);


//		g.setClip(roundRect);
//		// draw highlight
//		g.setStroke(new BasicStroke(8));
//		g.setColor(bgColor.brighter().brighter());
//		g.drawRoundRect(0, 0, width, height, arc, arc);
//		// draw shadow
//		g.setColor(bgColor.darker().darker());
//		g.drawRoundRect(-2, -2, width, height, arc, arc);
////		g.drawRoundRect(0, 0, width, height, arc, arc);
//		g.setClip(rectangle);
////		g.setStroke(new BasicStroke(1));

		// draw border
//		g.setStroke(new BasicStroke(1));
		g.setColor(color);
		g.drawRoundRect(0, 0, width-1, height-1, arc, arc);

		// draw char
//		g.setColor(color);
		g.setColor(bgColor.darker());
		Font font = g.getFont().deriveFont(Font.BOLD, (int) (getFont().getSize()*1.2));
		g.setFont(font);
		Rectangle2D stringBounds = font.getStringBounds(imageText, g.getFontRenderContext());
		float descent = font.getLineMetrics(imageText, g.getFontRenderContext()).getDescent();
//		print("descent: " + descent);
//		float ascent = font.getLineMetrics(imageText, g.getFontRenderContext()).getAscent();
//		print("ascent: " + ascent);
//		float uggscent = font.getLineMetrics(imageText, g.getFontRenderContext()).getHeight();
//		print("height: " + uggscent);
//		double half_h = g.getFontMetrics().getLineMetrics(imageText, null).getHeight()/2.0;
//		int textX = (int) ((width-g.getFontMetrics().charWidth('W'))/2.0);
//		int textY = (int) (height/2.0 + half_h);
		int textX = (int) ((width-stringBounds.getWidth())/2.0);
		int textY = (int) ((height + stringBounds.getHeight())/2.0-descent);
//		printStringBoundsInfo(stringBounds);

//		int textY = (int) ((height + stringBounds.getMaxY())/2.0);
//		char[] chars = new char[1];
//		imageText.getChars(0, 1, chars, 0);
//		print(chars[0]);
		g.drawString(imageText, textX, textY);
//		g.drawString(imageText, textX, (int)-stringBounds.getY());
//		g.drawString(imageText, textX, (int)width - 1);
		Font font2 = font.deriveFont(Font.PLAIN, (int) (font.getSize()));
		g.setFont(font2);
		g.setColor(color);
		print(textX + ", " + textY);
		g.drawString(imageText, textX, textY);
		g.dispose();
		return image;
	}

	private void printStringBoundsInfo(Rectangle2D stringBounds) {
		print("stringBounds: " + stringBounds
				+ ", getCenterY: " + stringBounds.getCenterY()
				+ ", getHeight: " + stringBounds.getHeight()
				+ ", getMinY: " + stringBounds.getMinY()
				+ ", getY: " + stringBounds.getY()
				+ ", getMaxY: " + stringBounds.getMaxY());
		print("stringBounds: " + stringBounds
				+ ", getCenterX: " + stringBounds.getCenterX()
				+ ", getWidth: " + stringBounds.getWidth()
				+ ", getMinX: " + stringBounds.getMinX()
				+ ", getX: " + stringBounds.getX()
				+ ", getMaxX: " + stringBounds.getMaxX());
	}

	private void print(String s){
		if(doPrint){
			System.out.println(s);
		}

	}
}
