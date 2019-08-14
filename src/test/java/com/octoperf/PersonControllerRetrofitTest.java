package com.octoperf;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DemoApplication.class }, webEnvironment = RANDOM_PORT)
public class PersonControllerRetrofitTest {

  @Value("${local.server.port}")
  private int port;

  private Retrofit retrofit;

  @Before
  public void setUp() {
    final OkHttpClient client = new OkHttpClient.Builder()
      .addInterceptor(new BasicAuthInterceptor("admin", "passw0rd"))
      .build();

    retrofit = new Retrofit.Builder()
      .baseUrl("http://localhost:"+port)
      .client(client)
      .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()))
      .build();
  }

  @Test
  public void shouldSayHello() throws IOException {
    final PersonApi api = retrofit.create(PersonApi.class);
    final Person person = api.johnSmith().execute().body();
    assertEquals("John", person.getFirstname());
    assertEquals("Smith", person.getLastname());
  }
}