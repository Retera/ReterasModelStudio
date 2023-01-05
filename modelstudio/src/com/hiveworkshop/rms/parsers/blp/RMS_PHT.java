package com.hiveworkshop.rms.parsers.blp;

import com.hiveworkshop.rms.editor.model.Bitmap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

public enum RMS_PHT {
	DIFFUSE     (new Bitmap("RMS_Placeholders\\Diffuse.blp"),       () -> ImageUtils.getCheckerImage(64, 64, 2, new Color(200, 80, 200, 255), new Color(110, 0, 110, 255))),
	NORMAL      (new Bitmap("RMS_Placeholders\\Normal.blp"),        () -> ImageUtils.getColorImage(new Color(.5f, .5f, 0, 1))),
	ORM         (new Bitmap("RMS_Placeholders\\ORM.blp"),           () -> ImageUtils.getColorImage(new Color(1f, .5f, 0, 0))),
	EMISSIVE    (new Bitmap("RMS_Placeholders\\Emissive.blp"),      () -> ImageUtils.getColorImage(Color.BLACK)),
	TEAM_COLOR  (new Bitmap("RMS_Placeholders\\Team Color.blp"),    () -> ImageUtils.getColorImage(Color.MAGENTA)),
	REFLECTIONS (new Bitmap("RMS_Placeholders\\Reflections.blp"),   () -> ImageUtils.getColorImage(new Color(.56f, .89f, .92f, 1))),
	;
	final Bitmap bitmap;
	final Supplier<BufferedImage> imageSupplier;

	RMS_PHT(Bitmap bitmap, Supplier<BufferedImage> imageSupplier){
		this.bitmap = bitmap;
		this.imageSupplier = imageSupplier;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public String getPath() {
		return bitmap.getRenderableTexturePath();
	}

	public BufferedImage getColorImage(){
		return imageSupplier.get();
	}
}
