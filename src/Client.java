import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by cataldo on 04/11/15.
 */
public class Client {

    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new MyModule(args));

    }
}
