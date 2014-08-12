// =================================================================================================================================
// IMPORTS

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
// =================================================================================================================================



// =================================================================================================================================
/**
 * A single preceptor (physican mentor/supervisor/teacher/trainer).  This object represents both the individual and that person's
 * traits and preferences.
 * 
 * @author Scott F. H. Kaplan <sfkaplan@cs.amherst.edu>
 * @version %G%
 */
public class Preceptor {
// =================================================================================================================================



    // =============================================================================================================================
    // DATA MEMBERS
    
    private String   _lastName;
    private String   _firstName;
    private double[] _practiceRanks;
    private boolean  _genderPreference;
    private boolean  _prefersFemale;
    private boolean  _spanishCapable;
    // private Location _home;

    private static final boolean _GENDER_MALE   = false;
    private static final boolean _GENDER_FEMALE = true;

    private static final int _LAST_NAME_INDEX           = 0;
    private static final int _FIRST_NAME_INDEX          = 1;
    private static final int _PRACTICE_TYPES_INDEX      = 2;
    private static final int _LOCATION_INDEX            = 3;
    private static final int _PRACTICE_REGION_INDEX     = 4;
    private static final int _GENDER_PREFERENCE_INDEX   = 5;
    private static final int _LANGUAGES_INDEX           = 6;
    private static final int _PREFERRED_DAY_INDEX       = 7;
    private static final int _SECONDARY_DAY_INDEX       = 8;
    private static final int _COMMENTS_INDEX            = 9;
    private static final int _numberFields              = 10;

    /**
     * A collection of case-insensitive strings that unambiguously indicate a male student.
     *
     * @see Preceptor.parseGenderPreference
     * @see Preceptor.parseGender
     */
    private static final String[] _MALE_TEXTS = { "m",
						  "male",
						  "man",
						  "men",
    };

    /**
     * A collection of case-insensitive strings that unambiguously indicate a male student.
     *
     * @see Preceptor.parseGenderPreference
     * @see Preceptor.parseGender
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
    public Preceptor (String record) {
		
	// Split the record into its parts.  Assume semi-colon delimiters (since commas may appear within fields).  Trim the results.
	String[] fields = record.split(";", -1);
	Utility.abortIfFalse(fields.length >= _numberFields, ("Preceptor.Preceptor(string): " +
							      "Record had the insufficient fields (" +
							      fields.length +
							      " must be at least " +
							      _numberFields +
							      "):\n  " +
							      record +
							      "\n"));
	for (int i = 0; i < fields.length; i += 1) {
	    fields[i] = fields[i].trim();
	}
		
	// Parse the fields, constructing the profile of the student.  Ignore preferred days and comments, since we don't use them
	// for matching right now.
	_lastName                   = fields[_LAST_NAME_INDEX];
	_firstName                  = fields[_FIRST_NAME_INDEX];
	String practiceTypesText    = fields[_PRACTICE_TYPES_INDEX];
	String locationText         = fields[_LOCATION_INDEX];
	String practiceRegionText   = fields[_PRACTICE_REGION_INDEX];
	String genderPreferenceText = fields[_GENDER_PREFERENCE_INDEX];
	String languagesText        = fields[_LANGUAGES_INDEX];

	// Construct a ranking mask from the information given.
	_practiceRanks = parsePracticeRanks(practiceTypesText, practiceRegionText);
	_genderPreference = parseGenderPreference(genderPreferenceText);
	if (_genderPreference) {
	    _prefersFemale = parseGender(genderPreferenceText);
	}
	_spanishCapable = parseLanguage(languagesText);


    } // Preceptor
    // =============================================================================================================================

	
	
    // =============================================================================================================================
    /**
     * Read a delimited list of student characteristics.  Construct each preceptor based on each record, and insert each into a
     * newly made set.
     * 
     * @param path The filename that contains the preceptor records.
     * @return A list of the preceptors read from the given path.
     */
    public static List<Preceptor> read (String path) {
		
	// Open the file for reading.
	FileReader file = null;
	try {
	    file = new FileReader(path);
	} catch (FileNotFoundException e) {
	    Utility.abort("Preceptor.readSet(): No such file " + path);
	}
	Scanner scanner = new Scanner(file);

	// Read the first line, assuming that it contains field headers.
	Utility.abortIfFalse(scanner.hasNextLine(), "Preceptor.readSet(): No lines of data!");
	String fieldNames = scanner.nextLine();
		
	// Read and parse the file's records, one at a time, creating a Preceptor from each and adding it to the set of such.
	List<Preceptor> preceptors = new ArrayList<Preceptor>();
	while (scanner.hasNextLine()) {
			
	    String record = scanner.nextLine();
	    Preceptor preceptor = new Preceptor(record);
	    preceptors.add(preceptor);
			
	}
		
	// Clean up and return the set of students.
	try {
	    file.close();
	} catch (IOException e) {
	    System.err.println("WARNING: Preceptor.readSet() failed upon closing path.  Continuing.");
	}
	return preceptors;
		
    } // readSet()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Given a string provided to indicate the preferred gender of the student, determine whether any preference is expressed at
     * all.
     *
     * @param genderText A case-insensitive string that represents the preceptor's desired gender for the student.  Some effort is
     *                   made to parse a number of different yet unambiguous designations of gender; see the code to determine which
     *                   are accepted.
     * @return <code>false</code> if the preceptor has no preference, <code>true</code> if the preceptor does have a preference.
     * @see Preceptor._MALE_TEXTS
     * @see Preceptor._FEMALE_TEXTS
     */

    private static boolean parseGenderPreference (String genderText) {

	// Is the message obviously not for a preference?
	if (genderText.matches("\\s*") || genderText.equalsIgnoreCase("none")) {
	    return false;
	}

	// Does the text indicate a female?
	genderText = genderText.toLowerCase();
	for (String femaleText : _FEMALE_TEXTS) {
	    if (genderText.contains(femaleText)) {
		return true;
	    }
	}

	// If not a female, does the text indicate a male?
	for (String maleText : _MALE_TEXTS) {
	    if (genderText.contains(maleText)) {
		return true;
	    }
	}

	// If neither, something is wrong.
	Utility.abort("Preceptor.parseGenderPreference(): Preference expected, but no gender found in " + genderText);

	// Dead code to placate the compiler.
	throw new RuntimeException("Somehow reached dead code in Preceptor.parseGenderPreference() on " + genderText);

    } // parseGenderPreference()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Assuming that the given text does indicate some gender, figure out which one and return it.
     *
     * @param genderText An indication of the preferred gender.
     * @return <code>true</code> if the preferred gender is <i>female</i>; <code>false</code> if it is <i>male</i>
     * @see Preceptor._MALE_TEXTS
     * @see Preceptor._FEMALE_TEXTS
     * @see Preceptor._GENDER_MALE
     * @see Preceptor._GENDER_FEMALE
     */
    
    private static boolean parseGender (String genderText) {

	// Does the text indicate a female?
	genderText = genderText.toLowerCase();
	for (String femaleText : _FEMALE_TEXTS) {
	    if (genderText.contains(femaleText)) {
		return _GENDER_FEMALE;
	    }
	}

	// If not a female, does the text indicate a male?
	for (String maleText : _MALE_TEXTS) {
	    if (genderText.contains(maleText)) {
		return true;
	    }
	}

	// If neither, something is wrong.
	Utility.abort("Preceptor.parseGender(): Preference expected, but no gender found in " + genderText);

	// Dead code to placate the compiler.
	throw new RuntimeException("Somehow reached dead code in Preceptor.parseGenderPreference() on " + genderText);	

    } // parseGender()
    // =============================================================================================================================

	
	
    // =============================================================================================================================
    /**
     * Given a string provided to indicate the proficy with foreign languages, determine whether the student is a <b>capable Spanish
     * speaker</b>.  Other languages are not considered; if a more complex foreign language model is needed, this function's
     * interface and body could easily be enhanced.
     *
     * @param languagesText A string that indicates the need for a student who is proficient in speaking Spanish.
     * @return <code>true</code> if the student should be a capable Spanish speaker (according to <code>languagesText</code>;
     *         <code>false</code> if the student need not be.
     */

    private static boolean parseLanguage (String languagesText) {

	// Spanish needed is indicated as a Y/N.
	if (languagesText.equalsIgnoreCase("y")) {
	    return true;
	} else {
	    return false;
	}

    } // parseLanguages()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Create an <i>array mask</i> that, when crossed with a <code>Student</code> ranking array, yields an array that indicates the
     * quality of those aspects of the match.
     *
     * @param practiceTypesText The kind of medicine practiced (e.g., family, pediatrics).
     * @param practiceRegionText The text that indicates in the geographic type of the practice (e.g., urban, rural).  It also
     *        happens to contain an indication of the percentage of the practice that is pediatric.  (This information should have
     *        been in its own field, but it is combined here for now.)
     * @return An <i>array mask</i> that matches the ranking of a <code>Student</code> that, when crossed, yields the quality of the
     *         match.
     */

    private static double[] parsePracticeRanks (String practiceTypesText, String practiceRegionText) {

	// Create a space to store the rank mask.
	double[] rankMask = new double[Student._numberPracticeFields];

	// First: Parse practice types.  They seem to follow one of the following forms:
	//   (0) [Blank]
	//   (1) FP [Family practitioner]
	//   (2) Internist
	//   (3) Pedi [Pediatrician]
	//   (4) FP/Internist
	//   (5) Pedi/FP
	//   (6) Internist/Geriatrician
	//   (7) Rural/Care of the Underserved
	//   The last is quite the annoying special case.  Match it first to manage it separately.
	String loweredPracticeTypes = practiceTypesText.toLowerCase();
	if (loweredPracticeTypes.contains("underserved")) {
	    rankMask[Student._UNDERSERVED_RANK_INDEX] = 1.0;
	} else if (practiceTypesText.equalsIgnoreCase("FP")) {
	    rankMask[Student._FAMILY_PRACTITIONER_RANK_INDEX] = 1.0;
	} else if (practiceTypesText.equalsIgnoreCase("Internist")) {
	    rankMask[Student._INTERNISTS_RANK_INDEX] = 1.0;
	} else if (practiceTypesText.equalsIgnoreCase("Pedi")) {
	    rankMask[Student._PEDIATRICIAN_RANK_INDEX] = 1.0;
	} else if (practiceTypesText.equalsIgnoreCase("FP/Internist")) {
	    rankMask[Student._FAMILY_PRACTITIONER_RANK_INDEX] = 0.5;
	    rankMask[Student._INTERNISTS_RANK_INDEX] = 0.5;
	} else if (practiceTypesText.equalsIgnoreCase("Pedi/FP")) {
	    rankMask[Student._PEDIATRICIAN_RANK_INDEX] = 0.5;
	    rankMask[Student._FAMILY_PRACTITIONER_RANK_INDEX] = 0.5;
	} else if (practiceTypesText.equalsIgnoreCase("Interist/Geriatrician")) {
	    rankMask[Student._INTERNISTS_RANK_INDEX] = 0.5;
	    rankMask[Student._GERIATRICIAN_RANK_INDEX] = 0.5;
	}

	// Second: The practice region must be split from its ancillary information regarding the percentage of pediatrics; that
	// practice region must then be parsed.  Be aware that some regions are not specified; furthermore, sometimes a region is
	// specified without a percentage on peds.
	String[] practiceRegionSplit = practiceRegionText.split(" ");
	if (practiceRegionSplit.length >= 1) {
	    String region = practiceRegionSplit[0];
	    if (region.equalsIgnoreCase("Urban")) {
		rankMask[Student._URBAN_SETTING_RANK_INDEX] = 1.0;
	    } else if (region.equalsIgnoreCase("Suburban")) {
		rankMask[Student._SUBURBAN_SETTING_RANK_INDEX] = 1.0;
	    } else if (region.equalsIgnoreCase("Rural")) {
		rankMask[Student._RURAL_SETTING_RANK_INDEX] = 1.0;
	    } else if (region.equalsIgnoreCase("Suburban/Urban")) {
		rankMask[Student._SUBURBAN_SETTING_RANK_INDEX] = 0.5;
		rankMask[Student._URBAN_SETTING_RANK_INDEX] = 0.5;
	    }
	}

	// Third: Handle the percentage pediatrics, if it's there.  It could have one of the following formats:
	//   (1) x%
	//   (2) x-y%
	//   Parse both; average the latter if applicable.
	if (practiceRegionSplit.length >= 2) {
	    String percentPeds = practiceRegionSplit[1];
	    Pattern pattern = Pattern.compile("(\\d+){1}-(\\d+)?%", Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(percentPeds);
	    if (!matcher.matches()) {
		System.err.println("WARNING: Preceptor.parsePracticeRanks(): Unable to parse percentage pediatrics = " + percentPeds);
	    } else {
		int firstPercentage = Integer.parseInt(matcher.group(1));
		double percentagePediatrics = 0.0;
		if (matcher.groupCount() == 1) {
		    percentagePediatrics = firstPercentage;
		} else {
		    int secondPercentage = Integer.parseInt(matcher.group(2));
		    percentagePediatrics = (firstPercentage + secondPercentage) / 2;
		}
		rankMask[Student._PEDIATRICIAN_RANK_INDEX] = percentagePediatrics;
	    }
	}

	return rankMask;

    } // parsePracticeRanks()
    // =============================================================================================================================



    // =============================================================================================================================
    public String getName () {
	return _lastName + ", " + _firstName;
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
	return (_firstName +
		' ' +
		_lastName +
		":\t" +
		(_genderPreference ? (_prefersFemale ? "female" : "  male") : "none") +
		", " +
		(_spanishCapable ? "   spanish speaking" : "non-spanish speaking"));

    } // toString()
    // =============================================================================================================================



    // =============================================================================================================================
    public double cross (Student student) {
	return student.cross(this);
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public double getRankMask (int position) {
	return _practiceRanks[position];
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public boolean hasGenderPreference () {
	return _genderPreference;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public boolean prefersFemale () {
	return _prefersFemale;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public boolean prefersSpanish () {
	return _spanishCapable;
    }
    // =============================================================================================================================



// =================================================================================================================================
} // class Preceptor
// =================================================================================================================================
