package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.actions.model.material.ChangeLayerStaticTextureAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.BitmapAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelTextureThings;
import com.hiveworkshop.rms.util.TwiComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TextureValuePanel2 extends ValuePanel<Bitmap> {

//	private final BasicComboPopup chooseTextureComboPopup;
//	private final TwiComboPopup<Bitmap> chooseTextureComboPopup3;
	private final TwiComboBox<Bitmap> textureChooser;
//	DefaultListModel<Bitmap> bitmapListModel;
	private TwiComboBox<Bitmap> staticTextureChooser;
//	private boolean listenersEnabled = true;

	private Bitmap bitmap;

	private int selectedRow;


	public TextureValuePanel2(ModelHandler modelHandler, String title) {
		super(modelHandler, title);

		textureChooser = new TwiComboBox<>(modelHandler.getModel().getTextures(), new Bitmap("", 1));
		textureChooser.setRenderer(ModelTextureThings.getTextureListRenderer());

		staticTextureChooser.setRenderer(ModelTextureThings.getTextureListRenderer());
		staticTextureChooser.setNewLinkedModelOf(modelHandler.getModel().getTextures());
		//todo

//		keyframePanel.getFloatTrackTableModel().addExtraColumn("Texture", "", String.class);  // 🎨 \uD83C\uDFA8

//		addBitmapChangeListeners();
	}


	@Override
	JComponent getStaticComponent() {
		staticTextureChooser = new TwiComboBox<>(new Bitmap("", 1));
		staticTextureChooser.setPrototypeDisplayValue(new Bitmap("", 1));
		staticTextureChooser.addItemListener(this::changeStaticBitmap);
		return staticTextureChooser;
	}

	@Override
	void reloadStaticValue(Bitmap bitmap) {
		this.bitmap = ((Layer) timelineContainer).getTextureBitmap();
		staticValue = bitmap;
		if (bitmap == null) {
			staticTextureChooser.setSelectedItem(this.bitmap);
			staticValue = staticTextureChooser.getSelected();
			if (staticValue != null) {
				this.bitmap = (Bitmap) staticTextureChooser.getSelectedItem();
			}
		} else {
			staticTextureChooser.setSelectedItem(bitmap);
		}

		if(animFlag != null){
			for(KeyframePanel<Bitmap> kfp : keyframePanelMap.values()){
				kfp.getFloatTrackTableModel().addExtraColumn("Texture", "", Bitmap.class);
//				if(kfp.getFloatTrackTableModel().getRowCount()>2){
//					System.out.println("editable 1,2: " + kfp.getFloatTrackTableModel().isCellEditable(1,2));
//				}
				kfp.getFloatTrackTableModel().setColEditable(true, 2);
				kfp.getFloatTrackTableModel().updateExtraButtonValues(modelHandler.getModel().getTextures().toArray(Bitmap[]::new));
//				DefaultCellEditor editor = new DefaultCellEditor(textureChooser);
//				kfp.getTable().setDefaultEditor(Bitmap.class, editor);
//				kfp.getTable().setDefaultEditor(Bitmap.class, new TableComboBoxEditor<>(new TwiComboBox<>(model.getTextures(), new Bitmap("", 1))));
				TwiComboBox<Bitmap> textureChooser = new TwiComboBox<>(modelHandler.getModel().getTextures(), new Bitmap("", 1));
				textureChooser.setRenderer(ModelTextureThings.getTextureListRenderer());
				kfp.getTable().setDefaultEditor(Bitmap.class, new TableComboBoxEditor<>(textureChooser));
//				kfp.setValueRenderingConsumer(this::valueCellRendering);
			}
		}

	}

//	private Bitmap[] getBitMatArr(){
//		for ()
//	}


	@Override
	Bitmap getZeroValue() {
//		return new Bitmap("Textures\\White.dds");
		return null;
	}

	@Override
	Bitmap parseValue(String valueString) {
		String polishedString = valueString.replaceAll("[\\D]", "");

		return textureChooser.getItemAt(Integer.parseInt(polishedString));
	}


	private String[] getBitmapNameList() {
		List<String> bitmapNames = new ArrayList<>();
		for (Sequence anim : animFlag.getAnimMap().keySet()){
			TreeMap<Integer, Entry<Bitmap>> entryMap = animFlag.getEntryMap(anim);
			if(entryMap != null){
				for (Entry<Bitmap> entry : entryMap.values()) {
					Bitmap tId = entry.getValue();
					if (tId != null) {
						bitmapNames.add(tId.getName());
					}
				}
			}
		}
		return bitmapNames.toArray(String[]::new);
	}

	protected KeyframePanel<Bitmap> addListeners(KeyframePanel<Bitmap> keyframePanel) {
		keyframePanel.getTable().setCellEditor(new DefaultCellEditor(textureChooser));
		keyframePanel.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("mouseClicked");
				checkChangeBitmapPressed(e.getPoint(), KeyEvent.VK_ENTER, keyframePanel);
			}
		});

		keyframePanel.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("CVP keyReleased! " + e.getKeyCode());
				if (e.getKeyCode() == KeyEvent.VK_T) {
					System.out.println("C-Point: " + e.getComponent().getLocation() + ", comp: " + e.getComponent());
					Point compPoint = e.getComponent().getLocation();
					Point point = new Point(compPoint.y, compPoint.x);

					checkChangeBitmapPressed(point, e.getKeyCode(), keyframePanel);
				}
			}
		});
		return keyframePanel;
	}

	protected void valueCellRendering(Component tableCellRendererComponent, Object value) {
		System.out.println("value: " + value);
		Rectangle bounds = tableCellRendererComponent.getBounds();
		ModelTextureThings.getTextureTableCellRenderer().getPaintedRect(tableCellRendererComponent, bounds, value);
//		Color bgColor = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), rowColor, 1.0f);
//		tableCellRendererComponent.setBackground(bgColor);
//		tableCellRendererComponent.setForeground(getTextColor(bgColor));
	}

	private void checkChangeBitmapPressed(Point point, int keyCode, KeyframePanel<Bitmap> keyframePanel) {
		System.out.println("checkChangeBitmapPressed");

		int colorChangeColumnIndex = keyframePanel.getTable().getColumnCount() - 2;
		if (keyCode == KeyEvent.VK_T || keyCode == KeyEvent.VK_ENTER && keyframePanel.getTable().getSelectedColumn() == colorChangeColumnIndex) {
			selectedRow = keyframePanel.getTable().getSelectedRow();
			textureChooser.setSelectedItem((Bitmap) keyframePanel.getFloatTrackTableModel().getValueAt(selectedRow, 1));
//			textureChooser.getComponentPopupMenu().show(keyframePanel.getTable().p);
//			textureChooser.showPopup();
			System.out.println("uggggg");
//			chooseTextureComboPopup.show(keyframePanel.getTable(), point.x, point.y);
//			chooseTextureComboPopup3.show(keyframePanel.getTable(), point.x, point.y);
		}
	}


	private void changeStaticBitmap(ItemEvent e) {

		if(e.getStateChange() == ItemEvent.SELECTED
				&& (((Layer) timelineContainer).getTextureBitmap() != null
				&& !((Layer) timelineContainer).getTextureBitmap().equals(e.getItem())
				|| ((Layer) timelineContainer).getTextureBitmap() == null && e.getItem() != null)) {
			int bitmapId = staticTextureChooser.getSelectedIndex();
//			Bitmap bitmap = bitmapListModel.get(staticTextureChooser.getSelectedIndex());
			Bitmap bitmap = (Bitmap) staticTextureChooser.getSelectedItem();

			ChangeLayerStaticTextureAction changeLayerStaticTextureAction = new ChangeLayerStaticTextureAction(bitmap, bitmapId, (Layer) timelineContainer, changeListener);
			undoManager.pushAction(changeLayerStaticTextureAction.redo());
		}
	}
	private void changeDynamicBitmap(ItemEvent e, KeyframePanel<Bitmap> keyframePanel) {
		Sequence sequence = keyframePanel.getSequence();
		if(e.getStateChange() == ItemEvent.SELECTED && animFlag != null && sequence != null) {
			int row = keyframePanel.getTable().getSelectedRow();
			int timeFromIndex = animFlag.getTimeFromIndex(sequence, row);
			Bitmap selected = textureChooser.getSelected();
			if(timeFromIndex != -1 && animFlag.getEntryAt(sequence, timeFromIndex).value != selected){
				Entry<Bitmap> newEntry = new Entry<>(timeFromIndex, selected);
				changeEntry(sequence, timeFromIndex, newEntry);
			}
		}

	}
	private void changeDynamicBitmap(Bitmap bitmap, KeyframePanel<Bitmap> keyframePanel) {
		Sequence sequence = keyframePanel.getSequence();
		if(animFlag != null && sequence != null) {
			int row = keyframePanel.getTable().getSelectedRow();
			int timeFromIndex = animFlag.getTimeFromIndex(sequence, row);
			if(timeFromIndex != -1 && animFlag.getEntryAt(sequence, timeFromIndex).value != bitmap){
				Entry<Bitmap> newEntry = new Entry<>(timeFromIndex, bitmap);
				changeEntry(sequence, timeFromIndex, newEntry);
			}
		}

	}

	protected AnimFlag<Bitmap> getNewAnimFlag() {
		return new BitmapAnimFlag(flagName);
	}

}
