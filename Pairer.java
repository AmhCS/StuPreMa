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
	if (args.length != 3) {
	    showUsageAndExit();
	}
	
	// Name the arguments.
	String studentsPath   = args[0];
	String preceptorsPath = args[1];
	String outputType     = args[2];
	
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
		    if (preceptor.hasPreMatch() && student.preMatch().equals(preceptor.getName(false))) {
			// Should be a match; verify.
			Utility.abortIfFalse(preceptor.preMatch().equals(student.getName(false)),
					     "Student (" + student.getName() + ") matched to " + student.preMatch() +
					     ", but preceptor (" + preceptor.getName() + ") is matched to " + preceptor.preMatch());
			student.match(preceptor);
			preceptors.remove(preceptor);
			preMatchedStudents.add(student);
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

	// Unify the lists of students and then emit their matching results.
	students.addAll(preMatchedStudents);
	if (outputType.equalsIgnoreCase("Readable")) {
	    emitReadable(students);
	} else if (outputType.equalsIgnoreCase("CSV")) {
	    emitCSV(students);
	} else {
	    Utility.abort("Unknown output type:" + outputType);
	}

    } // main ()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Write (to <code>stdout</code>) the list of students and the preceptor to which each is matched.  Show whether each student is
     * unpatched, pre-matched, or algorithmically matched.  The format written is meant for easy human reading.
     *
     * @param students The list of <code>Student</code>s, each of which has information on the <code>Preceptor</code> to whom a
     *                 match was found (if any).
     */
    private static void emitReadable (List<Student> students) {

	// Emit the pairings.
	for (Student student : students) {
	    String studentName = student.getName();
	    String preceptorName;
	    String matchType;
	    if (student.matched()) {
		if (student.hasPreMatch()) {
		    matchType = "pre";
		} else {
		    matchType = "alg";
		}
		preceptorName = student.getMatch().getName();
	    } else {
		matchType = "unm";
		preceptorName = "None";
	    }
	    System.out.printf("[%s]%40s\t%40s\n", matchType, studentName, preceptorName);
	}

    } // emitReadable ()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Write (to <code>stdout</code>) the list of students and the preceptor to which each is matched.  Show whether each student is
     * unpatched, pre-matched, or algorithmically matched.  The format written is a semicolon-delimited CSV file, intended to be
     * imported into a spreadsheet application.
     *
     * @param students The list of <code>Student</code>s, each of which has information on the <code>Preceptor</code> to whom a
     *                 match was found (if any).
     */
    private static void emitCSV (List<Student> students) {

	// Emit the pairings, showing the following fields for each:
	//   0. Student name
	//   1. Match type (pre/alg/unm)
	//   2. Preceptor name
	//   3. Preceptor location
	//   4. Practice type
	//   5. Day of the week
	System.out.printf("STUDENT NAME;MATCH TYPE;PRECEPTOR NAME;LOCATION;PRACTICE TYPE;MEETING DAY\n");
	for (Student student : students) {
	    String studentName = student.getName();
	    String preceptorName;
	    String preceptorLocation;
	    String practiceType;
	    String dayOfWeek;
	    String matchType;
	    if (student.matched()) {
		Preceptor preceptor = student.getMatch();
		if (student.hasPreMatch()) {
		    matchType     = "pre";
		} else {
		    matchType     = "alg";
		}	
		preceptorName     = preceptor.getName();
		preceptorLocation = preceptor.location();
		practiceType      = preceptor.practiceType();
		dayOfWeek         = preceptor.preferredDay();
	    } else {
		matchType         = "unm";
		preceptorName     = "None";
		preceptorLocation = "N/A";
		practiceType      = "N/A";
		dayOfWeek         = "N/A";
	    }
	    System.out.printf("\"%s\";%s;\"%s\";%s;%s;%s\n",
			      studentName,
			      matchType,
			      preceptorName,
			      preceptorLocation,
			      practiceType,
			      dayOfWeek);
	}

    } // emitCSV ()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Display how the program should be invoked and then exit.
     *
     * @param executablePath The pathname used to attempt to run the program.
     */

    private static void showUsageAndExit () {

	System.err.printf("USAGE: java Pairer <student list pathname>\n" +
			  "                   <preceptor list pathname>\n" +
			  "                   <output format [Readable|CSV]>\n");
	System.exit(1);

    } // showUsageAndExit ()
    // =============================================================================================================================



// =================================================================================================================================
} // Pairer
// =================================================================================================================================
