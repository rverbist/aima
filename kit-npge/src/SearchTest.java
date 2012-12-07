import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import aima.core.agent.Action;
import aima.core.environment.myproblem.ActionResultProvider;
import aima.core.environment.myproblem.ActionsProvider;
import aima.core.environment.myproblem.GoalTestProvider;
import aima.core.environment.myproblem.MoveDirectionAction;
import aima.core.environment.myproblem.StepCostProvider;
import aima.core.search.framework.Problem;
import aima.core.search.framework.Search;
import aima.core.search.framework.SearchAgent;
import domain.Point;
import domain.TwoDimensionalMap;
import domain.TwoDimensionalSpace;

public final class SearchTest
{
    private static final String PATH_COST = "pathCost";
    private static final String MAX_QUEUE_SIZE = "maxQueueSize";
    private static final String NODES_EXPANDED = "nodesExpanded";
    
    private final TwoDimensionalMap _state;
    private final int _size;

    public SearchTest(int size, TwoDimensionalMap state)
    {
        _state = state;
        _size = size;
    }

    public Callable<SearchTest.Result> runAsync(final String name, final Search search, final boolean createImage)
    {
        return new Callable<SearchTest.Result>(){
            @Override
            public Result call() throws Exception
            {
                return run(name, search, createImage);
            }
        };
    }
    
    public SearchTest.Result run(final String name, final Search search, final boolean createImage) throws Exception
    {
        final Problem problem = new Problem(_state, new ActionsProvider(), new ActionResultProvider(), new GoalTestProvider(), new StepCostProvider());

        final long tstart = System.currentTimeMillis();
        final SearchAgent agent = new SearchAgent(problem, search);
        final long tend = System.currentTimeMillis();

        final Result result = new Result();
        result.Size = _size;
        result.Name = name;
        result.ExecutionTime = (tend - tstart);
        result.MaxQueueSize = Integer.parseInt(agent.getInstrumentation().get(MAX_QUEUE_SIZE).toString());
        result.ExpandedNodes = Integer.parseInt(agent.getInstrumentation().get(NODES_EXPANDED).toString());
        result.PathCost = Double.parseDouble(agent.getInstrumentation().get(PATH_COST).toString());
        result.PathSize = agent.getActions().size();
        
        if (result.PathSize > 0 && createImage)
        {
            final TwoDimensionalSpace space = _state.getSpace();
            final BufferedImage image = new BufferedImage(_size, _size, BufferedImage.TYPE_INT_ARGB);
            for (int row = 0; row < space.getRows(); row++)
            {
                for (int column = 0; column < space.getColumns(); column++)
                {
                    if (space.isValue(new Point(row, column), TwoDimensionalSpace.OBSTACLE))
                    {
                        image.setRGB(column, row, Color.black.getRGB());
                    }
                    else
                    {
                        image.setRGB(column, row, Color.white.getRGB());
                    }
                }
            }
            Point current = space.getStart();
            for (final Action action : agent.getActions())
            {
                final MoveDirectionAction movement = (MoveDirectionAction) action;
                current = movement.translate(current);
                image.setRGB(current.getY(), current.getX(), Color.red.getRGB());
            }
            result.ImagePath = String.format("N%s-%s-P%.5g.png", result.Size, result.Name, result.PathCost);
            ImageIO.write(image, "png", new File(result.ImagePath));
        }
        return result;
    }

    public final class Result
    {
        public int Size;
        public String Name;
        public long ExecutionTime;
        public int MaxQueueSize;
        public int ExpandedNodes;
        public int PathSize;
        public double PathCost;
        public String ImagePath;
    }
}