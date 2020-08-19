package com.matrixeater.hacks;

import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import java.io.File;

public class GetMeGoodPaths {
    private static File toFix = new File("F:\\NEEDS_ORGANIZING\\AAAlteracIsle\\Buildings");

    public static void main(String[] args) {
        for(File subFile: toFix.listFiles()) {
            if(subFile.getName().toLowerCase().endsWith(".mdx")) {

                try {
                    EditableModel model = MdxUtils.loadEditable(subFile);
                    for (Attachment atc : model.sortedIdObjects(Attachment.class)) {
                        if (atc.getPath() != null && atc.getPath().contains("NagaBirth")) {
                            atc.setPath("SharedModels\\" + atc.getPath().substring(atc.getPath().lastIndexOf("\\") + 1));
                        }
                    }
                    MdxUtils.saveMdx(model, model.getFileRef());
                } catch (Exception e) {
                    System.err.println(subFile);
                    e.printStackTrace();
                }
            }
        }
    }
}
