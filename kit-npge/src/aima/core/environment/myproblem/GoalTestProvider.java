package aima.core.environment.myproblem;

import domain.TwoDimensionalMap;

public final class GoalTestProvider implements aima.core.search.framework.GoalTest
{
    @Override
    public boolean isGoalState(Object state)
    {
        // the goal state is a state where the actor location
        // equals the finish location
        final TwoDimensionalMap board = (TwoDimensionalMap) state;
        
        return board.getActor().equals(board.getFinish());
    }
}