package com.penelopec.penelopemobileapi.shared.core.result;

public class Void {
  private static final Void INSTANCE = new Void();

  private Void() {}

  public static Void singleton() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "Void";
  }
}
