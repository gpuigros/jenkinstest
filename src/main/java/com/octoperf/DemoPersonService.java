package com.octoperf;

import org.springframework.stereotype.Service;

@Service
final class DemoPersonService implements PersonService {
  @Override
  public Person johnSmith() {
    final Person person = new Person();
    person.setFirstname("John");
    person.setLastname("Smith");
    return person;
  }

  @Override
  public String hello(final Person person) {
    return "Hello " + person.getFirstname() + " " + person.getLastname() + "!";
  }
}