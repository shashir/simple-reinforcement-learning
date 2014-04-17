package org.autodidactus.rl.mdp;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MarkovDecisionProcess {

  private final Set<State> mStates = Sets.<State>newHashSet();
  private final Set<Action> mActions = Sets.<Action>newHashSet();
  /**
   * All edges from states to actions.
   */
  private final Map<State, Set<Action>> mActionsFromState =
      Maps.<State, Set<Action>>newHashMap();
  /**
   * All edges from actions back to other states.
   * Here we are concerned with efficiently picking a random state from a non-uniform distribution.
   * Moreover, we also want to keep track of rewards for landing on a state.
   */
  private final Map<Action, TreeMap<Double, Pair<State, Double>>> mStatesFromAction =
      Maps.<Action, TreeMap<Double, Pair<State, Double>>>newHashMap();

  /**
   * Gets all the actions that can be taken from a state.
   *
   * @param state from which the actions can be taken.
   * @return a set of actions that can be taken from a state. Null iff state not in MDP.
   */
  public Set<Action> getActionsFromState(final State state) {
    if (!mStates.contains(state)) {
      return null;
    }
    return ImmutableSet.copyOf(mActionsFromState.get(state));
  }

  /**
   * Gets all the actions that can be taken to reach a state.
   * This is an expensive operation.
   *
   * @param state to reach
   * @return a set of actions that can be taken to reach a state. Null iff state not in MDP.
   */
  public Set<Action> getActionsToState(final State state) {
    if (!mStates.contains(state)) {
      return null;
    }
    ImmutableSet.Builder<Action> setBuilder = new ImmutableSet.Builder<Action>();
    for (Action action : mActions) {
      if (getStatesFromAction(action).contains(state)) {
        setBuilder.add(action);
      }
    }
    return setBuilder.build();
  }

  /**
   * Gets all the states that can reached by taking this action.
   * This is a very expensive operation.
   *
   * @param action by which some states are reached.
   * @return a set of states that can be reached by taking this action. Null iff action not in MDP.
   */
  public Set<State> getStatesFromAction(final Action action) {
    if (!mActions.contains(action)) {
      return null;
    }
    ImmutableSet.Builder<State> setBuilder = new ImmutableSet.Builder<State>();
    TreeMap<Double, Pair<State, Double>> messyMapOfStates = mStatesFromAction.get(action);
    for (Pair<State, Double> pair : messyMapOfStates.values()) {
      setBuilder.add(pair._1);
    }
    return setBuilder.build();
  }

  /**
   * Gets all the states from which this action can be taken. Ideally this should be one.
   * This is an expensive operation.
   *
   * @param action which can be taken by some states.
   * @return a set of states which can take this action. Null iff action not in MDP.
   */
  public Set<State> getStatesToAction(final Action action) {
    if (!mActions.contains(action)) {
      return null;
    }
    ImmutableSet.Builder<State> setBuilder = new ImmutableSet.Builder<State>();
    for (State state : mStates) {
      if (getActionsFromState(state).contains(action)) {
        setBuilder.add(state);
      }
    }
    return setBuilder.build();
  }

  /**
   * Gets all states in MDP.
   *
   * @return all states in MDP.
   */
  public Set<State> getStates() {
    return ImmutableSet.copyOf(mStates);
  }

  /**
   * Gets all actions in MDP.
   *
   * @return all actions in MDP.
   */
  public Set<Action> getActions() {
    return ImmutableSet.copyOf(mActions);
  }

  /**
   * An action is just a special state.
   */
  public static class Action extends State {
    public Action(String name) {
      super(name);
    }
  }
}
