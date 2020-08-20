package com.matrixeater.hacks.jass;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import mpq.MPQException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class GetScriptFile {
    public static void main(String[] args) {
        final MpqCodebase.LoadedMPQ mpq;
        try {
            mpq = MpqCodebase.get().loadMPQ(Paths
                    .get("C:/Users/micro/OneDrive/Documents/Warcraft III/Maps/FromTemplarAndHitari/NWU S3 B14.w3x"));
        } catch (final MPQException e1) {
            e1.printStackTrace();
            return;
        } catch (final IOException e1) {
            e1.printStackTrace();
            return;
        }
        try {
            InputStream resourceAsStream = MpqCodebase.get().getResourceAsStream("scripts\\war3map.j");
            int thingRead;
            try(FileWriter writer = new FileWriter("C:/Users/micro/OneDrive/Documents/Warcraft III/Maps/FromTemplarAndHitari/Extracted/older_v2_wat3map.j")) {

                while((thingRead=resourceAsStream.read()) != -1) {
                    if(thingRead == '\r') {
                        writer.write('\n');
                    } else {
                        writer.write(thingRead);
                    }
                }
            }
//            Files.copy(resourceAsStream, Paths.get("C:/Users/micro/OneDrive/Documents/Warcraft III/Maps/FromTemplarAndHitari/Extracted/older_v2_wat3map.j"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        mpq.unload();
    }
}
