package lu.flier.script;

interface V8ContextAware {
	V8Context getContext();
	
	Object bindTo(V8Context ctxt);	
}
