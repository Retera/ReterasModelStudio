package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.actions.model.material.ChangeLayerStaticTextureAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TextureValuePanel extends ValuePanel<Integer> {

	private final BasicComboPopup chooseTextureComboPopup;
	private final JComboBox<Object> textureChooser;
	DefaultListModel<Bitmap> bitmapListModel;
	private JComboBox<String> staticTextureChooser;
	private boolean listenersEnabled = true;

	private Bitmap bitmap;

	private int selectedRow;


	public TextureValuePanel(final String title, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener, EditableModel model) {
		super(title, undoActionListener, modelStructureChangeListener);

		textureChooser = new JComboBox<>(getTextures(model));
		textureChooser.setModel(new DefaultComboBoxModel<>(getTextures(model)));
		textureChooser.addActionListener(this::setTextureId);
		chooseTextureComboPopup = new BasicComboPopup(textureChooser);

		staticTextureChooser.setModel(new DefaultComboBoxModel<>(getTextures(model)));

		floatTrackTableModel.addExtraColumn("Texture", "", String.class);  // 🎨 \uD83C\uDFA8

		addBitmapChangeListeners();
	}

	private void setTextureId(ActionEvent e) {
		System.out.println("ActionEvent: " + e);
		if (listenersEnabled) {
			changeTexture();
		}
	}

	private String[] getTextures(EditableModel model) {

		bitmapListModel = new DefaultListModel<>();
		List<String> bitmapNames = new ArrayList<>();

		for (final Bitmap bitmap : model.getTextures()) {
			bitmapNames.add(bitmap.getName());
			bitmapListModel.addElement(bitmap);
		}

		return bitmapNames.toArray(new String[0]);
	}

//	protected void valueCellRendering(Component tableCellRendererComponent, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//		if (column == 2) {
////			floatTrackTableModel.setValueAt(floatTrackTableModel.getValueAt(row, 1), row, column);
////			value = floatTrackTableModel.getValueAt(row, 1);
//		}
//	}

	@Override
	JComponent getStaticComponent() {
//		System.out.println("getStaticComponent");
		staticTextureChooser = new JComboBox<>();
		staticTextureChooser.addActionListener(e -> changeStaticBitmap());
		return staticTextureChooser;
	}

	@Override
	void reloadStaticValue(Integer bitmapId) {
//		System.out.println("reloadStaticValue");
		listenersEnabled = false;
		bitmap = ((Layer) timelineContainer).getTextureBitmap();
//		this.bitmapId = bitmapId;
		staticValue = bitmapId;
		if (bitmapId == -1) {
//			ActionListener[] actionListeners = staticTextureChooser.getActionListeners();
			ActionListener actionListener = staticTextureChooser.getActionListeners()[0];
			staticTextureChooser.removeActionListener(actionListener);
			staticTextureChooser.setSelectedItem(bitmap.getName());
			staticValue = staticTextureChooser.getSelectedIndex();
			this.bitmap = bitmapListModel.get(staticValue);
			staticTextureChooser.addActionListener(actionListener);
		} else if (bitmapId < bitmapListModel.size()) {
			staticTextureChooser.setSelectedIndex(bitmapId);
			this.bitmap = bitmapListModel.get(bitmapId);
		}
		listenersEnabled = true;

		if (animFlag != null) {
			floatTrackTableModel.updateExtraButtonValues(getBitmapNameList());
		}
	}


	@Override
	Integer getZeroValue() {
//		return new Bitmap("Textures\\White.dds");
		return 0;
	}

	@Override
	Integer parseValue(String valueString) {
		String polishedString = valueString.replaceAll("[\\D]", "");

		return Integer.parseInt(polishedString);
	}


	private String[] getBitmapNameList() {
		List<String> bitmapNames = new ArrayList<>();
		for (Object textureId : animFlag.getValues()) {
			if (textureId instanceof Integer) {
				int tId = (int) textureId;
				if (tId < bitmapListModel.size()) {
					bitmapNames.add(bitmapListModel.get(tId).getName());
				}
			}
		}
		return bitmapNames.toArray(String[]::new);
	}

	private void addBitmapChangeListeners() {
		keyframeTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("mouseClicked");
				checkChangeBitmapPressed(e.getPoint(), KeyEvent.VK_ENTER);
			}
		});

		keyframeTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("CVP keyReleased! " + e.getKeyCode());
				if (e.getKeyCode() == KeyEvent.VK_T) {
//					System.out.println("C-Point: " + e.getComponent().getLocation() + ", comp: " + e.getComponent());
					Point compPoint = e.getComponent().getLocation();
					Point point = new Point(compPoint.y, compPoint.x);

					checkChangeBitmapPressed(point, e.getKeyCode());
				}
			}
		});
	}

	private void checkChangeBitmapPressed(Point point, int keyCode) {
		System.out.println("checkChangeBitmapPressed");

		int colorChangeColumnIndex = keyframeTable.getColumnCount() - 2;
		if (keyCode == KeyEvent.VK_T || keyCode == KeyEvent.VK_ENTER && keyframeTable.getSelectedColumn() == colorChangeColumnIndex) {
			selectedRow = keyframeTable.getSelectedRow();
			listenersEnabled = false;
			textureChooser.setSelectedIndex((Integer) floatTrackTableModel.getValueAt(selectedRow, 1));
			listenersEnabled = true;
			chooseTextureComboPopup.show(keyframeTable, point.x, point.y);
		}
	}

	private void changeTexture() {
//		bitmapId = textureChooser.getSelectedIndex();

		changeEntry(selectedRow, 1, "Value", Integer.toString(textureChooser.getSelectedIndex()));
	}


	private void changeStaticBitmap() {
		System.out.println("ActionListener");
		if (listenersEnabled) {
			int bitmapId = staticTextureChooser.getSelectedIndex();
			Bitmap bitmap = bitmapListModel.get(staticTextureChooser.getSelectedIndex());

			ChangeLayerStaticTextureAction changeLayerStaticTextureAction = new ChangeLayerStaticTextureAction(bitmap, bitmapId, (Layer) timelineContainer, modelStructureChangeListener);
			changeLayerStaticTextureAction.redo();
			undoActionListener.pushAction(changeLayerStaticTextureAction);
		}
	}

}
