// =================================================================================================================================
// IMPORTS

import java.util.ArrayList;
import java.util.List;
// =================================================================================================================================



// =================================================================================================================================
/**
 * The initiating class that contains the <code>main()</code> method expected to start the program.  It will read the data provided
 * on students and preceptors, and for a given set of evaluator functions and a search mechanism, initiate the search.
 *
 * @author Scott F. H. Kaplan <sfkaplan@cs.amherst.edu>
 * @version %G%
 */

public class Pairer {
// =================================================================================================================================



    // =============================================================================================================================
    /**
     * The starting point of the program.  Evaluate the command-line arugments.  Then use them to load the provided data for the run,
     * create the evaluators and searcher, and perform the matching.
     *
     * @param args The command-line arguments.
     */

    public static void main (String[] args) {

	// Do we have the right number of arguments to work with?
	if (args.length != 2) {
	    showUsageAndExit();
	}
	
	// Name the arguments.
	String studentsPath            = args[0];
	String preceptorsPath          = args[1];
	
	// Create the data and operators.
	List<Student>   allStudents   = Student.read(studentsPath);
	List<Preceptor> allPreceptors = Preceptor.read(preceptorsPath);
		
	// Cull the students and preceptors of those who cannot be matched (due to insufficient information).
	List<Student> students = new ArrayList<Student>();
	for (Student student : allStudents) {
	    if (student.pairable()) {
		students.add(student);
	    } else {
		Utility.warning("Removing student from matching matrix: " + student.getName());
	    }
	}

	List<Preceptor> preceptors = new ArrayList<Preceptor>();
	for (Preceptor preceptor : allPreceptors) {
	    if (preceptor.pairable()) {
		preceptors.add(preceptor);
	    } else {
		Utility.warning("Removing preceptor from matching matrix: " + preceptor.getName());
	    }
	}

	// Make a cost matrix.
	double[][] costs = new double[students.size()][preceptors.size()];
	for (int i = 0; i < costs.length; i += 1) {
	    for (int j = 0; j < costs[0].length; j += 1) {

		// Because our matching algorithm seeks to minimize costs, we take the inverse to make lower scores better.
		costs[i][j] = 1 / students.get(i).cross(preceptors.get(j));

	    }
	}

	// Match and emit the results.
	HungarianAlgorithm matcher = new HungarianAlgorithm(costs);
	int[] matches = matcher.execute();
	for (int i = 0; i < matches.length; i += 1) {
	    String preceptorName = "None";
	    if (matches[i] != -1) {
		preceptorName = preceptors.get(matches[i]).getName();
	    }
	    System.out.printf("%30s\t%30s\n", students.get(i).getName(), preceptorName);
	}
		
    } // main
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Display how the program should be invoked and then exit.
     *
     * @param executablePath The pathname used to attempt to run the program.
     */

    private static void showUsageAndExit () {

	System.err.printf("USAGE: java Pairer <student list pathname>\n" +
			  "                   <preceptor list pathname>\n");
	System.exit(1);

    } // showUsageAndExit
    // =============================================================================================================================



// =================================================================================================================================
} // Pairer
// =================================================================================================================================
