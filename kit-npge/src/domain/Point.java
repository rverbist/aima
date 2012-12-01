package domain;

public final class Point implements Comparable<Point>
{
    private final int _x;
    private final int _y;

    public Point(final int x, final int y)
    {
        _x = x;
        _y = y;
    }

    public int getX()
    {
        return _x;
    }

    public int getY()
    {
        return _y;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int hashcode = 5;
        hashcode = hashcode * prime + _x;
        hashcode = hashcode * prime + _y;
        return hashcode;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof Point)
        {
            final Point point = (Point) other;
            return getX() == point.getX() && getY() == point.getY();
        }
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("[%s, %s]", _x, _y);
    }

    @Override
    public int compareTo(Point other)
    {
        int result = getX() - other.getX();
        if (result != 0)
        {
            return result;
        }
        return getY() - other.getY();
    }
}