package org.autodidactus.rl.mdp;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A state is a unique object in a machine.
 * Each state may transition to some other states.
 */
public class State {
  /**
   * Name of state.
   */
  private final String mName;

  /**
   * Construct state with name.
   *
   * @param name of the state (can not be null).
   */
  public State(final String name) {
    Preconditions.checkNotNull(name);
    mName = name;
  }

  /**
   * Gets the name of the state.
   * Every state in a state machine should have a unique name.
   *
   * @return name of the state.
   */
  public String getName() {
    return this.mName;
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
    State other = (State) o;
    return Objects.equal(this.getName(), other.getName());
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hashCode(this.getName());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("%s(%s)", this.getClass().getSimpleName().toString(), this.getName());
  }
}
