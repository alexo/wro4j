package lu.flier.script;

class ManagedV8Object {
	protected long obj;
	
	public ManagedV8Object(long obj) {
		this.obj = obj;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		
		this.internalRelease(this.obj);		
		
		this.obj = 0;
	}
	
	protected native void internalRelease(long ptr);
}
