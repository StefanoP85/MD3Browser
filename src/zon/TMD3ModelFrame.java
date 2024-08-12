package zon;

public class TMD3ModelFrame extends TMDEntity {
    public TCoordinate MinBound;
    public TCoordinate MaxBound;
    public TCoordinate LocalOrigin;
    public float Radius;
    public String Name;
    public TMD3ModelFrame(TCoordinate MinBound, TCoordinate MaxBound, TCoordinate LocalOrigin, float Radius, String Name) {
        super();
        this.MinBound = MinBound;
        this.MaxBound = MaxBound;
        this.LocalOrigin = LocalOrigin;
        this.Radius = Radius;
        this.Name = Name;
    }
}
