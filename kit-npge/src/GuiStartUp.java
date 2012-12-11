import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import domain.TwoDimensionalMap;
import domain.TwoDimensionalMapFactory;

import aima.core.environment.myproblem.heuristics.DiagonalDistanceHeuristicFunction;
import aima.core.environment.myproblem.heuristics.EuclideanDistanceHeuristicFunction;
import aima.core.environment.myproblem.heuristics.ManhattenDistanceHeuristicFunction;
import aima.core.environment.myproblem.heuristics.NoHeuristicFunction;
import aima.core.environment.myproblem.heuristics.SquaredEuclideanDistanceHeuristicFunction;
import aima.core.search.framework.GraphSearch;
import aima.core.search.framework.Search;
import aima.core.search.framework.TreeSearch;
import aima.core.search.informed.AStarSearch;
import aima.core.search.uninformed.BreadthFirstSearch;
import aima.core.search.uninformed.DepthFirstSearch;


public class GuiStartUp
{
    public static void main(String[] args)
    {
        final Model model = new Model();
        model.Strategies.put("AStar (Euclidean)", new AStarSearch(new GraphSearch(), new EuclideanDistanceHeuristicFunction()));
        model.Strategies.put("AStar (Diagonal)", new AStarSearch(new GraphSearch(), new DiagonalDistanceHeuristicFunction()));
        model.Strategies.put("AStar (Manhatten)", new AStarSearch(new GraphSearch(), new ManhattenDistanceHeuristicFunction()));
        model.Strategies.put("AStar (None)", new AStarSearch(new GraphSearch(), new NoHeuristicFunction()));
        model.Strategies.put("AStar (SquaredEuclidean)", new AStarSearch(new GraphSearch(), new SquaredEuclideanDistanceHeuristicFunction()));
        model.Strategies.put("BFS (Graph)", new BreadthFirstSearch(new GraphSearch()));
        model.Strategies.put("BFS (Tree)", new BreadthFirstSearch(new TreeSearch()));
        model.Strategies.put("DFS (Graph)", new DepthFirstSearch(new GraphSearch()));
        new Gui(model);
    }
}

@SuppressWarnings("serial")
class Gui extends JFrame
{
    public Gui(final Model model)
    {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(new GuiPanel(model));
        this.setVisible(true);
        this.setTitle("Kennisintensieve Toepassingen - NPGE - GUI");
        this.pack();
    }
}

class Model
{
    public int MinDimension = 10;
    public int MaxDimension = 10;
    public int Increment = 1;
    public double Obstacles = 0.25d;
    public Map<String, Search> Strategies = new TreeMap<String, Search>();
    public Set<String> SelectedStrategies = new TreeSet<String>();
    
    public Map<String, List<SearchTest.Result>> run()
    {
        final Map<String, Search> strategies = new TreeMap<String, Search>();
        final Map<String, List<SearchTest.Result>> results = new TreeMap<String, List<SearchTest.Result>>();
        final List<String> timeout = new ArrayList<String>();
        boolean solvable = true;
        
        for (final String name : SelectedStrategies)
        {
            strategies.put(name, Strategies.get(name));
            results.put(name, new ArrayList<SearchTest.Result>());
        }
        try
        {
            int dimension = MinDimension;
            while (dimension <= MaxDimension && !strategies.isEmpty())
            {
                final long map_create_start = System.currentTimeMillis();
                final TwoDimensionalMap state = TwoDimensionalMapFactory.createAssignmentMap(dimension);
                //final TwoDimensionalMap state = TwoDimensionalMapFactory.create(dimension, Obstacles);
                final long map_create_end = System.currentTimeMillis();
                System.out.printf("Dimension %s x %s map created in %s ms%n", dimension, dimension, (map_create_end - map_create_start));

                solvable = false;
                final SearchTest test = new SearchTest(dimension, state);

                final ExecutorService service = Executors.newFixedThreadPool(8);
                final List<Callable<SearchTest.Result>> tasks = new ArrayList<Callable<SearchTest.Result>>();
                for (final Map.Entry<String, Search> strategy : strategies.entrySet())
                {
                    tasks.add(test.runAsync(strategy.getKey(), strategy.getValue(), true));
                }
                for (final Future<SearchTest.Result> future : service.invokeAll(tasks))
                {
                    final SearchTest.Result result = future.get();
                    solvable = solvable || result.PathCost != 0;
                    if (solvable)
                    {
                        if (result.ExecutionTime > MaxDimension)
                        {
                            timeout.add(result.Name);
                        }
                        System.out.printf(" > solved using %s in %s ms.%n", result.Name, result.ExecutionTime);
                        results.get(result.Name).add(result);
                    }
                }
                service.shutdown();

                if (solvable)
                {
                    for (final String strategy : timeout)
                    {
                        System.err.printf("Strategy %s timed out!%n", strategy);
                        strategies.remove(strategy);
                    }
                    timeout.clear();
                    dimension += Increment;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return results;
    }
}

@SuppressWarnings("serial")
class GuiPanel extends JPanel
{
    public final Model _model;
    
    public final JTabbedPane _images;
    public final JLabel _lblMinDimension;
    public final JTextField _txtMinDimension;
    public final JLabel _lblMaxDimension;
    public final JTextField _txtMaxDimension;
    public final JLabel _lblIncrDimension;
    public final JTextField _txtIncrDimension;
    public final JLabel _lblObstacles;
    public final JTextField _txtObstacles;
    public final JPanel _strategies;
    public final JButton _btnGo;
    
    public GuiPanel(final Model model)
    {
        _model = model;
        
        _images = new JTabbedPane();
        _images.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        _lblMinDimension = new JLabel("Minimum Dimension");
        _txtMinDimension = new JTextField(String.valueOf(_model.MinDimension));
        _lblMaxDimension = new JLabel("Maximum Dimension");
        _txtMaxDimension = new JTextField(String.valueOf(_model.MaxDimension));
        _lblIncrDimension = new JLabel("Increment by");
        _txtIncrDimension = new JTextField(String.valueOf(_model.Increment));
        _lblObstacles = new JLabel("Obstacles");
        _txtObstacles = new JTextField(String.valueOf(_model.Obstacles));
        _strategies = new JPanel();
        _strategies.setLayout(new GridLayout(0, 1));
        for(final String strategy : _model.Strategies.keySet())
        {
            final JCheckBox chkStrategy = new JCheckBox(strategy);
            chkStrategy.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    boolean selected = chkStrategy.isSelected();
                    if (selected) _model.SelectedStrategies.add(strategy);
                    else _model.SelectedStrategies.remove(strategy);
                }
            });
            _strategies.add(chkStrategy);
        }
        _btnGo = new JButton("Go!");
        _btnGo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                try { _model.MinDimension = Integer.parseInt(_txtMinDimension.getText()); }
                catch(Exception e) { }
                try { _model.MaxDimension = Integer.parseInt(_txtMaxDimension.getText()); }
                catch(Exception e) { }
                try { _model.Increment = Integer.parseInt(_txtIncrDimension.getText()); }
                catch(Exception e) { }
                try { _model.Obstacles = Double.parseDouble(_txtObstacles.getText()); }
                catch(Exception e) { }
                
                final Map<String, List<SearchTest.Result>> results = _model.run();
                _images.removeAll();
                for(final Map.Entry<String, List<SearchTest.Result>> resultGroup : results.entrySet())
                {
                    for(final SearchTest.Result result : resultGroup.getValue())
                    {
                        _images.addTab(resultGroup.getKey(), new ResultPanel(result));
                    }
                }
            }
        });
        
        final GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(_lblMinDimension, 0, GroupLayout.PREFERRED_SIZE, 150)
                    .addComponent(_txtMinDimension, 0, GroupLayout.PREFERRED_SIZE, 150)
                    .addComponent(_lblMaxDimension, 0, GroupLayout.PREFERRED_SIZE, 150)
                    .addComponent(_txtMaxDimension, 0, GroupLayout.PREFERRED_SIZE, 150)
                    .addComponent(_lblIncrDimension, 0, GroupLayout.PREFERRED_SIZE, 150)
                    .addComponent(_txtIncrDimension, 0, GroupLayout.PREFERRED_SIZE, 150)
                    .addComponent(_lblObstacles, 0, GroupLayout.PREFERRED_SIZE, 150)
                    .addComponent(_txtObstacles, 0, GroupLayout.PREFERRED_SIZE, 150)
                    .addComponent(_strategies, 0, GroupLayout.PREFERRED_SIZE, 150)
                    .addComponent(_btnGo, 0, GroupLayout.PREFERRED_SIZE, 150))
                .addComponent(_images, 900, 900, 900));
        layout.setVerticalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addComponent(_lblMinDimension, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(_txtMinDimension, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(_lblMaxDimension, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(_txtMaxDimension, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(_lblIncrDimension, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(_txtIncrDimension, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(_lblObstacles, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(_txtObstacles, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(_strategies, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(_btnGo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(_images, 900, 900, 900));
        setLayout(layout);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}

@SuppressWarnings("serial")
class ResultPanel extends JPanel
{
    private BufferedImage _graph;
    //private JLabel _image;
    private ImageControl _image;
    
    private final JLabel _lblSize;
    private final JTextField _txtSize;
    private final JLabel _lblExecutionTime;
    private final JTextField _txtExecutionTime;
    private final JLabel _lblMaxQueueSize;
    private final JTextField _txtMaxQueueSize;
    private final JLabel _lblExpandedNodes;
    private final JTextField _txtExpandedNodes;
    private final JLabel _lblPathSize;
    private final JTextField _txtPathSize;
    private final JLabel _lblPathCost;
    private final JTextField _txtPathCost;
    
    public ResultPanel(final SearchTest.Result result)
    {
        _graph = null;
        //_image = new JLabel();
        try
        {
            _graph = ImageIO.read(new File(result.ImagePath));
            _image = new ImageControl(_graph);
            //_image = new JLabel(new ImageIcon(_graph));
        }
        catch (IOException e)
        {
            //_image = new JLabel("unable to load image");
        }
        
        _lblSize = new JLabel("Size");
        _lblExecutionTime = new JLabel("Execution Time");
        _lblMaxQueueSize = new JLabel("Max Queue Size");
        _lblExpandedNodes = new JLabel("Expanded Nodes");
        _lblPathSize = new JLabel("Path Size");
        _lblPathCost = new JLabel("Path Cost");
        
        _txtSize = new JTextField("" + result.Size);
        _txtSize.setHorizontalAlignment(JTextField.CENTER);
        _txtExecutionTime = new JTextField("" + result.ExecutionTime);
        _txtExecutionTime.setHorizontalAlignment(JTextField.CENTER);
        _txtMaxQueueSize = new JTextField("" + result.MaxQueueSize);
        _txtMaxQueueSize.setHorizontalAlignment(JTextField.CENTER);
        _txtExpandedNodes = new JTextField("" + result.ExpandedNodes);
        _txtExpandedNodes.setHorizontalAlignment(JTextField.CENTER);
        _txtPathSize = new JTextField("" + result.PathSize);
        _txtPathSize.setHorizontalAlignment(JTextField.CENTER);
        _txtPathCost = new JTextField("" + result.PathCost);
        _txtPathCost.setHorizontalAlignment(JTextField.CENTER);
        
        final GroupLayout layout = new GroupLayout(this);
        
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup().addComponent(_lblSize).addComponent(_txtSize))
                        .addGroup(layout.createParallelGroup().addComponent(_lblExecutionTime).addComponent(_txtExecutionTime))
                        .addGroup(layout.createParallelGroup().addComponent(_lblMaxQueueSize).addComponent(_txtMaxQueueSize))
                        .addGroup(layout.createParallelGroup().addComponent(_lblExpandedNodes).addComponent(_txtExpandedNodes))
                        .addGroup(layout.createParallelGroup().addComponent(_lblPathSize).addComponent(_txtPathSize))
                        .addGroup(layout.createParallelGroup().addComponent(_lblPathCost).addComponent(_txtPathCost)))
                .addComponent(_image));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(_lblSize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(_txtSize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(_lblExecutionTime, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(_txtExecutionTime, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(_lblMaxQueueSize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(_txtMaxQueueSize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(_lblExpandedNodes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(_txtExpandedNodes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(_lblPathSize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(_txtPathSize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(_lblPathCost, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(_txtPathCost, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addComponent(_image));
        
        this.setLayout(layout);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}

@SuppressWarnings("serial")
class ImageControl extends JComponent
{
    private final BufferedImage _image;
    
    public ImageControl(BufferedImage image)
    {
        _image = image;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        final Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;
        final Image image = _image.getScaledInstance(width, height, Image.SCALE_FAST);
        
        g.setXORMode(java.awt.Color.white);
        g.drawImage(image, x, y, width, height, null);
    }
}