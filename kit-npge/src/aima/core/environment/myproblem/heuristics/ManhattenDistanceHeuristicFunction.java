package aima.core.environment.myproblem.heuristics;

import aima.core.search.framework.HeuristicFunction;
import domain.Point;
import domain.TwoDimensionalMap;

public final class ManhattenDistanceHeuristicFunction implements HeuristicFunction
{
    @Override
    public double h(Object state)
    {
        final TwoDimensionalMap board = (TwoDimensionalMap) state;
        final Point actor = board.getActor();
        final Point finish = board.getFinish();

        final double cost = 1d;
        return cost * (Math.abs(actor.getX() - finish.getX()) + Math.abs(actor.getY() - finish.getY()));
    }
}