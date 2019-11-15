package ru.mail.auth.sdk.api.token;

import androidx.annotation.WorkerThread;
import ru.mail.auth.sdk.call.CallException;

public interface TokenExpiredListener {
    @WorkerThread
    void onTokenExpired() throws CallException;
}
