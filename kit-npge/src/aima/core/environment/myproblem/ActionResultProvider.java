package aima.core.environment.myproblem;

import aima.core.agent.Action;
import aima.core.search.framework.ResultFunction;
import domain.Point;
import domain.TwoDimensionalMap;

public final class ActionResultProvider implements ResultFunction
{    
    @Override
    public Object result(Object s, Action a)
    {
        // cast the arguments to the concrete types that are
        // used to model this problem
        final TwoDimensionalMap board = (TwoDimensionalMap) s;
        final MoveDirectionAction action = (MoveDirectionAction) a;
        
        // apply the movement action by translating the actor
        // location, resulting in the new state
        final Point future = action.translate(board.getActor());
        final TwoDimensionalMap result = board.setActor(future);
        return result;
    }
}