package domain;

public final class TwoDimensionalSpaceUsingBits extends TwoDimensionalSpace
{
    private final DataStore _layout;

    public TwoDimensionalSpaceUsingBits(final int rows, final int columns)
    {
        super(rows, columns, new Point(rows - 1, 0), new Point(0, columns / 2));
        _layout = new DataStore(1L * getRows() * getColumns());
    }

    @Override
    public byte getValue(int row, int column)
    {
        long index = 1L * row * getColumns() + column;
        return _layout.getBit(index) ? (byte)1 : (byte)0;
    }

    @Override
    public boolean setValue(int row, int column, byte value)
    {
        long index = 1L * row * getColumns() + column;
        if (0 <= row && row < getRows() && 0 <= column && column < getColumns())
        {
            if (getValue(row, column) != value)
            {
                _layout.setBit(index, value != 0);
                return true;
            }
        }
        return false;
    }
}

final class DataStore
{
    private final long[] _store;
    private final byte _size = 64;
    
    public DataStore(final long size)
    {
        _store = new long[(int) ((size + size - 1) / _size)];
    }

    public final void setBit(final long position, final boolean value)
    {
        final int slot = (int)(position / _size);
        final int bit = (int)(position % _size);
        _store[slot] = set(_store[slot], bit);
    }

    public final boolean getBit(final long position)
    {
        final int slot = (int)(position / _size);
        final int bit = (int)(position % _size);
        return get(_store[slot], bit);
    }

    private static final boolean get(long value, int bit)
    {
        return (value & (1 << bit)) != 0;
    }

    private static final long set(long value, int bit)
    {
        return (long)(value | (1 << bit));
    }
}