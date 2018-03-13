import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Request;
import org.example.HttpServer;
import org.example.enums.HttpStatus;
import org.junit.*;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Alex Avekau on 12.03.2018.
 */
public class IntegrationTest {

    static int port = 8042;
    int threadsNumber = 10;
    static HttpServer server;
    String[] files = new String[]{"text.html",
            "text.tgz",
            "text.txt.gz",
            "text.tar",
            "text.7z",
            "text.pdf",
            "text.zip",
            "text.txt"};

    @BeforeClass
    public static void startServer() throws Exception {
        server = new HttpServer("localhost", port, null);
        new Thread(() -> {
            while (true) {
                server.run();
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {

                }
            }
        }).start();
    }

    @AfterClass
    public static void stopServer() {
        server.shutdown();
    }

    @Test
    public void multiThreadTest() throws Exception {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadsNumber);
        Worker[] workers = new Worker[threadsNumber];
        for (int i = 0; i < threadsNumber; i++) {
            String randomLocation = "/1/" + files[ThreadLocalRandom.current().nextInt(0, files.length)];
            Worker worker = new Worker(startSignal, doneSignal, randomLocation, HttpStatus.OK.getCode());
            workers[i] = worker;
            new Thread(worker).start();
        }
        startSignal.countDown();
        doneSignal.await();
        assert !Arrays.stream(workers).filter(w -> w.failed == true).findAny().isPresent();
    }


    @Test
    public void multiThreadNotFoundTest() throws Exception {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(5);
        Worker[] workers = new Worker[5];
        for (int i = 0; i < 5; i++) {
            String randomLocation = "/2/" + files[ThreadLocalRandom.current().nextInt(0, files.length)];
            Worker worker = new Worker(startSignal, doneSignal, randomLocation, HttpStatus.NOT_FOUND.getCode());
            workers[i] = worker;
            new Thread(worker).start();
        }
        startSignal.countDown();
        doneSignal.await();
        assert !Arrays.stream(workers).filter(w -> w.failed == true).findAny().isPresent();
    }

    @Test
    public void typeNotSupportedTest() throws Exception {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(1);
        Worker[] workers = new Worker[1];
        for (int i = 0; i < 1; i++) {
            Worker worker = new Worker(startSignal, doneSignal, "/1/text.hz",
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE.getCode());
            workers[i] = worker;
            new Thread(worker).start();
        }
        startSignal.countDown();
        doneSignal.await();
        assert !Arrays.stream(workers).filter(w -> w.failed == true).findAny().isPresent();
    }

    class Worker implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final String location;
        private final int code;
        boolean failed = false;

        Worker(CountDownLatch startSignal, CountDownLatch doneSignal, final String location, int code) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.location = location;
            this.code = code;
        }

        public void run() {
            try {
                startSignal.await();
                try {
                    HttpResponse res = Request.Get("http://localhost:" + port + location)
                            .execute().returnResponse();
                    StatusLine status = res.getStatusLine();
                    assert code == status.getStatusCode();
                    assert "HTTP/1.1".equals(status.getProtocolVersion().toString());
                } catch (Exception e) {
                    failed = true;
                    e.printStackTrace();
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
