package zon;

public class TMD3Tag extends TMDEntity {
    public String Name;
    public TCoordinate Location;
    public TCoordinate OrientationX;
    public TCoordinate OrientationY;
    public TCoordinate OrientationZ;
    public TMD3Tag(String Name, TCoordinate Location, TCoordinate OrientationX, TCoordinate OrientationY, TCoordinate OrientationZ) {
        super();
        this.Name = Name;
        this.Location = Location;
        this.OrientationX = OrientationX;
        this.OrientationY = OrientationY;
        this.OrientationZ = OrientationZ;
    }
}
