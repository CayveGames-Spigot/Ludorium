package me.cayve.ludorium.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateMachine {

	public class State {
		private StateMachine stateMachine;
		
		public String ID;
		private Runnable regressAction;
		private Runnable progressAction;
		private Runnable action;
		private Runnable completeAction;
		private Runnable incompleteAction;
		
		public State(String ID, StateMachine stateMachine) {
			this.ID = ID;
			this.stateMachine = stateMachine;
		}
		
		private void runRegress() { if (regressAction != null) regressAction.run(); }
		private void runProgress() { if (progressAction != null) progressAction.run(); }
		private void runAction() { if (action != null) action.run(); }
		private void runComplete() { if (completeAction != null) completeAction.run(); }
		private void runIncomplete() { if (incompleteAction != null) incompleteAction.run(); }
		
		/**
		 * Run before action if the state has been regressed back to this state
		 * @param action
		 * @return
		 */
		public State registerRegress(Runnable action) {
			regressAction = action;
			return this;
		}
		
		/**
		 * Run before action if the state has been progressed to this state
		 * @param action
		 * @return
		 */
		public State registerProgress(Runnable action) {
			progressAction = action;
			return this;
		}
		
		/**
		 * The main action to run for this state
		 * @param action
		 * @return
		 */
		public State registerAction(Runnable action) {
			this.action = action;
			return this;
		}
		
		/**
		 * Run before the state progressed to the next one
		 * @param action
		 * @return
		 */
		public State registerComplete(Runnable action) {
			completeAction = action;
			return this;
		}
		
		/**
		 * Run before the state is regressed to the previous one
		 * @param action
		 * @return
		 */
		public State registerIncomplete(Runnable action) {
			incompleteAction = action;
			return this;
		}
		
		private State copy(String newID) {
			return new State(newID, stateMachine)
					.registerRegress(regressAction)
					.registerProgress(progressAction)
					.registerAction(action)
					.registerComplete(completeAction)
					.registerIncomplete(incompleteAction);
		}
		
		/**
		 * This allows you to chain state registration in your state machine registration
		 * @return
		 */
		public StateMachine buildState() { return stateMachine; }
	}
	
	private ArrayList<State> states = new ArrayList<>();
	private Map<String, Integer> contextualIndex = new HashMap<String, Integer>();
	private int index = -1;
	private boolean isComplete = false;
	
	public int getStateIndex() { return index; }
	public boolean hasStarted() { return index != -1; }
	/**
	 * Creates a new state.
	 * Call buildState() to return to the StateMachine chain
	 * @return
	 */
	public State newState() 
	{ 
		return newState(UUID.randomUUID().toString());
	}
	
	/**
	 * Creates a new state with an ID.
	 * Call buildState() to return to the StateMachine chain
	 * @param ID
	 * @return
	 */
	public State newState(String ID) 
	{ 
		State newState = new State(ID, this);
		
		states.add(newState);
		return newState;
	}
	
	/**
	 * Copies a given state and all of its callbacks.
	 * Call buildState() to return to the StateMachine chain
	 * @param ID
	 * @return
	 */
	public State copyState(String ID) {
		return copyState(ID, UUID.randomUUID().toString());
	}
	
	/**
	 * Copies a given state and all of its callbacks, and gives the copy a new ID.
	 * Call buildState() to return to the StateMachine chain
	 * @param ID
	 * @param newID
	 * @return
	 */
	public State copyState(String ID, String newID) {
		State newState = findState(ID).copy(newID);
		
		states.add(newState);
		return newState;
	}
	
	/**
	 * Finds a state with an ID
	 * @param ID
	 * @return
	 */
	public State findState(String ID) {
		for (State state : states)
			if (state.ID.equals(ID))
				return state;
		return null;
	}
	
	/**
	 * Whether this state machine is complete (inactive)
	 * @return
	 */
	public boolean isComplete() { return isComplete; }
	
	/**
	 * Progresses to the next state
	 */
	public void next() { skipTo(index + 1); }
	/**
	 * Regresses to the previous state
	 */
	public void previous() { skipTo(index - 1); }
	
	/**
	 * Marks the state machine as complete. This will be checked after each action is run.
	 * (e.g. If a completeAction marks the machine as complete, the rest of the actions will not run)
	 */
	public void complete() { isComplete = true; }
	/**
	 * Skips to a specific state by its ID
	 * If the state is not found, the state machine will self-complete
	 * @param ID
	 */
	public void skipTo(String ID) {
		skipTo(states.indexOf(findState(ID)));
	}
	private void skipTo(int state) {

		if (isComplete) return;
		
		boolean progressed = state >= index;
		
		if (hasStarted()) {
			if (progressed)
				states.get(index).runComplete();
			else
				states.get(index).runIncomplete();
			
			if (isComplete) return; //Check after each action is run
		}
		
		index = state;
		
		//If the state machine goes outside of bounds, mark it as complete
		if (index < 0 || index >= states.size())
		{
			isComplete = true;
			return;
		}
		
		if (progressed)
			states.get(index).runProgress();
		else
			states.get(index).runRegress();
		
		if (isComplete) return; //Check after each action is run
		
		states.get(index).runAction();
	}
	
	/**
	 * Contextual indexes link to the state they were first call at.
	 * If a state creates a new contextual index at, for example, state index of 3,
	 * calling this method again anywhere in state 3 will return 0. Calling the same index
	 * in state 4 would return 1 and so on. 
	 * 
	 * This is used for incremental logic used in states, without worrying about rearranging state order. 
	 * For example, if the order of colors needs to increment with each new state, 
	 * but you are copying state logic, the logic need only use contextualIndex("color")
	 * @param ID
	 * @return
	 */
	public int contextualIndex(String ID) {
		if (!contextualIndex.containsKey(ID))
			contextualIndex.put(ID, index);
		
		return index - contextualIndex.get(ID);
	}
}
