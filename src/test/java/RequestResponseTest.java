import org.example.enums.HttpStatus;
import org.example.server.impl.HttpRequest;
import org.example.server.impl.HttpResponse;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by Alex Avekau on 13.03.2018.
 */
public class RequestResponseTest {

    @Test
    public void requestTest() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("GET / HTTP/1.1\r\n");
        sb.append("Host: localhost:8082\r\n");
        sb.append("Connection: keep-alive\r\n");
        sb.append("\r\n");
        sb.append("some body\r\n\r\n");
        sb.append("some body");
        ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes());

        SocketChannel channel = Mockito.mock(SocketChannel.class);
        int read[] = new int[]{0};


        Mockito.when(channel.read(buffer)).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                buffer.position(buffer.capacity());
                if (++read[0] < 2)
                    return buffer.capacity();
                else
                    return -1;
            }
        });
        HttpRequest request = new HttpRequest();
        request.setChannel(channel);
        request.setBuffer(buffer);
        request.parse();
        assert "GET".equals(request.getHttpMethod());
        assert "localhost:8082".equals(request.getHeader("HOST"));
    }

    @Test
    public void responseTest() throws IOException {
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        HttpResponse response = new HttpResponse(channel);
        StringBuilder sb = new StringBuilder();
        Mockito.when(channel.write(Mockito.any(ByteBuffer.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ByteBuffer buffer = invocationOnMock.getArgumentAt(0, ByteBuffer.class);
                sb.append(new String(buffer.array(), Charset.forName("UTF-8")));
                buffer.position(buffer.limit());
                return buffer.capacity();
            }
        });
        response.setResponseStatus(HttpStatus.OK);
        response.print("test");
        response.println("test1");
        response.setHeader("content-type", "text/plain");
        try {
            response.send();
        } catch (NullPointerException npe) {
            //Mockito cannot mock the final method, so it's all right
            assert npe.getStackTrace()[0].toString()
                    .contains("java.nio.channels.spi.AbstractInterruptibleChannel.close");
        }

        String result = sb.toString();
        assert result.contains("OK");
        assert result.contains("text/plain");
        assert result.contains("Content-Length: 11");
    }
}
