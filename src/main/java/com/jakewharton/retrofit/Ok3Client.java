package com.jakewharton.retrofit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public final class Ok3Client implements Client {
  private final Call.Factory client;
  private static final byte[] NO_BODY = new byte[0];

  public Ok3Client() {
    this(new OkHttpClient());
  }

  public Ok3Client(OkHttpClient client) {
    this((Call.Factory) client);
  }

  public Ok3Client(Call.Factory client) {
    if (client == null) {
      throw new NullPointerException("client == null");
    }
    this.client = client;
  }

  @Override public Response execute(Request request) throws IOException {
    return parseResponse(client.newCall(createRequest(request)).execute());
  }

  static okhttp3.Request createRequest(Request request) {
    RequestBody requestBody;
    if (requiresRequestBody(request.getMethod()) && request.getBody() == null) {
      requestBody = RequestBody.create(null, NO_BODY);
    } else {
      requestBody = createRequestBody(request.getBody());
    }

    okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
        .url(request.getUrl())
        .method(request.getMethod(), requestBody);

    List<Header> headers = request.getHeaders();
    for (int i = 0, size = headers.size(); i < size; i++) {
      Header header = headers.get(i);
      String value = header.getValue();
      if (value == null) {
        value = "";
      }
      builder.addHeader(header.getName(), value);
    }

    return builder.build();
  }

  static Response parseResponse(okhttp3.Response response) {
    return new Response(response.request().url().toString(), response.code(), response.message(),
        createHeaders(response.headers()), createResponseBody(response.body()));
  }

  private static RequestBody createRequestBody(final TypedOutput body) {
    if (body == null) {
      return null;
    }
    final MediaType mediaType = MediaType.parse(body.mimeType());
    return new RequestBody() {
      @Override public MediaType contentType() {
        return mediaType;
      }

      @Override public void writeTo(BufferedSink sink) throws IOException {
        body.writeTo(sink.outputStream());
      }

      @Override public long contentLength() {
        return body.length();
      }
    };
  }

  private static TypedInput createResponseBody(final ResponseBody body) {
    if (body.contentLength() == 0) {
      return null;
    }
    return new TypedInput() {
      @Override public String mimeType() {
        MediaType mediaType = body.contentType();
        return mediaType == null ? null : mediaType.toString();
      }

      @Override public long length() {
        return body.contentLength();
      }

      @Override public InputStream in() throws IOException {
        return body.byteStream();
      }
    };
  }

  private static List<Header> createHeaders(Headers headers) {
    int size = headers.size();
    List<Header> headerList = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      headerList.add(new Header(headers.name(i), headers.value(i)));
    }
    return headerList;
  }

  private static boolean requiresRequestBody(String method) {
    return "POST".equals(method)
           || "PUT".equals(method)
           || "PATCH".equals(method)
           || "PROPPATCH".equals(method)
           || "REPORT".equals(method);
  }
}
