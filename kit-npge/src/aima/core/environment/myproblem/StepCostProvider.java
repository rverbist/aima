package aima.core.environment.myproblem;

import aima.core.agent.Action;
import aima.core.search.framework.StepCostFunction;

public final class StepCostProvider implements StepCostFunction
{
    @Override
    public double c(Object s, Action a, Object sDelta)
    {
        final MoveDirectionAction action = (MoveDirectionAction) a;
        
        return action.getValue();
    }
}