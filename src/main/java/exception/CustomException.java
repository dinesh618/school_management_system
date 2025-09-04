package exception;

public class CustomException extends RuntimeException{
    private String message;
    public CustomException(String message)
    {
        message = this.message;
    }

}
