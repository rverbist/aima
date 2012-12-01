package aima.core.environment.myproblem.heuristics;

import aima.core.search.framework.HeuristicFunction;
import domain.Point;
import domain.TwoDimensionalMap;

public final class DiagonalDistanceHeuristicFunction implements HeuristicFunction
{
    private static final double straight_cost = 1d;
    private static final double diagonal_cost = Math.sqrt(2);

    @Override
    public double h(Object state)
    {
        final TwoDimensionalMap board = (TwoDimensionalMap) state;
        final Point actor = board.getActor();
        final Point finish = board.getFinish();

        final int dx = Math.abs(actor.getX() - finish.getX()); // row distance delta
        final int dy = Math.abs(actor.getY() - finish.getY()); // column distance delta

        final int diagonal_steps = Math.min(dx, dy); // number of diagonal steps to goal
        final int straight_steps = dx + dy; // number of straight steps to goal

        return diagonal_cost * diagonal_steps
                // substract 2 steps for each diagonal step
                + straight_cost * (straight_steps - 2 * diagonal_steps);

        // final double cost = Math.sqrt(2);
        // return cost * Math.max(Math.abs(actor.getX() - finish.getX()),
        // Math.abs(actor.getY() - finish.getY()));
    }
}