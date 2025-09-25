package me.cayve.ludorium.utils;

import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

public class SourceKey {

	private final String key;
	private final String[] contexts;
	
	public SourceKey() {
		key = UUID.randomUUID().toString();
		contexts = new String[0];
	}
	
	public SourceKey(String key, String context) {
		this.key = key;
		this.contexts = new String[] { context };
	}
	
	public SourceKey(String key, String[] contexts) {
		this.key = key;
		this.contexts = contexts;
	}
	
	/**
	 * Creates a context for this key. Add more contexts by stringing .withContext()
	 * @param context
	 * @return
	 */
	public SourceKey withContext(String context) {
		if (context == null || context.isEmpty())
			return this;
		return new SourceKey(key, ArrayUtils.add(contexts, context));
	}

	/**
	 * This will check whether the internal keys match, and if sub contexts match at least all of this's.
	 * <p>
	 * For example:<br>
	 * [ 'CONTEXT1', 'CONTEXT2' ] != [ 'CONTEXT1' ]<br>
	 * [ 'CONTEXT1' ] == [ 'CONTEXT1', 'CONTEXT2' ]<br>
	 * [ 'CONTEXT3' ] != [ 'CONTEXT1', 'CONTEXT2' ]<br>
	 * [ 'CONTEXT1', 'CONTEXT2' ] == [ 'CONTEXT1', 'CONTEXT2', 'CONTEXT3' ]
	 */
	@Override
	public boolean equals(Object other) {
		SourceKey otherKey = (SourceKey) other;
		if (!key.equals(otherKey.key)) return false;

		for (int i = 0; i < contexts.length; i++)
			if (otherKey.contexts.length <= i || !contexts[i].equals(otherKey.contexts[i]))
				return false;

		return true;
	}
	
	@Override
	public String toString() {
		String contextStr = "";
		for (String s : contexts)
			contextStr += s + " ";
		return "Key: " + key + " Contexts (" + contexts.length + "): " + contextStr;
	}
}
