/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
 package main.internal;


/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class MulepluginaConnection {

  private final String id;

  public MulepluginaConnection(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void invalidate() {
    // do something to invalidate this connection!
  }
}
