import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import aima.core.environment.myproblem.heuristics.DiagonalDistanceHeuristicFunction;
import aima.core.environment.myproblem.heuristics.EuclideanDistanceHeuristicFunction;
import aima.core.environment.myproblem.heuristics.ManhattenDistanceHeuristicFunction;
import aima.core.environment.myproblem.heuristics.NoHeuristicFunction;
import aima.core.environment.myproblem.heuristics.SquaredEuclideanDistanceHeuristicFunction;
import aima.core.search.framework.GraphSearch;
import aima.core.search.framework.Search;
import aima.core.search.informed.AStarSearch;
import aima.core.search.uninformed.BreadthFirstSearch;
import aima.core.search.uninformed.DepthFirstSearch;
import domain.TwoDimensionalMap;
import domain.TwoDimensionalMapFactory;

public class StartUp
{
    private static final ExecutorService service = Executors.newFixedThreadPool(8);

    public static void main(String[] args)
    {
        final Map<String, Search> strategies = new TreeMap<String, Search>();
        final Map<String, List<SearchTest.Result>> results = new TreeMap<String, List<SearchTest.Result>>();
        final List<String> timeout = new ArrayList<String>();
        boolean solvable = true;

        /*
         * search parameters, feel free to edit these
         */
        final int MIN_DIMENSION = 1000;
        final int MAX_DIMENSION = 1000;
        final int INC_DIMENSION = 20;
        final int MAX_DURATION = 2500;
        final double OBSTACLE_DENSITY = 0.25d;
        final boolean CREATE_IMAGE = true; // slow!
        strategies.put("AStar (Euclidean)", new AStarSearch(new GraphSearch(), new EuclideanDistanceHeuristicFunction()));
        strategies.put("AStar (Diagonal)", new AStarSearch(new GraphSearch(), new DiagonalDistanceHeuristicFunction()));
        strategies.put("AStar (Manhatten)", new AStarSearch(new GraphSearch(), new ManhattenDistanceHeuristicFunction()));
        strategies.put("AStar (None)", new AStarSearch(new GraphSearch(), new NoHeuristicFunction()));
        strategies.put("AStar (SquaredEuclidean)", new AStarSearch(new GraphSearch(), new SquaredEuclideanDistanceHeuristicFunction()));
        strategies.put("BFS (Graph)", new BreadthFirstSearch(new GraphSearch()));
        //strategies.put("BFS (Tree)", new BreadthFirstSearch(new TreeSearch()));
        strategies.put("DFS (Graph)", new DepthFirstSearch(new GraphSearch()));

        // create result mapping based on selected search strategies
        for (final Map.Entry<String, Search> strategy : strategies.entrySet())
        {
            results.put(strategy.getKey(), new ArrayList<SearchTest.Result>());
        }
        try
        {
            /*
             * iterate every dimension in the range until either the maximum
             * dimension is reached or all search strategies have timed out.
             */
            int dimension = MIN_DIMENSION;
            while (dimension <= MAX_DIMENSION && !strategies.isEmpty())
            {
                /*
                 * prepare a map (the initial state) for this dimension
                 */
                final long map_create_start = System.currentTimeMillis();
                final TwoDimensionalMap state = TwoDimensionalMapFactory.create(dimension, OBSTACLE_DENSITY);
                final long map_create_end = System.currentTimeMillis();
                System.out.printf("Dimension %s x %s map created in %s ms%n", dimension, dimension, (map_create_end - map_create_start));

                // mark the current map as unsolvable, once one of the
                // algorithms has
                // found a solution, it will be marked as solvable. If no
                // solution is found
                // the dimension won't be increased and a new map will be
                // created.
                solvable = false;

                /*
                 * create the test context for this dimension
                 */
                final SearchTest test = new SearchTest(dimension, state);

                /*
                 * run the test (synchronously)
                 */
                /*
                 * for (final Map.Entry<String, Search> strategy :
                 * strategies.entrySet()) { final SearchTest.Result result =
                 * test.run(strategy.getKey(), strategy.getValue(),
                 * CREATE_IMAGE); solvable = solvable || result.PathCost != 0;
                 * if (solvable) { // mark algorithms that take longer than
                 * MAX_DURATION as timed out if (result.ExecutionTime >
                 * MAX_DURATION) { timeout.add(result.Name); }
                 * System.out.printf(" > solved using %s in %s ms.%n",
                 * result.Name, result.ExecutionTime);
                 * results.get(result.Name).add(result); } }
                 */

                /*
                 * run the test (asynchronously)
                 */
                final List<Callable<SearchTest.Result>> tasks = new ArrayList<Callable<SearchTest.Result>>();
                for (final Map.Entry<String, Search> strategy : strategies.entrySet())
                {
                    tasks.add(test.runAsync(strategy.getKey(), strategy.getValue(), CREATE_IMAGE));
                }
                for (final Future<SearchTest.Result> future : service.invokeAll(tasks))
                {
                    final SearchTest.Result result = future.get();
                    solvable = solvable || result.PathCost != 0;
                    if (solvable)
                    {
                        // mark algorithms that take longer than MAX_DURATION as
                        // timed out
                        if (result.ExecutionTime > MAX_DURATION)
                        {
                            timeout.add(result.Name);
                        }
                        System.out.printf(" > solved using %s in %s ms.%n", result.Name, result.ExecutionTime);
                        results.get(result.Name).add(result);
                    }
                }

                if (solvable)
                {
                    /*
                     * remove strategies that have timed out
                     */
                    for (final String strategy : timeout)
                    {
                        System.err.printf("Strategy %s timed out!%n", strategy);
                        strategies.remove(strategy);
                    }
                    timeout.clear();

                    /*
                     * increase the dimension
                     */
                    dimension += INC_DIMENSION;
                }
            }

            /*
             * output the results
             */
            for (final Map.Entry<String, List<SearchTest.Result>> resultSet : results.entrySet())
            {
                if (!resultSet.getValue().isEmpty())
                {
                    final String file = String.format("%s.txt", resultSet.getKey());
                    System.out.printf("Writing file %s ... ", file);

                    final FileOutputStream stream = new FileOutputStream(file);
                    final OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");

                    writer.write(String.format("%s;%s;%s;%s;%s;%s\r\n", "Size", "Execution Time", "Max Queue Size", "Expanded Nodes", "Path Cost", "Path Size"));
                    for (final SearchTest.Result result : resultSet.getValue())
                    {
                        writer.write(String.format("%s;%s;%s;%s;%s;%s\r\n", result.Size, result.ExecutionTime, result.MaxQueueSize, result.ExpandedNodes,
                                String.valueOf(result.PathCost).replace('.', ','), result.PathSize));
                    }

                    writer.close();
                    stream.close();
                    System.out.printf("Done%n");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        service.shutdown();
    }
}
