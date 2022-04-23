package com.hiveworkshop.wc3.gui.modelviewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.creator.actions.DrawCameraAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class CameraManagerPanel extends JPanel {
	private ModelView mdlDisp;
	private final DefaultListModel<Camera> cameras;
	private final JList<Camera> cameraBox;
	private ListSelectionListener boxChangeListener;

	public CameraManagerPanel(final ModelView mdlDisp, final CameraManagerPanelListener listener,
			final ModelStructureChangeListener modelStructureChangeListener, final ModelEditor modelEditor,
			final UndoActionListener undoActionListener) {
		this(mdlDisp, listener, null, modelStructureChangeListener, modelEditor, undoActionListener);
	}

	public CameraManagerPanel(final ModelView mdlDisp, final CameraManagerPanelListener listener,
			final Camera defaultCamera, final ModelStructureChangeListener modelStructureChangeListener,
			final ModelEditor modelEditor, final UndoActionListener undoActionListener) {
		this.mdlDisp = mdlDisp;
		final GroupLayout groupLayout = new GroupLayout(this);

		cameras = new DefaultListModel<>();
		cameras.addElement(null);
		for (final Camera animation : mdlDisp.getModel().getCameras()) {
			cameras.addElement(animation);
		}
		cameraBox = new JList<>(cameras);
		cameraBox.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList list, final Object value, final int index,
					final boolean isSelected, final boolean cellHasFocus) {
				Object display = value == null ? "(Default)" : value;
				if (value != null) {
					String displayToString;
					if (display instanceof Camera) {
						displayToString = ((Camera) display).getName();
					} else {
						displayToString = display.toString();
					}
					display = "(" + mdlDisp.getModel().getCameras().indexOf(value) + ") " + displayToString;
				}
				return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
			}
		});
		boxChangeListener = new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					final Camera value = cameraBox.getSelectedValue();
					if (value == null) {
						listener.setDefaultCamera();
					} else {
						listener.setCamera(value);
					}
				}
			}
		};

		cameraBox.addListSelectionListener(boxChangeListener);

		final JScrollPane cameraScroll = new JScrollPane(cameraBox);

		final JButton createCameraFromView = new JButton("Create Camera from Current View");
		createCameraFromView.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Camera cameraFromView = listener.createCameraFromCurrentView();
				final DrawCameraAction drawCameraAction = new DrawCameraAction(mdlDisp, modelStructureChangeListener,
						cameraFromView);
				drawCameraAction.redo();
				undoActionListener.pushAction(drawCameraAction);
			}
		});

		groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup().addGap(8)
				.addGroup(
						groupLayout.createParallelGroup().addComponent(cameraScroll).addComponent(createCameraFromView))
				.addGap(8));
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup().addGap(8).addComponent(cameraScroll).addGap(8)
				.addComponent(createCameraFromView).addGap(8));
		setLayout(groupLayout);

		cameraBox.setSelectedIndex(0);
	}

	public void reload() {
		final Camera selectedItem = cameraBox.getSelectedValue();
		cameras.removeAllElements();
		boolean sawLast = selectedItem == null;
		cameras.addElement(null);
		for (final Camera animation : mdlDisp.getModel().getCameras()) {
			cameras.addElement(animation);
			if (animation == selectedItem) {
				sawLast = true;
			}
		}
		if (sawLast) {
			cameraBox.setSelectedValue(selectedItem, true);
		} else if (selectedItem != null) {
			for (final Camera animation : mdlDisp.getModel().getCameras()) {
				if (animation.getName().equals(selectedItem.getName())) {
					cameraBox.setSelectedValue(animation, true);
					break;
				}
			}
		}
	}

	public Camera getCurrentCamera() {
		return cameraBox.getSelectedValue();
	}

	public void setModel(final ModelView modelView) {
		this.mdlDisp = modelView;
		reload();
	}

	public void setCurrentCamera(final Camera currentCamera) {
		cameraBox.setSelectedValue(currentCamera, true);
		boxChangeListener.valueChanged(new ListSelectionEvent(this, 0, 0, false));
	}
}
