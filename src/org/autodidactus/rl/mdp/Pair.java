package org.autodidactus.rl.mdp;

import com.google.common.base.Objects;

public class Pair<A, B> {
  public final A _1;
  public final B _2;
  public Pair(A a, B b) {
    _1 = a;
    _2 = b;
  }
  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (null == o) {
      return false;
    }
    if (!o.getClass().equals(this.getClass())) {
      return false;
    }
    Pair other = (Pair) o;
    System.out.println(other._1.getClass());
    System.out.println(this._1.getClass());
    return Objects.equal(this._1, other._1) && Objects.equal(this._2, other._2);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hashCode(_1, _2);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("(%s, %s)", _1.toString(), _2.toString());
  }
}
