package aima.core.environment.myproblem.heuristics;

import aima.core.search.framework.HeuristicFunction;

public final class NoHeuristicFunction implements HeuristicFunction
{
    @Override
    public double h(Object state)
    {
        return 0;
    }

}
