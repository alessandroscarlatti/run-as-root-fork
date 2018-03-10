package dyorgio.runtime.out.process;

import java.io.Serializable;
import java.net.Socket;

/**
 * ______    __                         __           ____             __     __  __  _
 * ___/ _ | / /__ ___ ___ ___ ____  ___/ /______    / __/______ _____/ /__ _/ /_/ /_(_)
 * __/ __ |/ / -_|_-<(_-</ _ `/ _ \/ _  / __/ _ \  _\ \/ __/ _ `/ __/ / _ `/ __/ __/ /
 * /_/ |_/_/\__/___/___/\_,_/_//_/\_,_/_/  \___/ /___/\__/\_,_/_/ /_/\_,_/\__/\__/_/
 * Saturday, 3/10/2018
 *
 * Socket Transaction
 *
 * make a request, get a response, or get a timeout
 *
 * response can be a success, or it can be an exception
 *
 * so there will be three callbacks:
 *  - response
 *  - error
 *  - timeout
 *
 *  I = data type in
 *  O = data type out
 */
public class SocketTransaction<I extends Serializable, O extends Serializable> {
    private I request;
    private O response;

    private boolean started;
    private boolean completed;
    private boolean timedOut;

    private boolean success;
    private Throwable err;

    private String host;
    private int timeoutMs;

    private SuccessCallback<O> onSuccess;
    private ErrorCallback<? extends Throwable> onErr;
    private Runnable onTimeout;

    public SocketTransaction(I request) {
        initDefaults();
        this.request = request;
    }

    private void initDefaults() {
        host = "localhost";
        onSuccess = noOpSuccessCallback();
        onErr = noOpErrorCallback();
        onTimeout = noOpTimeoutCallback();
    }

    public SocketTransaction onSuccess(SuccessCallback<O> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public SocketTransaction onErr(ErrorCallback<? extends Throwable> onErr) {
        this.onErr = onErr;
        return this;
    }

    public SocketTransaction onTimeout(Runnable onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    private SuccessCallback<O> noOpSuccessCallback() {
        return (response) -> {
            System.out.printf("response was: %s%n", response);
        };
    }

    private ErrorCallback<? extends Throwable> noOpErrorCallback() {
        return (err) -> {
            System.err.println("Socket Transaction Error.");
            err.printStackTrace();
        };
    }

    private Runnable noOpTimeoutCallback() {
        return () -> {
            System.out.printf("Socket Transaction timed out after %d ms.%n", timeoutMs);
        };
    }

    @FunctionalInterface
    public interface SuccessCallback<T extends Serializable> {
        void onSuccess(T response);
    }

    @FunctionalInterface
    public interface ErrorCallback<T extends Throwable> {
        void onError(T error);
    }
}
