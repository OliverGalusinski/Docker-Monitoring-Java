import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        CheckForNewContainer checkForNewContainer = new CheckForNewContainer();
        checkForNewContainer.setName("CheckForNewContainer-Thread");
        checkForNewContainer.start();
    }
}
