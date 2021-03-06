/** *****************************************************************************
 * Copyright 2017 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package dyorgio.runtime.out.process;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;

/**
 * Constants and utility methods used in an out process execution.
 *
 * @author dyorgio
 */
public class OutProcessUtils {

    /**
     * System property flag to identify an out process code at runtime.
     */
    public static final String RUNNING_AS_OUT_PROCESS = "$RunnningAsOutProcess";

    /**
     * Get current Thread classpath.
     *
     * @return A string of current classpath elements splited by
     * <code>File.pathSeparatorChar</code>
     */
    public static String getCurrentClasspath() {
        StringBuilder buffer = new StringBuilder();
        for (URL url : ((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs()) {
            buffer.append(new File(url.getPath()));
            buffer.append(File.pathSeparatorChar);
        }
        String classpath = buffer.toString();
        classpath = classpath.substring(0, classpath.lastIndexOf(File.pathSeparatorChar));
        return classpath;
    }

    /**
     * Creates a new <code>ObjectInputStream</code> from
     * <code>inputStream</code> parameter, reads a <code>Callable</code> command
     * from it, executes call, and write results on <code>objOut</code>.
     * <br>
     * After executing <code>Callable.call()</code> a primitive boolean is wrote
     * in <code>objOut</code> to sinalize the execution state:
     * <br>
     * <code>true</code>: OK execution. Result is wrote on
     * <code>objOut</code><br>
     * <code>false</code>: An <code>Exception</code> occurred.
     * <code>Exception</code> is wrote on <code>objOut</code><br>
     *
     * @param inputStream A source of the command.
     * @param objOut The output for result.
     * @throws IOException
     * @see ObjectInputStream
     * @see Callable
     * @see ObjectOutputStream
     */
    public static void readCommandExecuteAndRespond(InputStream inputStream, ObjectOutputStream objOut) throws IOException {
        try {
            // Read current command
//            Callable<Serializable> callable = (Callable<Serializable>) new ObjectInputStream(inputStream).readObject();

//            OneRunOutProcess.Penguin penguin = (OneRunOutProcess.Penguin) new ObjectInputStream(inputStream).readObject();

            Callable callable = (Callable) new ObjectInputStream(inputStream).readObject();

            Serializable result = (String & Serializable) callable.call();

//            Serializable result = callable.call();
            // Reply with result
            objOut.writeBoolean(true);
            objOut.writeObject(result);
            objOut.flush();
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                // Reply with error
                objOut.writeBoolean(false);
                objOut.writeObject(e);
                objOut.flush();
            } catch (Throwable ex) {
                // Reply with safe error (without not-serializable objects).
                objOut.writeObject(new RuntimeException(ex.getMessage()));
                objOut.flush();
            }
        }
    }
}
