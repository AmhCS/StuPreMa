// =================================================================================================================================
// IMPORTS

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
// =================================================================================================================================



// =================================================================================================================================
/**
 * A single student.  This object represents both the individual and that person's traits and preferences.
 * 
 * @author Scott F. H. Kaplan <sfkaplan@cs.amherst.edu>
 * @version %G%
 */
public class Student {
// =================================================================================================================================



    // =============================================================================================================================
    // DATA MEMBERS
    
    /** The last name of the student. */
    private String  _lastName;

    /** The first name of the student. */
    private String  _firstName;

    /**
     * Whether the student is female (<code>true</code>) or male (<code>false</code>).
     * @see _GENDER_MALE
     * @see _GENDER_FEMALE
     */
    private boolean _isFemale;

    /** Whether the student speaks Spanish capably. */
    private boolean _speaksSpanish;

    /**
     * Among a set of possible practice types, a rank ordering of their desirability.  Each entry in the array correlates to one
     * particular practice type (e.g., geriatrics, family practice); the value in an entry indicates its rank, where <code>1</code>
     * is most desirable.
     * @see _PEDIATRICIAN_INDEX
     * @see _FAMILY_PRACTITIONER_INDEX
     * @see _INTERNISTS_INDEX
     * @see _GERIATRICIAN_INDEX
     * @see _RURAL_SETTING_INDEX
     * @see _SUBURBAN_SETTING_INDEX
     * @see _URBAN_SETTING_INDEX
     * @see _UNDERSERVED_INDEX
     * @see Preceptor._rankMask
     */
    private int[]   _practiceRanks;

    /** The location of the student's home. */
    private String  _home;

    /** The preceptor with whom this student has been pre-matched (if any). */
    private String _preMatch;

    /** The <code>Preceptor</code> to whom this student is matched (if any). */
    private Preceptor _preceptor;

    /**
     * Whether sufficient information for the fields above is provided to properly match this student with a <code>Preceptor</code>.
     * @see Student.cross
     */
    private boolean _sufficientForMatching;

    /** A constant that, when compared with <code>_isFemale</code>, indicates whether the student is male. */
    private static final boolean _GENDER_MALE   = false;

    /** A constant that, when compared with <code>_isFemale</code>, indicates whether the student is female. */
    private static final boolean _GENDER_FEMALE = true;

    private static final int _LAST_NAME_INDEX           = 0;
    private static final int _FIRST_NAME_INDEX          = 1;
    private static final int _GENDER_INDEX              = 2;
    private static final int _PEDIATRICIAN_INDEX        = 3;
    private static final int _FAMILY_PRACTITIONER_INDEX = 4;
    private static final int _INTERNISTS_INDEX          = 5;
    private static final int _GERIATRICIAN_INDEX        = 6;
    private static final int _RURAL_SETTING_INDEX       = 7;
    private static final int _SUBURBAN_SETTING_INDEX    = 8;
    private static final int _URBAN_SETTING_INDEX       = 9;
    private static final int _UNDERSERVED_INDEX         = 10;
    private static final int _LANGUAGES_INDEX           = 11;
    private static final int _LIVING_LOCATION_INDEX     = 12;
    private static final int _COMMENTS_INDEX            = 13;
    private static final int _PRE_MATCHED_INDEX         = 14;
    private static final int _numberFields              = 15;

    private static final int _BEGIN_PRACTICE_RANK_INDEX = _PEDIATRICIAN_INDEX;
    private static final int _END_PRACTICE_RANK_INDEX   = _UNDERSERVED_INDEX + 1;

    // These are public because they need to be used by the Preceptor class as well.  SFHK: Ideally, it seems clear that the ranking
    // vectors and masks should be contained in a class.  Refactor some day.
    public static final int _PEDIATRICIAN_RANK_INDEX        = _PEDIATRICIAN_INDEX        - _BEGIN_PRACTICE_RANK_INDEX;
    public static final int _FAMILY_PRACTITIONER_RANK_INDEX = _FAMILY_PRACTITIONER_INDEX - _BEGIN_PRACTICE_RANK_INDEX;
    public static final int _INTERNISTS_RANK_INDEX          = _INTERNISTS_INDEX          - _BEGIN_PRACTICE_RANK_INDEX;
    public static final int _GERIATRICIAN_RANK_INDEX        = _GERIATRICIAN_INDEX        - _BEGIN_PRACTICE_RANK_INDEX;
    public static final int _RURAL_SETTING_RANK_INDEX       = _RURAL_SETTING_INDEX       - _BEGIN_PRACTICE_RANK_INDEX;
    public static final int _SUBURBAN_SETTING_RANK_INDEX    = _SUBURBAN_SETTING_INDEX    - _BEGIN_PRACTICE_RANK_INDEX;
    public static final int _URBAN_SETTING_RANK_INDEX       = _URBAN_SETTING_INDEX       - _BEGIN_PRACTICE_RANK_INDEX;
    public static final int _UNDERSERVED_RANK_INDEX         = _UNDERSERVED_INDEX         - _BEGIN_PRACTICE_RANK_INDEX;
    public static final int _numberPracticeFields           = _END_PRACTICE_RANK_INDEX   - _BEGIN_PRACTICE_RANK_INDEX;

    private static final double _RANKS_WEIGHT   = 0.5;
    private static final double _GENDER_WEIGHT  = 0.25;
    private static final double _SPEAKSSPANISH_WEIGHT = 0.25;

    /**
     * A collection of case-insensitive strings that unambiguously indicate a male student.
     *
     * @see Student.parseGender
     */
    private static final String[] _MALE_TEXTS = { "m",
						  "male",
						  "man",
						  "men"
    };

    /**
     * A collection of case-insensitive strings that unambiguously indicate a male student.
     *
     * @see Student.parseGender
     */
    private static final String[] _FEMALE_TEXTS = { "f",
						    "female",
						    "woman",
						    "women"
    };
    // =============================================================================================================================

	
	
    // =============================================================================================================================
    /**
     * Constructor from a CSV record read as a single line of text.
     * 
     * @param record The text of the complete, unparsed CSV record.
     */
    public Student (String record) {
		
	// Split the record into its parts.  Assume semi-colon delimiters (since comments are likely to contain commas).  Trim the
	// results.
	String[] fields = record.split(";", -1);
	Utility.abortIfFalse(fields.length >= _numberFields, ("Student.Student(string): " +
							      "Record had the insufficent fields (" +
							      fields.length +
							      " must be at least " +
							      _numberFields +
							      "):\n  " +
							      record +
							      "\n"));
	for (int i = 0; i < fields.length; i += 1) {
	    fields[i] = fields[i].trim();
	}

	// Parse the fields, constructing the profile of the student.  If any one of the attempts to parse the given data is a
	// fundamental failure, emit a warning message and record that insufficient information is available to match this student.
	_lastName      = fields[_LAST_NAME_INDEX];
	_firstName     = fields[_FIRST_NAME_INDEX];
	try {
	    _isFemale      = parseGender(fields[_GENDER_INDEX]);
	    _speaksSpanish = parseLanguages(fields[_LANGUAGES_INDEX]);
	    _practiceRanks = parsePracticeRanks(Arrays.copyOfRange(fields, _BEGIN_PRACTICE_RANK_INDEX, _END_PRACTICE_RANK_INDEX));
	    _preMatch      = parsePreMatch(fields[_PRE_MATCHED_INDEX]);
	    _sufficientForMatching = true;
	} catch (InsufficientDataException e) {
	    Utility.warning(String.format("Unable to read complete profile from record for student %s, %s\n\tMESSAGE: %s",
					  _lastName,
					  _firstName,
					  e.getMessage()));
	    _sufficientForMatching = false;
	}

    } // Student()
    // =============================================================================================================================

	
	
    // =============================================================================================================================
    /**
     * Measure the quality of the match between this vertex and another one.  This method is abstract because the quality of such a
     * match will depend largely on the details of the subclass and how its characteristics will match to another of its type or
     * another type.
     *
     * @param preceptor The preceptor against whose traits a match is to be calculated.
     * @return The quality of the match between this <code>Student</code> and the <code>Preceptor</code>.  Higher scores are better
     *         matches.
     */

    public double cross (Preceptor preceptor) {

	// Crossing should not have been requested unless the student and preceptor are pairable.
	Utility.abortIfFalse(this.pairable() && preceptor.pairable(),
			     String.format("Tried to pair unpairable student ({0}) and preceptor ({1})",
					   this.getName(),
					   preceptor.getName()));

	// The preceptor's mask should be crossed with the inverse of the ranking, since we are aiming for maximization.
	double rankMatchQuality = 0.0;
	for (int i = 0; i < _practiceRanks.length; i += 1) {
	    double maskedValue = (_practiceRanks.length - _practiceRanks[i] + 1) * preceptor.getRankMask(i);
	    rankMatchQuality += maskedValue;
	}

	// If there is no gender preference, then it's a good match.  If there is one and the gender does match, it's an even better
	// match.
	double genderMatchQuality = 0.0;
	if (preceptor.hasGenderPreference()) {
	    if ((preceptor.prefersFemale() && (_isFemale == _GENDER_FEMALE)) ||
		(!preceptor.prefersFemale()) && (_isFemale == _GENDER_MALE)) {
		genderMatchQuality = 1.0;
	    }
	} else {
	    genderMatchQuality = 0.75;
	}

	// If spanish-speaking is not required, it's a good match.  If it is required and provided, it's an even better match.
	double spanishMatchQuality = 0.0;
	if (preceptor.prefersSpanish()) {
	    if (_speaksSpanish) {
		spanishMatchQuality = 1.0;
	    }
	} else {
	    spanishMatchQuality = 0.75;
	}

	// Combine them all with weights.
	double matchQuality = ((rankMatchQuality    * _RANKS_WEIGHT) +
			       (genderMatchQuality  * _GENDER_WEIGHT) +
			       (spanishMatchQuality * _SPEAKSSPANISH_WEIGHT));

	return matchQuality;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Read a delimited list of student characteristics.  Construct each student based on each record, and insert each into a newly
     * made set.
     * 
     * @param path The filename that contains the student records.
     * @return A list of the students read from the given path.
     */
    public static List<Student> read (String path) {
		
	// Open the file for reading.
	FileReader file = null;
	try {
	    file = new FileReader(path);
	} catch (FileNotFoundException e) {
	    Utility.abort("Student.read(): No such file " + path);
	}
	Scanner scanner = new Scanner(file);

	// Read the first line, assuming that it contains field headers.
	Utility.abortIfFalse(scanner.hasNextLine(), "Student.read(): No lines of data!");
	String fieldNames = scanner.nextLine();
		
	// Read and parse the file's records, one at a time, creating a Student from each and adding it to the set of such.
	List<Student> students = new ArrayList<Student>();
	while (scanner.hasNextLine()) {
			
	    String record = scanner.nextLine();
	    Student student = new Student(record);
	    students.add(student);
			
	}
		
	// Clean up and return the set of students.
	try {
	    file.close();
	} catch (IOException e) {
	    Utility.warning("Student.read() failed upon closing path.  Continuing.");
	}
	return students;
		
    } // read()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Given a string provided to indicate the gender of the student, parse that string and store, as a simple boolean, whether the
     * student is female or male.
     *
     * @param genderText A case-insensitive string that represents the gender of the student.  Some effort is made to parse a number
     *                   of different yet unambiguous designations of gender; see the code to determine which are accepted.
     * @return <code>false</code> if the student is male, <code>true</code> if the student is female.
     * @thrown InsufficientDataException when the <code>genderText</code> is not clearly an indication of male or female.
     * @see Student._MALE_TEXTS
     * @see Student._FEMALE_TEXTS
     * @see Student._GENDER_MALE
     * @see Student._GENDER_FEMALE
     */

    private static boolean parseGender (String genderText) throws InsufficientDataException {

	// Does the text indicate a female?
	for (String femaleText : _FEMALE_TEXTS) {
	    if (femaleText.equalsIgnoreCase(genderText)) {
		return _GENDER_FEMALE;
	    }
	}

	// If not a female, does the text indicate a male?
	for (String maleText : _MALE_TEXTS) {
	    if (maleText.equalsIgnoreCase(genderText)) {
		return _GENDER_MALE;
	    }
	}

	// If neither, something is wrong.
	throw new InsufficientDataException("Unable to parse gender: " + genderText);

    } // parseGender()
    // =============================================================================================================================

	
	
    // =============================================================================================================================
    /**
     * Given a string provided to indicate the proficy with foreign languages, determine whether the student is a <b>capable Spanish
     * speaker</b>.  Other languages are not considered; if a more complex foreign language model is needed, this function's
     * interface and body could easily be enhanced.
     *
     * @param languagesText A string that indicates a student's proficiency in speaking Spanish.
     * @return <code>true</code> if this student is a capable Spanish speaker (according to <code>languagesText</code>;
     *         <code>false</code> otherwise.
     * @throws InsufficientDataException when the <code>languagesText</code> does not indicate a clear <i>yes</i> or <i>no</i>.
     */

    private static boolean parseLanguages (String languagesText) throws InsufficientDataException {

	// Search for the existence of the substring "spanish" (case insensitive) to consider this student "spanish-capable".
	if (languagesText.equalsIgnoreCase("yes")) {
	    return true;
	} else if (languagesText.equalsIgnoreCase("no")) {
	    return false;
	} else {
	    throw new InsufficientDataException("Unable to parse spanish-speaking capability: " + languagesText);
	}

    } // parseLanguages()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Create an array that will store the ranks, in their indexed order, verifying that each is properly expressed as an integer
     * within the appropriate ranking range, and that each such integer in that range appears exactly once.
     *
     * @param practiceRanksText The textual representation of the sequence of ranks, one per rank-ordered field.
     * @return A sequence of integers that represent the rank ordering of each field, guaranteed to contain one unique rank per
     *         field.
     * @throws InsufficientDataException when the collection of ranks is incorrect or incomplete.
     */

    private static int[] parsePracticeRanks (String[] practiceRanksText) throws InsufficientDataException {

	// Sanity check.
	Utility.abortIfFalse(practiceRanksText.length == _numberPracticeFields,
			     "Student.parsePracticeRanks() passed wrong number of text fields: " +
			     practiceRanksText.length +
			     " vs. " + 
			     _numberPracticeFields);

	// Create both a space to store the converted ranks as well as a space to record in which position each rank is found.  The
	// latter is initialized to -1 because that is not a valid index, and thus marks the given rank as not yet encountered in
	// the processing of each rank specified.
	int[] practiceRanks = new int[_numberPracticeFields];
	int[] ranksUsedPosition = new int[_numberPracticeFields];
	for (int i = 0; i < _numberPracticeFields; i += 1) {
	    ranksUsedPosition[i] = -1;
	}

	// Now visit each rank.  First convert it from a string to a integer, validating that the given rank is:
	//   (a) Actually convertable to an integer;
	//   (b) A valid rank from 1 to _numberPracticeFields;
	//   (c) Used in exactly one place in the rank listing.
	for (int i = 0; i < _numberPracticeFields; i += 1) {
	    try {
		practiceRanks[i] = Integer.parseInt(practiceRanksText[i]);
	    } catch (NumberFormatException e) {
		throw new InsufficientDataException("Couldn't parse rank value " +
						    practiceRanksText[i] +
						    " at position " +
						    i);
	    }
	    if ((practiceRanks[i] < 1) || (practiceRanks[i] > _numberPracticeFields)) {
		throw new InsufficientDataException("Rank " + practiceRanks[i] + " at position " + i + " is out of range");
	    }
	    int currentRank = practiceRanks[i];
	    int rankPosition = currentRank - 1;
	    if (ranksUsedPosition[rankPosition] != -1) {
		throw new InsufficientDataException("Rank " +
						    currentRank +
						    " used in both positions " +
						    ranksUsedPosition[rankPosition] +
						    " and " +
						    i);
	    }
	    ranksUsedPosition[rankPosition] = i;
	}

	// All converted and validated, so return it.
	return practiceRanks;

    } // parsePracticeRanks()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Parse the pre-match field to determine if this <code>Student</code> is already matched to some <code>Preceptor</code>.
     *
     * @param preMatchText The text of the pre-matched field in the record that defines this <code>Student</code>.
     * @return the given name of the preceptor to whom this student is matched, if given; <code>null</code> if the field is empty.
     */
    private String parsePreMatch (String preMatchText) {

	// If there is anything here, assume that it's the name of a preceptor to whom this student is pre-matched.
	return (!preMatchText.equals("") ? preMatchText : null);

    } // parsePreMatch()
    // =============================================================================================================================



    // =============================================================================================================================
    public String getName (boolean lastNameFirst) {
	if (lastNameFirst) {
	    return _lastName + ", " + _firstName;
	} else {
	    return _firstName + " " + _lastName;
	}
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public String getName () {
	return getName(true);
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public boolean pairable () {
	return _sufficientForMatching;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public boolean hasPreMatch () {
	return (_preMatch != null);
    }
    // =============================================================================================================================


    // =============================================================================================================================
    public String preMatch () {
	return _preMatch;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public void match (Preceptor preceptor) {
	_preceptor = preceptor;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public boolean matched () {
	return (_preceptor != null);
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public Preceptor getMatch () {
	return _preceptor;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Generate a textual representation of this student.
     *
     * @return A single string that encapsulates the traits of this student.
     */

    public String toString () {

	// Ranks not yet shown.
	String result = _firstName + ' ' + _lastName + ":\t";
	if (_sufficientForMatching) {
	    result += ((_isFemale ? "female" : "  male") +
		       ", " +
		       (_speaksSpanish ? "    spanish speaking" : "non-spanish speaking"));
	} else {
	    result += "WARNING: Insufficient information provided.";
	}

	return result;

    } // toString()
    // =============================================================================================================================



// =================================================================================================================================
} // class Student
// =================================================================================================================================
