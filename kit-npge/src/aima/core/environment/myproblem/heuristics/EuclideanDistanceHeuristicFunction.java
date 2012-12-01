package aima.core.environment.myproblem.heuristics;

import aima.core.search.framework.HeuristicFunction;
import domain.Point;
import domain.TwoDimensionalMap;

public final class EuclideanDistanceHeuristicFunction implements HeuristicFunction
{
    @Override
    public double h(Object state)
    {
        final TwoDimensionalMap board = (TwoDimensionalMap) state;
        final Point actor = board.getActor();
        final Point finish = board.getFinish();

        return Math.sqrt(Math.pow(actor.getX() - finish.getX(), 2) + Math.pow(actor.getY() - finish.getY(), 2));
    }
}