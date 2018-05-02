package io.bitjynx;

import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;

class OpTask extends RecursiveTask<NumberedBlock> {
  private final int _key;
  private final IBlock _v1;
  private final IBlock _v2;
  private final BinaryOperator<IBlock> _op;

  OpTask(int key, IBlock v1, IBlock v2, BinaryOperator<IBlock> op) {
    _key = key;
    _v1 = v1;
    _v2 = v2;
    _op = op;
  }

  @Override
  protected NumberedBlock compute() {
    IBlock b = _op.apply(_v1, _v2);
    return new NumberedBlock(_key, b);
  }
}
