import com.microsoft.graph.authentication.IAuthenticationProvider;

import java.io.IOException;

public interface SampleClass {

    void run() throws IOException;

    String getToken();

    IAuthenticationProvider getAuthenticationProvider();
}
