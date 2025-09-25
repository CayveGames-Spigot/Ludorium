package me.cayve.ludorium.games.events;

public class ActionChoiceEvent<T> extends InstanceEvent {

	private T[] choices;
	
	public ActionChoiceEvent(int playerTurn, T[] choices) {
		super(playerTurn);
		this.choices = choices;
	}

	public T[] getActionChoices() { return choices; }
}
