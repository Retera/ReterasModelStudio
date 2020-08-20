package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import org.lwjgl.LWJGLException;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public abstract class AbstractSnapshottingListCellRenderer<TYPE> extends DefaultListCellRenderer {
	private final Map<TYPE, ImageIcon> matrixShellToCachedRenderer = new HashMap<>();
	private final MDLSnapshot modelSnapshot;
	private final ResettableVertexFilter<TYPE> matrixFilter;

	public AbstractSnapshottingListCellRenderer(final ModelView modelDisplay) {
		matrixFilter = createFilter();
		try {
			modelSnapshot = new MDLSnapshot(modelDisplay, 64, 64, null);
			modelSnapshot.zoomToFit();
		} catch (final LWJGLException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract ResettableVertexFilter<TYPE> createFilter();

	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
			final boolean iss, final boolean chf) {
		final TYPE matrixShell = valueToType(value);
		ImageIcon myIcon = matrixShellToCachedRenderer.get(matrixShell);
		if (myIcon == null) {
			try {
				modelSnapshot.zoomToFit(matrixFilter.reset(matrixShell));
				myIcon = new ImageIcon(modelSnapshot.getBufferedImage(matrixFilter));
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			matrixShellToCachedRenderer.put(matrixShell, myIcon);
		}
		super.getListCellRendererComponent(list, matrixShell.toString(), index, iss, chf);
		setIcon(myIcon);
		return this;
	}

	protected abstract TYPE valueToType(Object value);

	protected interface ResettableVertexFilter<TYPE> extends VertexFilter<GeosetVertex> {
		ResettableVertexFilter<TYPE> reset(final TYPE matrix);
	}
}
