package domain;

import java.util.Set;
import java.util.TreeSet;

public final class TwoDimensionalSpaceUsingHashSets extends TwoDimensionalSpace
{
    private final Set<Point> _layout;
    
    public TwoDimensionalSpaceUsingHashSets(final int rows, final int columns)
    {
        super(rows, columns, new Point(rows - 1, 0), new Point(0, columns / 2));
        _layout = new TreeSet<Point>();
    }

    @Override
    public byte getValue(int row, int column)
    {
        return _layout.contains(new Point(row, column)) ? TwoDimensionalSpace.OBSTACLE : TwoDimensionalSpace.EMPTY;
    }

    @Override
    public boolean setValue(int row, int column, byte value)
    {
        if (0 <= row && row < getRows() && 0 <= column && column < getColumns())
        {
            if (getValue(row, column) != value)
            {
                if (value == TwoDimensionalSpace.OBSTACLE)
                {
                    _layout.add(new Point(row, column));
                }
                return true;
            }
        }
        return false;
    }
}
