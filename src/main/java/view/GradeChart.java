package view;

import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberTickUnitSource;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import configuration.UserPreferences;

/**
 * Methods for displaying the assignment's grade distribution as a bar chart.
 */
public class GradeChart {
    private static final double PADDING = 0.1;
    private static final double MINIMUM_GRADE = 0.0;
    private static final double MAXIMUM_GRADE = 20.0;

    /**
     * Create a bar chart of the grades.
     *
     * @param grades All the grades achieved on this assignment.
     */
    public static void showGradeDistribution(String title, double[] grades) {
        // Calculate categories: just ints, or ints-and-halves if any half grades appear
        double step = allIntegerGrades(grades) ? 1.0 : 0.5;

        // Construct the dataset
        CategoryDataset dataset = createDataset(grades, step);

        // Create the chart
        JFreeChart chart = createChart(title, dataset);

        // Arrange in a panel and increase width to show all labels
        ChartPanel panel = new ChartPanel(chart);
        Dimension preferred = panel.getPreferredSize();
        panel.setPreferredSize(new Dimension(preferred.width * 5 / 4, preferred.height));

        // Display in a new window
        JFrame frame = new JFrame("Grade distribution");
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /* Are all these grades whole integer values? */
    private static boolean allIntegerGrades(double[] grades) {
        return Arrays.stream(grades).allMatch(g -> g % 1 == 0);
    }

    private static CategoryDataset createDataset(double[] grades, double step) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series = "Grades"; // not shown

        // Create all "bars" at zero
        for (double grade = MINIMUM_GRADE; grade <= MAXIMUM_GRADE; grade += step) {
            dataset.addValue((Number) 0, series, gradeString(grade));
        }

        // Add each student to a bar
        for (double grade : grades) {
            if (dataset.getValue(series, gradeString(grade)) != null) {
                dataset.incrementValue(1, series, gradeString(grade));
            }
        }
        
        return dataset;
    }

    private static String gradeString(double grade) {
        // Show grade as whole numbers and half-symbols
        if (grade % 1 == 0) {
            return Integer.toString((int) grade);
        } else if (grade == 0.5) {
            return "½";
        } else if (grade % 1 == 0.5) {
            return Integer.toString((int) Math.floor(grade)) + "½";
        }
        return Double.toString(grade);
    }

    private static JFreeChart createChart(String title, CategoryDataset dataset) {
        // Create the chart
        JFreeChart chart = ChartFactory.createBarChart(
            "Grade distribution for " + title,
            "Grade",
            "Number of students",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            false,
            false
        );

        // Improve appearance
        var plot = chart.getCategoryPlot();
        var x = plot.getDomainAxis();
        double outerMargin = 2 * PADDING / dataset.getColumnCount();
        x.setLowerMargin(outerMargin);
        x.setUpperMargin(outerMargin);
        x.setCategoryMargin(PADDING);

        // Show frequencies as integers
        var y = plot.getRangeAxis();
        y.setStandardTickUnits(new NumberTickUnitSource(true));

        // Use dark theme if appropriate
        if (UserPreferences.isDarkThemeSelected()) {
            StandardChartTheme.createDarknessTheme().apply(chart);
        }

        return chart;
    }
}
