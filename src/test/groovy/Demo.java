import com.scarlatti.runAsRoot.RootExecutor;
import org.junit.Test;

/**
 * ______    __                         __           ____             __     __  __  _
 * ___/ _ | / /__ ___ ___ ___ ____  ___/ /______    / __/______ _____/ /__ _/ /_/ /_(_)
 * __/ __ |/ / -_|_-<(_-</ _ `/ _ \/ _  / __/ _ \  _\ \/ __/ _ `/ __/ / _ `/ __/ __/ /
 * /_/ |_/_/\__/___/___/\_,_/_//_/\_,_/_/  \___/ /___/\__/\_,_/_/ /_/\_,_/\__/\__/_/
 * Saturday, 2/24/2018
 */
public class Demo {

    @Test
    public void runApp() throws Exception {

        RootExecutor rootExecutor = new RootExecutor();
        String returnValue = rootExecutor.call(() -> {
            javax.swing.JOptionPane.showMessageDialog(null, "what do you know");
            System.out.println("hello");
            return "what do you know";
        });

        System.out.printf("return value: %s%n", returnValue);
    }
}
