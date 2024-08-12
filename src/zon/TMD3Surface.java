package zon;

import java.util.Vector;

public class TMD3Surface extends TMDEntity {
    public String SurfaceID;
    public String SurfaceName;
    public int Flags;
    public Vector<TMD3Shader> MD3Shaders;
    public Vector<TMD3Triangle> MD3Triangles;
    public Vector<TMD3TextVertex> MD3TextVertexes;
    public Vector<TMD3Vertex> MD3Vertexes;
    public TMD3Surface(String SurfaceID, String SurfaceName, int Flags) {
        super();
        MD3Shaders = new Vector<TMD3Shader>();
        MD3Triangles = new Vector<TMD3Triangle>();
        MD3TextVertexes = new Vector<TMD3TextVertex>();
        MD3Vertexes = new Vector<TMD3Vertex>();
        this.SurfaceID = SurfaceID;
        this.SurfaceName = SurfaceName;
        this.Flags = Flags;
    }
}
