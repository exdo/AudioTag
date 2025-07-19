package xyz.idaoteng.audiotag.exception;

public class InvalidPlaceholderException extends Exception {
    public InvalidPlaceholderException(String placeholder) {
        super(String.format("无效的占位符 '%s'。请只使用下拉框中提供的占位符。", placeholder));
    }
}
