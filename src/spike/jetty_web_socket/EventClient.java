package jetty_web_socket;
import java.net.URI;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.eclipse.jetty.util.component.LifeCycle;


public class EventClient {
	public static void main(String[] args)
    {
        URI uri = URI.create(String.format("ws://%s:8080/events/", args[0]));

        try
        {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            try
            {
                // Attempt Connect
                Session session = container.connectToServer(EventSocket.class,uri);
                // Send a message
                session.getBasicRemote().sendText("Hello");
                Thread.sleep(10);
                // Close session
                session.close();
            }
            finally
            {
                // Force lifecycle stop when done with container.
                // This is to free up threads and resources that the
                // JSR-356 container allocates. But unfortunately
                // the JSR-356 spec does not handle lifecycles (yet)
                if (container instanceof LifeCycle)
                {
                    ((LifeCycle)container).stop();
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }
}
