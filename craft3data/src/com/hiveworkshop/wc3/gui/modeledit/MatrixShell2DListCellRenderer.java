package com.hiveworkshop.wc3.gui.modeledit;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Matrix;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class MatrixShell2DListCellRenderer extends AbstractSnapshottingListCellRenderer2D<MatrixShell> {

	public MatrixShell2DListCellRenderer(final ModelView modelDisplay, final ModelView otherDisplay) {
		super(modelDisplay, otherDisplay);
	}

	private static final class VertexMatrixFilter implements ResettableVertexFilter<MatrixShell> {
		private Matrix matrix;

		@Override
		public VertexMatrixFilter reset(final MatrixShell matrixShell) {
			this.matrix = matrixShell.getMatrix();
			return this;
		}

		@Override
		public boolean isAccepted(final GeosetVertex vertex) {
			return vertex.isLinkingSameBones(matrix.getBones());
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
		return object.getMatrix().getBones().isEmpty() ? false
				: modelDisp.getModel().contains(object.getMatrix().getBones().get(0));
	}

	@Override
	protected Vertex getRenderVertex(final MatrixShell value) {
		return null;
	}
}
