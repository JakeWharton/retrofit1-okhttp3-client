package com.jakewharton.retrofit;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import retrofit.RestAdapter;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedString;

import static com.google.common.truth.Truth.assertThat;
import static okio.Okio.buffer;
import static okio.Okio.source;

public final class Ok3ClientIntegrationTest {
  @Rule public final MockWebServer server = new MockWebServer();

  private interface Service {
    @GET("/") Response get();
    @POST("/") Response post(@Body TypedInput body);
  }

  @Before public void setUp() {
    RestAdapter restAdapter = new RestAdapter.Builder()
        .setEndpoint(server.url("/").toString())
        .setClient(new Ok3Client())
        .build();
    service = restAdapter.create(Service.class);
  }

  private Service service;

  @Test public void get() throws InterruptedException, IOException {
    server.enqueue(new MockResponse()
        .addHeader("Hello", "World")
        .setBody("Hello!"));

    Response response = service.get();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getReason()).isEqualTo("OK");
    assertThat(response.getUrl()).isEqualTo(server.url("/").toString());
    assertThat(response.getHeaders()).contains(new Header("Hello", "World"));
    assertThat(buffer(source(response.getBody().in())).readUtf8()).isEqualTo("Hello!");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/");
  }

  @Test public void post() throws IOException, InterruptedException {
    server.enqueue(new MockResponse()
        .addHeader("Hello", "World")
        .setBody("Hello!"));

    Response response = service.post(new TypedString("Hello?"));
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getReason()).isEqualTo("OK");
    assertThat(response.getUrl()).isEqualTo(server.url("/").toString());
    assertThat(response.getHeaders()).contains(new Header("Hello", "World"));
    assertThat(buffer(source(response.getBody().in())).readUtf8()).isEqualTo("Hello!");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/");
    assertThat(request.getBody().readUtf8()).isEqualTo("Hello?");
  }
}
