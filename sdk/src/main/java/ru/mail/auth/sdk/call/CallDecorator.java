package ru.mail.auth.sdk.call;

public class CallDecorator<R> implements MethodCall<R> {

    private final MethodCall<R> mDecorated;

    public CallDecorator(MethodCall<R> decorated) {
        mDecorated = decorated;
    }

    @Override
    public R execute() throws CallException {
        return mDecorated.execute();
    }
}
