package com.mobius.software.amqp.performance.commons.util;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ReverseMap<K, V>
{
	private ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
	private ConcurrentHashMap<V, K> reverse = new ConcurrentHashMap<>();

	public V put(K key, V value)
	{
		V curr = map.put(key, value);
		reverse.put(value, key);
		return curr;
	}

	public V putIfAbsent(K key, V value)
	{
		V curr = map.putIfAbsent(key, value);
		if (curr == null)
			reverse.put(value, key);
		return curr;
	}

	public V getValue(K key)
	{
		return map.get(key);
	}

	public K getKey(V value)
	{
		return reverse.get(value);
	}

	public V remove(K key)
	{
		V curr = map.remove(key);
		if (curr != null)
			reverse.remove(curr);
		return curr;
	}

	public V removeKey(K key)
	{
		return map.remove(key);
	}
	
	public K removeValue(V value)
	{
		return reverse.remove(value);
	}
	
	public K removeByValue(V value)
	{
		K curr = reverse.remove(value);
		if (curr != null)
			map.remove(curr);
		return curr;
	}

	public boolean containsKey(K key)
	{
		return map.containsKey(key);
	}

	public boolean containsValue(V value)
	{
		return reverse.containsKey(value);
	}

	public Iterator<Entry<K, V>> interator()
	{
		return map.entrySet().iterator();
	}
	
	public void clear() 
	{
		map.clear();
		reverse.clear();
	}
}
