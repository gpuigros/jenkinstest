package com.octoperf;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
class DemoExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({ DemoException.class })
  protected ResponseEntity<Object> handleNotFound(
    Exception ex, WebRequest request) {
    return handleExceptionInternal(ex, "Demo Exception Encountered",
      new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }
}