package io.bitmagic;

import static io.bitmagic.BitVectorTest.platform_test;

public class Timer {

  public Timer(String testName, int repeats) {
    _testName = testName;
    _repeats = repeats;
    _start = System.currentTimeMillis();
  }

  public void report() {
    _finish = System.currentTimeMillis();
    long elapsed = _finish - _start;
    double duration = (double)(_finish - _start) / 1000;

    System.out.print(_testName + " ; ");
    if (platform_test) {
      System.out.println(duration);
      return;
    }

    System.out.print(elapsed + ";" + duration + ";");
    if (_repeats > 0)
    {
      double ops_per_sec = (double)_repeats / duration;
      System.out.print(ops_per_sec);
    }
    System.out.println();
  }

  private String  _testName;
  private long _start;
  private long _finish;
  private int _repeats;
};

