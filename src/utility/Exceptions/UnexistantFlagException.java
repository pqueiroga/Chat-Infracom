package utility.Exceptions;

public class UnexistantFlagException extends Exception {
	public UnexistantFlagException() {
		super("The specified flag does not exist.");
	}
}
