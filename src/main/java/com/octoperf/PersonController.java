package com.octoperf;

import lombok.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.ConstructorProperties;

@RestController
@RequestMapping({"/person"})
class PersonController {
  @NonNull
  private final PersonService persons;

  @GetMapping({"/johnsmith"})
  public Person hello() {
    return this.persons.johnSmith();
  }

  @PostMapping({"/hello"})
  public String postHello(@RequestBody Person person) {
    return this.persons.hello(person);
  }

  @ConstructorProperties({"persons"})
  PersonController(@NonNull PersonService persons) {
    if (persons == null) {
      throw new NullPointerException("persons");
    } else {
      this.persons = persons;
    }
  }

  @GetMapping("/exception")
  public void exception() throws DemoException {
    throw new DemoException();
  }
}
