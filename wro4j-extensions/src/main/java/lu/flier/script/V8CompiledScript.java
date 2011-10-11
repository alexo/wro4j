package lu.flier.script;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class V8CompiledScript extends CompiledScript
{
	private final V8ScriptEngine engine;
	
	private long data; 
	
	V8CompiledScript(V8ScriptEngine engine, String script) throws Exception 
	{
		this.engine = engine;
		
		this.data = internalCompile(script);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		
		this.internalRelease(this.data);	
		
		this.data = 0;
	}
	
	private native long internalCompile(String String) throws Exception;
	
	private native void internalRelease(long ptr);
	
	private native Object internalExecute(long compiledScript, ScriptContext context) throws Exception;
	
    @Override
    public Object eval(ScriptContext context) throws ScriptException
    {
    	try {
			return this.engine.getV8Context().bind(this.internalExecute(this.data, context));
		} catch (Exception e) {
			throw new ScriptException(e);
		}
    }

    @Override
    public ScriptEngine getEngine()
    {
        return this.engine;
    }
}
