package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.DrawGeometryAction;
import com.hiveworkshop.rms.editor.actions.mesh.AddGeometryAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Pair;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;
import java.util.*;

public class DrawFaceActivity extends DrawActivity {

	public DrawFaceActivity(ModelHandler modelHandler,
	                        ModelEditorManager modelEditorManager) {
		super(modelHandler, modelEditorManager);
	}
	public DrawFaceActivity(ModelHandler modelHandler,
	                        ModelEditorManager modelEditorManager,
	                        ModelEditorActionType3 lastEditorType) {
		super(modelHandler, modelEditorManager, lastEditorType);
	}

	@Override
	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (MouseEventHelpers.matches(e, getModify(), getSnap())) {
			Vec2 point = getPoint(e);
			mouseStartPoint.set(point);
			this.inverseViewProjectionMatrix.set(viewProjectionMatrix).invert();
			this.viewProjectionMatrix.set(viewProjectionMatrix);
			setHalfScreenXY();

			List<UndoAction> setupActions = new ArrayList<>();
			Set<GeosetVertex> vertices = new LinkedHashSet<>();
			Set<GeosetVertex> selectedVertices = modelView.getSelectedVertices();
			Map<Geoset, List<Pair<GeosetVertex, GeosetVertex>>> edges = getEdges(selectedVertices);

			if (!edges.isEmpty()) {
				Vec3 tempTriNorm = new Vec3();
				for (Geoset geoset : edges.keySet()) {
					GeosetVertex vert = new GeosetVertex(Vec3.ZERO, Vec3.Z_AXIS);
					vert.addTVertex(new Vec2(0, 0));

					Set<Triangle> tris = new LinkedHashSet<>();
					for (Pair<GeosetVertex, GeosetVertex> edge : edges.get(geoset)) {
						tempTriNorm.setAsPlaneNorm(edge.getFirst(), edge.getSecond(), vert);
						boolean shouldFlipTri = tempTriNorm.dot(edge.getFirst().getNormal()) < 0;
						Triangle newTri = shouldFlipTri
								? new Triangle(edge.getSecond(), edge.getFirst(), vert)
								: new Triangle(edge.getFirst(), edge.getSecond(), vert);
						tris.add(newTri);
					}

					UndoAction setupAction = getSetupAction(geoset, Collections.singleton(vert), tris);
					setupActions.add(setupAction);
					vertices.add(vert);
				}

				UndoAction setupAllAction = new CompoundAction("SetupActions", setupActions);

				Mat4 rotMat = getRotMat();
				startPoint3d.set(get3DPoint(mouseStartPoint));
				transformAction = new DrawGeometryAction("Draw Face", startPoint3d, rotMat, vertices, setupAllAction, null).doSetup();
				transformAction.setScale(Vec3.ZERO);
			}
		}
	}

	private Map<Geoset, List<Pair<GeosetVertex, GeosetVertex>>> getEdges(Set<GeosetVertex> selectedVertices) {
		HashMap<Geoset, List<Pair<GeosetVertex, GeosetVertex>>> geosetEdgeMap = new HashMap<>();
		Set<GeosetVertex> checkedVerts = new HashSet<>();

		for (GeosetVertex vertex : selectedVertices) {
			checkedVerts.add(vertex);
			for (Triangle triangle : vertex.getTriangles()) {
				for (GeosetVertex triVert : triangle.getVerts()) {
					if (!checkedVerts.contains(triVert) && selectedVertices.contains(triVert)) {
						Pair<GeosetVertex, GeosetVertex> edge = new Pair<>(vertex, triVert);
						geosetEdgeMap.computeIfAbsent(vertex.getGeoset(), k -> new ArrayList<>()).add(edge);
					}
				}
			}
		}
		return geosetEdgeMap;
	}

	protected UndoAction getSetupAction(Geoset geoset, Collection<GeosetVertex> vertices, Collection<Triangle> triangles) {
		if (geoset == null) {
			return super.getSetupAction(vertices, triangles);
		} else {
			List<UndoAction> undoActions = new ArrayList<>();

			undoActions.add(new AddGeometryAction(geoset, vertices, triangles, true, null));

			undoActions.add(new SetSelectionUggAction(vertices, modelView, "Select Mesh", null));
			return new CompoundAction("Draw Mesh", undoActions,  changeListener::geosetsUpdated);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (transformAction != null) {
			transformAction.setTranslation(get3DPoint(getPoint(e)));
		}
	}
}
