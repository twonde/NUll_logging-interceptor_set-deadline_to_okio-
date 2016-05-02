/*
 * Copyright 2016 Tony.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okhttp3.mockwebserver;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.junit.Rule;
import org.junit.Test;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResponseBodyLostTest {
    @Rule
    public final MockWebServer server = new MockWebServer();

    @Test
    public void test() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("Content/Type")
                .setResponseCode(500)
                .setHeader("Upgrade", "websocket")
                .setHeader("Connection", "Downgrade")
                .setHeader("Sec-WebSocket-Accept", "ujmZX4KXZqjwy6vi1aQFH5p4Ygk="));

        final Request request = new Request.Builder()
                .url(server.url("/path"))
                .header("User-Agent", "Test Case")
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();

        WebSocketCall webSocketCall = WebSocketCall.create(client, request);
        final BlockingQueue<Response> responses = new LinkedBlockingDeque<>();

        webSocketCall.enqueue(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                responses.add(response);
            }

            @Override
            public void onFailure(IOException e, Response response) {
                responses.add(response);
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
                System.exit(0);
            }

            @Override
            public void onPong(Buffer payload) {}

            @Override
            public void onClose(int code, String reason) {}
        });
        Response r = responses.poll(5, TimeUnit.SECONDS);
        //assertNull(r);
       // assertNull(r.body());
        assertNotNull(r);
        assertNotNull(r.body());
        System.out.print(r.body().contentType().toString()+"contecttype");
          assertEquals("Content/Type", r.body().contentType().toString());
    }
}