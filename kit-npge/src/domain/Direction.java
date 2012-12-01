package domain;

public enum Direction
{
    North(-1, 0, 1), 
    East(0, 1, 1), 
    South(1, 0, 1), 
    West(0, -1, 1), 
    
    NorthEast(-1, 1, Math.sqrt(2)), 
    SouthEast(1, 1, Math.sqrt(2)), 
    SouthWest(1, -1, Math.sqrt(2)), 
    NorthWest(-1, -1, Math.sqrt(2));

    private final int _rowOffset;
    private final int _columnOffset;
    private final double _value;

    private Direction(int rowOffset, int columnOffset, double value)
    {
        _rowOffset = rowOffset;
        _columnOffset = columnOffset;
        _value = value;
    }

    public double getValue()
    {
        return _value;
    }

    public Point translate(final Point location)
    {
        final int row = location.getX();
        final int column = location.getY();
        return new Point(row + _rowOffset, column + _columnOffset);
    }
}
