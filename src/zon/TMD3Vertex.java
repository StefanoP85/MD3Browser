package zon;

public class TMD3Vertex extends TMDEntity {
    public short X;
    public short Y;
    public short Z;
    public byte NormalLatitude;
    public byte NormalLongitude;
    public TMD3Vertex(short X, short Y, short Z, byte NormalLatitude, byte NormalLongitude) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.NormalLatitude = NormalLatitude;
        this.NormalLongitude = NormalLongitude;
    }
}
