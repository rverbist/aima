package aima.core.environment.myproblem;

import aima.core.agent.Action;
import domain.Direction;
import domain.Point;

public final class MoveDirectionAction implements Action
{
    private final Direction _direction;

    public MoveDirectionAction(final Direction direction)
    {
        _direction = direction;
    }

    public double getValue()
    {
        return _direction.getValue();
    }

    public Point translate(final Point location)
    {
        return _direction.translate(location);
    }

    @Override
    public boolean isNoOp()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        return _direction.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof MoveDirectionAction)
        {
            final MoveDirectionAction action = (MoveDirectionAction) other;
            return _direction.equals(action._direction);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("Move %s.", _direction);
    }
}