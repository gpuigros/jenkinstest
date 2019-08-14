package com.octoperf;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PersonApi {

  @GET("/person/johnsmith")
  Call<Person> johnSmith();
}