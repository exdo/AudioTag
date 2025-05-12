package xyz.idaoteng.audiotag.exception;

public class ApiException extends Exception{
    public ApiException(String message) {
        super(message);
    }

    public ApiException() {}
}
