package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.MatrixShell;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

public final class MatrixShell2DListCellRenderer extends AbstractSnapshottingListCellRenderer2D<MatrixShell> {

	public MatrixShell2DListCellRenderer(final ModelView modelDisplay, final ModelView otherDisplay) {
		super(modelDisplay, otherDisplay);
	}

	private static final class VertexMatrixFilter implements ResettableVertexFilter<MatrixShell> {
		private Matrix matrix;

		@Override
		public VertexMatrixFilter reset(final MatrixShell matrixShell) {
			matrix = matrixShell.getMatrix();
			return this;
		}

		@Override
		public boolean isAccepted(final GeosetVertex vertex) {
			return vertex.getBoneAttachments().equals(matrix.getBones());
		}

	}

	@Override
	protected ResettableVertexFilter<MatrixShell> createFilter() {
		return new VertexMatrixFilter();
	}

	@Override
	protected MatrixShell valueToType(final Object value) {
		return (MatrixShell) value;
	}

	@Override
	protected boolean contains(final ModelView modelDisp, final MatrixShell object) {
		return !object.getMatrix().getBones().isEmpty() && modelDisp.getModel().contains(object.getMatrix().getBones().get(0));
	}

	@Override
	protected Vec3 getRenderVertex(final MatrixShell value) {
		return null;
	}
}
