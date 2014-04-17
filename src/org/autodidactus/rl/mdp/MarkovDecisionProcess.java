package org.autodidactus.rl.mdp;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MarkovDecisionProcess {

  private final Set<State> mStates;
  private final Set<Action> mActions;

  /**
   * All edges from states to actions.
   */
  private final Map<State, Set<Action>> mActionsFromState;
  /**
   * All edges from actions back to other states.
   * Here we are concerned with efficiently picking a random state from a non-uniform distribution.
   * Moreover, we also want to keep track of rewards for landing on a state.
   */
  private final Map<Action, TreeMap<Double, Pair<State, Double>>> mStatesFromAction;

  /**
   * Construct a markov decision process (MDP) from states, actions, a map from each state
   * to available actions, and a map from each action to target states, zipped with their
   * cumulative probabilities and rewards. Use a builder which will manage the requirements.
   *
   * @param states in the MDP.
   * @param actions in the MDP.
   * @param actionsFromState a map from each state to a set of available actions.
   * @param statesFromAction a map from each action to a map from cumulative probabilities
   *        to (state, reward) pairs.
   */
  private MarkovDecisionProcess(
      final Set<State> states,
      final Set<Action> actions,
      final Map<State, Set<Action>> actionsFromState,
      final Map<Action, TreeMap<Double, Pair<State, Double>>> statesFromAction) {
    // Check not null.
    Preconditions.checkNotNull(states);
    Preconditions.checkNotNull(actions);
    Preconditions.checkNotNull(actionsFromState);
    Preconditions.checkNotNull(statesFromAction);

    // Validate that all states are represented by actionsFromState.
    Preconditions.checkArgument(states.equals(actionsFromState.keySet()));

    // Validate that all actions in actionsFromState are found in actions.
    for (Set<Action> actionsFromEachState : actionsFromState.values()) {
      Preconditions.checkNotNull(actionsFromEachState);
      // And all states pointed to are in states.
      for (Action actionFromThisState : actionsFromEachState) {
        Preconditions.checkArgument(actions.contains(actionFromThisState));
      }
    }

    // Validate that all actions are represented by statesFromAction.
    Preconditions.checkArgument(actions.equals(statesFromAction.keySet()));

    // Validate that all states in statesFromAction are found in states.
    for (TreeMap<Double, Pair<State, Double>> messOfStates : statesFromAction.values()) {
      Preconditions.checkNotNull(messOfStates);
      double previous = 0.0;
      for (Map.Entry<Double, Pair<State, Double>> entry : messOfStates.entrySet()) {
        // Cumulative probabilities aught to be in the interval (previous, 1.)
        Preconditions.checkArgument(entry.getKey() <= 1.0 && entry.getKey() > previous);
        previous = entry.getKey();
        // Target states should be declared in states.
        Preconditions.checkArgument(states.contains(entry.getValue()._1));
        // Reward can not be null.
        Preconditions.checkNotNull(entry.getValue()._2);
      }
      // Total cumulative probability should be about 1.0.
      Preconditions.checkArgument(1.0 == messOfStates.lastKey());
    }

    // Set members.
    this.mStates = states;
    this.mActions = actions;
    this.mActionsFromState = actionsFromState;
    this.mStatesFromAction = statesFromAction;
  }

  /**
   * Gets all the actions that can be taken from a state.
   *
   * @param state from which the actions can be taken.
   * @return a set of actions that can be taken from a state. Null iff state not in MDP.
   */
  public Set<Action> getActionsFromState(final State state) {
    Preconditions.checkArgument(mStates.contains(state));
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
    Preconditions.checkArgument(mStates.contains(state));
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
    return getStateRewardMapFromAction(action).keySet();
  }

  /**
   * Gets all the states from which this action can be taken. Ideally this should be one.
   * This is an expensive operation.
   *
   * @param action which can be taken by some states.
   * @return a set of states which can take this action. Null iff action not in MDP.
   */
  public Set<State> getStatesToAction(final Action action) {
    Preconditions.checkArgument(mActions.contains(action));
    ImmutableSet.Builder<State> setBuilder = new ImmutableSet.Builder<State>();
    for (State state : mStates) {
      if (getActionsFromState(state).contains(action)) {
        setBuilder.add(state);
      }
    }
    return setBuilder.build();
  }

  /**
   * Given an action, return a map from all possible resulting states and corresponding rewards.
   *
   * @param action resulting in some states with rewards.
   * @return map of states to reward having taken the given action.
   */
  public Map<State, Double> getStateRewardMapFromAction(final Action action) {
    Preconditions.checkArgument(mActions.contains(action));
    ImmutableMap.Builder<State, Double> mapBuilder = new ImmutableMap.Builder<State, Double>();
    TreeMap<Double, Pair<State, Double>> messyMapOfStates = mStatesFromAction.get(action);
    for (Pair<State, Double> pair : messyMapOfStates.values()) {
      mapBuilder.put(pair._1, pair._2);
    }
    return mapBuilder.build();
  }

  /**
   * Given an action, return a map from all possible resulting states
   * and corresponding probabilities.
   *
   * @param action resulting in some states with probabilities.
   * @return map of states to probabilities having taken the given action.
   */
  public Map<State, Double> getStateProbablityMapFromAction(final Action action) {
    Preconditions.checkArgument(mActions.contains(action));
    ImmutableMap.Builder<State, Double> mapBuilder = new ImmutableMap.Builder<State, Double>();
    TreeMap<Double, Pair<State, Double>> messyMapOfStates = mStatesFromAction.get(action);
    double previous = 0.0;
    for (Map.Entry<Double, Pair<State, Double>> entry : messyMapOfStates.entrySet()) {
      mapBuilder.put(entry.getValue()._1, entry.getKey() - previous);
      previous = entry.getKey();
    }
    return mapBuilder.build();
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

  @Override
  public String toString() {
    String out = "";
    for (State state : mStates) {
      Set<Action> actions = getActionsFromState(state);
      for (Action action : actions) {
        Map<State, Double> stateProbability = getStateProbablityMapFromAction(action);
        Map<State, Double> stateReward = getStateRewardMapFromAction(action);
        Preconditions.checkArgument(stateProbability.keySet().equals(stateReward.keySet()));
        for (State toState : stateProbability.keySet()) {
          out += String.format("%s -- (%s, P=%f, R=%f) --> %s\n",
              state,
              action,
              stateProbability.get(toState),
              stateReward.get(toState),
              toState);
        }
      }
    }
    return out;
  }

  /**
   * An action is just a special state.
   */
  public static class Action extends State {
    public Action(final String name) {
      super(name);
    }
  }

  public static void main(String[] args) {
    State a = new State("a");
    State b = new State("b");
    State c = new State("c");
    Action one = new Action("one");
    Action two = new Action("two");
    Map<State, Set<Action>> statesToActions = Maps.newHashMap();
    statesToActions.put(a, Sets.newHashSet(one));
    statesToActions.put(b, Sets.<Action>newHashSet(two));
    statesToActions.put(c, Sets.<Action>newHashSet(two, one));

    Map<Action, TreeMap<Double, Pair<State, Double>>> actionsToState = Maps.newHashMap();
    TreeMap<Double, Pair<State, Double>> twoEdge = Maps.newTreeMap();
    twoEdge.put(1.0, new Pair(new State("a"), 5.0));
    TreeMap<Double, Pair<State, Double>> oneEdge = Maps.newTreeMap();
    oneEdge.put(.1, new Pair(b, 2.0));
    oneEdge.put(1.0, new Pair(a, 3.0));
    actionsToState.put(one, oneEdge);
    actionsToState.put(new Action("two"), twoEdge);
    MarkovDecisionProcess mdp = new MarkovDecisionProcess(
        Sets.newHashSet(a, b, c),
        Sets.newHashSet(one, two),
        statesToActions,
        actionsToState
        );
    System.out.println(mdp);
    System.out.println(mdp.getStates());
    System.out.println(mdp.getActions());
    System.out.println(mdp.getStateProbablityMapFromAction(new Action("two")));
    System.out.println(mdp.getStatesToAction(new Action("one")));
  }
}
