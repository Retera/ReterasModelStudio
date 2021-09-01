package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.MatrixShell;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

public final class MatrixShell2DListCellRenderer extends AbstractSnapshottingListCellRenderer2D<MatrixShell> {

	public MatrixShell2DListCellRenderer(EditableModel model, EditableModel other) {
		super(model, other);
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
//			return vertex.getBones().equals(matrix.getBones());
			return vertex.getMatrix().equals(matrix);
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
	protected boolean contains(EditableModel model, final MatrixShell object) {
		return !object.getMatrix().getBones().isEmpty() && model.contains(object.getMatrix().getBones().get(0));
	}

	@Override
	protected Vec3 getRenderVertex(final MatrixShell value) {
		return null;
	}
}
