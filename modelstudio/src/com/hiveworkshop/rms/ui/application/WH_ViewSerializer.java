package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.util.ModelDependentView;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class WH_ViewSerializer {

	public static ViewSerializer getViewSerilizer(){
		return getViewSerilizer(null);
	}
	public static ViewSerializer getViewSerilizer(byte[] viewMap1){
		ViewSerializer viewSerializer = new ViewSerializer(){

			@Override
			public void writeView(View view, ObjectOutputStream objectOutputStream) throws IOException {
				String str = view.getClass().getName() + "%" + view.hashCode();
//				String str = view.getClass().getPackageName() + "%" + view.hashCode();
				System.out.println(str);
				objectOutputStream.writeUTF(str);
			}

			@Override
			public View readView(ObjectInputStream objectInputStream) throws IOException {
				String s = objectInputStream.readUTF();
				String[] split = s.split("%")[0].split("\\.");

//				View titledView = getTitledView(split[split.length - 1]);
//				System.out.println(titledView);
//				return titledView;
				System.out.println(split[split.length - 1] + " (" + s + ")");

				ModelDependentView view =  switch (split[split.length - 1]){
//					case "DisplayViewUgg" -> new DisplayViewUgg("Ortho");
//					case "PerspectiveViewUgg" -> new PerspectiveViewUgg();
//					case "PreviewView" -> new PreviewView();
//					case "TimeSliderView" -> new TimeSliderView();
//					case "ModelViewManagingView" -> new ModelViewManagingView();
//					case "ModelingCreatorToolsView" -> new ModelingCreatorToolsView(viewportListener);
//					case "ModelComponentsView" -> new ModelComponentsView();
//					default -> new DisplayViewUgg(split[split.length - 1]);
					default -> null;
				};
				if(view != null){
					WindowHandler2.getAllViews().add(view);
				} else {
					System.out.println("not one of those views: " + split[split.length - 1] + " (" + s + ")");
					return WindowHandler2.getTitledView(split[split.length - 1]);
				}

				System.out.println(split[split.length - 1] + " (" + s + ")");
				return view;
			}
		};
		boolean investigateViewMap = false;

		if (investigateViewMap && viewMap1 != null) {
			try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(viewMap1))) {
//			    getViewSerializer().readView(objectInputStream);
				System.out.println("loading views");
				viewSerializer.readView(objectInputStream);
				System.out.println("done loading views?");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("failed.. loading internal original view");
//			    extracted();
			}
		}

		return viewSerializer;
	}
}
