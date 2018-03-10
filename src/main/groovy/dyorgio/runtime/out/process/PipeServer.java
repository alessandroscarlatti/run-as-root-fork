package dyorgio.runtime.out.process;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Pipe SocketServer to communicate with out process.
 */
public class PipeServer implements AutoCloseable {

    private ServerSocket server;
    private String secret;
    private final Thread listener;
    private Throwable err;
    private Serializable response;
    private Throwable serverErr;

    private Serializable request;

    private Runnable onDone;

    public PipeServer(final Serializable request, Runnable onDone) {
        this.request = request;
        initServer();

        listener = listener();
        listener.start();

        this.onDone = onDone;
    }

    protected void initServer() {
        Random r = new Random(System.currentTimeMillis());
        ServerSocket tmpServer = null;
        while (true) {
            try {
                tmpServer = new ServerSocket(1025 + r.nextInt(65535 - 1024));
                break;
            } catch (Exception e) {}
        }

        this.server = tmpServer;
        this.secret = r.nextLong() + ":" + r.nextLong() + ":" + r.nextLong() + ":" + r.nextLong();
    }

    private Thread listener() {
        return new Thread() {

            private ObjectInputStream objIn;
            private ObjectOutputStream objOut;

            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Socket s = server.accept();
                        if (s != null) {

                            String clientSecret = getClientSecret(s);

                            if (clientSecret.equals(secret)) {

                                writeRequest(s, request);

                                if (hasResponded(s)) {
                                    response = getResponse(s);
                                    done();
                                } else {
                                    err = getThrowable(s);
                                    done();
                                }
                            } else {
                                serverErr = new IllegalStateException("Client secret did not match server secret: was " + clientSecret);
                                done();
                            }
                        }
                    } catch (Exception e) {
                        serverErr = e;
                        done();
                    }
                }
            }

            private void initObjIn(Socket s) throws Exception {
                objIn = new ObjectInputStream(s.getInputStream());
            }

            private void initObjOut(Socket s) throws Exception {
                objOut = new ObjectOutputStream(s.getOutputStream());
            }

            private String getClientSecret(Socket s) throws Exception {
                if (objIn == null) initObjIn(s);
                return objIn.readUTF();
            }

            private void writeRequest(Socket s, Serializable request) throws Exception {
                if (objOut == null) initObjOut(s);

                objOut.writeObject(request);
                objOut.flush();
            }

            private Boolean hasResponded(Socket s) throws Exception {
                if (objIn == null) initObjIn(s);
                return objIn.readBoolean();
            }

            private Throwable getThrowable(Socket s) throws Exception {
                if (objIn == null) initObjIn(s);
                return (Throwable) objIn.readObject();
            }

            private Serializable getResponse(Socket s) throws Exception {
                if (objIn == null) initObjIn(s);
                return (Serializable) objIn.readObject();
            }
        };
    }

    protected void done() {
        onDone.run();
    }

    @Override
    public void close() {

        System.out.println("closing thread");

        try {
            listener.interrupt();
            server.close();
        } catch (Exception e) {
        }

        try {
            listener.join();
        } catch (Exception e) {
        }
    }

    protected int getPort() {
        return server.getLocalPort();
    }

    protected String getSecret() {
        return secret;
    }

    public Throwable getErr() {
        return err;
    }

    public Serializable getResponse() {
        return response;
    }

    public Throwable getServerErr() {
        return serverErr;
    }
}