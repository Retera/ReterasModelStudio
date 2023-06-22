package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.uv.MirrorTVerticesAction;
import com.hiveworkshop.rms.editor.actions.uv.SwapXYTVerticesAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class FlipUVs {
	public static class FlipUVsX extends ActionFunction {
		public FlipUVsX() {
			super(TextKey.MIRROR_ALL_UVS_X, FlipUVsX::flip);
			setMenuItemMnemonic(KeyEvent.VK_X);
		}
		private static void flip(ModelHandler m) {
			flipAllUVsDim(m, Vec2.X_AXIS, new Vec2(0.5, 0.5));
		}
	}

	public static class FlipUVsY extends ActionFunction {
		public FlipUVsY() {
			super(TextKey.MIRROR_ALL_UVS_Y, FlipUVsY::flip);
			setMenuItemMnemonic(KeyEvent.VK_Y);
		}


		private static void flip(ModelHandler m) {
			flipAllUVsDim(m, Vec2.Y_AXIS, new Vec2(0.5, 0.5));
		}
	}
	public static class FlipSelUVsX extends ActionFunction {
		public FlipSelUVsX() {
			super(TextKey.MIRROR_SELECTED_UVS_X, FlipSelUVsX::flip);
		}


		private static void flip(ModelHandler m) {
			flipSelectedUVsDim(m, Vec2.X_AXIS, null, 0);
		}
	}

	public static class FlipSelUVsY extends ActionFunction {
		public FlipSelUVsY() {
			super(TextKey.MIRROR_SELECTED_UVS_Y, FlipSelUVsY::flip);
		}


		private static void flip(ModelHandler m) {
			flipSelectedUVsDim(m, Vec2.Y_AXIS, null, 0);
		}
	}

	public static class InvertAllUVs extends ActionFunction {
		public InvertAllUVs() {
			super(TextKey.INVERT_ALL_UVS, InvertAllUVs::inverseAllUVs);
			setMenuItemMnemonic(KeyEvent.VK_S);
		}

		public static void inverseAllUVs(ModelHandler modelHandler) {
			if (modelHandler != null) {
				Set<Vec2> tVerts = new HashSet<>();
				for (final Geoset geo : modelHandler.getModel().getGeosets()) {
					for (final GeosetVertex vertex : geo.getVertices()) {
						tVerts.addAll(vertex.getTverts());
					}
				}
				modelHandler.getUndoManager().pushAction(new SwapXYTVerticesAction(tVerts, ModelStructureChangeListener.changeListener).redo());
			}
		}
	}

	public static void flipAllUVsDim(ModelHandler modelHandler, Vec2 axis, Vec2 center) {
		if (modelHandler != null) {
			Set<Vec2> tVerts = new HashSet<>();
			for (Geoset geo : modelHandler.getModel().getGeosets()) {
				for (GeosetVertex vertex : geo.getVertices()) {
					tVerts.addAll(vertex.getTverts());
				}
			}
			UndoAction action = new MirrorTVerticesAction(tVerts, center, axis, ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	public static void flipSelectedUVsDim(ModelHandler modelHandler, Vec2 axis, Vec2 center, int uvLayerIndex) {
		ModelView modelView = modelHandler.getModelView();
		Set<GeosetVertex> selectedVertices = modelView.getSelectedVertices();
		UndoAction action = getFlippSelectedAction(axis, center, selectedVertices, uvLayerIndex);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	private static UndoAction getFlippSelectedAction(Vec2 axis, Vec2 center, Set<GeosetVertex> selectedVertices, int uvLayerIndex) {
		Set<Vec2> tVerts = new HashSet<>();
		for (GeosetVertex vertex : selectedVertices) {
			tVerts.add(vertex.getTVertex(uvLayerIndex));
		}
		if (center == null) {
			center = Vec2.centerOfGroup(tVerts);
		}
		return new MirrorTVerticesAction(tVerts, center, axis, ModelStructureChangeListener.changeListener);
	}
}
