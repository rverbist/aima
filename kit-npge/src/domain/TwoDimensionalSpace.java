package domain;

public abstract class TwoDimensionalSpace
{
    protected final int _rows;
    protected final int _columns;
    protected final Point _start;
    protected final Point _finish;

    public static final byte EMPTY = 0;
    public static final byte OBSTACLE = 1;

    public TwoDimensionalSpace(final int rows, final int columns, final Point start, final Point finish)
    {
        _rows = rows;
        _columns = columns;
        _start = start;
        _finish = finish;
    }

    public int getRows()
    {
        return _rows;
    }

    public int getColumns()
    {
        return _columns;
    }

    public Point getStart()
    {
        return _start;
    }

    public Point getFinish()
    {
        return _finish;
    }

    public boolean isValue(final int row, final int column, final byte value)
    {
        if (0 <= row && row < _rows && 0 <= column && column < _columns)
        {
            return getValue(row, column) == value;
        }
        return false;
    }

    public boolean isValue(final Point point, final byte value)
    {
        return isValue(point.getX(), point.getY(), value);
    }

    public byte getValue(final Point point)
    {
        return getValue(point.getX(), point.getY());
    }

    public boolean setValue(final Point point, final byte value)
    {
        return setValue(point.getX(), point.getY(), value);
    }

    public abstract byte getValue(final int row, final int column);

    public abstract boolean setValue(final int row, final int column, final byte value);

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof TwoDimensionalSpaceUsingArrays)
        {
            final TwoDimensionalSpaceUsingArrays space = (TwoDimensionalSpaceUsingArrays) other;
            if (getRows() != space.getRows())
            {
                return false;
            }
            if (getColumns() != space.getColumns())
            {
                return false;
            }
            for (int row = 0; row < getRows(); row++)
            {
                for (int column = 0; column < getColumns(); column++)
                {
                    if (getValue(row, column) != space.getValue(row, column))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hashcode = 5;
        hashcode = hashcode * 31 + getRows();
        hashcode = hashcode * 31 + getColumns();
        for (int row = 0; row < getRows(); row++)
        {
            for (int column = 0; column < getColumns(); column++)
            {
                hashcode = hashcode * 31 + getValue(row, column) * 3;
            }
        }
        return hashcode;
    }
}