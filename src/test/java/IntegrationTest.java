import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Request;
import org.example.HttpServer;
import org.example.enums.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public class IntegrationTest {

    int port = 8042;
    int threadsNumber = 50;

    @Before
    public void startServer() throws Exception {

    }

    @Test
    public void multiThreadTest() throws Exception {
        HttpServer server = new HttpServer("localhost", port, null);
        new Thread(() -> {
            while (true) {
                server.run();
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadsNumber);
        Worker[] workers = new Worker[threadsNumber];
        for (int i = 0; i < threadsNumber; i++) {
            Worker worker = new Worker(startSignal, doneSignal);
            workers[i] = worker;
            new Thread(worker).start();
        }
        startSignal.countDown();
        doneSignal.await();
        assert !Arrays.stream(workers).filter(w -> w.failed == true).findAny().isPresent();
        server.shutdown();
    }


    class Worker implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        boolean failed = false;

        Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
        }

        public void run() {
            try {
                startSignal.await();
                try {
                    HttpResponse res = Request.Get("http://localhost:" + port + "/text.txt").execute().returnResponse();
                    StatusLine status = res.getStatusLine();
                    assert HttpStatus.OK.getCode() == status.getStatusCode();
                    assert "HTTP/1.1".equals(status.getProtocolVersion().toString());
                } catch (Exception e) {
                    failed = true;
                } finally {
                    doneSignal.countDown();
                }
            } catch (InterruptedException ex) {
            } finally {
                doneSignal.countDown();
            }
        }
    }
}
