package com.hiveworkshop.rms.ui.browsers.unit;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

class UnitButton extends JButton {
	private final GameObject unit;

	public UnitButton(Consumer<UnitButton> buttonConsumer, boolean isShowLevel, GameObject unit) {
		super(unit.getScaledIcon(UnitOptionPanel.ICON_SIZE));

		setFocusable(false);
		this.unit = unit;
		String uberTip = getUberTip();

		String name = this.unit.getName();

		if (isShowLevel) {
			name += " - " + WEString.getString("WESTRING_LEVEL") + " " + this.unit.getFieldValue("level");
		}

		if (uberTip.length() > 0) {
			uberTip = "<html>" + name + "<br>--<br>" + uberTip + "</html>";
		} else {
			uberTip = name;
		}
		setToolTipText(uberTip);
		addActionListener(e -> buttonConsumer.accept(this));

		setDisabledIcon(this.unit.getScaledTintedIcon(Color.green, UnitOptionPanel.ICON_SIZE));
		setMargin(new Insets(0, 0, 0, 0));
		setBorder(null);
	}

	private String getUberTip() {
		String uberTip = unit.getField("Ubertip");
		if (uberTip.length() < 1) {
			uberTip = unit.getField("UberTip");
		}
		if (uberTip.length() < 1) {
			uberTip = unit.getField("uberTip");
		}
		uberTip = uberTip.replace("|n", "<br>");
		uberTip = uberTip.replace("|cffffcc00", "");
		uberTip = uberTip.replace("|r", "");

		uberTip = getNewUberTip(uberTip);
		return uberTip;
	}

	private String getNewUberTip(String uberTip) {
		StringBuilder newUberTip = new StringBuilder();
		int depth = 0; //Row char index
		int tipLength = uberTip.length();
		for (int i = 0; i < tipLength; i++) {
			if (uberTip.charAt(i) == '<' && tipLength - 4 > i && uberTip.startsWith("<br>", i)) {
				i += 3;
				depth = 0;
				newUberTip.append("<br>");
			} else {
				if (depth > 80 && uberTip.charAt(i) == ' ') {
					depth = 0;
					newUberTip.append("<br>");
				}
				newUberTip.append(uberTip.charAt(i));
				depth++;
			}
		}
		return newUberTip.toString();
	}

	public GameObject getUnit() {
		return unit;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		if (!isEnabled()) {
			g.translate(1, 1);
		}
		super.paintComponent(g);
		if (!isEnabled()) {
			g.translate(-1, -1);

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setColor(Color.GRAY);
			for (int i = 0; i < 2; i++) {
				g2.setColor(g2.getColor().brighter());
				g2.draw3DRect(i, i, getWidth() - i * 2 - 1, getHeight() - i * 2 - 1, false);
			}
			g2.dispose();
		}
	}
}
