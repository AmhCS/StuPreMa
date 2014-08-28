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
	List<Student>   students   = Student.read(studentsPath);
	List<Preceptor> preceptors = Preceptor.read(preceptorsPath);
		
	// Cull the students and preceptors of those who cannot be matched (due to insufficient information).
	List<Student> filteredStudents = new ArrayList<Student>();
	for (Student student : students) {
	    if (student.pairable()) {
		filteredStudents.add(student);
	    } else {
		Utility.warning("Removing student from matching matrix: " + student.getName());
	    }
	}
	students = filteredStudents;

	List<Preceptor> filteredPreceptors = new ArrayList<Preceptor>();
	for (Preceptor preceptor : preceptors) {
	    if (preceptor.pairable()) {
		filteredPreceptors.add(preceptor);
	    } else {
		Utility.warning("Removing preceptor from matching matrix: " + preceptor.getName());
	    }
	}
	preceptors = filteredPreceptors;

	// Cull the students and preceptors already matched to one another.  Verify that matching.
	List<Student> preMatchedStudents = new ArrayList<Student>();
	filteredStudents = new ArrayList<Student>();
	for (Student student : students) {

	    if (student.hasPreMatch()) {

		for (Preceptor preceptor : preceptors) {
		    if (preceptor.hasPreMatch() && student.preMatch().equals(preceptor.getName())) {
			Utility.abortIfFalse(preceptor.hasPreMatch() && preceptor.preMatch().equals(student.getName()),
					     "Student (" + student.getName() + ") matched to " + student.preMatch() +
					     ", but preceptor does not match back.");
			student.match(preceptor);
			preceptors.remove(preceptor);
			break;
		    }
		}
		Utility.abortIfFalse(student.matched(), ("Student (" + student.getName() + ") prematched to " +
							 student.preMatch() + ", but no such preceptor found."));

	    } else {

		// A non-pre-matched student is still in the running to be paired algorithmically.
		filteredStudents.add(student);

	    }

	}
	students = filteredStudents;

	// Make a cost matrix.
	double[][] costs = new double[students.size()][preceptors.size()];
	for (int i = 0; i < students.size(); i += 1) {
	    for (int j = 0; j < preceptors.size(); j += 1) {

		// Because our matching algorithm seeks to minimize costs, we take the inverse to make lower scores better.
		costs[i][j] = 1 / students.get(i).cross(preceptors.get(j));

	    }
	}

	// Pair remaining students to preceptors based on the results.
	HungarianAlgorithm matcher = new HungarianAlgorithm(costs);
	int[] matches = matcher.execute();
	for (int i = 0; i < matches.length; i += 1) {

	    if (matches[i] != -1) {
		Student student = students.get(i);
		Preceptor preceptor = preceptors.get(matches[i]);
		student.match(preceptor);
	    }

	}

	// Emit the pairings, printing first the pre-matched ones, and then the matched ones.
	for (Student student : preMatchedStudents) {
	    String studentName = student.getName();
	    Utility.abortIfFalse(student.matched(), "In pre-matched list, unexpectedly unmatched student: " + studentName);
	    String preceptorName = student.getMatch().getName();
	    System.out.printf("[pre]%30s\t%30s\n", studentName, preceptorName);
	}
	for (Student student : students) {
	    String studentName = student.getName();
	    if (student.matched()) {
		String preceptorName = student.getMatch().getName();
		System.out.printf("[alg]%30s\t%30s\n", studentName, preceptorName);
	    } else {
		System.out.printf("[unm]%30s\tNone\n", studentName);
	    }
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
