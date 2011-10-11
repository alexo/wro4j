package lu.flier.script;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public final class V8ScriptEngine extends AbstractScriptEngine implements Invocable, Compilable
{
    private final V8ScriptEngineFactory factory;

    V8ScriptEngine(V8ScriptEngineFactory factory)
    {
        assert factory != null;

        this.factory = factory;        
        this.context = new V8Context();
        	
        getV8Context().enter();

        Bindings scope = getBindings(ScriptContext.ENGINE_SCOPE);

        scope.put(ENGINE, factory.getEngineName());
        scope.put(ENGINE_VERSION, factory.getEngineVersion());
        scope.put(NAME, factory.getName());
        scope.put(LANGUAGE, factory.getLanguageName());
        scope.put(LANGUAGE_VERSION, factory.getLanguageVersion());
    }    

	public V8Context getV8Context()
    {
    	return (V8Context) this.context;
    }
        
    private String readAll(Reader reader) throws IOException
    {
    	StringBuilder sb = new StringBuilder();
    	
    	char[] buffer = new char[8192];
    	int read;

    	while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
		    sb.append(buffer, 0, read);
		}
			
		return sb.toString();
    }    
    
    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException
    {
    	if (script == null) throw new IllegalArgumentException("empty script");
    	
        try {
			return new V8CompiledScript(this, script).eval(context);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException
    {    	
        try {
			return eval(readAll(reader), context);
		} catch (IOException e) {
			throw new ScriptException(e);
		}
    }

    @Override
    public Bindings createBindings()
    {
    	return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory()
    {
        return this.factory;
    }    

	@Override
	public CompiledScript compile(String script) throws ScriptException 
	{	
		try {
			return new V8CompiledScript(this, script);
		} catch (Exception e) {
			throw new ScriptException(e); 
		} 
	}

	@Override
	public CompiledScript compile(Reader script) throws ScriptException {
		try {
			return compile(readAll(script));
		} catch (IOException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public Object invokeMethod(Object thiz, String name, Object... args)
			throws ScriptException, NoSuchMethodException 
	{
		if (thiz instanceof V8Object) {
			return ((V8Function) ((V8Object) thiz).get(name)).invoke(args);
		}
		
		Class<?>[] types = new Class<?>[args.length];
		
		for (int i=0; i<args.length; i++) {
			types[i] = args[i].getClass();
		}
				
		Method method = thiz.getClass().getMethod(name, types);
		
		if (method == null) throw new NoSuchMethodException(name);
		
		try {
			return method.invoke(thiz, args);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public Object invokeFunction(String name, Object... args)
			throws ScriptException, NoSuchMethodException 
	{		
		V8Function func = (V8Function) getV8Context().getGlobal().get(name);
		
		if (func == null) throw new NoSuchMethodException(name);
		
		return func.invoke(args);
	}

	@Override
	public <T> T getInterface(Class<T> clasz) {		
        if (clasz == null || !clasz.isInterface()) {
            throw new IllegalArgumentException("interface Class expected");
        }
        
		return clasz.cast(Proxy.newProxyInstance(clasz.getClassLoader(), new Class<?>[] { clasz }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				
				return invokeFunction(method.getName(), args);
			}
			
		}));
	}

	@Override
	public <T> T getInterface(final Object thiz, Class<T> clasz) {
        if (thiz == null) {
            throw new IllegalArgumentException("script object can not be null");
        }
        if (clasz == null || !clasz.isInterface()) {
            throw new IllegalArgumentException("interface Class expected");
        }
        
		return clasz.cast(Proxy.newProxyInstance(clasz.getClassLoader(), new Class<?>[] { clasz }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				
				return invokeMethod(thiz, method.getName(), args);
			}
			
		}));
	}

	public native static void gc();

	/**
	 * Optional notification that the system is running low on memory.
	 * V8 uses these notifications to attempt to free memory.
	 */
	public native static void lowMemory();
	
    /**
     * Optional notification that the embedder is idle.
     * V8 uses the notification to reduce memory footprint.
     * This call can be used repeatedly if the embedder remains idle.
     * Returns true if the embedder should stop calling IdleNotification
     * until real work has been done.  This indicates that V8 has done
     * as much cleanup as it will be able to do.
     */
	public native static boolean idle();
}
