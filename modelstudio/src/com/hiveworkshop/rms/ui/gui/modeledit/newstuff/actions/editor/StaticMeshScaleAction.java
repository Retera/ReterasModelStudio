package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.util.Vec3;

public class StaticMeshScaleAction implements GenericScaleAction {
	private final ModelView modelView;
	private final Vec3 center;
	private final Vec3 scale = new Vec3(1,1,1);

	public StaticMeshScaleAction(ModelView modelView, Vec3 center) {
		this.modelView = modelView;
		this.center = center;
	}

	@Override
	public void undo() {
		Vec3 revScale = new Vec3(1,1,1).divide(scale);
		rawScale(center, revScale);
	}

	@Override
	public void redo() {
		rawScale(center, scale);
	}

	@Override
	public String actionName() {
		return "scale";
	}

	@Override
	public void updateScale(Vec3 scale) {
		this.scale.multiply(scale);
		rawScale(center, scale);
	}

	public void rawScale(Vec3 center, Vec3 scale) {
		for (Vec3 vertex : modelView.getSelectedVertices()) {
			vertex.scale(center, scale);
		}
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (modelView.isSelected(object)) {
				object.getPivotPoint().scale(center, scale);
				if (object instanceof Bone) {
					translateBone((Bone) object, scale);
				} else if (object instanceof CollisionShape) {
					ExtLog extents = ((CollisionShape) object).getExtents();
					if ((extents != null) && (scale.x == scale.x) && (scale.y == scale.z)) {
						extents.setBoundsRadius(extents.getBoundsRadius() * scale.x);
					}
				}
			}
		}
	}

	public void translateBone(Bone object, Vec3 scale) {
		Vec3AnimFlag translation = (Vec3AnimFlag) object.find("Translation");
		if (translation != null) {
			for (int i = 0; i < translation.size(); i++) {
				translation.getValues().get(i).multiply(scale);
				if (translation.tans()) {
					Vec3 inTanData = translation.getInTans().get(i);
					inTanData.multiply(scale);
					Vec3 outTanData = translation.getInTans().get(i);
					outTanData.multiply(scale);
				}
			}
		}
	}

}
