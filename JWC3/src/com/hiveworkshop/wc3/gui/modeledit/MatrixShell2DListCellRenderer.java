package com.hiveworkshop.wc3.gui.modeledit;

import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Matrix;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class MatrixShell2DListCellRenderer extends AbstractSnapshottingListCellRenderer2D<MatrixShell> {

	public MatrixShell2DListCellRenderer(final MDLDisplay modelDisplay, final MDLDisplay otherDisplay) {
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
	protected boolean contains(final MDLDisplay modelDisp, final MatrixShell object) {
		return modelDisp.getMDL().contains(object.getMatrix().getBones().get(0));
	}

	@Override
	protected Vertex getRenderVertex(final MatrixShell value) {
		return null;
	}
}
