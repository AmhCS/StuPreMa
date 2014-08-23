public class InsufficientDataException extends Exception {

    private String _message;

    public InsufficientDataException (String message) {
	this._message = message;
    }

    public String getMessage () {
	return _message;
    }

}
