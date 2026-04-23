package com.matrixeater.hacks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;

public class SanityFixer {
    private static final String PATHNAME = System.getProperty("user.home") + "/Documents/Warcraft/Models/Request/StonemaulMidget";

	public static void main(final String[] args) {
        final File dir = new File(PATHNAME);
        for (final File modelFile : dir.listFiles()) {
            if (modelFile.getName().toLowerCase().endsWith(".mdx")) {
                processModel(modelFile);
            }
        }
    }

    private static void processModel(final File modelFile) {
        final EditableModel model = EditableModel.read(modelFile);

        final boolean fix = true;
        int unused = 0;
        int warn = 0;
        int severe = 0;
        final List<AnimFlag> flags = model.getAllAnimFlags();
        for (final AnimFlag flag : flags) {
            if (fix) {
                flag.sort();
            }
            final ArrayList values = flag.getValues();
            final ArrayList<Integer> times = flag.getTimes();
            for (int i = flag.size() - 2; i >= 1; i--) {
                final Object prevValue = values.get(i - 1);
                final Object value = values.get(i);
                final Object nextValue = values.get(i + 1);
                final Integer prevTime = times.get(i - 1);
                final Integer time = times.get(i);
                final Integer nextTime = times.get(i + 1);
                if (equals(prevValue, value) && equals(nextValue, value)) {
                    final Animation prevSeq = getSeqAtTime(model, prevTime);
                    final Animation curSeq = getSeqAtTime(model, time);
                    final Animation nextSeq = getSeqAtTime(model, nextTime);
                    if (prevSeq == curSeq && curSeq == nextSeq) {
                        System.out.println(i + ": same values (" + times.get(i) + ")");
                        unused++;
                        if (fix) {
                            removeIndex(flag, values, times, i);
                        }
                    }
                }
            }
            for (int i = flag.size() - 1; i >= 1; i--) {
                final Integer prevTime = times.get(i - 1);
                final Integer time = times.get(i);
                if (prevTime.equals(time)) {
                    warn++;
                    if (fix) {
                        removeIndex(flag, values, times, i);
                    }
                }
            }
            for (int i = flag.size() - 1; i >= 0; i--) {
                final Integer time = times.get(i);
                final Animation curSeq = getSeqAtTime(model, time);
                if (curSeq == null) {
                    unused++;
                    if (fix) {
                        removeIndex(flag, values, times, i);
                    }
                }
            }
            if (flag.getGlobalSeq() != null) {
                if (times.size() > 0 && times.get(0) > 0) {
                    severe++;
                    if (fix) {
                        final Integer lastTime = times.get(times.size() - 1);
                        final Object lastValue = values.get(times.size() - 1);
                        times.add(0, 0);
                        values.add(0, lastValue);
                        if (flag.tans()) {
                            final ArrayList inTans = flag.getInTans();
                            inTans.add(0, inTans.get(inTans.size() - 1));
                            final ArrayList outTans = flag.getOutTans();
                            outTans.add(0, outTans.get(outTans.size() - 1));
                        }
                    }
                }
            }
        }
        ArrayList<EventObject> allEventObjects = model.sortedIdObjects(EventObject.class);
        List<EventObject> evtsToNuke = new ArrayList<>();
        for (EventObject eventObject: allEventObjects) {
        	ArrayList<Integer> eventTrack = eventObject.getEventTrack();
            for (int i = eventTrack.size() - 1; i >= 0; i--) {
                final Integer time = eventTrack.get(i);
                final Animation curSeq = getSeqAtTime(model, time);
                if (curSeq == null) {
                    unused++;
                    if (fix) {
                    	eventTrack.remove(i);
                    }
                }
            }
            if (fix && eventTrack.isEmpty()) {
            	evtsToNuke.add(eventObject);
            }
        }
        for (EventObject eventObject: evtsToNuke) {
        	model.remove(eventObject);
        }
        System.out.println("unused: " + unused);
        System.out.println("warn: " + warn);

        model.printTo(
                new File(PATHNAME + "/Output/" + model.getName() + ".mdx"),
                false);
    }

    private static boolean equals(final Object prevValue, final Object value) {
        if (prevValue instanceof Vertex && value instanceof Vertex) {
            return ((Vertex) prevValue).equalLocs((Vertex) value)
                    || ((Vertex) prevValue).roughlyEqualContents((Vertex) value);
        }
        if (prevValue instanceof QuaternionRotation && value instanceof QuaternionRotation) {
            return ((QuaternionRotation) prevValue).equalContents((QuaternionRotation) value)
                    || ((QuaternionRotation) prevValue).roughlyEqualContents((QuaternionRotation) value);
        }
        return value.equals(prevValue);
    }

    private static Animation getSeqAtTime(final EditableModel model, final int time) {
        final ArrayList<Animation> anims = model.getAnims();
        for (final Animation anim : anims) {
            if (anim.getStart() <= time && anim.getEnd() >= time) {
                return anim;
            }
        }
        return null;
    }

    private static void removeIndex(final AnimFlag flag, final ArrayList values, final ArrayList<Integer> times,
            final int i) {
        values.remove(i);
        times.remove(i);
        if (flag.tans()) {
            flag.getInTans().remove(i);
            flag.getOutTans().remove(i);
        }
    }
}