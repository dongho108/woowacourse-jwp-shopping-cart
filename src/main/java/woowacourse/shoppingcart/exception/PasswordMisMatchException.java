package woowacourse.shoppingcart.exception;

public class PasswordMisMatchException extends LoginException {
    private static final String MESSAGE = "비밀번호가 일치하지 않습니다.";
    public PasswordMisMatchException() {
        super(MESSAGE);
    }
}
