// Copyright 2014 Square, Inc.
package com.jakewharton.retrofit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.junit.Test;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedString;

import static com.google.common.truth.Truth.assertThat;
import static okio.Okio.buffer;
import static okio.Okio.source;

public final class Ok3ClientTest {
  private static final String HOST = "http://example.com";

  @Test public void get() {
    Request request = new Request("GET", HOST + "/foo/bar/?kit=kat", null, null);
    okhttp3.Request okRequest = Ok3Client.createRequest(request);

    assertThat(okRequest.method()).isEqualTo("GET");
    assertThat(okRequest.url().toString()).isEqualTo(HOST + "/foo/bar/?kit=kat");
    assertThat(okRequest.headers().size()).isEqualTo(0);
    assertThat(okRequest.body()).isNull();
  }

  @Test public void post() throws IOException {
    TypedString body = new TypedString("hi");
    Request request = new Request("POST", HOST + "/foo/bar/", null, body);
    okhttp3.Request okRequest = Ok3Client.createRequest(request);

    assertThat(okRequest.method()).isEqualTo("POST");
    assertThat(okRequest.url().toString()).isEqualTo(HOST + "/foo/bar/");
    assertThat(okRequest.headers().size()).isEqualTo(0);

    RequestBody okBody = okRequest.body();
    assertThat(okBody).isNotNull();

    Buffer buffer = new Buffer();
    okBody.writeTo(buffer);
    assertThat(buffer.readUtf8()).isEqualTo("hi");
  }

  @Test public void headers() {
    List<Header> headers = new ArrayList<>();
    headers.add(new Header("kit", "kat"));
    headers.add(new Header("foo", "bar"));
    headers.add(new Header("ping", null));
    Request request = new Request("GET", HOST + "/this/", headers, null);
    okhttp3.Request okRequest = Ok3Client.createRequest(request);

    Headers okHeaders = okRequest.headers();
    assertThat(okHeaders.size()).isEqualTo(3);
    assertThat(okHeaders.get("kit")).isEqualTo("kat");
    assertThat(okHeaders.get("foo")).isEqualTo("bar");
    assertThat(okHeaders.get("ping")).isEqualTo("");
  }

  @Test public void response() throws IOException {
    okhttp3.Response okResponse = new okhttp3.Response.Builder()
        .code(200).message("OK")
        .body(new TestResponseBody("hello", "text/plain"))
        .addHeader("foo", "bar")
        .addHeader("kit", "kat")
        .protocol(Protocol.HTTP_1_1)
        .request(new okhttp3.Request.Builder()
            .url(HOST + "/foo/bar/")
            .get()
            .build())
        .build();
    Response response = Ok3Client.parseResponse(okResponse);

    assertThat(response.getUrl()).isEqualTo(HOST + "/foo/bar/");
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getReason()).isEqualTo("OK");
    assertThat(response.getHeaders()) //
        .containsExactly(new Header("foo", "bar"), new Header("kit", "kat"));
    TypedInput responseBody = response.getBody();
    assertThat(responseBody.mimeType()).isEqualTo("text/plain");
    assertThat(buffer(source(responseBody.in())).readUtf8()).isEqualTo("hello");
  }

  @Test public void responseNoContentType() throws IOException {
    okhttp3.Response okResponse = new okhttp3.Response.Builder()
        .code(200).message("OK")
        .body(new TestResponseBody("hello", null))
        .addHeader("foo", "bar")
        .addHeader("kit", "kat")
        .protocol(Protocol.HTTP_1_1)
        .request(new okhttp3.Request.Builder()
            .url(HOST + "/foo/bar/")
            .get()
            .build())
        .build();
    Response response = Ok3Client.parseResponse(okResponse);

    assertThat(response.getUrl()).isEqualTo(HOST + "/foo/bar/");
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getReason()).isEqualTo("OK");
    assertThat(response.getHeaders()) //
        .containsExactly(new Header("foo", "bar"), new Header("kit", "kat"));
    TypedInput responseBody = response.getBody();
    assertThat(responseBody.mimeType()).isNull();
    assertThat(buffer(source(responseBody.in())).readUtf8()).isEqualTo("hello");
  }

  @Test public void emptyResponse() throws IOException {
    okhttp3.Response okResponse = new okhttp3.Response.Builder()
        .code(200)
        .message("OK")
        .body(new TestResponseBody("", null))
        .addHeader("foo", "bar")
        .addHeader("kit", "kat")
        .protocol(Protocol.HTTP_1_1)
        .request(new okhttp3.Request.Builder()
            .url(HOST + "/foo/bar/")
            .get()
            .build())
        .build();
    Response response = Ok3Client.parseResponse(okResponse);

    assertThat(response.getUrl()).isEqualTo(HOST + "/foo/bar/");
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getReason()).isEqualTo("OK");
    assertThat(response.getHeaders()) //
        .containsExactly(new Header("foo", "bar"), new Header("kit", "kat"));
    assertThat(response.getBody()).isNull();
  }

  private static final class TestResponseBody extends ResponseBody {
    private final Buffer buffer;
    private final String contentType;

    private TestResponseBody(String content, String contentType) {
      this.buffer = new Buffer().writeUtf8(content);
      this.contentType = contentType;
    }

    @Override public MediaType contentType() {
      return contentType == null ? null : MediaType.parse(contentType);
    }

    @Override public long contentLength() {
      return buffer.size();
    }

    @Override public BufferedSource source() {
      return buffer.clone();
    }
  }
}
