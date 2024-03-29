// =================================================================================================================================
/**
 * The initiating class that contains the <code>main()</code> method expected to start the program.  It will read the data provided
 * on students and preceptors, and for a given set of evaluator functions and a search mechanism, initiate the search.
 *
 * @author Scott F. H. Kaplan <sfkaplan@cs.amherst.edu>
 * @version %G%
 */

public class Utility {
// =================================================================================================================================


	
    // =============================================================================================================================
    private static final int _debug = 1;
    // =============================================================================================================================



    // =============================================================================================================================
    public static void abort (String msg) {
		
	System.err.println("ABORT: " + msg);
	System.exit(1);
		
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public static void abortIfFalse (boolean test, String msg) {
		
	if (!test) {
	    abort(msg);
	}
		
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public static void warning (String message) {

	System.err.println("WARNING: " + message);

    }	
    // =============================================================================================================================



    // =============================================================================================================================
    public static void debug (int level, String message) {

	if (level <= _debug) {
	    System.err.println("DEBUG:\t" + message);
	}

    }
    // =============================================================================================================================

	
	
// =================================================================================================================================
} // class Utility
// =================================================================================================================================
