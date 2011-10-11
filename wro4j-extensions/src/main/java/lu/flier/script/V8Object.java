package lu.flier.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

public class V8Object extends ManagedV8Object implements Bindings, V8ContextAware
{
	protected V8Context ctxt;

	public V8Object(long obj) {
		super(obj);
		
		this.ctxt = V8Context.getCurrent();
	}

	@Override
	public V8Context getContext() {
		return ctxt;
	}

	@Override
	public Object bindTo(V8Context ctxt) {
		this.ctxt = ctxt;
		
		return this;
	}

	private native String[] internalGetKeys();
	
	@Override
	public native int size();

	@Override
	public native boolean isEmpty();

	@Override
	public native void clear();

	@Override
	public native boolean containsKey(Object key);

	@Override
	public Object get(Object key) {
		return this.ctxt.bind(internalGet(key));
	}
	
	@Override
	public Object put(String name, Object value) {
		return this.ctxt.bind(internalPut(name, value));
	}

	@Override
	public Object remove(Object key) {
		return this.ctxt.bind(internalRemove(key));
	}

	private native Object internalGet(Object key);
	private native Object internalPut(String name, Object value);
	private native Object internalRemove(Object key);

	@Override
	public Set<String> keySet() {
		Set<String> keys = new HashSet<String>();
		
		for (String key : internalGetKeys()) {
			keys.add(key);
		}
		
		return keys;
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean containsValue(Object value) {
		for (String name : internalGetKeys()) {		
			Object v = this.get(name);
			
			if (v == null || value == null) {
				if (v == value) return true;
			} else if (v.getClass() == value.getClass()) {
				if (0 == ((Comparable) value).compareTo(this.get(name))) {
					return true;					
				}
			}			
		}
		return false;
	}

	@Override
	public Collection<Object> values() {
		Collection<Object> values = new ArrayList<Object>();
		
		for (String name : internalGetKeys()) {
			values.add(get(name));
		}
		
		return values;
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>();
		
		for (String name : internalGetKeys()) {
			entries.add(new HashMap.SimpleEntry<String, Object>(name, get(name)));
		}
		
		return entries;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		for (Map.Entry<? extends String, ? extends Object> entry: toMerge.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}
}
