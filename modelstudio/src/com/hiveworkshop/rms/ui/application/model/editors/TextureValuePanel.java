package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.actions.model.material.ChangeLayerStaticTextureAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelTextureThings;
import com.hiveworkshop.rms.util.TwiComboBoxModel;
import com.hiveworkshop.rms.util.TwiComboPopup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TextureValuePanel extends ValuePanel<Integer> {

//	private final BasicComboPopup chooseTextureComboPopup;
	private final TwiComboPopup<Bitmap> chooseTextureComboPopup3;
	private final JComboBox<Bitmap> textureChooser;
	DefaultListModel<Bitmap> bitmapListModel;
	private JComboBox<Bitmap> staticTextureChooser;
//	private boolean listenersEnabled = true;

	private Bitmap bitmap;

	private int selectedRow;


	public TextureValuePanel(ModelHandler modelHandler, String title) {
		super(modelHandler, title);

		getTextures(modelHandler.getModel());

		textureChooser = new JComboBox<>();
		Bitmap[] bitmaps = modelHandler.getModel().getTextures().toArray(new Bitmap[0]);
		textureChooser.setModel(new TwiComboBoxModel<>(bitmaps));
		textureChooser.setRenderer(ModelTextureThings.getTextureListRenderer());
		textureChooser.addItemListener(this::setTextureId);
//		chooseTextureComboPopup = new BasicComboPopup(textureChooser);
		chooseTextureComboPopup3 = new TwiComboPopup<>(textureChooser, new Bitmap("Textures\\White.dds"));

		staticTextureChooser.setRenderer(ModelTextureThings.getTextureListRenderer());
		staticTextureChooser.setModel(new DefaultComboBoxModel<>(bitmaps));
		//todo

//		keyframePanel.getFloatTrackTableModel().addExtraColumn("Texture", "", String.class);  // 🎨 \uD83C\uDFA8

		addBitmapChangeListeners();
	}

	private void setTextureId(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED && ((Layer) timelineContainer).getTextures() != null && !((Layer) timelineContainer).getTextures().get(selectedRow).equals(e.getItem())) {
			changeEntry(keyframePanel.getSequence(), selectedRow, "Value", Integer.toString(textureChooser.getSelectedIndex()));
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
		staticTextureChooser = new JComboBox<>();
		staticTextureChooser.addItemListener(this::changeStaticBitmap);
		return staticTextureChooser;
	}

	@Override
	void reloadStaticValue(Integer bitmapId) {
		bitmap = ((Layer) timelineContainer).getTextureBitmap();
		staticValue = bitmapId;
		if (bitmapId == -1) {
			staticTextureChooser.setSelectedItem(bitmap);
			staticValue = staticTextureChooser.getSelectedIndex();
			if (staticValue != -1) {
				this.bitmap = bitmapListModel.get(staticValue);
			}
		} else if (bitmapId < bitmapListModel.size()) {
			this.bitmap = bitmapListModel.get(bitmapId);
			staticTextureChooser.setSelectedItem(bitmap);
		}

		if(animFlag != null){
			for(KeyframePanel<Integer> kfp : keyframePanelMap.values()){
//				kfp.addAllowedCharatcters("");
				kfp.getFloatTrackTableModel().addExtraColumn("Texture", "", String.class);  // 🎨 \uD83C\uDFA8
				kfp.getFloatTrackTableModel().updateExtraButtonValues(getBitmapNameList());
			}
		}
//		if (animFlag != null) {
//			keyframePanel.getFloatTrackTableModel().updateExtraButtonValues(getBitmapNameList());
//		}
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
		for (Sequence anim : animFlag.getAnimMap().keySet()){
			TreeMap<Integer, Entry<Integer>> entryMap = animFlag.getEntryMap(anim);
			if(entryMap != null){
				for (Entry<Integer> entry : entryMap.values()) {
					int tId = entry.getValue();
					if (tId < bitmapListModel.size()) {
						bitmapNames.add(bitmapListModel.get(tId).getName());
					}
//			if (tId < bitmapListModel.size()) {
//				bitmapNames.add(bitmapListModel.get(tId).getName());
//			}
				}
			}
		}
		return bitmapNames.toArray(String[]::new);
	}

	private void addBitmapChangeListeners() {
		keyframePanel.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("mouseClicked");
				checkChangeBitmapPressed(e.getPoint(), KeyEvent.VK_ENTER);
			}
		});

		keyframePanel.getTable().addKeyListener(new KeyAdapter() {
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

		int colorChangeColumnIndex = keyframePanel.getTable().getColumnCount() - 2;
		if (keyCode == KeyEvent.VK_T || keyCode == KeyEvent.VK_ENTER && keyframePanel.getTable().getSelectedColumn() == colorChangeColumnIndex) {
			selectedRow = keyframePanel.getTable().getSelectedRow();
			textureChooser.setSelectedIndex((Integer) keyframePanel.getFloatTrackTableModel().getValueAt(selectedRow, 1));
//			chooseTextureComboPopup.show(keyframePanel.getTable(), point.x, point.y);
			chooseTextureComboPopup3.show(keyframePanel.getTable(), point.x, point.y);
		}
	}


	private void changeStaticBitmap(ItemEvent e) {

		if(e.getStateChange() == ItemEvent.SELECTED
				&& (((Layer) timelineContainer).getTextureBitmap() != null
				&& !((Layer) timelineContainer).getTextureBitmap().equals(e.getItem())
				|| ((Layer) timelineContainer).getTextureBitmap() == null && e.getItem() != null)){
			int bitmapId = staticTextureChooser.getSelectedIndex();
			Bitmap bitmap = bitmapListModel.get(staticTextureChooser.getSelectedIndex());

			ChangeLayerStaticTextureAction changeLayerStaticTextureAction = new ChangeLayerStaticTextureAction(bitmap, bitmapId, (Layer) timelineContainer, changeListener);
			undoManager.pushAction(changeLayerStaticTextureAction.redo());
		}
	}

	protected AnimFlag<Integer> getNewAnimFlag() {
		return new IntAnimFlag(flagName);
	}

}
