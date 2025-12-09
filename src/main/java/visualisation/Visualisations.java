package visualisation;

import java.util.SortedMap;

import javax.swing.JFrame;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.data.statistics.*;

import configuration.UserPreferences;

/**
 * Visualisation Class.
 */
public class Visualisations {
    private final static double[] ST_ANDREWS_CLASS_BOUNDARIES = {0, 4, 7, 10.5, 13.5, 16.5, 20};

    /**
     * Create a bar chart of the grades.
     *
     * @param grades All the grades achieved on this assignment.
     */
    public static void createBarChart(double[] grades) {
        SimpleHistogramDataset dataset = new SimpleHistogramDataset(0);

        for (int i = 0; i < ST_ANDREWS_CLASS_BOUNDARIES.length - 1; i++) {
            double lower = ST_ANDREWS_CLASS_BOUNDARIES[i];
            double upper = ST_ANDREWS_CLASS_BOUNDARIES[i + 1];
            boolean includeLower = (i == 0); // exclusive below (except first bucket)
            boolean includeUpper = true; // inclusive above
            dataset.addBin(new SimpleHistogramBin(lower, upper, includeLower, includeUpper));
        }

        dataset.addObservations(grades);

        // Create the chart
        JFreeChart chart = ChartFactory.createHistogram(
            "Grade distribution",
            "Grade",
            "Number of students",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            false,
            false
        );
        var plot = chart.getXYPlot();

        var x = (NumberAxis) plot.getDomainAxis();
        x.setRange(0, 20);

        var y = plot.getRangeAxis();
        y.setLowerBound(0);
        y.setStandardTickUnits(new NumberTickUnitSource(true));

        if (UserPreferences.isDarkThemeSelected()) {
            StandardChartTheme.createDarknessTheme().apply(chart);
        }
        
        ChartPanel panel = new ChartPanel(chart);

        // Display in a frame
        JFrame frame = new JFrame("Grade distribution");
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
