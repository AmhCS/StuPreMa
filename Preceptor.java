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

    /** The last name of the preceptor. */
    private String   _lastName;

    /** The first name of the preceptor. */
    private String   _firstName;

    /**
     * A array of weights, used the mask the value of the practice type of this preceptor when matched to a given student.
     * @see Student._practiceRanks
     */
    private double[] _practiceMask;

    /**
     * A array of weights, used the mask the value of the practice type of this preceptor when matched to a given student.
     * @see Student._settingRanks
     */
    private double[] _settingMask;

    /** Whether this preceptor has any preference for one gender or the other. */
    private boolean  _genderPreference;

    /**
     * If the preceptor has a preference, whether it for a female or a male.
     * @see _GENDER_FEMALE
     * @see _GENDER_MALE
     */
    private boolean  _prefersFemale;

    /** Whether this preceptor needs a student who is capabale of interacting in Spanish. */
    private boolean  _spanishCapable;

    /** The location of a preceptor's practice. */
    private String   _location;

    /** The type of practice in which the preceptor works. */
    private String   _practiceType;

    /** The preferred day of the week for working with students. */
    private String   _dayOfWeek;
    
    /** The student with whom this preceptor has been pre-matched (if any). */
    private String   _preMatch;

    /** The <code>Student</code> to whom this student is matched (if any). */
    private Student  _student;

    /**
     * Whether sufficient information for the fields above is provided to properly match this student with a <code>Preceptor</code>.
     * @see Student.cross
     */
    private boolean  _sufficientForMatching;

    /**
     * A boolean constant to represent a male.
     * @see _prefersFemale
     */
    private static final boolean _GENDER_MALE   = false;

    /**
     * A boolean constant to represent a female.
     * @see _prefersFemale
     */
    private static final boolean _GENDER_FEMALE = true;

    private static final int _LAST_NAME_INDEX         = 0;
    private static final int _FIRST_NAME_INDEX        = 1;
    private static final int _PRACTICE_TYPES_INDEX    = 2;
    private static final int _LOCATION_INDEX          = 3;
    private static final int _PRACTICE_SETTING_INDEX  = 4;
    private static final int _GENDER_PREFERENCE_INDEX = 5;
    private static final int _LANGUAGES_INDEX         = 6;
    private static final int _PREFERRED_DAY_INDEX     = 7;
    private static final int _SECONDARY_DAY_INDEX     = 8;
    private static final int _COMMENTS_INDEX          = 9;
    private static final int _PRE_MATCHED_INDEX       = 10;
    private static final int _numberFields            = 11;

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
		
	// Parse the fields, constructing the profile of the student.  Ignore comments, since we don't use them for matching right
	// now.
	_lastName                   = fields[_LAST_NAME_INDEX];
	_firstName                  = fields[_FIRST_NAME_INDEX];
	_practiceType               = fields[_PRACTICE_TYPES_INDEX];
	_location                   = fields[_LOCATION_INDEX];
	String settingText   = fields[_PRACTICE_SETTING_INDEX];
	String genderPreferenceText = fields[_GENDER_PREFERENCE_INDEX];
	String languagesText        = fields[_LANGUAGES_INDEX];
	_dayOfWeek                  = fields[_PREFERRED_DAY_INDEX];
	String preMatchText         = fields[_PRE_MATCHED_INDEX];

	// Construct a ranking mask from the information given.
	try {
	    _practiceMask          = parsePractice(_practiceType, settingText);
	    _settingMask           = parseSetting(settingText);
	    _genderPreference  = parseGenderPreference(genderPreferenceText);
	    if (_genderPreference) {
		_prefersFemale = parseGender(genderPreferenceText);
	    }
	    _spanishCapable    = parseLanguage(languagesText);
	    _preMatch          = parsePreMatch(preMatchText);
	    _sufficientForMatching = true;
	} catch (InsufficientDataException e) {
	    Utility.warning(String.format("Unable to read complete profile from record for preceptor %s, %s\n\tMESSAGE: %s",
					  _lastName,
					  _firstName,
					  e.getMessage()));
	    _sufficientForMatching = false;
	}

	// Pre-match parsing is special because it can trump an otherwise insufficient record.  So, we want to process this field
	// irrespective of the success in having parsed the other fields.
	_preMatch          = parsePreMatch(preMatchText);
	if (_preMatch != null) {
	    _sufficientForMatching = true;
	}

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
     * @throws InsufficientDataException when the <code>genderText</code> contains neither a clear expression of a gender, nor a
     *         clear expression of <i>no preference</i>.
     * @see Preceptor._MALE_TEXTS
     * @see Preceptor._FEMALE_TEXTS
     */

    private static boolean parseGenderPreference (String genderText) throws InsufficientDataException {

	// Is the message blank?
	if (genderText.matches("\\s*")) {
	    throw new InsufficientDataException("Gender preference field is blank");
	}

	if (genderText.equalsIgnoreCase("none")) {
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

	// If neither, this is not a sufficiently clear expression.
	throw new InsufficientDataException("Preference expected, but no identifiable gender expressed: " + genderText);

    } // parseGenderPreference()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Assuming that the given text does indicate some gender, figure out which one and return it.  That assumption is based on a
     * previous test performed by <code>parseGenderPreference</code>.
     *
     * @param genderText An indication of the preferred gender.
     * @return <code>true</code> if the preferred gender is <i>female</i>; <code>false</code> if it is <i>male</i>
     * @throws InsufficientDataException when <code>genderText</code> does not indicate either <i>female</i> or <i>male</i> as a
     *         choice of preferred gender.
     * @see Preceptor.parseGenderPreference
     * @see Preceptor._MALE_TEXTS
     * @see Preceptor._FEMALE_TEXTS
     * @see Preceptor._GENDER_MALE
     * @see Preceptor._GENDER_FEMALE
     */
    
    private static boolean parseGender (String genderText) throws InsufficientDataException {

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

	// If neither, this is not a sufficiently clear expression.
	throw new InsufficientDataException("Specific gender expected, but no identifiable gender expressed: " + genderText);

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
     * @throws InsufficientDataException when a clear expression of <i>yes</i> or <i>no</i> is not provided in
     *         <code>languagesText</code>.
     */

    private static boolean parseLanguage (String languagesText) throws InsufficientDataException {

	// Spanish needed is indicated as a Y/N.
	if (languagesText.equalsIgnoreCase("y")) {
	    return true;
	} else if (languagesText.equalsIgnoreCase("n")) {
	    return false;
	} else {
	    throw new InsufficientDataException("Expected y/n, but got neither in: " + languagesText);
	}

    } // parseLanguages()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Create an <i>array mask</i> that, when crossed with a <code>Student</code> ranking array, yields an array that indicates the
     * quality the match with respect to practice type.
     *
     * @param practiceTypesText The kind of medicine practiced (e.g., family, pediatrics).
     * @param settingText The text that describes both the setting of the practice (not handled here) and the percentage of
     *                    pediatrics associated with the preceptor.  SFHK: This combination is stupid; change it next year.
     * @return An <i>array mask</i> that matches the ranking of a <code>Student</code> that, when crossed, yields the quality of the
     *         match.
     * @throws InsufficientDataException when the various fields provided do not yield a clear indication of how to construct the
     *         ranks mask.
     */

    private static double[] parsePractice (String practiceTypesText, String settingText) throws InsufficientDataException {

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
	String loweredPracticeTypes = practiceTypesText.toLowerCase();
	if (practiceTypesText.equalsIgnoreCase("FP")) {
	    rankMask[Student._FAMILY_PRACTITIONER_RANK_INDEX] = 1.0;
	} else if (practiceTypesText.equalsIgnoreCase("IM")) {
	    rankMask[Student._INTERNISTS_RANK_INDEX]          = 1.0;
	} else if (practiceTypesText.equalsIgnoreCase("Ped")) {
	    rankMask[Student._PEDIATRICIAN_RANK_INDEX]        = 1.0;
	} else if (practiceTypesText.equalsIgnoreCase("Geriatrician")) {
	    rankMask[Student._GERIATRICIAN_RANK_INDEX]        = 1.0;
	} else if (practiceTypesText.equalsIgnoreCase("FP/IM")) {
	    rankMask[Student._FAMILY_PRACTITIONER_RANK_INDEX] = 0.75;
	    rankMask[Student._INTERNISTS_RANK_INDEX]          = 0.75;
	} else if (practiceTypesText.equalsIgnoreCase("Ped/FP")) {
	    rankMask[Student._PEDIATRICIAN_RANK_INDEX]        = 0.75;
	    rankMask[Student._FAMILY_PRACTITIONER_RANK_INDEX] = 0.75;
	} else if (practiceTypesText.equalsIgnoreCase("IM/Geriatrician")) {
	    rankMask[Student._INTERNISTS_RANK_INDEX]          = 0.75;
	    rankMask[Student._GERIATRICIAN_RANK_INDEX]        = 0.75;
	}

	// Second: Handle the percentage pediatrics, if it's there and this isn't a straight pediatrician.  It could have one of the
	// following formats:
	//   (1) x%
	//   (2) x-y%
	// Parse both; average the latter if applicable.
	String[] settingSplit = settingText.split(" ");
	if ((settingSplit.length >= 2) && (rankMask[Student._PEDIATRICIAN_RANK_INDEX] == 0.0)) {
	    String percentPeds = settingSplit[1];
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

    }
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Create an <i>array mask</i> that, when crossed with a <code>Student</code> ranking array, yields an array that indicates the
     * quality the match with respect to the practice's setting (e.g., <i>urban</i>, <i>rural</i>).
     *
     * @param settingText The text that indicates in the geographic type of the practice (e.g., urban, rural).  It also
     *        happens to contain an indication of the percentage of the practice that is pediatric.  (This information should have
     *        been in its own field, but it is combined here for now.)
     * @return An <i>array mask</i> that matches the ranking of a <code>Student</code> that, when crossed, yields the quality of the
     *         match.
     * @throws InsufficientDataException when the various fields provided do not yield a clear indication of how to construct the
     *         ranks mask.
     */
    private static double[] parseSetting (String settingText) throws InsufficientDataException {

	// Create the rank mask.
	double[] rankMask = new double[Student._numberSettingFields];

	// The setting must be split from its ancillary information regarding the percentage of pediatrics; that setting must then
	// be parsed.
	String[] settingSplit = settingText.split(" ");
	if (settingSplit.length >= 1) {
	    String setting = settingSplit[0];
	    if (setting.equalsIgnoreCase("Urban")) {
		rankMask[Student._URBAN_SETTING_RANK_INDEX]    = 1.0;
	    } else if (setting.equalsIgnoreCase("Suburban")) {
		rankMask[Student._SUBURBAN_SETTING_RANK_INDEX] = 1.0;
	    } else if (setting.equalsIgnoreCase("Rural")) {
		rankMask[Student._RURAL_SETTING_RANK_INDEX]    = 1.0;
	    } else if (setting.equalsIgnoreCase("Underserved")) {
		rankMask[Student._UNDERSERVED_RANK_INDEX]      = 1.0;
	    } else if (setting.equalsIgnoreCase("Urban/Underserved")    || setting.equalsIgnoreCase("Underserved/Urban")) {
		rankMask[Student._UNDERSERVED_RANK_INDEX]      = 0.5;
		rankMask[Student._URBAN_SETTING_RANK_INDEX]    = 0.5;
	    } else if (setting.equalsIgnoreCase("Suburban/Underserved") || setting.equalsIgnoreCase("Underserved/Suburban")) {
		rankMask[Student._UNDERSERVED_RANK_INDEX]      = 0.5;
		rankMask[Student._SUBURBAN_SETTING_RANK_INDEX] = 0.5;
	    } else if (setting.equalsIgnoreCase("Rural/Underserved")    || setting.equalsIgnoreCase("Underserved/Rural")) {
		rankMask[Student._UNDERSERVED_RANK_INDEX]      = 0.5;
		rankMask[Student._RURAL_SETTING_RANK_INDEX]    = 0.5;
	    } else if (setting.equalsIgnoreCase("Suburban/Urban")       || setting.equalsIgnoreCase("Urban/Suburban")) {
		rankMask[Student._SUBURBAN_SETTING_RANK_INDEX] = 0.5;
		rankMask[Student._URBAN_SETTING_RANK_INDEX]    = 0.5;
	    } else if (setting.equalsIgnoreCase("Suburban/Rural")       || setting.equalsIgnoreCase("Rural/Suburban")) {
		rankMask[Student._SUBURBAN_SETTING_RANK_INDEX] = 0.5;
		rankMask[Student._RURAL_SETTING_RANK_INDEX]    = 0.5;
	    } else {
		Utility.warning("Unable to parse setting = " + setting);
	    }
	}

	return rankMask;

    } // parseSetting()
    // =============================================================================================================================



    // =============================================================================================================================
    /**
     * Parse the pre-match field to determine if this <code>Preceptor</code> is already matched to some <code>Student</code>.
     *
     * @param preMatchText The text of the pre-matched field in the record that defines this <code>Preceptor</code>.
     * @return the given name of the student to whom this preceptor is matched, if given; <code>null</code> if the field is empty.
     */
    private String parsePreMatch (String preMatchText) {

	// If there is anything here, assume that it's the name of a preceptor to whom this preceptor is pre-matched.
	return (!preMatchText.equals("") ? preMatchText : null);

    } // parsePreMatch()
    // =============================================================================================================================



    // =============================================================================================================================
    public boolean pairable () {
	return _sufficientForMatching;
    }
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
    public double getPracticeMask (int position) {
	return _practiceMask[position];
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public double getSettingMask (int position) {
	return _settingMask[position];
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



    // =============================================================================================================================
    public String preferredDay () {
	return _dayOfWeek;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public String location () {
	return _location;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public String practiceType () {
	return _practiceType;
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
    public void match (Student student) {
	_student = student;
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public boolean matched () {
	return (_student == null);
    }
    // =============================================================================================================================



    // =============================================================================================================================
    public Student getMatch () {
	return _student;
    }
    // =============================================================================================================================



// =================================================================================================================================
} // class Preceptor
// =================================================================================================================================
