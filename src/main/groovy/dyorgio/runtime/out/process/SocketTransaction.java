package dyorgio.runtime.out.process;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

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
public class SocketTransaction<I extends Serializable, O extends Serializable> implements AutoCloseable {
    private I request;
    private O response;

    private boolean started;
    private boolean completed;

    private CountDownLatch doneLatch;

    private String host;
    private int timeoutMs;

    private SuccessCallback<O> onSuccess;
    private ErrorCallback onErr;
    private ErrorCallback onSocketErr;
    private Runnable onTimeout;

    private PipeServer server;

    public SocketTransaction() {
        initDefaults();
        initServer();
    }

    public SocketTransaction(I request) {
        this.request = request;

        initDefaults();
        initServer();
    }

    protected void initDefaults() {
        host = "localhost";
        onSuccess = noOpSuccessCallback();
        onErr = noOpErrorCallback();
        onTimeout = noOpTimeoutCallback();

        doneLatch = new CountDownLatch(1);
    }

    protected void initServer() {
        started = true;
        server = new PipeServer(request, this::onDone);
    }

    public SocketTransaction<I, O> onSuccess(SuccessCallback<O> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public SocketTransaction<I, O> onErr(ErrorCallback<? extends Throwable> onErr) {
        this.onErr = onErr;
        return this;
    }

    public SocketTransaction<I, O> onTimeout(Runnable onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    protected void onDone() {
        doneLatch.countDown();
    }

    public void setRequest(I request) {
        this.request = request;
    }

    public SocketTransaction<I, O> withTimeout(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    /**
     * This is the method that initiates all the socket threads...
     * @return the response, if successful.
     */
    public O exchange() throws TransactionTimeoutException {

        if (!completed) {
            O response = doExchange();
            completed = true;
            return response;
        }

        throw new IllegalStateException("This Socket Transaction has already been initiated.");
    }

    private O doExchange() throws TransactionTimeoutException {
        try {
            doneLatch.await();

            // now we interpret the server results.
            if (server.getServerErr() != null) {
                throw new RuntimeException("Transaction error.", server.getServerErr());
            }

            if (server.getErr() != null) {
                throw new RuntimeException("Transaction error.", server.getErr());
            }

            if (server.getResponse() != null) {
                return (O) server.getResponse();
            }

            throw new IllegalStateException("Transaction state does not have success or error data.");

        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interruption while waiting for transaction to complete.", e);
        }
    }

    @Override
    public void close() throws Exception {
        server.close();
    }

    public int getPort() {
        return server.getPort();
    }

    public String getSecret() {
        return server.getSecret();
    }

    private SuccessCallback<O> noOpSuccessCallback() {
        return (response) -> {
            System.out.printf("response was: %s%n", response);
        };
    }

    private ErrorCallback<? extends Throwable> noOpErrorCallback() {
        return (err) -> {
            System.err.println("Socket Transaction returned an exception.");
            err.printStackTrace();
        };
    }

    private ErrorCallback<? extends Throwable> noOpSocketErrCallback() {
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

    /**
     * Checked exception so that it is clear how the user must handle timeouts.
     */
    public static class TransactionTimeoutException extends Exception {
        public TransactionTimeoutException(String message) {
            super(message);
        }

        public TransactionTimeoutException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
