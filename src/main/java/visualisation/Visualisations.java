package visualisation;

import java.util.List;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.VerticalBarPlot;

/**
 * Visualisation Class.
 */
public class Visualisations {

    /**
     * Create a bar chart of the grades.
     *
     * @param gradeValues An ordered list of the grade values to plot.
     */
    public static void createBarChart(List<Integer> gradeValues) {
        // Create a data table
        Table dataTable = Table.create("dataTable");

        // Create the x-axis labels of grades
        StringColumn gradesLabels = StringColumn.create("gradeLabels");
        for (double i = 0.0; i <= 20.0; i += 0.5) {
            gradesLabels.append(String.valueOf(i));
        }

        // Store the data points
        DoubleColumn grades = DoubleColumn.create("gradeValues", gradeValues);

        // Plot x against y
        dataTable.addColumns(gradesLabels, grades);

        // Display the visualisation
        Plot.show(VerticalBarPlot.create("Grade distribution", dataTable, "gradeLabels", "gradeValues"));
    }
}
