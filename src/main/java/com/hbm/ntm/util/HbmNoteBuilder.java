package com.hbm.ntm.util;

import java.util.ArrayList;
import java.util.List;

public final class HbmNoteBuilder {
    private final StringBuilder beat = new StringBuilder();

    private HbmNoteBuilder() {
    }

    public static HbmNoteBuilder start() {
        return new HbmNoteBuilder();
    }

    public HbmNoteBuilder add(Instrument instrument, Note note, Octave octave) {
        if (!beat.isEmpty()) {
            beat.append('-');
        }
        beat.append(instrument.ordinal()).append(':').append(note.ordinal()).append(':').append(octave.ordinal());
        return this;
    }

    public String end() {
        return beat.toString();
    }

    public static List<HbmTuple.Triplet<Instrument, Note, Octave>> translate(String beat) {
        if (beat == null || beat.isEmpty()) {
            return List.of();
        }
        String[] hits = beat.split("-");
        List<HbmTuple.Triplet<Instrument, Note, Octave>> notes = new ArrayList<>(hits.length);
        try {
            for (String hit : hits) {
                String[] components = hit.split(":");
                Instrument instrument = Instrument.values()[Integer.parseInt(components[0])];
                Note note = Note.values()[Integer.parseInt(components[1])];
                Octave octave = Octave.values()[Integer.parseInt(components[2])];
                notes.add(new HbmTuple.Triplet<>(instrument, note, octave));
            }
            return List.copyOf(notes);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    public enum Instrument {
        PIANO,
        BASSDRUM,
        SNARE,
        CLICKS,
        BASSGUITAR
    }

    public enum Note {
        F_SHARP,
        G,
        G_SHARP,
        A,
        A_SHARP,
        B,
        C,
        C_SHARP,
        D,
        D_SHARP,
        E,
        F
    }

    public enum Octave {
        LOW,
        MID,
        HIGH
    }
}
