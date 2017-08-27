package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.actions.DeleteAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.ExtrudeAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.MoveAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.RotateAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.SelectionActionType;
import com.hiveworkshop.wc3.gui.modeledit.actions.SnapAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.SnapNormalsAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.SpecialDeleteAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.UVMoveAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.UVSelectAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.UVSelectionActionType;
import com.hiveworkshop.wc3.gui.modeledit.actions.UVSnapAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.VertexActionType;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelChangeNotifier;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.TeamColorAddAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.ModelSelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManager;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Matrix;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.Callback;

/**
 * A wrapper for an MDL to control how it is displayed onscreen, between all
 * viewports.
 *
 * Eric Theller 6/7/2012
 */
public class MDLDisplay implements UndoManager {
	private final List<CoordDisplayListener> coordinateListeners;
	ProgramPreferences programPreferences;
	UndoHandler undoHandler;

	MDL model;
	// List<Vertex> selection = new ArrayList<>();
	ModelSelectionManager selectionManager;
	List<TVertex> uvselection = new ArrayList<>();
	List<Geoset> visibleGeosets = new ArrayList<>();
	List<Geoset> editableGeosets = new ArrayList<>();
	Geoset highlight;
	public static Color selectColor = Color.red;
	ModelPanel mpanel;

	List<UndoAction> actionStack = new ArrayList<>();
	List<UndoAction> redoStack = new ArrayList<>();

	int actionType = -1;
	UndoAction currentAction;
	int actionTypeUV = -1;
	UndoAction currentUVAction;

	boolean dispChildren = false;
	boolean dispPivotNames = false;
	boolean dispCameras = false;
	boolean dispCameraNames = false;

	UVPanel uvpanel = null;

	private final ModelChangeNotifier modelChangeNotifier;

	public MDLDisplay(final MDL mdlr, final ModelPanel mpanel, final int vertexSize,
			final ToolbarButtonGroup<SelectionItemTypes> selectionTypeNotifier) {
		model = mdlr;
		this.selectionManager = new ModelSelectionManager(this, vertexSize, selectionTypeNotifier);
		visibleGeosets.addAll(mdlr.getGeosets());
		editableGeosets.addAll(mdlr.getGeosets());
		coordinateListeners = new ArrayList<>();
		this.mpanel = mpanel;
		modelChangeNotifier = new ModelChangeNotifier();
	}

	public ModelSelectionManager getSelectionManager() {
		return selectionManager;
	}

	public void reloadTextures() {
		if (mpanel != null) {
			mpanel.perspArea.reloadTextures();
		}
	}

	public void setUVPanel(final UVPanel panel) {
		uvpanel = panel;
	}

	public MDL getMDL() {
		return model;
	}

	public List<Geoset> getVisibleGeosets() {
		return visibleGeosets;
	}

	public List<Geoset> getEditableGeosets() {
		return editableGeosets;
	}

	public List<TVertex> getUVSelection() {
		return uvselection;
	}

	public void setUvselection(final List<TVertex> uvselection) {
		this.uvselection = uvselection;
	}

	public Geoset getHighlight() {
		return highlight;
	}

	public void setDispPivots(final boolean flag) {
		selectionManager.setShowPivots(flag);
		// if (!flag) {
		// for (final IdObject o : model.getIdObjects()) {
		// final Vertex ver = o.getPivotPoint();
		// selection.remove(ver);
		// }
		// }
	}

	public void setDispPivotNames(final boolean flag) {
		dispPivotNames = flag;
	}

	public void setDispChildren(final boolean flag) {
		dispChildren = flag;

		ArrayList<IdObject> geoParents = null;
		ArrayList<IdObject> geoSubParents = null;
		if (dispChildren) {
			geoParents = new ArrayList<>();
			geoSubParents = new ArrayList<>();
			for (final Geoset geo : editableGeosets) {
				for (final GeosetVertex ver : geo.getVertices()) {
					for (final Bone b : ver.getBones()) {
						if (!geoParents.contains(b)) {
							geoParents.add(b);
						}
					}
				}
			}
			// childMap = new HashMap<IdObject,ArrayList<IdObject>>();
			for (final IdObject obj : model.getIdObjects()) {
				if (!geoParents.contains(obj)) {
					boolean valid = false;
					for (int i = 0; !valid && i < geoParents.size(); i++) {
						valid = geoParents.get(i).childOf(obj);
					}
					if (valid) {
						geoSubParents.add(obj);
					}
					// if( obj.parent != null )
					// {
					// ArrayList<IdObject> children = childMap.get(obj.parent);
					// if( children == null )
					// {
					// children = new ArrayList<IdObject>();
					// childMap.put(obj.parent, children);
					// }
					// children.add(obj);
					// }
				}
			}
			// System.out.println(geoSubParents);
		}
		if (dispChildren) {
			for (final IdObject o : model.getIdObjects()) {
				// boolean hasRef = false;//highlight != null &&
				// highlight.containsReference(o);
				// if( dispChildren )
				// {
				// for( int i = 0; !hasRef && i < editableGeosets.size(); i++ )
				// {
				// hasRef = editableGeosets.get(i).containsReference(o);
				// }
				// }
				if (!(geoParents.contains(o) || geoSubParents.contains(o)))// !dispChildren
																			// ||
																			// hasRef
																			// )
				{
					final Vertex ver = o.getPivotPoint();
					if (selection.contains(ver)) {
						selection.remove(ver);
					}
				}
			}
		}
	}

	public void setDispCameras(final boolean flag) {
		dispCameras = flag;
		if (!flag) {
			for (final Camera cam : model.getCameras()) {
				final Vertex ver = cam.getPosition();
				final Vertex targ = cam.getTargetPosition();
				selection.remove(ver);
				selection.remove(targ);
			}
		}
	}

	public void setDispCameraNames(final boolean flag) {
		dispCameraNames = flag;
	}

	public boolean isGeosetVisible(final int i) {
		return visibleGeosets.contains(model.getGeoset(i));
	}

	public boolean isGeosetEditable(final int i) {
		return editableGeosets.contains(model.getGeoset(i));
	}

	public boolean isGeosetHighlighted(final int i) {
		return highlight == (model.getGeoset(i));
	}

	public void setMatrix(final DefaultListModel<BoneShell> bones) {
		if (!lockdown) {
			final Matrix mx = new Matrix();
			mx.setBones(new ArrayList<Bone>());
			for (int i = 0; i < bones.size(); i++) {
				mx.add(bones.get(i).bone);
			}
			for (int i = 0; i < selection.size(); i++) {
				final Vertex vert = selection.get(i);
				if (vert.getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) vert;
					gv.clearBoneAttachments();
					gv.addBoneAttachments(mx.getBones());
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Action refused.");
		}
	}

	public void setBoneName(final String name) {
		if (!lockdown) {
			if (selection.size() != 1) {
				JOptionPane.showMessageDialog(null, "Cannot rename multiple nodes at once.");
				return;
			}
			final Vertex selectedVertex = selection.get(0);
			for (final IdObject bone : this.model.getIdObjects()) {
				if (bone.getPivotPoint() == selectedVertex) {
					bone.setName(name);
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Action refused.");
		}
	}

	public void cogBones() {
		final ArrayList<IdObject> selBones = new ArrayList<>();
		for (final IdObject b : model.getIdObjects()) {
			if (selection.contains(b.getPivotPoint()) && !selBones.contains(b)) {
				selBones.add(b);
			}
		}
		// HashMap<IdObject,ArrayList<IdObject>> childMap = new
		// HashMap<IdObject,ArrayList<IdObject>>();
		//
		// for( IdObject obj: model.getIdObjects())
		// {
		// if( obj.parent != null )
		// {
		// ArrayList<IdObject> children = childMap.get(obj.parent);
		// if( children == null )
		// {
		// children = new ArrayList<IdObject>();
		// childMap.put(obj.parent, children);
		// }
		// children.add(obj);
		// }
		// }

		for (final IdObject obj : selBones) {
			if (Bone.class.isAssignableFrom(obj.getClass())) {
				final Bone bone = (Bone) obj;
				final ArrayList<GeosetVertex> childVerts = new ArrayList<>();
				for (final Geoset geo : model.getGeosets()) {
					childVerts.addAll(geo.getChildrenOf(bone));
					// if( obj.parent != null )
					// {
					// ArrayList<IdObject> children = childMap.get(obj.parent);
					// if( children == null )
					// {
					// children = new ArrayList<IdObject>();
					// childMap.put(obj.parent, children);
					// }
					// children.add(obj);
					// }
				}
				if (childVerts.size() > 0) {
					bone.getPivotPoint().setTo(Vertex.centerOfGroup(childVerts));
				}
			}
		}
	}

	public void highlightGeoset(final Geoset g, final boolean flag) {
		if (flag) {
			highlight = g;
		} else {
			if (g == highlight) {
				highlight = null;
			}
		}
	}

	public void makeGeosetVisible(final Geoset g, final boolean flag) {
		if (flag) {
			if (!visibleGeosets.contains(g)) {
				visibleGeosets.add(g);
			}
		} else {
			visibleGeosets.remove(g);
		}
	}

	public void makeGeosetEditable(final Geoset g, final boolean flag) {
		if (flag) {
			if (!editableGeosets.contains(g)) {
				editableGeosets.add(g);
			}
		} else {
			// int n = g.numVerteces();
			// for( int i = 0; i < n; i++ )
			// {
			// selection.remove(g.getVertex(i));
			// }
			selection.removeAll(g.getVertices());
			editableGeosets.remove(g);
		}
	}

	public void highlightGeoset(final int i, final boolean flag) {
		final Geoset g = model.getGeoset(i);
		highlightGeoset(g, flag);
	}

	public void makeGeosetVisible(final int i, final boolean flag) {
		final Geoset g = model.getGeoset(i);
		makeGeosetVisible(g, flag);
	}

	public void makeGeosetEditable(final int i, final boolean flag) {
		final Geoset g = model.getGeoset(i);
		makeGeosetEditable(g, flag);
	}

	public void drawTriangles(final Graphics g, final Geoset geo, final CoordinateSystem vp) {
		if (programPreferences.viewMode() == 0 || true) {
			for (final Triangle t : geo.getTriangle()) {
				final double[] x = t.getCoords(vp.getPortFirstXYZ());
				final double[] y = t.getCoords(vp.getPortSecondXYZ());
				final int[] xint = new int[4];
				final int[] yint = new int[4];
				for (int ix = 0; ix < 3; ix++) {
					xint[ix] = (int) Math.round(vp.convertX(x[ix]));
					yint[ix] = (int) Math.round(vp.convertY(y[ix]));
				}
				xint[3] = xint[0];
				yint[3] = yint[0];
				g.drawPolyline(xint, yint, 4);
			}
		} else if (programPreferences.viewMode() == 1) {
			for (final Triangle t : geo.getTriangle()) {
				final double[] x = t.getCoords(vp.getPortFirstXYZ());
				final double[] y = t.getCoords(vp.getPortSecondXYZ());
				final int[] xint = new int[4];
				final int[] yint = new int[4];
				for (int ix = 0; ix < 3; ix++) {
					xint[ix] = (int) Math.round(vp.convertX(x[ix]));
					yint[ix] = (int) Math.round(vp.convertY(y[ix]));
				}
				xint[3] = xint[0];
				yint[3] = yint[0];
				g.drawPolyline(xint, yint, 4);
				g.fillPolygon(xint, yint, 4);
			}
		}
	}

	public void drawFittedTriangles(final Graphics g, final Rectangle bounds, final byte a, final byte b,
			final VertexFilter<? super GeosetVertex> filter, final Vertex extraHighlightPoint) {
		final List<Triangle> triangles = new ArrayList<>();
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		g.setColor(Color.GRAY);
		for (final Geoset geo : model.getGeosets()) {
			for (final Triangle t : geo.getTriangle()) {
				boolean drawTriangle = false;
				for (final GeosetVertex vertex : t.getVerts()) {
					if (filter.isAccepted(vertex)) {
						drawTriangle = true;
					}
				}
				if (drawTriangle) {
					triangles.add(t);
				}
				final double[] x = t.getCoords(a);
				for (final double xval : x) {
					if (xval < minX) {
						minX = xval;
					}
					if (xval > maxX) {
						maxX = xval;
					}
				}
				final double[] y = t.getCoords(b);
				for (final double yval : y) {
					final double yCoord = -yval;
					if (yCoord < minY) {
						minY = yCoord;
					}
					if (yCoord > maxY) {
						maxY = yCoord;
					}
				}
			}
		}
		final double deltaX = maxX - minX;
		final double deltaY = maxY - minY;
		final double boxSize = Math.max(deltaX, deltaY);
		minX -= (boxSize - deltaX) / 2;
		minY -= (boxSize - deltaY) / 2;
		final AffineTransform transform = ((Graphics2D) g).getTransform();
		((Graphics2D) g).scale(bounds.getWidth() / boxSize, bounds.getHeight() / boxSize);
		((Graphics2D) g).translate(-minX, -minY);
		for (final Geoset geo : model.getGeosets()) {
			for (final Triangle t : geo.getTriangle()) {
				drawTriangle(g, a, b, t);
			}
		}
		g.setColor(Color.RED);
		for (final Triangle t : triangles) {
			drawTriangle(g, a, b, t);
		}
		g.setColor(Color.YELLOW);
		if (extraHighlightPoint != null) {
			final int x = (int) extraHighlightPoint.getCoord(a);
			final int y = (int) -extraHighlightPoint.getCoord(b);
			((Graphics2D) g).drawOval(x - 5, y - 5, 10, 10);
			((Graphics2D) g).drawLine(x, y - 10, x, y + 10);
			((Graphics2D) g).drawLine(x - 10, y, x + 10, y);
		}
		((Graphics2D) g).setTransform(transform);
	}

	private void drawTriangle(final Graphics g, final byte a, final byte b, final Triangle t) {
		final double[] x = t.getCoords(a);
		final double[] y = t.getCoords(b);
		final int[] xint = new int[4];
		final int[] yint = new int[4];
		for (int ix = 0; ix < 3; ix++) {
			xint[ix] = (int) Math.round(x[ix]);
			yint[ix] = (int) Math.round(-y[ix]);
		}
		xint[3] = xint[0];
		yint[3] = yint[0];
		g.drawPolyline(xint, yint, 4);
	}

	public void drawTriangles(final Graphics g, final Geoset geo, final UVViewport vp, final int layerId) {
		if (programPreferences.viewMode() == 0 || true) {
			for (final Triangle t : geo.getTriangle()) {
				final double[] x = t.getTVertCoords((byte) 0, layerId);
				final double[] y = t.getTVertCoords((byte) 1, layerId);
				final int[] xint = new int[4];
				final int[] yint = new int[4];
				for (int ix = 0; ix < 3; ix++) {
					xint[ix] = (int) Math.round(vp.convertX(x[ix]));
					yint[ix] = (int) Math.round(vp.convertY(y[ix]));
				}
				xint[3] = xint[0];
				yint[3] = yint[0];
				g.drawPolyline(xint, yint, 4);
			}
		} else if (programPreferences.viewMode() == 1) {
			for (final Triangle t : geo.getTriangle()) {
				final double[] x = t.getTVertCoords((byte) 0, layerId);
				final double[] y = t.getTVertCoords((byte) 1, layerId);
				final int[] xint = new int[4];
				final int[] yint = new int[4];
				for (int ix = 0; ix < 3; ix++) {
					xint[ix] = (int) Math.round(vp.convertX(x[ix]));
					yint[ix] = (int) Math.round(vp.convertY(y[ix]));
				}
				xint[3] = xint[0];
				yint[3] = yint[0];
				g.drawPolyline(xint, yint, 4);
				g.fillPolygon(xint, yint, 4);
			}
		}
	}

	public void drawVerteces(final Graphics g, final Geoset geo, final Viewport vp, final int vertexSize) {
		for (final Vertex ver : geo.getVertices()) {
			g.fillRect((int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))) - vertexSize,
					(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))) - vertexSize, 1 + vertexSize * 2,
					1 + vertexSize * 2);
		}
	}

	public void drawVerteces(final Graphics g, final Geoset geo, final Viewport vp, final int vertexSize,
			final List<Vertex> selection) {
		if (programPreferences.showNormals()) {
			for (final GeosetVertex ver : geo.getVertices()) {
				final Color temp = g.getColor();
				if (ver.getNormal() != null) {
					g.setColor(programPreferences.getNormalsColor());
					g.drawLine((int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),
							(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))),
							(int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ())
									+ ver.getNormal().getCoord(vp.getPortFirstXYZ()) * 12 / vp.getZoomAmount())),
							(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ())
									+ ver.getNormal().getCoord(vp.getPortSecondXYZ()) * 12 / vp.getZoomAmount())));
				}
			}
		}
		g.setColor(programPreferences.getVertexColor());
		for (final Vertex ver : geo.getVertices()) {
			final Color temp = g.getColor();
			if (selection.contains(ver)) {
				g.setColor(MDLDisplay.selectColor);
			}
			g.fillRect((int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))) - vertexSize,
					(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))) - vertexSize, 1 + vertexSize * 2,
					1 + vertexSize * 2);
			if (selection.contains(ver)) {
				g.setColor(temp);
			}
		}
	}

	public void drawTVerteces(final Graphics g, final Geoset geo, final UVViewport vp, final int vertexSize,
			final int layerId) {
		for (int i = 0; i < geo.getVertices().size(); i++) {
			final TVertex ver = geo.getVertices().get(i).getTVertex(layerId);
			g.fillRect((int) Math.round(vp.convertX(ver.getCoord(0))) - vertexSize,
					(int) Math.round(vp.convertY(ver.getCoord(1))) - vertexSize, 1 + vertexSize * 2,
					1 + vertexSize * 2);
		}
	}

	public void drawTVerteces(final Graphics g, final Geoset geo, final UVViewport vp, final int vertexSize,
			final List<TVertex> selection, final int layerId) {
		g.setColor(Color.black);
		for (int i = 0; i < geo.getVertices().size(); i++) {
			final TVertex ver = geo.getVertices().get(i).getTVertex(layerId);
			final Color temp = g.getColor();
			if (selection.contains(ver)) {
				g.setColor(MDLDisplay.selectColor);
			}
			g.fillRect((int) Math.round(vp.convertX(ver.getCoord(0))) - vertexSize,
					(int) Math.round(vp.convertY(ver.getCoord(1))) - vertexSize, 1 + vertexSize * 2,
					1 + vertexSize * 2);
			if (selection.contains(ver)) {
				g.setColor(temp);
			}
		}
	}

	public void drawGeosets(final Graphics g, final Viewport vp, final int vertexSize) {
		for (final Geoset geo : model.getGeosets()) {
			if (visibleGeosets.contains(geo) && !editableGeosets.contains(geo) && geo != highlight) {
				g.setColor(programPreferences.getVisibleUneditableColor());// new
																			// Color(150,
																			// 150,
																			// 255)
				drawTriangles(g, geo, vp);
			}
		}
		for (final Geoset geo : model.getGeosets()) {
			if (editableGeosets.contains(geo) && geo != highlight) {
				g.setColor(programPreferences.getTriangleColor());
				drawTriangles(g, geo, vp);
			}
		}
		for (final Geoset geo : model.getGeosets()) {
			if (editableGeosets.contains(geo) && geo != highlight) {
				drawVerteces(g, geo, vp, vertexSize, selection);
			}
		}
		for (final Geoset geo : model.getGeosets()) {
			if (geo == highlight) {
				g.setColor(programPreferences.getHighlighTriangleColor());
				drawTriangles(g, geo, vp);
				g.setColor(programPreferences.getHighlighVertexColor());
				drawVerteces(g, geo, vp, vertexSize);
			}
		}
	}

	public void drawGeosets(final Graphics g, final UVViewport vp, final int vertexSize) {
		for (final Geoset geo : model.getGeosets()) {
			if (visibleGeosets.contains(geo) && !editableGeosets.contains(geo) && geo != highlight) {
				g.setColor(new Color(150, 150, 255));
				drawTriangles(g, geo, vp, uvpanel.currentLayer());
			}
		}
		for (final Geoset geo : model.getGeosets()) {
			if (editableGeosets.contains(geo) && geo != highlight) {
				g.setColor(new Color(190, 190, 190));
				drawTriangles(g, geo, vp, uvpanel.currentLayer());
			}
		}
		for (final Geoset geo : model.getGeosets()) {
			if (editableGeosets.contains(geo) && geo != highlight) {
				g.setColor(new Color(0, 0, 255));
				drawTVerteces(g, geo, vp, vertexSize, uvselection, uvpanel.currentLayer());
			}
		}
		for (final Geoset geo : model.getGeosets()) {
			if (geo == highlight) {
				g.setColor(new Color(255, 255, 0));
				drawTriangles(g, geo, vp, uvpanel.currentLayer());
				g.setColor(new Color(0, 255, 0));
				drawTVerteces(g, geo, vp, vertexSize, uvpanel.currentLayer());
			}
		}
	}

	public void drawPivots(final Graphics g, final Viewport vp, final int vertexSize) {
		if (dispPivots) {
			// HashMap<IdObject,ArrayList<IdObject>> childMap = null;
			ArrayList<IdObject> geoParents = null;
			ArrayList<IdObject> geoSubParents = null;
			if (dispChildren) {
				geoParents = new ArrayList<>();
				geoSubParents = new ArrayList<>();
				for (final Geoset geo : editableGeosets) {
					for (final GeosetVertex ver : geo.getVertices()) {
						for (final Bone b : ver.getBones()) {
							if (!geoParents.contains(b)) {
								geoParents.add(b);
							}
						}
					}
				}
				// childMap = new HashMap<IdObject,ArrayList<IdObject>>();
				for (final IdObject obj : model.getIdObjects()) {
					if (!geoParents.contains(obj)) {
						boolean valid = false;
						for (int i = 0; !valid && i < geoParents.size(); i++) {
							valid = geoParents.get(i).childOf(obj);
						}
						if (valid) {
							geoSubParents.add(obj);
						}
						// if( obj.parent != null )
						// {
						// ArrayList<IdObject> children =
						// childMap.get(obj.parent);
						// if( children == null )
						// {
						// children = new ArrayList<IdObject>();
						// childMap.put(obj.parent, children);
						// }
						// children.add(obj);
						// }
					}
				}
				// System.out.println(geoSubParents);
			}
			g.setColor(Color.magenta.darker());
			g.setFont(new Font("Arial", Font.BOLD, 12));
			if (!dispChildren) {
				for (final IdObject o : model.getIdObjects()) {
					final Vertex ver = o.getPivotPoint();
					if (selection.contains(ver)) {
						g.setColor(Color.red.darker());
					}
					if (dispPivotNames) {
						g.drawString(o.getName(), (int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),
								(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
					}
					g.fillRect((int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))) - vertexSize,
							(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))) - vertexSize,
							1 + vertexSize * 2, 1 + vertexSize * 2);
					if (selection.contains(ver)) {
						g.setColor(Color.magenta.darker());
					}
				}
			} else {
				for (final IdObject o : model.getIdObjects()) {
					// boolean hasRef = false;//highlight != null &&
					// highlight.containsReference(o);
					// if( dispChildren )
					// {
					// for( int i = 0; !hasRef && i < editableGeosets.size();
					// i++ )
					// {
					// hasRef = editableGeosets.get(i).containsReference(o);
					// }
					// }
					if (geoParents.contains(o) || geoSubParents.contains(o))// !dispChildren
																			// ||
																			// hasRef
																			// )
					{
						final Vertex ver = o.getPivotPoint();
						if (selection.contains(ver)) {
							g.setColor(Color.red.darker());
						}
						if (dispPivotNames) {
							g.drawString(o.getName(), (int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),
									(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
						}
						g.fillRect((int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))) - vertexSize,
								(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))) - vertexSize,
								1 + vertexSize * 2, 1 + vertexSize * 2);
						if (selection.contains(ver)) {
							g.setColor(Color.magenta.darker());
						}
					}
				}
			}
		}
	}

	public void drawCameras(final Graphics g, final Viewport vp, final int vertexSize) {
		if (dispCameras) {
			g.setColor(Color.green.darker());
			g.setFont(new Font("Arial", Font.BOLD, 12));
			for (final Camera cam : model.getCameras()) {
				final Graphics2D g2 = ((Graphics2D) g.create());
				final Vertex ver = cam.getPosition();
				final Vertex targ = cam.getTargetPosition();
				final boolean verSel = selection.contains(ver);
				final boolean tarSel = selection.contains(targ);
				final Point start = new Point((int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),
						(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
				final Point end = new Point((int) Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ()))),
						(int) Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ()))));
				if (dispCameraNames) {
					boolean changedCol = false;

					if (verSel) {
						g2.setColor(Color.orange.darker());
						changedCol = true;
					}
					g2.drawString(cam.getName(), (int) Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),
							(int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
					if (tarSel) {
						g2.setColor(Color.orange.darker());
						changedCol = true;
					} else if (verSel) {
						g2.setColor(Color.green.darker());
						changedCol = false;
					}
					g2.drawString(cam.getName() + "_target",
							(int) Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ()))),
							(int) Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ()))));
					if (changedCol) {
						g2.setColor(Color.green.darker());
					}
				}

				g2.translate(end.x, end.y);
				g2.rotate(-(Math.PI / 2 + Math.atan2(end.x - start.x, end.y - start.y)));
				final int size = (int) (20 * vp.getZoomAmount());
				final double dist = start.distance(end);

				if (verSel) {
					g2.setColor(Color.orange.darker());
				}
				// Cam
				g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
				g2.drawRect((int) dist - size, -size, size * 2, size * 2);

				if (tarSel) {
					g2.setColor(Color.orange.darker());
				} else if (verSel) {
					g2.setColor(Color.green.darker());
				}
				// Target
				g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
				g2.drawLine(0, 0, size, size);// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())+5)),
												// (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())+5)));
				g2.drawLine(0, 0, size, -size);// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())-5)),
												// (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())-5)));

				if (!verSel && tarSel) {
					g2.setColor(Color.green.darker());
				}
				g2.drawLine(0, 0, (int) dist, 0);
			}
		}
	}

	public void selectVerteces(final Rectangle2D.Double area, final byte dim1, final byte dim2,
			final int selectionType) {
		if (!lockdown) {
			beenSaved = false;
			final ArrayList<Vertex> oldSelection = new ArrayList<>(selection);
			switch (selectionType) {
			case 0:
				selection.clear();
				for (final Geoset geo : editableGeosets) {
					for (final Vertex v : geo.getVertecesInArea(area, dim1, dim2)) {
						if (!selection.contains(v)) {
							selection.add(v);
						}
					}
				}
				if (dispPivots) {
					ArrayList<IdObject> geoParents = null;
					ArrayList<IdObject> geoSubParents = null;
					if (dispChildren) {
						geoParents = new ArrayList<>();
						geoSubParents = new ArrayList<>();
						for (final Geoset geo : editableGeosets) {
							for (final GeosetVertex ver : geo.getVertices()) {
								for (final Bone b : ver.getBones()) {
									if (!geoParents.contains(b)) {
										geoParents.add(b);
									}
								}
							}
						}
						// childMap = new
						// HashMap<IdObject,ArrayList<IdObject>>();
						for (final IdObject obj : model.getIdObjects()) {
							if (!geoParents.contains(obj)) {
								boolean valid = false;
								for (int i = 0; !valid && i < geoParents.size(); i++) {
									valid = geoParents.get(i).childOf(obj);
								}
								if (valid) {
									geoSubParents.add(obj);
								}
								// if( obj.parent != null )
								// {
								// ArrayList<IdObject> children =
								// childMap.get(obj.parent);
								// if( children == null )
								// {
								// children = new ArrayList<IdObject>();
								// childMap.put(obj.parent, children);
								// }
								// children.add(obj);
								// }
							}
						}
						// System.out.println(geoSubParents);
					}

					if (!dispChildren) {
						for (final Vertex ver : model.getPivots()) {
							if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
								selection.add(ver);
							}
						}
					} else {
						for (final IdObject o : model.getIdObjects()) {
							// boolean hasRef = false;//highlight != null &&
							// highlight.containsReference(o);
							// if( dispChildren )
							// {
							// for( int i = 0; !hasRef && i <
							// editableGeosets.size(); i++ )
							// {
							// hasRef =
							// editableGeosets.get(i).containsReference(o);
							// }
							// }
							if (geoParents.contains(o) || geoSubParents.contains(o))// !dispChildren
																					// ||
																					// hasRef
																					// )
							{
								final Vertex ver = o.getPivotPoint();
								if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
									selection.add(ver);
								}
							}
						}
					}
				}
				if (dispCameras) {
					for (final Camera cam : model.getCameras()) {
						Vertex ver = cam.getPosition();
						if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
							selection.add(ver);
						}
						ver = cam.getTargetPosition();
						if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
							selection.add(ver);
						}
					}
				}
				break;
			case 1:
				for (final Geoset geo : editableGeosets) {
					for (final Vertex v : geo.getVertecesInArea(area, dim1, dim2)) {
						if (!selection.contains(v)) {
							selection.add(v);
						}
					}
				}
				// if( dispPivots )
				// for( Vertex ver: model.getPivots() )
				// {
				// if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) &&
				// !selection.contains(ver) )
				// {
				// selection.add(ver);
				// }
				// }
				if (dispPivots) {
					ArrayList<IdObject> geoParents = null;
					ArrayList<IdObject> geoSubParents = null;
					if (dispChildren) {
						geoParents = new ArrayList<>();
						geoSubParents = new ArrayList<>();
						for (final Geoset geo : editableGeosets) {
							for (final GeosetVertex ver : geo.getVertices()) {
								for (final Bone b : ver.getBones()) {
									if (!geoParents.contains(b)) {
										geoParents.add(b);
									}
								}
							}
						}
						// childMap = new
						// HashMap<IdObject,ArrayList<IdObject>>();
						for (final IdObject obj : model.getIdObjects()) {
							if (!geoParents.contains(obj)) {
								boolean valid = false;
								for (int i = 0; !valid && i < geoParents.size(); i++) {
									valid = geoParents.get(i).childOf(obj);
								}
								if (valid) {
									geoSubParents.add(obj);
								}
								// if( obj.parent != null )
								// {
								// ArrayList<IdObject> children =
								// childMap.get(obj.parent);
								// if( children == null )
								// {
								// children = new ArrayList<IdObject>();
								// childMap.put(obj.parent, children);
								// }
								// children.add(obj);
								// }
							}
						}
						// System.out.println(geoSubParents);
					}

					if (!dispChildren) {
						for (final Vertex ver : model.getPivots()) {
							if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
								selection.add(ver);
							}
						}
					} else {
						for (final IdObject o : model.getIdObjects()) {
							// boolean hasRef = false;//highlight != null &&
							// highlight.containsReference(o);
							// if( dispChildren )
							// {
							// for( int i = 0; !hasRef && i <
							// editableGeosets.size(); i++ )
							// {
							// hasRef =
							// editableGeosets.get(i).containsReference(o);
							// }
							// }
							if (geoParents.contains(o) || geoSubParents.contains(o))// !dispChildren
																					// ||
																					// hasRef
																					// )
							{
								final Vertex ver = o.getPivotPoint();
								if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
									selection.add(ver);
								}
							}
						}
					}
				}
				if (dispCameras) {
					for (final Camera cam : model.getCameras()) {
						Vertex ver = cam.getPosition();
						if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
							selection.add(ver);
						}
						ver = cam.getTargetPosition();
						if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
							selection.add(ver);
						}
					}
				}
				break;
			case 2:
				for (final Geoset geo : editableGeosets) {
					selection.removeAll(geo.getVertecesInArea(area, dim1, dim2));
				}
				if (dispPivots) {
					for (final Vertex ver : model.getPivots()) {
						if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2))) {
							selection.remove(ver);
						}
					}
				}
				if (dispCameras) {
					for (final Camera cam : model.getCameras()) {
						Vertex ver = cam.getPosition();
						if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
							selection.remove(ver);
						}
						ver = cam.getTargetPosition();
						if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2)) && !selection.contains(ver)) {
							selection.remove(ver);
						}
					}
				}
				break;
			}
			redoStack.clear();
			actionStack.add(
					new SelectAction(oldSelection, selection, this, SelectionActionType.fromLegacyId(selectionType)));
		}
	}

	public void selectTVerteces(final Rectangle2D.Double area, final int selectionType) {
		if (!lockdown) {
			beenSaved = false;
			final ArrayList<TVertex> oldSelection = new ArrayList<>(uvselection);
			switch (selectionType) {
			case 0:
				uvselection.clear();
				for (final Geoset geo : editableGeosets) {
					for (final TVertex v : geo.getTVertecesInArea(area, uvpanel.currentLayer())) {
						if (!uvselection.contains(v)) {
							uvselection.add(v);
						}
					}
				}
				break;
			case 1:
				for (final Geoset geo : editableGeosets) {
					for (final TVertex v : geo.getTVertecesInArea(area, uvpanel.currentLayer())) {
						if (!uvselection.contains(v)) {
							uvselection.add(v);
						}
					}
				}
				break;
			case 2:
				for (final Geoset geo : editableGeosets) {
					uvselection.removeAll(geo.getTVertecesInArea(area, uvpanel.currentLayer()));
				}
				break;
			}
			redoStack.clear();
			actionStack.add(new UVSelectAction(oldSelection, uvselection, this,
					UVSelectionActionType.fromLegacyId(selectionType)));
		}
	}

	public void selectVerteces(final ArrayList<Vertex> newSelection, final int selectionType) {
		if (!lockdown) {
			beenSaved = false;
			switch (selectionType) {
			case 0:
				selection.clear();
				for (final Vertex v : newSelection) {
					if (!selection.contains(v)) {
						selection.add(v);
					}
				}
				break;
			case 1:
				for (final Vertex v : newSelection) {
					if (!selection.contains(v)) {
						selection.add(v);
					}
				}
				break;
			case 2:
				selection.removeAll(newSelection);
				break;
			}
		}
	}

	public void updateAction(final Point2D.Double mouseStart, final Point2D.Double mouseStop, final byte dim1,
			final byte dim2) {
		// Points need to be in geometry/model space
		if (!lockdown) {
			beenSaved = false;
			Vertex v = null;
			switch (actionType) {
			case 3:// Move
				double deltaX = mouseStop.x - mouseStart.x;
				double deltaY = mouseStop.y - mouseStart.y;
				for (final Vertex ver : selection) {
					if (getDimEditable(dim1)) {
						ver.translateCoord(dim1, deltaX);
					}
					if (getDimEditable(dim2)) {
						ver.translateCoord(dim2, deltaY);
					}
				}
				if (getDimEditable(dim1)) {
					((MoveAction) currentAction).getMoveVector().translateCoord(dim1, deltaX);
				}
				if (getDimEditable(dim2)) {
					((MoveAction) currentAction).getMoveVector().translateCoord(dim2, deltaY);
				}
				break;
			case 4:// Rotate
				final ArrayList<Normal> normals = ((RotateAction) currentAction).getNormals();
				v = Vertex.centerOfGroup(selection);
				double cx = v.getCoord(dim1);
				double cy = v.getCoord(dim2);
				double dx = mouseStart.x - cx;
				double dy = mouseStart.y - cy;
				double r = Math.sqrt(dx * dx + dy * dy);
				double ang = Math.acos(dx / r);
				if (dy < 0) {
					ang = -ang;
				}

				dx = mouseStop.x - cx;
				dy = mouseStop.y - cy;
				r = Math.sqrt(dx * dx + dy * dy);
				double ang2 = Math.acos(dx / r);
				if (dy < 0) {
					ang2 = -ang2;
				}
				final double deltaAng = ang2 - ang;
				if (selection.size() > 1) {
					for (final Vertex ver : selection) {
						final double x1 = ver.getCoord(dim1);
						final double y1 = ver.getCoord(dim2);
						dx = x1 - cx;
						dy = y1 - cy;
						r = Math.sqrt(dx * dx + dy * dy);
						double verAng = Math.acos(dx / r);
						if (dy < 0) {
							verAng = -verAng;
						}
						// if( getDimEditable(dim1) )
						double nextDim = Math.cos(verAng + deltaAng) * r + cx;
						if (!Double.isNaN(nextDim)) {
							ver.setCoord(dim1, Math.cos(verAng + deltaAng) * r + cx);
						}
						// if( getDimEditable(dim2) )
						nextDim = Math.sin(verAng + deltaAng) * r + cy;
						if (!Double.isNaN(nextDim)) {
							ver.setCoord(dim2, Math.sin(verAng + deltaAng) * r + cy);
						}
						// if( getDimEditable(dim1) )
						((MoveAction) currentAction).getMoveVectors().get(selection.indexOf(ver)).translateCoord(dim1,
								ver.getCoord(dim1) - x1);
						// if( getDimEditable(dim2) )
						((MoveAction) currentAction).getMoveVectors().get(selection.indexOf(ver)).translateCoord(dim2,
								ver.getCoord(dim2) - y1);
					}

				}
				cx = 0;
				cy = 0;
				for (final Vertex ver : normals) {
					final double x1 = ver.getCoord(dim1);
					final double y1 = ver.getCoord(dim2);
					dx = x1 - cx;
					dy = y1 - cy;
					r = Math.sqrt(dx * dx + dy * dy);
					double verAng = Math.acos(dx / r);
					if (dy < 0) {
						verAng = -verAng;
					}
					// if( getDimEditable(dim1) )
					double nextDim = Math.cos(verAng + deltaAng) * r + cx;
					if (!Double.isNaN(nextDim)) {
						ver.setCoord(dim1, Math.cos(verAng + deltaAng) * r + cx);
					}
					// if( getDimEditable(dim2) )
					nextDim = Math.sin(verAng + deltaAng) * r + cy;
					if (!Double.isNaN(nextDim)) {
						ver.setCoord(dim2, Math.sin(verAng + deltaAng) * r + cy);
					}
					// if( getDimEditable(dim1) )
					((RotateAction) currentAction).getNormalMoveVectors().get(normals.indexOf(ver)).translateCoord(dim1,
							ver.getCoord(dim1) - x1);
					// if( getDimEditable(dim2) )
					((RotateAction) currentAction).getNormalMoveVectors().get(normals.indexOf(ver)).translateCoord(dim2,
							ver.getCoord(dim2) - y1);
				}
				break;
			case 5:
				v = Vertex.centerOfGroup(selection);
				double cxs = v.getCoord(dim1);
				double cys = v.getCoord(dim2);
				double czs = 0;
				double dxs = mouseStart.x - cxs;
				double dys = mouseStart.y - cys;
				double dzs = 0;
				final double startDist = Math.sqrt(dxs * dxs + dys * dys);
				dxs = mouseStop.x - cxs;
				dys = mouseStop.y - cys;
				final double endDist = Math.sqrt(dxs * dxs + dys * dys);
				final double distRatio = endDist / startDist;
				cxs = v.getCoord((byte) 0);
				cys = v.getCoord((byte) 1);
				czs = v.getCoord((byte) 2);
				for (final Vertex ver : selection) {
					dxs = ver.getCoord((byte) 0) - cxs;
					dys = ver.getCoord((byte) 1) - cys;
					dzs = ver.getCoord((byte) 2) - czs;
					// startDist is now the distance to vertex from center,
					// endDist is now the change in distance of mouse
					if (getDimEditable(0)) {
						ver.translateCoord((byte) 0, dxs * (distRatio - 1));
					}
					if (getDimEditable(1)) {
						ver.translateCoord((byte) 1, dys * (distRatio - 1));
					}
					if (getDimEditable(2)) {
						ver.translateCoord((byte) 2, dzs * (distRatio - 1));
					}
					if (getDimEditable(0)) {
						((MoveAction) currentAction).getMoveVectors().get(selection.indexOf(ver))
								.translateCoord((byte) 0, dxs * (distRatio - 1));
					}
					if (getDimEditable(1)) {
						((MoveAction) currentAction).getMoveVectors().get(selection.indexOf(ver))
								.translateCoord((byte) 1, dys * (distRatio - 1));
					}
					if (getDimEditable(2)) {
						((MoveAction) currentAction).getMoveVectors().get(selection.indexOf(ver))
								.translateCoord((byte) 2, dzs * (distRatio - 1));
					}
				}
				break;
			case 6:
				deltaX = mouseStop.x - mouseStart.x;
				deltaY = mouseStop.y - mouseStart.y;
				for (final Vertex ver : selection) {
					if (getDimEditable(dim1)) {
						ver.translateCoord(dim1, deltaX);
					}
					if (getDimEditable(dim2)) {
						ver.translateCoord(dim2, deltaY);
					}
				}
				if (getDimEditable(dim1)) {
					((ExtrudeAction) currentAction).getBaseMovement().getMoveVector().translateCoord(dim1, deltaX);
				}
				if (getDimEditable(dim2)) {
					((ExtrudeAction) currentAction).getBaseMovement().getMoveVector().translateCoord(dim2, deltaY);
					// extrudeSelection(mouseStart,mouseStop,dim1,dim2);
				}

				break;
			case 7:
				deltaX = mouseStop.x - mouseStart.x;
				deltaY = mouseStop.y - mouseStart.y;
				for (final Vertex ver : selection) {
					if (getDimEditable(dim1)) {
						ver.translateCoord(dim1, deltaX);
					}
					if (getDimEditable(dim2)) {
						ver.translateCoord(dim2, deltaY);
					}
				}
				if (getDimEditable(dim1)) {
					((ExtrudeAction) currentAction).getBaseMovement().getMoveVector().translateCoord(dim1, deltaX);
				}
				if (getDimEditable(dim2)) {
					((ExtrudeAction) currentAction).getBaseMovement().getMoveVector().translateCoord(dim2, deltaY);
				}
				// double deltaXe = mouseStop.x - mouseStart.x;
				// double deltaYe = mouseStop.y - mouseStart.y;
				//
				// ArrayList<Triangle> edges = new ArrayList<Triangle>();
				// ArrayList<Triangle> brokenFaces = new ArrayList<Triangle>();
				//
				// ArrayList<GeosetVertex> copies = new
				// ArrayList<GeosetVertex>();
				// ArrayList<Triangle> selTris = new ArrayList<Triangle>();
				// for( int i = 0; i < selection.size(); i++ )
				// {
				// Vertex vert = selection.get(i);
				// if( vert.getClass() == GeosetVertex.class )
				// {
				// GeosetVertex gv = (GeosetVertex)vert;
				// // copies.add(new GeosetVertex(gv));
				//
				// // selTris.addAll(gv.getTriangles());
				// for( int ti = 0; ti < gv.getTriangles().size(); ti++ )
				// {
				// Triangle temp = gv.getTriangles().get(ti);
				// if( !selTris.contains(temp) )
				// {
				// selTris.add(temp);
				// }
				// }
				// }
				// else
				// {
				// // copies.add(null);
				// System.out.println("GeosetVertex "+i+" was not found.");
				// }
				// }
				// System.out.println(selection.size()+" verteces cloned into
				// "+copies.size()+ " more.");
				// for( Triangle tri: selTris )
				// {
				// if( !selection.contains(tri.get(0))
				// ||!selection.contains(tri.get(1))
				// ||!selection.contains(tri.get(2)) )
				// {
				// int selVerts = 0;
				// GeosetVertex gv = null;
				// GeosetVertex gvTemp = null;
				// GeosetVertex gvCopy =
				// null;//copies.get(selection.indexOf(gv));
				// GeosetVertex gvTempCopy =
				// null;//copies.get(selection.indexOf(gvTemp));
				// for( int i = 0; i < 3; i++ )
				// {
				// GeosetVertex a = tri.get(i);
				// if( selection.contains(a) )
				// {
				// selVerts++;
				// // GeosetVertex b = copies.get(selection.indexOf(a));
				// GeosetVertex b = new GeosetVertex(a);
				// copies.add(b);
				// tri.set(i,b);
				// a.triangles.remove(tri);
				// b.triangles.add(tri);
				// if( gv == null )
				// {
				// gv = a;
				// gvCopy = b;
				//
				// }
				// else if( gvTemp == null )
				// {
				// gvTemp = a;
				// gvTempCopy = b;
				// }
				// }
				// }
				// if( selVerts == 2 )
				// {
				// if( gvCopy == null )
				// {
				// System.out.println("Vertex (gvCopy) copy found as null!");
				// }
				// if( gvTempCopy == null )
				// {
				// System.out.println("Vertex (gvTempCopy) copy found as
				// null!");
				// }
				// Triangle newFace = new
				// Triangle(null,null,null,gv.getGeoset());
				//
				// int indexA = tri.indexOf(gvTempCopy);
				// int indexB = tri.indexOf(gvCopy);
				// int indexC = -1;
				//
				// for( int i = 0; i < 3 && indexC == -1; i++ )
				// {
				// if( i != indexA && i != indexB )
				// {
				// indexC = i;
				// }
				// }
				//
				// System.out.println(" Indeces:
				// "+indexA+","+indexB+","+indexC);
				//
				// newFace.set(indexA,gv);
				// newFace.set(indexB,gvTemp);
				// newFace.set(indexC,gvCopy);
				// //Make sure it's included later
				// gvTemp.triangles.add(newFace);
				// gv.getTriangles().add(newFace);
				// gvCopy.triangles.add(newFace);
				// gv.getGeoset().addTriangle(newFace);
				//
				// System.out.println("New Face: ");
				// System.out.println(newFace.get(0));
				// System.out.println(newFace.get(1));
				// System.out.println(newFace.get(2));
				//
				// newFace = new Triangle(null,null,null,gv.getGeoset());
				//
				// newFace.set(indexA,gvCopy);
				// newFace.set(indexB,gvTemp);
				// newFace.set(indexC,gvTempCopy);
				// //Make sure it's included later
				// gvCopy.triangles.add(newFace);
				// gvTemp.triangles.add(newFace);
				// gvTempCopy.triangles.add(newFace);
				// gv.getGeoset().addTriangle(newFace);
				//
				// System.out.println("New Alternate Face: ");
				// System.out.println(newFace.get(0));
				// System.out.println(newFace.get(1));
				// System.out.println(newFace.get(2));
				// }
				// }
				// }
				// for( Vertex vert: selection )
				// {
				//
				// vert.translateCoord(dim1,deltaXe);
				// vert.translateCoord(dim2,deltaYe);
				// }
				//
				// for( GeosetVertex cgv: copies )
				// {
				// if( cgv != null )
				// {
				// cgv.getGeoset().addVertex(cgv);
				// }
				// }
				break;
			}
		}
	}

	public void updateUVAction(final Point2D.Double mouseStart, final Point2D.Double mouseStop) {
		// Points need to be in geometry/model space
		if (!lockdown) {
			final byte dim1 = 0;
			final byte dim2 = 1;
			beenSaved = false;
			TVertex v = null;
			switch (actionTypeUV) {
			case 3:// Move
				final double deltaX = mouseStop.x - mouseStart.x;
				final double deltaY = mouseStop.y - mouseStart.y;
				for (final TVertex ver : uvselection) {
					if (!uvpanel.getDimLock(dim1)) {
						ver.translateCoord(dim1, deltaX);
					}
					if (!uvpanel.getDimLock(dim2)) {
						ver.translateCoord(dim2, deltaY);
					}
				}
				if (!uvpanel.getDimLock(dim1)) {
					((UVMoveAction) currentUVAction).getMoveVector().translateCoord(dim1, deltaX);
				}
				if (!uvpanel.getDimLock(dim2)) {
					((UVMoveAction) currentUVAction).getMoveVector().translateCoord(dim2, deltaY);
				}
				break;
			case 4:// Rotate
				v = TVertex.centerOfGroup(uvselection);
				final double cx = v.getCoord(dim1);
				final double cy = v.getCoord(dim2);
				double dx = mouseStart.x - cx;
				double dy = mouseStart.y - cy;
				double r = Math.sqrt(dx * dx + dy * dy);
				double ang = Math.acos(dx / r);
				if (dy < 0) {
					ang = -ang;
				}

				dx = mouseStop.x - cx;
				dy = mouseStop.y - cy;
				r = Math.sqrt(dx * dx + dy * dy);
				double ang2 = Math.acos(dx / r);
				if (dy < 0) {
					ang2 = -ang2;
				}
				final double deltaAng = ang2 - ang;
				if (uvselection.size() > 1) {
					for (final TVertex ver : uvselection) {
						final double x1 = ver.getCoord(dim1);
						final double y1 = ver.getCoord(dim2);
						dx = x1 - cx;
						dy = y1 - cy;
						r = Math.sqrt(dx * dx + dy * dy);
						double verAng = Math.acos(dx / r);
						if (dy < 0) {
							verAng = -verAng;
						}
						// if( getDimEditable(dim1) )
						double nextDim = Math.cos(verAng + deltaAng) * r + cx;
						if (!Double.isNaN(nextDim)) {
							ver.setCoord(dim1, Math.cos(verAng + deltaAng) * r + cx);
						}
						// if( getDimEditable(dim2) )
						nextDim = Math.sin(verAng + deltaAng) * r + cy;
						if (!Double.isNaN(nextDim)) {
							ver.setCoord(dim2, Math.sin(verAng + deltaAng) * r + cy);
						}
						// if( getDimEditable(dim1) )
						((UVMoveAction) currentUVAction).getMoveVectors().get(uvselection.indexOf(ver))
								.translateCoord(dim1, ver.getCoord(dim1) - x1);
						// if( getDimEditable(dim2) )
						((UVMoveAction) currentUVAction).getMoveVectors().get(uvselection.indexOf(ver))
								.translateCoord(dim2, ver.getCoord(dim2) - y1);
					}

				}
				break;
			case 5:
				v = TVertex.centerOfGroup(uvselection);
				double cxs = v.getCoord(dim1);
				double cys = v.getCoord(dim2);
				double dxs = mouseStart.x - cxs;
				double dys = mouseStart.y - cys;
				final double startDist = Math.sqrt(dxs * dxs + dys * dys);
				dxs = mouseStop.x - cxs;
				dys = mouseStop.y - cys;
				final double endDist = Math.sqrt(dxs * dxs + dys * dys);
				final double distRatio = endDist / startDist;
				cxs = v.getCoord((byte) 0);
				cys = v.getCoord((byte) 1);
				for (final TVertex ver : uvselection) {
					dxs = ver.getCoord((byte) 0) - cxs;
					dys = ver.getCoord((byte) 1) - cys;
					// startDist is now the distance to vertex from center,
					// endDist is now the change in distance of mouse
					if (!uvpanel.getDimLock(0)) {
						ver.translateCoord((byte) 0, dxs * (distRatio - 1));
					}
					if (!uvpanel.getDimLock(1)) {
						ver.translateCoord((byte) 1, dys * (distRatio - 1));
					}
					if (!uvpanel.getDimLock(0)) {
						((UVMoveAction) currentUVAction).getMoveVectors().get(uvselection.indexOf(ver))
								.translateCoord((byte) 0, dxs * (distRatio - 1));
					}
					if (!uvpanel.getDimLock(1)) {
						((UVMoveAction) currentUVAction).getMoveVectors().get(uvselection.indexOf(ver))
								.translateCoord((byte) 1, dys * (distRatio - 1));
					}
				}
				break;
			case 6:// NO EXTRUSIONS IN UV MODE

				break;
			case 7:// NO EXTRUSIONS IN UV MODE

				break;
			}
		}
	}

	public void extrudeSelection(final Point2D.Double mouseStart, final Point2D.Double mouseStop, final byte dim1,
			final byte dim2) {
		if (!lockdown) {
			beenSaved = false;
			final double deltaXe = mouseStop.x - mouseStart.x;
			final double deltaYe = mouseStop.y - mouseStart.y;

			final ArrayList<GeosetVertex> copies = new ArrayList<>();
			final ArrayList<Triangle> selTris = new ArrayList<>();
			for (int i = 0; i < selection.size(); i++) {
				final Vertex vert = selection.get(i);
				if (vert.getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) vert;
					copies.add(new GeosetVertex(gv));

					for (int ti = 0; ti < gv.getTriangles().size(); ti++) {
						final Triangle temp = gv.getTriangles().get(ti);
						if (!selTris.contains(temp)) {
							selTris.add(temp);
						}
					}
				} else {
					copies.add(null);
					System.out.println("GeosetVertex " + i + " was not found.");
				}
				vert.translateCoord(dim1, deltaXe);
				vert.translateCoord(dim2, deltaYe);
			}
			for (final Triangle tri : selTris) {
				if (!selection.contains(tri.get(0)) || !selection.contains(tri.get(1))
						|| !selection.contains(tri.get(2))) {
					for (int i = 0; i < 3; i++) {
						final GeosetVertex a = tri.get(i);
						if (selection.contains(a)) {
							final GeosetVertex b = copies.get(selection.indexOf(a));
							tri.set(i, b);
							a.getTriangles().remove(tri);
							b.getTriangles().add(tri);
						}
					}
				}
			}
			System.out.println(selection.size() + " verteces cloned into " + copies.size() + " more.");
			final ArrayList<Triangle> newTriangles = new ArrayList<>();
			for (int k = 0; k < selection.size(); k++) {
				final Vertex vert = selection.get(k);
				if (vert.getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) vert;
					final ArrayList<Triangle> gvTriangles = new ArrayList<>(gv.getTriangles());
					// for( Triangle tri: gv.getGeoset().getTriangle() )
					// {
					// if( tri.contains(gv) )
					// {
					// // boolean good = true;
					// // for( Vertex vTemp: tri.getAll() )
					// // {
					// // if( !selection.contains( vTemp ) )
					// // {
					// // good = false;
					// // break;
					// // }
					// // }
					// // if( good )
					// gvTriangles.add(tri);
					// }
					// }
					for (final Triangle tri : gvTriangles) {
						for (int gvI = 0; gvI < tri.getAll().length; gvI++) {
							final GeosetVertex gvTemp = tri.get(gvI);
							if (!gvTemp.equalLocs(gv)) {
								int ctCount = 0;
								Triangle temp = null;
								boolean okay = false;
								for (final Triangle triTest : gvTriangles) {
									if (triTest.contains(gvTemp)) {
										ctCount++;
										temp = triTest;
										if (temp.containsRef(gvTemp) && temp.containsRef(gv)) {
											okay = true;
										}
									}
								}
								if (okay && ctCount == 1 && selection.contains(gvTemp)) {
									final GeosetVertex gvCopy = copies.get(selection.indexOf(gv));
									final GeosetVertex gvTempCopy = copies.get(selection.indexOf(gvTemp));
									if (gvCopy == null) {
										System.out.println("Vertex (gvCopy) copy found as null!");
									}
									if (gvTempCopy == null) {
										System.out.println("Vertex (gvTempCopy) copy found as null!");
									}
									Triangle newFace = new Triangle(null, null, null, gv.getGeoset());

									final int indexA = temp.indexOf(gv);
									final int indexB = temp.indexOf(gvTemp);
									int indexC = -1;

									for (int i = 0; i < 3 && indexC == -1; i++) {
										if (i != indexA && i != indexB) {
											indexC = i;
										}
									}

									System.out.println(" Indeces: " + indexA + "," + indexB + "," + indexC);

									newFace.set(indexA, gv);
									newFace.set(indexB, gvTemp);
									newFace.set(indexC, gvCopy);
									// Make sure it's included later
									// gvTemp.triangles.add(newFace);
									// gv.getTriangles().add(newFace);
									// gvCopy.triangles.add(newFace);
									// gv.getGeoset().addTriangle(newFace);
									boolean bad = false;
									for (final Triangle t : newTriangles) {
										// if( t.equals(newFace) )
										// {
										// bad = true;
										// break;
										// }
										if (t.contains(gv) && t.contains(gvTemp)) {
											bad = true;
											break;
										}
									}
									if (!bad) {
										newTriangles.add(newFace);

										System.out.println("New Face: ");
										System.out.println(newFace.get(0));
										System.out.println(newFace.get(1));
										System.out.println(newFace.get(2));

										newFace = new Triangle(null, null, null, gv.getGeoset());

										newFace.set(indexA, gvCopy);
										newFace.set(indexB, gvTemp);
										newFace.set(indexC, gvTempCopy);
										// Make sure it's included later
										newTriangles.add(newFace);

										System.out.println("New Alternate Face: ");
										System.out.println(newFace.get(0));
										System.out.println(newFace.get(1));
										System.out.println(newFace.get(2));

									}
								}
							}
						}
					}
				}
			}

			for (final Triangle t : newTriangles) {
				for (final GeosetVertex gv : t.getAll()) {
					if (!gv.getTriangles().contains(t)) {
						gv.getTriangles().add(t);
					}
					if (!gv.getGeoset().contains(t)) {
						gv.getGeoset().addTriangle(t);
					}
				}
			}
			for (final GeosetVertex cgv : copies) {
				if (cgv != null) {
					boolean inGeoset = false;
					for (final Triangle t : cgv.getGeoset().getTriangle()) {
						if (t.containsRef(cgv)) {
							inGeoset = true;
							break;
						}
					}
					if (inGeoset) {
						cgv.getGeoset().addVertex(cgv);
					}
				}
			}
		}
	}

	public void clone(final List<Vertex> source, final boolean selectCopies) {
		final List<Vertex> oldSelection = new ArrayList<>(selection);

		final ArrayList<GeosetVertex> vertCopies = new ArrayList<>();
		final ArrayList<Triangle> selTris = new ArrayList<>();
		final ArrayList<IdObject> selBones = new ArrayList<>();
		final ArrayList<IdObject> newBones = new ArrayList<>();
		for (int i = 0; i < source.size(); i++) {
			final Vertex vert = source.get(i);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				vertCopies.add(new GeosetVertex(gv));

				// for( int ti = 0; ti < gv.getTriangles().size(); ti++ )
				// {
				// Triangle temptr = gv.getTriangles().get(ti);
				// if( !selTris.contains(temptr) )
				// {
				// selTris.add(temptr);
				// }
				// }
			} else {
				vertCopies.add(null);
			}
		}
		for (final IdObject b : model.getIdObjects()) {
			if (source.contains(b.getPivotPoint()) && !selBones.contains(b)) {
				selBones.add(b);
				newBones.add(b.copy());
			}
		}
		final ArrayList<Triangle> newTriangles = new ArrayList<>();
		for (int k = 0; k < source.size(); k++) {
			final Vertex vert = source.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final ArrayList<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
				// WHY IS GV.TRIANGLES WRONG????
				for (final Triangle tri : gv.getGeoset().getTriangle()) {
					if (tri.contains(gv)) {
						boolean good = true;
						for (final Vertex vTemp : tri.getAll()) {
							if (!source.contains(vTemp)) {
								good = false;
								break;
							}
						}
						if (good) {
							gvTriangles.add(tri);
							if (!selTris.contains(tri)) {
								selTris.add(tri);
							}
						}
					}
				}
			}
		}
		for (final Triangle tri : selTris) {
			final GeosetVertex a = vertCopies.get(source.indexOf(tri.get(0)));
			final GeosetVertex b = vertCopies.get(source.indexOf(tri.get(1)));
			final GeosetVertex c = vertCopies.get(source.indexOf(tri.get(2)));
			newTriangles.add(new Triangle(a, b, c, a.getGeoset()));
		}
		for (final GeosetVertex gv : vertCopies) {
			if (gv != null) {
				model.add(gv);
			}
		}
		for (final Triangle tri : newTriangles) {
			if (tri != null) {
				model.add(tri);
			}
			tri.forceVertsUpdate();
		}
		for (final IdObject b : newBones) {
			if (b != null) {
				model.add(b);
			}
		}
		if (selectCopies) {
			selection.clear();
			for (final Vertex ver : vertCopies) {
				if (ver != null) {
					selection.add(ver);
					if (ver.getClass() == GeosetVertex.class) {
						final GeosetVertex gv = (GeosetVertex) ver;
						for (int i = 0; i < gv.getBones().size(); i++) {
							final Bone b = gv.getBones().get(i);
							if (selBones.contains(b)) {
								gv.getBones().set(i, (Bone) newBones.get(selBones.indexOf(b)));
							}
						}
					}
				}
			}
			// for( IdObject b: newBones )
			// {
			// if( b != null )
			// {
			// selection.add(b.getPivotPoint());
			// }
			// }
			for (final IdObject b : newBones) {
				selection.add(b.getPivotPoint());
				if (selBones.contains(b.getParent())) {
					b.setParent(newBones.get(selBones.indexOf(b.getParent())));
				}
			}
		}
	}

	public void startAction(final Point2D.Double mouseStart, final byte dim1, final byte dim2, final int actionType) {
		// Points need to be in geometry/model space
		if (!lockdown) {
			beenSaved = false;
			if (this.actionType == -1) {
				this.actionType = actionType;
				if (programPreferences.isCloneOn()) {
					clone(selection, true);
					programPreferences.setCloneOn(false);
				}
				switch (actionType) {
				case 3:
					MoveAction temp = new MoveAction();
					temp.storeSelection(selection);
					temp.createEmptyMoveVector();
					temp.setActType(VertexActionType.fromLegacyId(actionType));
					currentAction = temp;
					break;
				case 4:
					temp = new RotateAction();
					temp.storeSelection(selection);
					temp.createEmptyMoveVectors();
					temp.setActType(VertexActionType.fromLegacyId(actionType));
					currentAction = temp;
					break;
				case 5:
					temp = new MoveAction();
					temp.storeSelection(selection);
					temp.createEmptyMoveVectors();
					temp.setActType(VertexActionType.fromLegacyId(actionType));
					currentAction = temp;
					break;
				case 6:
					ArrayList<GeosetVertex> copies = new ArrayList<>();
					ArrayList<Triangle> selTris = new ArrayList<>();
					for (int i = 0; i < selection.size(); i++) {
						final Vertex vert = selection.get(i);
						if (vert.getClass() == GeosetVertex.class) {
							final GeosetVertex gv = (GeosetVertex) vert;
							copies.add(new GeosetVertex(gv));

							for (int ti = 0; ti < gv.getTriangles().size(); ti++) {
								final Triangle temptr = gv.getTriangles().get(ti);
								if (!selTris.contains(temptr)) {
									selTris.add(temptr);
								}
							}
						} else {
							copies.add(null);
							System.out.println("GeosetVertex " + i + " was not found.");
						}
					}
					for (final Triangle tri : selTris) {
						if (!selection.contains(tri.get(0)) || !selection.contains(tri.get(1))
								|| !selection.contains(tri.get(2))) {
							for (int i = 0; i < 3; i++) {
								final GeosetVertex a = tri.get(i);
								if (selection.contains(a)) {
									final GeosetVertex b = copies.get(selection.indexOf(a));
									tri.set(i, b);
									a.getTriangles().remove(tri);
									if (a.getTriangles().contains(tri)) {
										System.out.println("It's a bloody war!");
									}
									b.getTriangles().add(tri);
								}
							}
						}
					}
					System.out.println(selection.size() + " verteces cloned into " + copies.size() + " more.");
					ArrayList<Triangle> newTriangles = new ArrayList<>();
					for (int k = 0; k < selection.size(); k++) {
						final Vertex vert = selection.get(k);
						if (vert.getClass() == GeosetVertex.class) {
							final GeosetVertex gv = (GeosetVertex) vert;
							final ArrayList<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
							// WHY IS GV.TRIANGLES WRONG????
							for (final Triangle tri : gv.getGeoset().getTriangle()) {
								if (tri.contains(gv)) {
									boolean good = true;
									for (final Vertex vTemp : tri.getAll()) {
										if (!selection.contains(vTemp)) {
											good = false;
											break;
										}
									}
									if (good) {
										gvTriangles.add(tri);
									}
								}
							}
							for (final Triangle tri : gvTriangles) {
								for (final GeosetVertex copyVer : copies) {
									if (copyVer != null) {
										if (tri.containsRef(copyVer)) {
											System.out.println("holy brejeezers!");
										}
									}
								}
								for (int gvI = 0; gvI < tri.getAll().length; gvI++) {
									final GeosetVertex gvTemp = tri.get(gvI);
									if (!gvTemp.equalLocs(gv) && gvTemp.getGeoset() == gv.getGeoset()) {
										int ctCount = 0;
										Triangle temptr = null;
										boolean okay = false;
										for (final Triangle triTest : gvTriangles) {
											if (triTest.contains(gvTemp)) {
												ctCount++;
												temptr = triTest;
												if (temptr.containsRef(gvTemp) && temptr.containsRef(gv)) {
													okay = true;
												}
											}
										}
										if (okay && ctCount == 1 && selection.contains(gvTemp)) {
											final GeosetVertex gvCopy = copies.get(selection.indexOf(gv));
											final GeosetVertex gvTempCopy = copies.get(selection.indexOf(gvTemp));
											if (gvCopy == null) {
												System.out.println("Vertex (gvCopy) copy found as null!");
											}
											if (gvTempCopy == null) {
												System.out.println("Vertex (gvTempCopy) copy found as null!");
											}
											Triangle newFace = new Triangle(null, null, null, gv.getGeoset());

											final int indexA = temptr.indexOf(gv);
											final int indexB = temptr.indexOf(gvTemp);
											int indexC = -1;

											for (int i = 0; i < 3 && indexC == -1; i++) {
												if (i != indexA && i != indexB) {
													indexC = i;
												}
											}

											System.out.println(" Indeces: " + indexA + "," + indexB + "," + indexC);

											newFace.set(indexA, gv);
											newFace.set(indexB, gvTemp);
											newFace.set(indexC, gvCopy);
											// Make sure it's included later
											// gvTemp.triangles.add(newFace);
											// gv.getTriangles().add(newFace);
											// gvCopy.triangles.add(newFace);
											// gv.getGeoset().addTriangle(newFace);
											boolean bad = false;
											for (final Triangle t : newTriangles) {
												// if( t.equals(newFace) )
												// {
												// bad = true;
												// break;
												// }
												if (t.contains(gv) && t.contains(gvTemp)) {
													bad = true;
													break;
												}
											}
											if (!bad) {
												newTriangles.add(newFace);

												System.out.println("New Face: ");
												System.out.println(newFace.get(0));
												System.out.println(newFace.get(1));
												System.out.println(newFace.get(2));

												newFace = new Triangle(null, null, null, gv.getGeoset());

												newFace.set(indexA, gvCopy);
												newFace.set(indexB, gvTemp);
												newFace.set(indexC, gvTempCopy);
												// Make sure it's included later
												newTriangles.add(newFace);

												System.out.println("New Alternate Face: ");
												System.out.println(newFace.get(0));
												System.out.println(newFace.get(1));
												System.out.println(newFace.get(2));

											}
										}
									}
								}
							}
						}
					}

					for (final Triangle t : newTriangles) {
						for (final GeosetVertex gv : t.getAll()) {
							if (!gv.getTriangles().contains(t)) {
								gv.getTriangles().add(t);
							}
							if (!gv.getGeoset().contains(t)) {
								gv.getGeoset().addTriangle(t);
							}
						}
					}
					for (final GeosetVertex cgv : copies) {
						if (cgv != null) {
							boolean inGeoset = false;
							for (final Triangle t : cgv.getGeoset().getTriangle()) {
								if (t.containsRef(cgv)) {
									inGeoset = true;
									break;
								}
							}
							if (inGeoset) {
								cgv.getGeoset().addVertex(cgv);
							}
						}
					}
					int probs = 0;
					for (int k = 0; k < selection.size(); k++) {
						final Vertex vert = selection.get(k);
						if (vert.getClass() == GeosetVertex.class) {
							final GeosetVertex gv = (GeosetVertex) vert;
							for (final Triangle t : gv.getTriangles()) {
								System.out.println("SHOULD be one: " + Collections.frequency(gv.getTriangles(), t));
								if (!t.containsRef(gv)) {
									probs++;
								}
							}
						}
					}
					System.out.println("Extrude finished with " + probs + " inexplicable errors.");
					ExtrudeAction tempe = new ExtrudeAction();
					tempe.storeSelection(selection);
					tempe.setType(true);
					tempe.storeBaseMovement(new Vertex(0, 0, 0));
					tempe.setAddedTriangles(newTriangles);
					tempe.setAddedVerts(copies);
					currentAction = tempe;
					break;
				case 7:

					final ArrayList<Triangle> edges = new ArrayList<>();
					final ArrayList<Triangle> brokenFaces = new ArrayList<>();

					copies = new ArrayList<>();
					selTris = new ArrayList<>();
					for (int i = 0; i < selection.size(); i++) {
						final Vertex vert = selection.get(i);
						if (vert.getClass() == GeosetVertex.class) {
							final GeosetVertex gv = (GeosetVertex) vert;
							// copies.add(new GeosetVertex(gv));

							// selTris.addAll(gv.getTriangles());
							for (int ti = 0; ti < gv.getTriangles().size(); ti++) {
								final Triangle temptr = gv.getTriangles().get(ti);
								if (!selTris.contains(temptr)) {
									selTris.add(temptr);
								}
							}
						} else {
							// copies.add(null);
							System.out.println("GeosetVertex " + i + " was not found.");
						}
					}
					System.out.println(selection.size() + " verteces cloned into " + copies.size() + " more.");
					newTriangles = new ArrayList<>();
					final ArrayList<GeosetVertex> copiedGroup = new ArrayList<>();
					for (final Triangle tri : selTris) {
						if (!selection.contains(tri.get(0)) || !selection.contains(tri.get(1))
								|| !selection.contains(tri.get(2))) {
							int selVerts = 0;
							GeosetVertex gv = null;
							GeosetVertex gvTemp = null;
							GeosetVertex gvCopy = null;// copies.get(selection.indexOf(gv));
							GeosetVertex gvTempCopy = null;// copies.get(selection.indexOf(gvTemp));
							for (int i = 0; i < 3; i++) {
								final GeosetVertex a = tri.get(i);
								if (selection.contains(a)) {
									selVerts++;
									final GeosetVertex b = new GeosetVertex(a);
									copies.add(b);
									copiedGroup.add(a);
									tri.set(i, b);
									a.getTriangles().remove(tri);
									b.getTriangles().add(tri);
									if (gv == null) {
										gv = a;
										gvCopy = b;
									} else if (gvTemp == null) {
										gvTemp = a;
										gvTempCopy = b;
									}
								}
							}
							if (selVerts == 2) {
								if (gvCopy == null) {
									System.out.println("Vertex (gvCopy) copy found as null!");
								}
								if (gvTempCopy == null) {
									System.out.println("Vertex (gvTempCopy) copy found as null!");
								}
								Triangle newFace = new Triangle(null, null, null, gv.getGeoset());

								final int indexA = tri.indexOf(gvCopy);
								final int indexB = tri.indexOf(gvTempCopy);
								int indexC = -1;

								for (int i = 0; i < 3 && indexC == -1; i++) {
									if (i != indexA && i != indexB) {
										indexC = i;
									}
								}

								System.out.println(" Indeces: " + indexA + "," + indexB + "," + indexC);

								newFace.set(indexA, gv);
								newFace.set(indexB, gvTemp);
								newFace.set(indexC, gvCopy);
								// Make sure it's included later
								gvTemp.getTriangles().add(newFace);
								gv.getTriangles().add(newFace);
								gvCopy.getTriangles().add(newFace);
								gv.getGeoset().addTriangle(newFace);
								newTriangles.add(newFace);

								System.out.println("New Face: ");
								System.out.println(newFace.get(0));
								System.out.println(newFace.get(1));
								System.out.println(newFace.get(2));

								newFace = new Triangle(null, null, null, gv.getGeoset());

								newFace.set(indexA, gvCopy);
								newFace.set(indexB, gvTemp);
								newFace.set(indexC, gvTempCopy);
								// Make sure it's included later
								gvCopy.getTriangles().add(newFace);
								gvTemp.getTriangles().add(newFace);
								gvTempCopy.getTriangles().add(newFace);
								gv.getGeoset().addTriangle(newFace);
								newTriangles.add(newFace);

								System.out.println("New Alternate Face: ");
								System.out.println(newFace.get(0));
								System.out.println(newFace.get(1));
								System.out.println(newFace.get(2));
							}
						}
					}

					for (final GeosetVertex cgv : copies) {
						if (cgv != null) {
							cgv.getGeoset().addVertex(cgv);
						}
					}

					tempe = new ExtrudeAction();
					tempe.storeSelection(selection);
					tempe.setType(false);
					tempe.storeBaseMovement(new Vertex(0, 0, 0));
					tempe.setAddedTriangles(newTriangles);
					tempe.setAddedVerts(copies);
					tempe.setCopiedGroup(copiedGroup);
					currentAction = tempe;
					break;
				}
			} else {
				JOptionPane.showMessageDialog(null, "UI Error: Cannot perform two actions at once.");
			}
		}
	}

	public void startUVAction(final Point2D.Double mouseStart, final int actionTypeUV) {
		// Points need to be in geometry/model space
		if (!lockdown) {
			final byte dim1 = 0;
			final byte dim2 = 1;
			beenSaved = false;
			if (this.actionTypeUV == -1) {
				this.actionTypeUV = actionTypeUV;
				switch (actionTypeUV) {
				case 3:
					UVMoveAction temp = new UVMoveAction();
					temp.storeSelection(uvselection);
					temp.createEmptyMoveVector();
					temp.setActType(VertexActionType.fromLegacyId(actionTypeUV));
					currentUVAction = temp;
					break;
				case 4:
					temp = new UVMoveAction();
					temp.storeSelection(uvselection);
					temp.createEmptyMoveVectors();
					temp.setActType(VertexActionType.fromLegacyId(actionTypeUV));
					currentUVAction = temp;
					break;
				case 5:
					temp = new UVMoveAction();
					temp.storeSelection(uvselection);
					temp.createEmptyMoveVectors();
					temp.setActType(VertexActionType.fromLegacyId(actionTypeUV));
					currentUVAction = temp;
					break;
				case 6:
					break;
				case 7:
					break;
				}
			} else {
				JOptionPane.showMessageDialog(null, "UI Error: Cannot perform two actions at once.");
			}
		}
	}

	public void finishUVAction(final Point2D.Double mouseStart, final Point2D.Double mouseStop) {
		if (!lockdown) {
			beenSaved = false;
			// Points need to be in geometry/model space

			// switch( actionType )
			// {
			// case 3:
			// break;
			// case 4:
			// break;
			// case 5:
			// break;
			// case 6:
			// break;
			// case 7:
			// break;
			// }
			updateUVAction(mouseStart, mouseStop);
			redoStack.clear();
			actionStack.add(currentUVAction);
			currentUVAction = null;
			actionTypeUV = -1;
		}
	}

	public void finishAction(final Point2D.Double mouseStart, final Point2D.Double mouseStop, final byte dim1,
			final byte dim2) {
		if (!lockdown) {
			beenSaved = false;
			// Points need to be in geometry/model space

			// switch( actionType )
			// {
			// case 3:
			// break;
			// case 4:
			// break;
			// case 5:
			// break;
			// case 6:
			// break;
			// case 7:
			// break;
			// }
			updateAction(mouseStart, mouseStop, dim1, dim2);
			redoStack.clear();
			actionStack.add(currentAction);
			currentAction = null;
			actionType = -1;
		}
	}

	public void manualMove() {
		if (!lockdown) {
			beenSaved = false;
			final MoveAction temp = new MoveAction();
			temp.storeSelection(selection);
			temp.createEmptyMoveVector();
			temp.setActType(VertexActionType.fromLegacyId(actionType));
			final JPanel inputPanel = new JPanel();
			final GridLayout layout = new GridLayout(6, 1);
			inputPanel.setLayout(layout);
			final JSpinner[] spinners = new JSpinner[3];
			inputPanel.add(new JLabel("Move Z:"));
			inputPanel.add(spinners[0] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
			inputPanel.add(new JLabel("Move Y:"));
			inputPanel.add(spinners[1] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
			inputPanel.add(new JLabel("Move X:"));
			inputPanel.add(spinners[2] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
			final int x = JOptionPane.showConfirmDialog(this.mpanel, inputPanel, "Manual Translation",
					JOptionPane.OK_CANCEL_OPTION);
			if (x != JOptionPane.OK_OPTION) {
				return;
			}
			final double deltaX = ((Number) spinners[0].getValue()).doubleValue();
			final double deltaY = ((Number) spinners[1].getValue()).doubleValue();
			final double deltaZ = ((Number) spinners[2].getValue()).doubleValue();
			for (final Vertex ver : selection) {
				ver.translateCoord((byte) 0, deltaX);
				ver.translateCoord((byte) 1, deltaY);
				ver.translateCoord((byte) 2, deltaZ);
			}
			temp.getMoveVector().translateCoord((byte) 0, deltaX);
			temp.getMoveVector().translateCoord((byte) 1, deltaY);
			temp.getMoveVector().translateCoord((byte) 2, deltaZ);
			// updateAction(new
			// Vertex(0,0,0),((MoveAction)currentAction).moveVector,0,1);
			redoStack.clear();
			actionStack.add(temp);
			// break;
		}
	}

	public void manualRotate() {
		if (!lockdown) {
			beenSaved = false;
			final JPanel inputPanel = new JPanel();
			final GridLayout layout = new GridLayout(6, 1);
			inputPanel.setLayout(layout);
			final JSpinner[] spinners = new JSpinner[3];
			inputPanel.add(new JLabel("Rotate Z degrees:"));
			inputPanel.add(spinners[0] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
			inputPanel.add(new JLabel("Rotate Y degrees:"));
			inputPanel.add(spinners[1] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
			inputPanel.add(new JLabel("Rotate X degrees:"));
			inputPanel.add(spinners[2] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
			final int x = JOptionPane.showConfirmDialog(this.mpanel, inputPanel, "Manual Rotation",
					JOptionPane.OK_CANCEL_OPTION);
			if (x != JOptionPane.OK_OPTION) {
				return;
			}

			final RotateAction temp = new RotateAction();
			temp.storeSelection(selection);
			temp.createEmptyMoveVectors();
			temp.setActType(VertexActionType.ROTATE); // i think this is rot
			final double deltaXAngle = Math.toRadians(((Number) spinners[0].getValue()).doubleValue());
			final double deltaYAngle = Math.toRadians(((Number) spinners[1].getValue()).doubleValue());
			final double deltaZAngle = Math.toRadians(((Number) spinners[2].getValue()).doubleValue());
			immediateRotate(temp, (byte) 0, (byte) 2, deltaXAngle);
			immediateRotate(temp, (byte) 1, (byte) 0, deltaYAngle);
			immediateRotate(temp, (byte) 1, (byte) 2, deltaZAngle);
			// updateAction(new
			// Vertex(0,0,0),((MoveAction)currentAction).moveVector,0,1);
			redoStack.clear();
			actionStack.add(temp);
			// break;
		}
	}

	private void immediateRotate(final RotateAction action, final byte dim1, final byte dim2, final double deltaAng) {
		final ArrayList<Normal> normals = action.getNormals();
		final Vertex v = Vertex.centerOfGroup(selection);
		double cx = v.getCoord(dim1);
		double cy = v.getCoord(dim2);
		double dx, dy, r;
		if (selection.size() > 1) {
			for (final Vertex ver : selection) {
				final double x1 = ver.getCoord(dim1);
				final double y1 = ver.getCoord(dim2);
				dx = x1 - cx;
				dy = y1 - cy;
				r = Math.sqrt(dx * dx + dy * dy);
				double verAng = Math.acos(dx / r);
				if (dy < 0) {
					verAng = -verAng;
				}
				// if( getDimEditable(dim1) )
				double nextDim = Math.cos(verAng + deltaAng) * r + cx;
				if (!Double.isNaN(nextDim)) {
					ver.setCoord(dim1, Math.cos(verAng + deltaAng) * r + cx);
				}
				// if( getDimEditable(dim2) )
				nextDim = Math.sin(verAng + deltaAng) * r + cy;
				if (!Double.isNaN(nextDim)) {
					ver.setCoord(dim2, Math.sin(verAng + deltaAng) * r + cy);
				}
				// if( getDimEditable(dim1) )
				((MoveAction) action).getMoveVectors().get(selection.indexOf(ver)).translateCoord(dim1,
						ver.getCoord(dim1) - x1);
				// if( getDimEditable(dim2) )
				((MoveAction) action).getMoveVectors().get(selection.indexOf(ver)).translateCoord(dim2,
						ver.getCoord(dim2) - y1);
			}

		}
		cx = 0;
		cy = 0;
		for (final Vertex ver : normals) {
			final double x1 = ver.getCoord(dim1);
			final double y1 = ver.getCoord(dim2);
			dx = x1 - cx;
			dy = y1 - cy;
			r = Math.sqrt(dx * dx + dy * dy);
			double verAng = Math.acos(dx / r);
			if (dy < 0) {
				verAng = -verAng;
			}
			// if( getDimEditable(dim1) )
			double nextDim = Math.cos(verAng + deltaAng) * r + cx;
			if (!Double.isNaN(nextDim)) {
				ver.setCoord(dim1, Math.cos(verAng + deltaAng) * r + cx);
			}
			// if( getDimEditable(dim2) )
			nextDim = Math.sin(verAng + deltaAng) * r + cy;
			if (!Double.isNaN(nextDim)) {
				ver.setCoord(dim2, Math.sin(verAng + deltaAng) * r + cy);
			}
			// if( getDimEditable(dim1) )
			(action).getNormalMoveVectors().get(normals.indexOf(ver)).translateCoord(dim1, ver.getCoord(dim1) - x1);
			// if( getDimEditable(dim2) )
			(action).getNormalMoveVectors().get(normals.indexOf(ver)).translateCoord(dim2, ver.getCoord(dim2) - y1);
		}
	}

	public void manualSet() {
		if (!lockdown && selection.size() > 0) {
			beenSaved = false;
			final MoveAction temp = new MoveAction();
			temp.storeSelection(selection);
			temp.createEmptyMoveVector();
			temp.setActType(VertexActionType.MOVE);
			final JPanel inputPanel = new JPanel();
			final GridLayout layout = new GridLayout(6, 1);
			inputPanel.setLayout(layout);
			final JSpinner[] spinners = new JSpinner[3];
			inputPanel.add(new JLabel("Move Z:"));
			inputPanel.add(spinners[0] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
			inputPanel.add(new JLabel("Move Y:"));
			inputPanel.add(spinners[1] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
			inputPanel.add(new JLabel("Move X:"));
			inputPanel.add(spinners[2] = new JSpinner(new SpinnerNumberModel(0.0, -100000.00, 100000.0, 0.0001)));
			final int x = JOptionPane.showConfirmDialog(this.mpanel, inputPanel, "Manual Translation",
					JOptionPane.OK_CANCEL_OPTION);
			if (x != JOptionPane.OK_OPTION) {
				return;
			}
			final double deltaX = ((Number) spinners[0].getValue()).doubleValue();
			final double deltaY = ((Number) spinners[1].getValue()).doubleValue();
			final double deltaZ = ((Number) spinners[2].getValue()).doubleValue();
			final Vertex centerOfGravity = Vertex.centerOfGroup(selection);
			final Vertex changeVector = new Vertex(deltaX - centerOfGravity.x, deltaY - centerOfGravity.y,
					deltaZ - centerOfGravity.z);
			for (final Vertex ver : selection) {
				ver.translateCoord((byte) 0, changeVector.x);
				ver.translateCoord((byte) 1, changeVector.y);
				ver.translateCoord((byte) 2, changeVector.z);
			}
			temp.getMoveVector().translateCoord((byte) 0, changeVector.x);
			temp.getMoveVector().translateCoord((byte) 1, changeVector.y);
			temp.getMoveVector().translateCoord((byte) 2, changeVector.z);
			redoStack.clear();
			actionStack.add(temp);
		}
	}

	public void snap() {
		if (!lockdown) {
			beenSaved = false;
			final ArrayList<Vertex> oldLocations = new ArrayList<>();
			final Vertex cog = Vertex.centerOfGroup(selection);
			for (int i = 0; i < selection.size(); i++) {
				oldLocations.add(new Vertex(selection.get(i)));
			}
			final SnapAction temp = new SnapAction(selection, oldLocations, cog);
			temp.redo();// a handy way to do the snapping!
			actionStack.add(temp);
		}
	}

	public void snapNormals() {
		if (!lockdown) {
			beenSaved = false;
			final ArrayList<Vertex> oldLocations = new ArrayList<>();
			final ArrayList<Vertex> selectedNormals = new ArrayList<>();
			final Normal snapped = new Normal(0, 0, 1);
			for (int i = 0; i < selection.size(); i++) {
				if (selection.get(i) instanceof GeosetVertex) {
					final GeosetVertex gv = (GeosetVertex) selection.get(i);
					if (gv.getNormal() != null) {
						oldLocations.add(new Vertex(gv.getNormal()));
						selectedNormals.add(gv.getNormal());
					} // else no normal to snap!!!
				}
			}
			final SnapNormalsAction temp = new SnapNormalsAction(selectedNormals, oldLocations, snapped);
			temp.redo();// a handy way to do the snapping!
			actionStack.add(temp);
		}
	}

	public void snapUVs() {
		if (!lockdown) {
			beenSaved = false;
			final ArrayList<TVertex> oldLocations = new ArrayList<>();
			final TVertex cog = TVertex.centerOfGroup(uvselection);
			for (int i = 0; i < uvselection.size(); i++) {
				oldLocations.add(new TVertex(uvselection.get(i)));
			}
			final UVSnapAction temp = new UVSnapAction(uvselection, oldLocations, cog);
			temp.redo();// a handy way to do the snapping!
			actionStack.add(temp);
		}
	}

	public void delete() {
		if (!lockdown) {
			final ArrayList<Geoset> remGeosets = new ArrayList<>();// model.getGeosets()
			beenSaved = false;
			final ArrayList<Triangle> deletedTris = new ArrayList<>();
			for (int i = 0; i < selection.size(); i++) {
				if (selection.get(i).getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) selection.get(i);
					for (final Triangle t : gv.getTriangles()) {
						t.getGeoset().removeTriangle(t);
						if (!deletedTris.contains(t)) {
							deletedTris.add(t);
						}
					}
					gv.getGeoset().remove(gv);
				}
			}
			for (int i = model.getGeosets().size() - 1; i >= 0; i--) {
				if (model.getGeosets().get(i).isEmpty()) {
					final Geoset g = model.getGeoset(i);
					remGeosets.add(g);
					model.remove(g);
					if (g.getGeosetAnim() != null) {
						model.remove(g.getGeosetAnim());
					}
				}
			}
			if (remGeosets.size() <= 0) {
				final DeleteAction temp = new DeleteAction(selection, deletedTris);
				actionStack.add(temp);
			} else {
				final SpecialDeleteAction temp = new SpecialDeleteAction(selection, deletedTris, remGeosets, model);
				actionStack.add(temp);
			}
		}
	}

	public void selectAll() {
		if (!lockdown) {
			beenSaved = false;
			final ArrayList<Vertex> oldSelection = new ArrayList<>(selection);
			selection.clear();
			for (final Geoset geo : editableGeosets) {
				for (final GeosetVertex v : geo.getVertices()) {
					if (!selection.contains(v)) {
						selection.add(v);
					}
				}
			}
			for (final IdObject o : model.getIdObjects()) {
				final Vertex v = o.getPivotPoint();
				if (!selection.contains(v)) {
					selection.add(v);
				}
			}
			final SelectAction temp = new SelectAction(oldSelection, selection, this, SelectionActionType.SELECT_ALL);
			actionStack.add(temp);
		}
	}

	public void selectAllUV() {
		if (!lockdown) {
			beenSaved = false;
			final ArrayList<TVertex> oldSelection = new ArrayList<>(uvselection);
			uvselection.clear();
			for (final Geoset geo : editableGeosets) {
				for (int i = 0; i < geo.getVertices().size(); i++) {
					final TVertex v = geo.getVertices().get(i).getTVertex(uvpanel.currentLayer());
					if (!uvselection.contains(v)) {
						uvselection.add(v);
					}
				}
			}
			final UVSelectAction temp = new UVSelectAction(oldSelection, uvselection, this,
					UVSelectionActionType.SELECT_ALL);
			actionStack.add(temp);
		}
	}

	public void viewMatrices() {
		final ArrayList<Bone> boneRefs = new ArrayList<>();
		for (final Vertex ver : selection) {
			if (ver instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) ver;
				for (final Bone b : gv.getBones()) {
					if (!boneRefs.contains(b)) {
						boneRefs.add(b);
					}
				}
			}
		}
		String boneList = "";
		for (int i = 0; i < boneRefs.size(); i++) {
			if (i == boneRefs.size() - 2) {
				boneList = boneList + boneRefs.get(i).getName() + " and ";
			} else if (i == boneRefs.size() - 1) {
				boneList = boneList + boneRefs.get(i).getName();
			} else {
				boneList = boneList + boneRefs.get(i).getName() + ", ";
			}
		}
		if (boneRefs.size() == 0) {
			boneList = "Nothing was selected that was attached to any bones.";
		}
		final JTextArea tpane = new JTextArea(boneList);
		tpane.setLineWrap(true);
		tpane.setWrapStyleWord(true);
		tpane.setEditable(false);
		tpane.setSize(230, 400);

		final JScrollPane jspane = new JScrollPane(tpane);
		jspane.setPreferredSize(new Dimension(270, 230));

		JOptionPane.showMessageDialog(null, jspane);
		// for( IdObject obj: selBones )
		// {

		// }
	}

	public void insideOut() {
		// Called both by a menu button and by the mirroring function
		if (!lockdown) {
			final ArrayList<Triangle> selTris = new ArrayList<>();
			for (int i = 0; i < selection.size(); i++) {
				final Vertex vert = selection.get(i);
				if (vert.getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) vert;

					for (int ti = 0; ti < gv.getTriangles().size(); ti++) {
						final Triangle temptr = gv.getTriangles().get(ti);
						if (!selTris.contains(temptr)) {
							selTris.add(temptr);
						}
					}
				} else {
					System.out.println("GeosetVertex " + i + " was not found for \"insideOut\" function.");
				}
			}

			for (int i = selTris.size() - 1; i >= 0; i--) {
				boolean goodTri = true;
				for (final Vertex v : selTris.get(i).getAll()) {
					if (!selection.contains(v)) {
						goodTri = false;
					}
				}
				if (!goodTri) {
					selTris.remove(i);
				}
			}

			for (final Triangle tri : selTris) {
				tri.flip();
			}
		}
	}

	public void mirror(final byte dim, final boolean flipModel) {
		if (!lockdown) {
			final byte mirrorDim = dim;
			final Vertex center = Vertex.centerOfGroup(selection);// Calc center
																	// of mass
			for (int i = 0; i < selection.size(); i++) {
				final Vertex vert = selection.get(i);
				vert.setCoord(mirrorDim, 2 * center.getCoord(mirrorDim) - vert.getCoord(mirrorDim));
				if (vert.getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) vert;
					final Normal normal = gv.getNormal();
					if (normal != null) {
						// Flip normals, preserve lighting!
						normal.setCoord(mirrorDim, -normal.getCoord(mirrorDim));
					}
				}
			}
			final ArrayList<IdObject> selBones = new ArrayList<>();
			for (final IdObject b : model.getIdObjects()) {
				if (selection.contains(b.getPivotPoint()) && !selBones.contains(b)) {
					selBones.add(b);
				}
			}
			for (final IdObject obj : selBones) {
				obj.flipOver(dim);
			}
			if (flipModel) {
				insideOut();
			}
		}
	}

	public void mirrorUV(final byte dim) {
		if (!lockdown) {
			final byte mirrorDim = dim;
			final TVertex center = TVertex.centerOfGroup(uvselection);// Calc
																		// center
																		// of
																		// mass
			for (int i = 0; i < uvselection.size(); i++) {
				final TVertex vert = uvselection.get(i);
				vert.setCoord(mirrorDim, 2 * center.getCoord(mirrorDim) - vert.getCoord(mirrorDim));
			}
		}
	}

	public void expandSelection() {
		if (!lockdown) {
			beenSaved = false;
			final ArrayList<Vertex> oldSelection = new ArrayList<>(selection);
			final ArrayList<Triangle> oldTris = new ArrayList<>();
			for (final Vertex v : oldSelection) {
				if (v instanceof GeosetVertex) {
					final GeosetVertex gv = (GeosetVertex) v;
					for (final Triangle triangle : gv.getTriangles()) {
						if (!oldTris.contains(triangle)) {
							oldTris.add(triangle);
						}
					}
				}
			}
			// selection.clear();
			for (final Geoset geo : editableGeosets) {
				for (final GeosetVertex v : geo.getVertices()) {
					for (final Triangle tri : oldTris) {
						if (tri.containsRef(v)) {
							if (!selection.contains(v)) {
								selection.add(v);
							}
						}
					}
				}
			}
			final SelectAction temp = new SelectAction(oldSelection, selection, this,
					SelectionActionType.EXPAND_SELECTION);
			actionStack.add(temp);
		}
	}

	public void expandSelectionUV() {
		if (!lockdown) {
			beenSaved = false;
			final ArrayList<TVertex> oldSelection = new ArrayList<>(uvselection);

			// *** update parenting system
			for (final Geoset geo : editableGeosets) {
				for (int i = 0; i < geo.getVertices().size(); i++) {
					final TVertex v = geo.getVertices().get(i).getTVertex(uvpanel.currentLayer());
					v.setParent(geo.getVertices().get(i));
				}
			}

			final ArrayList<Triangle> oldTris = new ArrayList<>();
			for (final TVertex v : oldSelection) {
				final GeosetVertex gv = v.getParent();
				for (final Triangle triangle : gv.getTriangles()) {
					if (!oldTris.contains(triangle)) {
						oldTris.add(triangle);
					}
				}
			}
			// selection.clear();
			for (final Geoset geo : editableGeosets) {
				for (final GeosetVertex v : geo.getVertices()) {
					for (final Triangle tri : oldTris) {
						if (tri.containsRef(v)) {
							if (!uvselection.contains(v.getTVertex(uvpanel.currentLayer()))) {
								uvselection.add(v.getTVertex(uvpanel.currentLayer()));
							}
						}
					}
				}
			}
			final UVSelectAction temp = new UVSelectAction(oldSelection, uvselection, this,
					UVSelectionActionType.EXPAND_SELECTION);
			actionStack.add(temp);
		}
	}

	public void invertSelection() {
		final ArrayList<Vertex> oldSelection = new ArrayList<>(selection);
		for (final Geoset geo : editableGeosets) {
			for (final GeosetVertex v : geo.getVertices()) {
				if (selection.contains(v)) {
					selection.remove(v);
				} else {
					selection.add(v);
				}
			}
		}
		if (dispPivots) {
			ArrayList<IdObject> geoParents = null;
			ArrayList<IdObject> geoSubParents = null;
			if (dispChildren) {
				geoParents = new ArrayList<>();
				geoSubParents = new ArrayList<>();
				for (final Geoset geo : editableGeosets) {
					for (final GeosetVertex ver : geo.getVertices()) {
						for (final Bone b : ver.getBones()) {
							if (!geoParents.contains(b)) {
								geoParents.add(b);
							}
						}
					}
				}
				// childMap = new HashMap<IdObject,ArrayList<IdObject>>();
				for (final IdObject obj : model.getIdObjects()) {
					if (!geoParents.contains(obj)) {
						boolean valid = false;
						for (int i = 0; !valid && i < geoParents.size(); i++) {
							valid = geoParents.get(i).childOf(obj);
						}
						if (valid) {
							geoSubParents.add(obj);
						}
						// if( obj.parent != null )
						// {
						// ArrayList<IdObject> children =
						// childMap.get(obj.parent);
						// if( children == null )
						// {
						// children = new ArrayList<IdObject>();
						// childMap.put(obj.parent, children);
						// }
						// children.add(obj);
						// }
					}
				}
				// System.out.println(geoSubParents);
			}

			if (!dispChildren) {
				for (final Vertex ver : model.getPivots()) {
					if (selection.contains(ver)) {
						selection.remove(ver);
					} else {
						selection.add(ver);
					}
				}
			} else {
				for (final IdObject o : model.getIdObjects()) {
					// boolean hasRef = false;//highlight != null &&
					// highlight.containsReference(o);
					// if( dispChildren )
					// {
					// for( int i = 0; !hasRef && i <
					// editableGeosets.size(); i++ )
					// {
					// hasRef = editableGeosets.get(i).containsReference(o);
					// }
					// }
					if (geoParents.contains(o) || geoSubParents.contains(o))// !dispChildren
																			// ||
																			// hasRef
																			// )
					{
						final Vertex ver = o.getPivotPoint();
						if (selection.contains(ver)) {
							selection.remove(ver);
						} else {
							selection.add(ver);
						}
					}
				}
			}
		}
		// for( IdObject o: model.getIdObjects() )
		// {
		// Vertex v = o.getPivotPoint();
		// if( selection.contains(v) )
		// selection.remove(v);
		// else
		// selection.add(v);
		// }
		final SelectAction temp = new SelectAction(oldSelection, selection, this, SelectionActionType.INVERT_SELECTION);
		actionStack.add(temp);
	}

	public void invertSelectionUV() {
		final ArrayList<TVertex> oldSelection = new ArrayList<>(uvselection);
		for (final Geoset geo : editableGeosets) {
			for (int i = 0; i < geo.getVertices().size(); i++) {
				final TVertex v = geo.getVertices().get(i).getTVertex(uvpanel.currentLayer());
				if (uvselection.contains(v)) {
					uvselection.remove(v);
				} else {
					uvselection.add(v);
				}
			}
		}
		final UVSelectAction temp = new UVSelectAction(oldSelection, uvselection, this,
				UVSelectionActionType.INVERT_SELECTION);
		actionStack.add(temp);
	}

	public void selFromMain() {
		final ArrayList<TVertex> oldSelection = new ArrayList<>(uvselection);

		uvselection.clear();
		for (final Vertex ver : selectionManager.getSelectedVertices()) {
			if (ver instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) ver;
				final TVertex myT = gv.getTverts().get(uvpanel.currentLayer());
				if (!uvselection.contains(myT)) {
					uvselection.add(myT);
				}
			}
		}
		final UVSelectAction temp = new UVSelectAction(oldSelection, uvselection, this,
				UVSelectionActionType.SELECT_FROM_VIEWER);
		actionStack.add(temp);
	}

	public void undo() {
		if (actionStack.size() > 0) {
			final UndoAction temp = actionStack.get(actionStack.size() - 1);
			actionStack.remove(temp);
			temp.undo();
			redoStack.add(temp);

			// cureSelection();
			modelChangeNotifier.modelChanged();
		} else {
			JOptionPane.showMessageDialog(null, "Nothing to undo!");
		}
	}

	public boolean canUndo() {
		return (actionStack.size() > 0);
	}

	public boolean canRedo() {
		return (redoStack.size() > 0);
	}

	public void redo() {
		if (redoStack.size() > 0) {
			final UndoAction temp = redoStack.get(redoStack.size() - 1);
			redoStack.remove(temp);
			temp.redo();
			actionStack.add(temp);
			modelChangeNotifier.modelChanged();
		} else {
			JOptionPane.showMessageDialog(null, "Nothing to redo!");
		}
	}

	public String undoText() {
		if (canUndo()) {
			final UndoAction temp = actionStack.get(actionStack.size() - 1);
			return temp.actionName();
		} else {
			return "Can't undo";
		}
	}

	public String redoText() {
		if (canRedo()) {
			final UndoAction temp = redoStack.get(redoStack.size() - 1);
			return temp.actionName();
		} else {
			return "Can't redo";
		}
	}

	public boolean getDimEditable(final int dim) {
		return !programPreferences.getDimLock(dim);
	}

	public ProgramPreferences getProgramPreferences() {
		return programPreferences;
	}

	public void setProgramPreferences(final ProgramPreferences prefs) {
		programPreferences = prefs;
	}

	/**
	 * Warning: Totally might be null.
	 *
	 * @return
	 */
	public ModelPanel getModelPanel() {
		return mpanel;
	}

	/**
	 * Warning: Totally might be null.
	 *
	 * @return
	 */
	public UVPanel getUVPanel() {
		return uvpanel;
	}

	public void notifyUpdate() {
		if (mpanel != null) {
			mpanel.repaint();
		}
		if (uvpanel != null) {
			uvpanel.repaint();
		}
	}

	public void refreshUndo() {
		undoHandler.refreshUndo();
	}

	public boolean isDispPivots() {
		return selectionManager.isShowPivots();
	}

	public boolean isDispChildren() {
		return dispChildren;
	}

	public boolean isDispPivotNames() {
		return dispPivotNames;
	}

	public boolean isDispCameras() {
		return dispCameras;
	}

	public boolean isDispCameraNames() {
		return dispCameraNames;
	}

	public UndoHandler getUndoHandler() {
		return undoHandler;
	}

	public void setUndoHandler(final UndoHandler undoHandler) {
		this.undoHandler = undoHandler;
	}

	public void notifyCoordDisplay(final byte a, final byte b, final double aVal, final double bVal) {
		for (final CoordDisplayListener coordinateListener : coordinateListeners) {
			coordinateListener.notifyUpdate(a, b, aVal, bVal);
		}
	}

	public void addCoordDisplayListener(final CoordDisplayListener listener) {
		this.coordinateListeners.add(listener);
	}

	@Override
	public void pushAction(final UndoAction action) {
		actionStack.add(action);
	}

	public ModelChangeNotifier getModelChangeNotifier() {
		return modelChangeNotifier;
	}

	public void addTeamColor(final Callback<List<Geoset>> listener) {
		final TeamColorAddAction teamColorAddAction = new TeamColorAddAction(selectionManager.getSelectedFaces(),
				getMDL(), listener, selectionManager);
		teamColorAddAction.redo();
		pushAction(teamColorAddAction);
	}
}
