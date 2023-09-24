package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.AddTriangleAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class CreateFace extends ActionFunction {
	public CreateFace() {
		super(TextKey.CREATE_FACE, CreateFace::createFace);
	}

	public static void createFace(ModelHandler modelHandler) {
		if (!isTextField() && modelHandler != null) {
			MainPanel mainPanel = ProgramGlobals.getMainPanel();

			if (!(ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE)) {
				try {
//					Viewport viewport = mainPanel.getViewportListener().getViewport();
//					Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
//					Viewport viewport = mainPanel.getViewportListener().getViewport();
//					Vec3 facingVector = new Vec3(0, 0, 1);
					UndoAction createFaceFromSelection = createFaceFromSelection(modelHandler.getModelView(), null);

					if (createFaceFromSelection != null) {
						modelHandler.getUndoManager().pushAction(createFaceFromSelection.redo());
					}
				} catch (final FaceCreationException exc) {
					JOptionPane.showMessageDialog(mainPanel, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
				}
			} else {
				JOptionPane.showMessageDialog(mainPanel,
						"Unable to create face, wrong selection mode", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private static boolean isTextField() {
		Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return (focusedComponent instanceof JTextComponent);
	}

	public static UndoAction createFaceFromSelection(ModelView modelView, Vec3 preferredFacingVector) {
		Set<GeosetVertex> selection = modelView.getSelectedVertices();
		if (selection.size() != 3) {
			throw new FaceCreationException(
					"A face can only be created from exactly 3 vertices (you have " + selection.size() + " selected)");
		}
		GeosetVertex[] verticesArray = selection.toArray(new GeosetVertex[0]);

		Geoset geoset = selection.stream().findAny().orElse(new GeosetVertex(0, 0, 0)).getGeoset();

		boolean sameGeoset = Arrays.stream(verticesArray).allMatch(vertex -> vertex.getGeoset() == geoset);
		boolean triangleExists = verticesArray[0].getTriangles().stream().anyMatch(triangle -> triangle.containsSameVerts(verticesArray));

		if (sameGeoset && !triangleExists) {
			Triangle newTriangle = new Triangle(verticesArray[0], verticesArray[1], verticesArray[2], geoset);
			Vec3 facingVector = newTriangle.getNormal();

			if (preferredFacingVector == null) {
				preferredFacingVector = verticesArray[0].getNormal();
			}
			double cosine = facingVector.dot(preferredFacingVector) / (facingVector.length() * preferredFacingVector.length());
			if (cosine < 0) {
				newTriangle.flip(false);
			}

			return new AddTriangleAction(geoset, Collections.singletonList(newTriangle));
		} else if (!sameGeoset) {
			throw new FaceCreationException(
					"All three vertices to create a face must be a part of the same Geoset");
		} else if (triangleExists) {
			throw new FaceCreationException("Triangle already exists");
		}
		return null;
	}
}
