package domain;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class TwoDimensionalDistanceMap
{
    private final TwoDimensionalSpace _space;

    public TwoDimensionalDistanceMap(final TwoDimensionalSpace space, final Point finish)
    {
        _space = space;
        for (int row = 0; row < space.getRows(); row++)
        {
            for (int column = 0; column < space.getColumns(); column++)
            {
                _distances.put(new Point(row, column), Double.POSITIVE_INFINITY);
            }
        }
        _distances.put(finish, 0d);
        
        createInfluenceMap(finish);
    }

    private final Map<Point, Double> _distances = new HashMap<Point, Double>();

    private void setDistanceIfLower(final Point point, final Double value)
    {
        if (value < getDistance(point))
        {
            setDistance(point, value);
        }
    }

    private void setDistance(final Point point, final Double value)
    {
        _distances.put(point, value);
    }

    private Double getDistance(final Point point)
    {
        return _distances.get(point);
    }

    public void createInfluenceMap(Point node)
    {
        final Set<Point> closed = new HashSet<Point>();
        final Queue<Point> queue = new ArrayDeque<Point>();
        queue.add(node);
        while (!queue.isEmpty())
        {
            final Point center = queue.poll();
            closed.add(center);
            
            for (final Direction direction : Direction.values())
            {
                final Point neigbor = direction.translate(center);
                if (_space.isValue(neigbor, TwoDimensionalSpace.EMPTY))
                {
                    setDistanceIfLower(neigbor, getDistance(center) + direction.getValue());
                    if (!closed.contains(neigbor))
                    {
                        queue.add(neigbor);
                    }
                }
            }
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        for (int row = 0; row < _space.getRows(); row++)
        {
            for (int column = 0; column < _space.getColumns(); column++)
            {
                final Point point = new Point(row, column);
                sb.append('|');
                Double value = _distances.get(point);
                if (value == null)
                {
                    sb.append(" ");
                }
                else
                {
                    sb.append(String.format("%02f", value));
                }
            }
            sb.append('|');
            sb.append('\n');
        }
        return sb.toString();
    }
}
