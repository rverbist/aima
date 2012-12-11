package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TwoDimensionalMapFactory
{
    private TwoDimensionalMapFactory()
    {
    }

    public static final Random rng = new Random();
    public static final double OBSTACLE_DENSITY_PCT = 0.25d;
    
    public static final TwoDimensionalMap createAssignmentMap(int size)
    {
        System.out.printf("TwoDimensionalMapFactory: allocating memory for dimension %s ...", size);
        final TwoDimensionalSpace space = createSpace(size);
        System.out.printf("done%n");
        
        System.out.printf("TwoDimensionalMapFactory: generating random map ...");
        final TwoDimensionalMap map = new TwoDimensionalMap(space);
        final Point finish = map.getFinish();
        final Point start = map.getStart();
        for(int column = 0; column < space.getColumns() - 1; column++)
        {
            space.setValue(finish.getX() + 1, column, TwoDimensionalSpace.OBSTACLE);
        }
        for(int row = finish.getX() + 1; row < space.getRows() - 1; row++)
        {
            space.setValue(row, space.getColumns() - 2, TwoDimensionalSpace.OBSTACLE);
        }
        space.setValue(start.getX(), space.getColumns() - 3, TwoDimensionalSpace.OBSTACLE);
        System.out.printf("done%n");
        
        return map;
    }
    
    public static final TwoDimensionalMap create(int size)
    {
        return create(size, OBSTACLE_DENSITY_PCT);
    }

    public static final TwoDimensionalMap create(final int size, final double density)
    {
        System.out.printf("TwoDimensionalMapFactory: allocating memory for dimension %s ...", size);
        final TwoDimensionalSpace space = createSpace(size);
        System.out.printf("done%n");

        System.out.printf("TwoDimensionalMapFactory: generating random map ...");
        final TwoDimensionalMap map = new TwoDimensionalMap(space);
        // i know, abstract factory pattern...
        generateMapSimple(space, map, density);
        //generateMapMedium(space, map, density);
        //generateMapLarge(space, map, density);
        System.out.printf("done%n");
        
        return map;
    }

    private static final TwoDimensionalSpace createSpace(int size)
    {
        if (size < 100)
            return new TwoDimensionalSpaceUsingArrays(size, size);
        return new TwoDimensionalSpaceUsingBits(size, size);
    }
    
    private static void generateMapSimple(final TwoDimensionalSpace space, final TwoDimensionalMap map, final double density)
    {
        final Point start = map.getStart();
        final Point finish = map.getFinish();
        final int obstacles = (int) ((space.getRows() * space.getColumns() - 2) * density);
        int remaining = obstacles;
        while (remaining > 0)
        {
            final Point location = new Point(rng.nextInt(space.getRows()), rng.nextInt(space.getColumns()));
            if (!location.equals(start) && !location.equals(finish) && space.setValue(location, TwoDimensionalSpace.OBSTACLE))
            {
                remaining--;
            }
        }
    }

    @SuppressWarnings("unused")
    private static void generateMapMedium(final TwoDimensionalSpace space, final TwoDimensionalMap map, final double density)
    {
        for (int row = 0; row < space.getRows(); row++)
        {
            for (int column = 0; column < space.getColumns(); column++)
            {
                boolean obstacle = rng.nextDouble() < density;
                space.setValue(new Point(row, column), obstacle ? TwoDimensionalSpace.OBSTACLE : TwoDimensionalSpace.EMPTY);
            }
        }
        space.setValue(map.getStart(), TwoDimensionalSpace.EMPTY);
        space.setValue(map.getFinish(), TwoDimensionalSpace.EMPTY);
    }
    
    @SuppressWarnings("unused")
    private static void generateMapLarge(final TwoDimensionalSpace space, final TwoDimensionalMap map, final double density)
    {
        // one of the requirements to use this algorithm is that the map is a perfect square.
        // so that getRows() == getColumns()
        final int size = space.getRows(); 
        final Point start = map.getStart();
        final Point finish = map.getFinish();
        
        // set up the environment
        final int logical_threads = Runtime.getRuntime().availableProcessors();
        final ExecutorService service = Executors.newFixedThreadPool(logical_threads);
        final List<Callable<Object>> tasks = new ArrayList<Callable<Object>>(logical_threads);

        // divide & conquer
        // each split the problem in partitions of roughly equal size
        // note that we're running into some race conditions with most map implementations
        // this is however negligible due to the sheer size of the map. in order to fix this
        // you have to align the partitions with the _size member of the DataStore class
        // see TwoDimensionalMapUsingBits
        final long partitions = logical_threads;
        final long partitions_size = (size * size) / partitions;
        for (int thread = 0; thread < logical_threads; thread++)
        {
            final long partition_start = partitions_size * thread;
            final long partition_end = Math.min(partition_start + partitions_size - 1, size * size);
            final long partition_size = partition_end - partition_start;
            tasks.add(new Callable<Object>()
            {
                private final Random random = new Random();
                
                @Override
                public Object call() throws Exception
                {
                    // this is the algorithm used by generateMapSimple
                    int obstacles = (int)(partition_size * density);
                    while (obstacles > 0)
                    {
                        // select a random position from the partition
                        final long position = partition_start + random.nextInt((int)(partition_size));
                        final Point location = new Point((int)(position / size), (int)(position % size));
                        if (!location.equals(start) && !location.equals(finish) && space.setValue(location, TwoDimensionalSpace.OBSTACLE))
                        {
                            obstacles--;
                        }
                    }
                    return null;
                }
            });
        }
        try
        {
            service.invokeAll(tasks);
        }
        catch (InterruptedException e)
        {
            // ...
        }
        service.shutdown();
    }

    @SuppressWarnings("unused")
    private static void validate(final TwoDimensionalSpace space)
    {
        int validation = 0;
        for (int row = 0; row < space.getRows(); row++)
        {
            for (int column = 0; column < space.getColumns(); column++)
            {
                validation += space.isValue(row, column, TwoDimensionalSpace.OBSTACLE) ? 1 : 0;
            }
        }
        double count = space.getRows() * space.getColumns();
        double observed_density = validation / count;
        System.out.printf("Observed density: %s%n", observed_density);
    }
}
