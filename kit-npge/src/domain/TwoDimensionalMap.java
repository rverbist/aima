package domain;

public final class TwoDimensionalMap
{
    private final TwoDimensionalSpace _space;
    private final Point _actor;

    public TwoDimensionalMap(final TwoDimensionalSpace space)
    {
        _space = space;
        _actor = getStart();
    }

    private TwoDimensionalMap(final TwoDimensionalMap map, final Point actor)
    {
        _space = map._space;
        _actor = actor;
    }

    public final Point getStart()
    {
        return _space.getStart();
    }

    public final Point getFinish()
    {
        return _space.getFinish();
    }

    public final Point getActor()
    {
        return _actor;
    }
    
    // used to draw, debugging, don't use this :(
    public final TwoDimensionalSpace getSpace()
    {
        return _space;
    }

    public final boolean isTraversable(final Point point)
    {
        return _space.isValue(point, TwoDimensionalSpace.EMPTY);
    }

    public final TwoDimensionalMap setActor(final Point actor)
    {
        return new TwoDimensionalMap(this, actor);
    }

    @Override
    public int hashCode()
    {
        // note: _space isn't included in the hashcode
        // it's never going to change anyway.
        int hashcode = 5;
        hashcode = hashcode * 31 + _actor.getX();
        hashcode = hashcode * 31 + _actor.getY();
        return hashcode;
    }

    @Override
    public boolean equals(Object other)
    {
        // note: _space isn't included in the equals
        // it's never going to change anyway.
        if (other instanceof TwoDimensionalMap)
        {
            final TwoDimensionalMap map = (TwoDimensionalMap) other;
            return _actor.equals(map._actor);
        }
        return false;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        final Point start = getStart();
        final Point end = getFinish();
        final Point actor = getActor();
        for (int row = 0; row < _space.getRows(); row++)
        {
            for (int column = 0; column < _space.getColumns(); column++)
            {
                final Point point = new Point(row, column);
                sb.append('|');
                if (point.equals(start))
                {
                    sb.append('S');
                }
                else if (point.equals(end))
                {
                    sb.append('F');
                }
                else if (point.equals(actor))
                {
                    sb.append('A');
                }
                else if (_space.isValue(point, TwoDimensionalSpace.OBSTACLE))
                {
                    sb.append('X');
                }
                else
                {
                    sb.append(' ');
                }
            }
            sb.append('|');
            sb.append('\n');
        }
        return sb.toString();
    }
}
