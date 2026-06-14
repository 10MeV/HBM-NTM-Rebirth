package com.hbm.util;

/**
 * Legacy 1.7.10 package bridge for note beat construction.
 */
@Deprecated(forRemoval = false)
public class NoteBuilder {
    private final StringBuilder beat = new StringBuilder();

    public static NoteBuilder start() {
        return new NoteBuilder();
    }

    public NoteBuilder add(Instrument instrument, Note note, Octave octave) {
        if (!beat.isEmpty()) {
            beat.append('-');
        }
        beat.append(instrument.ordinal()).append(':').append(note.ordinal()).append(':').append(octave.ordinal());
        return this;
    }

    public String end() {
        return beat.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Tuple.Triplet<Instrument, Note, Octave>[] translate(String beat) {
        if (beat == null || beat.isEmpty()) {
            return new Tuple.Triplet[0];
        }
        String[] hits = beat.split("-");
        Tuple.Triplet<Instrument, Note, Octave>[] notes = new Tuple.Triplet[hits.length];
        try {
            for (int i = 0; i < hits.length; i++) {
                String[] components = hits[i].split(":");
                Instrument instrument = Instrument.values()[Integer.parseInt(components[0])];
                Note note = Note.values()[Integer.parseInt(components[1])];
                Octave octave = Octave.values()[Integer.parseInt(components[2])];
                notes[i] = new Tuple.Triplet(instrument, note, octave);
            }
            return notes;
        } catch (Exception ignored) {
            return new Tuple.Triplet[0];
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
