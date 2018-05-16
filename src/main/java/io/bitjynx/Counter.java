package io.bitjynx;

class Counter {
  private int _cnt = 0;
  Counter(int start) {
    _cnt = start;
  }

  public int get() { return _cnt; }
  public void inc() { ++_cnt; }
}

