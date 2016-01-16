Retrofit 1 OkHttp 3 Client
==========================

A OkHttp 3 client implementation for Retrofit 1.



Usage
-----

Create an instance of `Ok3Client` wrapping your `OkHttpClient` or `Call.Factory` and pass it to
`setClient`.

```java
OkHttpClient client = // ...
RestAdapter restAdapter = new RestAdapter.Builder()
    .setClient(new Ok3Client(client))
    .setEndpoint(..)
    .build()
```

You can also use the no-arg constructor for a default `OkHttpClient` instance.



Download
--------

Gradle:
```groovy
compile 'com.jakewharton.retrofit:retrofit1-okhttp3-client:1.0.2'
```
or Maven:
```xml
<dependency>
  <groupId>com.jakewharton.retrofit</groupId>
  <artifactId>retrofit1-okhttp3-client</artifactId>
  <version>1.0.2</version>
</dependency>
```



License
-------

    Copyright 2016 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
