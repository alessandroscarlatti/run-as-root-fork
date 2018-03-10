package dyorgio.runtime.out.process;

/**
 * ______    __                         __           ____             __     __  __  _
 * ___/ _ | / /__ ___ ___ ___ ____  ___/ /______    / __/______ _____/ /__ _/ /_/ /_(_)
 * __/ __ |/ / -_|_-<(_-</ _ `/ _ \/ _  / __/ _ \  _\ \/ __/ _ `/ __/ / _ `/ __/ __/ /
 * /_/ |_/_/\__/___/___/\_,_/_//_/\_,_/_/  \___/ /___/\__/\_,_/_/ /_/\_,_/\__/\__/_/
 * Saturday, 3/10/2018
 */
public class ProcessToRun implements CallableSerializable<String> {
    @Override
    public String call() throws Exception {
            javax.swing.JOptionPane.showMessageDialog(null, "what do you know");
            System.out.println("hello");
            return "what do you know";
    }
}
