package com.hiveworkshop.rms.editor.actions.mesh;

public enum VertexActionType {
	MOVE, ROTATE, SCALE, UNKNOWN;

	public static VertexActionType fromLegacyId(final int id) {
		return switch (id) {
			case 3 -> MOVE;
			case 4 -> ROTATE;
			case 5 -> SCALE;
			default -> UNKNOWN;
		};
	}
}
