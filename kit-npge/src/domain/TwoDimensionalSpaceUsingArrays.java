package domain;

public final class TwoDimensionalSpaceUsingArrays extends TwoDimensionalSpace
{
    private final byte[][] _layout;

    public TwoDimensionalSpaceUsingArrays(final int rows, final int columns)
    {
        super(rows, columns, new Point(rows - 1, 0), new Point(0, columns / 2));
        _layout = new byte[_rows][_columns];
    }

    @Override
    public byte getValue(final int row, final int column)
    {
        return _layout[row][column];
    }

    @Override
    public boolean setValue(final int row, final int column, final byte value)
    {
        if (0 <= row && row < getRows() && 0 <= column && column < getColumns())
        {
            if (_layout[row][column] != value)
            {
                _layout[row][column] = value;
                return true;
            }
        }
        return false;
    }
}
