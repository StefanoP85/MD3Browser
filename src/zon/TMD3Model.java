package zon;

import java.util.Vector;

public class TMD3Model extends TMDEntity {
    public String HeaderID;
    public int HeaderVersion;
    public String HeaderName;
    public int Flags;
    public int NumberOfFrames;
    public int NumberOfTags;
    public int NumberOfSurfaces;
    public int NumberOfSkins;
    public Vector<TMD3Frame> Frames;
    public Vector<TMD3Tag> Tags;
    public Vector<TMD3TagFrame> TagFrames;
    public Vector<TMD3Surface> Surfaces;
    public TMD3Model() {
        super();
        Frames = new Vector<TMD3Frame>();
        Tags = new Vector<TMD3Tag>();
        TagFrames = new Vector<TMD3TagFrame>();
        Surfaces = new Vector<TMD3Surface>();
    }
}
