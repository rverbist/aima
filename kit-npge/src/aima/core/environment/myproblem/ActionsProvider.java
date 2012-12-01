package aima.core.environment.myproblem;

import java.util.LinkedHashSet;
import java.util.Set;

import aima.core.agent.Action;
import aima.core.search.framework.ActionsFunction;
import domain.*;

public final class ActionsProvider implements ActionsFunction
{
    @Override
    public Set<Action> actions(Object s)
    {
        // cast the arguments to the concrete types that are
        // used to model this problem
        final TwoDimensionalMap board = (TwoDimensionalMap) s;

        final Set<Action> actions = new LinkedHashSet<Action>();
        for (final Direction direction : Direction.values())
        {
            // create a new movement action for each adjacent
            // location that can be traversed
            final Point future = direction.translate(board.getActor());
            if (board.isTraversable(future))
            {
                actions.add(new MoveDirectionAction(direction));
            }
        }
        return actions;
    }
}