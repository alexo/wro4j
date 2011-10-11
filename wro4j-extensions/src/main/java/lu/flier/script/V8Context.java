package lu.flier.script;

import javax.script.Bindings;
import javax.script.SimpleScriptContext;

public class V8Context extends SimpleScriptContext
{
	private long ctxt;
	
	V8Context(long ctxt)
	{
		this.ctxt = ctxt;
	}
	
	public V8Context()
	{
		this.ctxt = internalCreate();
	}
	
	@Override
	public Bindings getBindings(int scope) {
		if (scope == ENGINE_SCOPE) {
			return getGlobal();
		}

		return super.getBindings(scope);
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {
		if (scope == ENGINE_SCOPE) {
			getGlobal().clear();
		}
		super.setBindings(bindings, scope);
	}

	@Override
	public Object getAttribute(String name, int scope) {
		if (scope == ENGINE_SCOPE) {
			return getGlobal().get(name);
		}
		
		return super.getAttribute(name, scope);
	}

	@Override
	public Object removeAttribute(String name, int scope) {
		if (scope == ENGINE_SCOPE) {
			return getGlobal().remove(name);
		}
		return super.removeAttribute(name, scope);
	}

	@Override
	public int getAttributesScope(String name) {
		if (getGlobal().containsKey(name)) {
            return ENGINE_SCOPE;
		}
		return super.getAttributesScope(name);
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		if (scope == ENGINE_SCOPE) {
			getGlobal().put(name, value);
		} else {
			super.setAttribute(name, value, scope);
		}
	}

	public void dispose()
	{
		if (this.ctxt > 0) 
		{			
			this.internalRelease(this.ctxt);
			
			this.ctxt = 0;
		}
	}
	
	
	@Override
	protected void finalize() throws Throwable 
	{
		super.finalize();
		
		this.dispose();
	}
	
	public native static V8Context getEntered();
	public native static V8Context getCurrent();
	public native static V8Context getCalling();
	public native static boolean inContext();
	
	public void enter()
	{
		internalEnter(this.ctxt);
	}
	
	public void leave()
	{
		internalLeave(this.ctxt);
	}
	
	public Object bind(Object obj) 
	{
		return obj instanceof V8ContextAware ? ((V8ContextAware) obj).bindTo(this) : obj;
	}
	
	public V8Object getGlobal()
	{
		return (V8Object) internalGetGlobal(this.ctxt).bindTo(this);
	}	

	private native long internalCreate();
	private native void internalRelease(long ctxt);
	private native void internalEnter(long ctxt);
	private native void internalLeave(long ctxt);
	private native V8Object internalGetGlobal(long ctxt);
}
