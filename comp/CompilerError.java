// Nome: Iago Freitas Piedade      RA: 587567
// Nome: Lucas Alexandre Occaso    RA: 620505

package comp;

public class CompilerError extends RuntimeException {
	public CompilerError(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getErrorMessage() { return errorMessage; }

	private String errorMessage;
	private static final long serialVersionUID = 1L;

}
