package zon;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
    public Vector<TMD3ModelFrame> ModelFrames;
    public Vector<TMD3Tag> Tags;
    public Vector<TMD3Surface> Surfaces;
    public TMD3Model() {
        super();
        ModelFrames = new Vector<TMD3ModelFrame>();
        Tags = new Vector<TMD3Tag>();
        Surfaces = new Vector<TMD3Surface>();
    }
    public String toString() {
        StringBuilder SB = new StringBuilder();
        DecimalFormatSymbols DFS = new DecimalFormatSymbols();
        DFS.setDecimalSeparator('.');
        DecimalFormat DF = new DecimalFormat("#.#######", DFS);
        SB.append("# MD3 Export");
        SB.append(String.format("\n\nname %s", HeaderName));
        SB.append("\n\n# Frames");
        for (int I = 0; I < NumberOfFrames; I++) {
            TMD3ModelFrame MD3ModelFrame = ModelFrames.get(I);
            SB.append(String.format("\nframe %s", MD3ModelFrame.Name));
        }
        SB.append("\n\n# Tags");
        for (int I = 0; I < NumberOfTags; I++) {
            SB.append(String.format("\n\n# %d.Tag", I + 1));
            TMD3Tag MD3FrameTag = Tags.get(I);
            SB.append(String.format("\ntag %s", MD3FrameTag.Name));
            SB.append(String.format("\ntp %s %s %s", DF.format(MD3FrameTag.Location.X), DF.format(MD3FrameTag.Location.Y), DF.format(MD3FrameTag.Location.Z)));
            SB.append(String.format("\ntr %s %s %s %s %s %s %s %s %s",
                    DF.format(MD3FrameTag.OrientationX.X), DF.format(MD3FrameTag.OrientationX.Y), DF.format(MD3FrameTag.OrientationX.Z),
                    DF.format(MD3FrameTag.OrientationY.X), DF.format(MD3FrameTag.OrientationY.Y), DF.format(MD3FrameTag.OrientationY.Z),
                    DF.format(MD3FrameTag.OrientationZ.X), DF.format(MD3FrameTag.OrientationZ.Y), DF.format(MD3FrameTag.OrientationZ.Z)));
        }
        SB.append("\n\n# Surfaces");
        for (int I = 0; I < NumberOfSurfaces; I++) {
            SB.append(String.format("\n\n# %d.Surface", I + 1));
            TMD3Surface MD3Surface = Surfaces.get(I);
            SB.append(String.format("\nmesh %s", MD3Surface.SurfaceName));
            for (int J = 0; J < MD3Surface.MD3Shaders.size(); J++)
                SB.append(String.format("\nmaterial %s", MD3Surface.MD3Shaders.get(J).Name));
            SB.append("\n");
            for (int J = 0; J < MD3Surface.MD3TextVertexes.size(); J++) {
                TMD3TextVertex MD3TextVertex = MD3Surface.MD3TextVertexes.get(J);
                SB.append(String.format("\nvt %s %s", DF.format(MD3TextVertex.U), DF.format(MD3TextVertex.V)));
            }
            for (int J = 0; J < MD3Surface.MD3Triangles.size(); J++) {
                TMD3Triangle MD3Triangle = MD3Surface.MD3Triangles.get(J);
                SB.append(String.format("\nfm %d %d %d", MD3Triangle.I0, MD3Triangle.I1, MD3Triangle.I2));
            }
            int VertexIndex = 0;
            for (int FrameIndex = 0; FrameIndex < ModelFrames.size(); FrameIndex++) {
                SB.append(String.format("\n\n# %d.Frame", FrameIndex + 1));
                for (int J = 0; J < MD3Surface.MD3TextVertexes.size(); J++) {
                    TMD3Vertex MD3Vertex = MD3Surface.MD3Vertexes.get(VertexIndex);
                    SB.append(String.format("\nvp %s %s %s %d %d", DF.format(MD3Vertex.X), DF.format(MD3Vertex.Y), DF.format(MD3Vertex.Z), MD3Vertex.NormalLatitude, MD3Vertex.NormalLongitude));
                    VertexIndex++;
                }
            }
        }
        SB.append("\n\n# End Of File\n");
        return SB.toString();
    }
}
