package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ObjectTabTreeBrowserTransferHandler extends TransferHandler {
	private final DataFlavor dataFlavor;

	public ObjectTabTreeBrowserTransferHandler(final WorldEditorDataType worldEditorDataType) {
		dataFlavor = new DataFlavor(byte[].class, "JWc3ObjData:" + worldEditorDataType.name());
	}

	/**
	 * Perform the actual data import.
	 */
	@Override
	public boolean importData(final TransferHandler.TransferSupport info) {
		try {
			byte[] data = null;
			War3ObjectDataChangeset pastedObjects = null;

			// If we can't handle the import, bail now.
			if (!canImport(info)) {
				return false;
			}

			final UnitEditorTree editorPanel = (UnitEditorTree) info.getComponent();
			// Fetch the data -- bail if this fails
			try {
				data = (byte[]) info.getTransferable().getTransferData(dataFlavor);
				pastedObjects = new War3ObjectDataChangeset(editorPanel.getWar3ObjectDataChangesetKindChar());
				try (BlizzardDataInputStream inputStream = new BlizzardDataInputStream(
						new ByteArrayInputStream(data))) {

					pastedObjects.load(inputStream, null, false);
				}
			} catch (final UnsupportedFlavorException ufe) {
				System.out.println("importData: unsupported data flavor");
				ufe.printStackTrace();
				ExceptionPopup.display(ufe);
				return false;
			} catch (final IOException ioe) {
				System.out.println("importData: I/O exception");
				ioe.printStackTrace();
				ExceptionPopup.display(ioe);
				return false;
			}

			if (info.isDrop()) { // This is a drop
				final UnitEditorPanel.DropLocation dl = (UnitEditorPanel.DropLocation) info.getDropLocation();
				final Point dropPoint = dl.getDropPoint();
				// discard drop point, unit location is based on tree sorter folders
			} else { // This is a paste
			}
			editorPanel.acceptPastedObjectData(pastedObjects);
			return true;
		} catch (final Throwable th) {
			th.printStackTrace();
			ExceptionPopup.display(th);
			return false;
		}
	}

	/**
	 * Bundle up the data for export.
	 */
	@Override
	protected Transferable createTransferable(final JComponent c) {
		try {
			final UnitEditorTree unitEditorPanel = (UnitEditorTree) c;
			final War3ObjectDataChangeset selectedUnitsAsChangeset = unitEditorPanel.copySelectedObjects();
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try (BlizzardDataOutputStream blizzardStream = new BlizzardDataOutputStream(outputStream)) {
				selectedUnitsAsChangeset.save(blizzardStream, false);
			} catch (final IOException e) {
				System.out.println("failed to copy");
				e.printStackTrace();
			}
			final byte[] byteArray = outputStream.toByteArray();
			return new Transferable() {
				final DataFlavor[] flavors = { dataFlavor };

				@Override
				public boolean isDataFlavorSupported(final DataFlavor flavor) {
					for (final DataFlavor flavorAllowed : flavors) {
						if (flavorAllowed.equals(flavor)) {
							return true;
						}
					}
					return false;
				}

				@Override
				public DataFlavor[] getTransferDataFlavors() {
					return flavors;
				}

				@Override
				public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
					return byteArray;
				}
			};
		} catch (final Throwable th) {
			th.printStackTrace();
			ExceptionPopup.display(th);
			return null;
		}
	}

	/**
	 * The list handles both copy and move actions.
	 */
	@Override
	public int getSourceActions(final JComponent c) {
		return COPY_OR_MOVE;
	}

	/**
	 * When the export is complete, remove the old list entry if the action was a
	 * move.
	 */
	@Override
	protected void exportDone(final JComponent c, final Transferable data, final int action) {
		if (action != MOVE) {
			return;
		}
		// final JList list = (JList) c;
		// final DefaultListModel model = (DefaultListModel) list.getModel();
		// final int index = list.getSelectedIndex();
		// model.remove(index);
	}

	/**
	 * We only support importing strings.
	 */
	@Override
	public boolean canImport(final TransferHandler.TransferSupport support) {
		// we only import Strings
		return support.isDataFlavorSupported(dataFlavor);
	}
}