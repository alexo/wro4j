/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;


/**
 * Used to evaluate javascript on the serverside using rhino javascript engine. Encapsulate and hides all implementation
 * details used by rhino to evaluate javascript on the serverside.
 *
 * @author Alex Objelean
 */
public class RhinoScriptBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(RhinoScriptBuilder.class);
  private Context context;
  private ScriptableObject scope;

  final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("jav8");
  final Compilable compilable = (Compilable) scriptEngine;

  final StringBuffer sb = new StringBuffer();

  private RhinoScriptBuilder() {
    this(null);
  }

  private RhinoScriptBuilder(final ScriptableObject scope) {
    //this.scope = createContext(scope);
    try {
      final InputStream script = getClass().getResourceAsStream("commons.js");
      sb.append(IOUtils.toString(script));
    } catch (final IOException e) {
      throw new WroRuntimeException("", e);
    }
  }

  public static void main(final String[] args) throws Exception {
    final ScriptEngineManager engineManager = new ScriptEngineManager();
    System.out.println(engineManager.getEngineFactories());
    final ScriptEngine scriptEngine = engineManager.getEngineByName("js");

    final Compilable c = (Compilable) scriptEngine;
    final CompiledScript script = c.compile("print('Hello World')");    //compile

    script.eval();
  }

  /**
   * @return the context
   */
  public ScriptableObject getScope() {
    return this.scope;
  }

  /**
   * Initialize the context.
   */
  private ScriptableObject createContext(final ScriptableObject initialScope) {
    // remove any existing context.
    this.context = ContextFactory.getGlobal().enterContext();
    context.setOptimizationLevel(-1);
    // TODO redirect errors from System.err to LOG.error()
    context.setErrorReporter(new ToolErrorReporter(false));
    context.setLanguageVersion(Context.VERSION_1_7);
    InputStream script = null;
    final ScriptableObject scope = (ScriptableObject) context.initStandardObjects(initialScope);
    try {
      script = getClass().getResourceAsStream("commons.js");

      compilable.compile(new InputStreamReader(script));

      //context.evaluateReader(scope, new InputStreamReader(script), "common.js", 1, null);
    //} catch (final IOException e) {
    } catch (final Exception e) {
      throw new RuntimeException("Problem while evaluationg commons script.", e);
    } finally {
      IOUtils.closeQuietly(script);
    }
    return scope;
  }


  /**
   * Add a clinet side environment to the script context (client-side aware).
   *
   * @return {@link RhinoScriptBuilder} used to chain evaluation of the scripts.
   * @throws IOException
   */
  public RhinoScriptBuilder addClientSideEnvironment() {
    try {
      final String SCRIPT_ENV = "env.rhino-1.2.js";
      final InputStream script = getClass().getResourceAsStream(SCRIPT_ENV);

      sb.append(IOUtils.toString(script));

      //evaluate(script, SCRIPT_ENV);
      return this;
    } catch (final IOException e) {
      throw new RuntimeException("Couldn't initialize env.rhino script", e);
    }
  }


  public RhinoScriptBuilder addJSON() {
    try {
      final String SCRIPT_ENV = "json2.min.js";
      final InputStream script = getClass().getResourceAsStream(SCRIPT_ENV);

      sb.append(IOUtils.toString(script));

//      evaluate(script, SCRIPT_ENV);
      return this;
    } catch (final IOException e) {
      throw new RuntimeException("Couldn't initialize json2.min.js script", e);
    }
  }


  /**
   * Evaluates a script and return {@link RhinoScriptBuilder} for a chained script evaluation.
   *
   * @param stream {@link InputStream} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return {@link RhinoScriptBuilder} chain with required script evaluated.
   * @throws IOException if the script couldn't be retrieved.
   */
  public RhinoScriptBuilder evaluateChain(final InputStream stream, final String sourceName)
    throws IOException {
    Validate.notNull(stream);
    try {
      sb.append(IOUtils.toString(stream));
      //context.evaluateReader(scope, new InputStreamReader(stream), sourceName, 1, null);
      return this;
    } finally {
      stream.close();
    }
  }


  /**
   * Evaluates a script and return {@link RhinoScriptBuilder} for a chained script evaluation.
   *
   * @param script the string representation of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public RhinoScriptBuilder evaluateChain(final String script, final String sourceName) {
    Validate.notNull(script);
    sb.append(script);
    //context.evaluateString(scope, script, sourceName, 1, null);
    return this;
  }


  /**
   * Evaluates a script from a stream.
   *
   * @param script {@link InputStream} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final InputStream stream, final String sourceName)
    throws IOException {
    Validate.notNull(stream);
    try {
      sb.append(IOUtils.toString(stream));
      final CompiledScript script = compilable.compile(sb.toString());
      return script.eval();
    } catch (final ScriptException e) {
      LOG.error("JavaScriptException occured: " + e.getMessage());
      throw new WroRuntimeException("JavaScriptException occured", e);
    } finally {
      stream.close();
    }
  }


  /**
   * Evaluates a script from a reader.
   *
   * @param reader {@link Reader} of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final Reader reader, final String sourceName)
    throws IOException {
    Validate.notNull(reader);
    try {
      sb.append(IOUtils.toString(reader));
      final CompiledScript script = compilable.compile(sb.toString());
      return script.eval();
    } catch (final ScriptException e) {
      LOG.error("JavaScriptException occured: " + e.getMessage());
      throw new WroRuntimeException("JavaScriptException occured", e);
    } finally {
      reader.close();
    }
  }


  /**
   * Evaluates a script.
   *
   * @param script string representation of the script to evaluate.
   * @param sourceName the name of the evaluated script.
   * @return evaluated object.
   * @throws IOException if the script couldn't be retrieved.
   */
  public Object evaluate(final String script, final String sourceName) {
    Validate.notNull(script);
    try {
      sb.append(script);
      //System.out.println("compile script: " + sb);
      final CompiledScript compiledScript = compilable.compile(sb.toString());
      return compiledScript.eval();
    } catch (final ScriptException e) {
      LOG.error("JavaScriptException occured: " + e.getMessage());
      throw new WroRuntimeException("JavaScriptException occured", e);
    }
  }


  /**
   * @return default {@link RhinoScriptBuilder} for script evaluation chaining.
   */
  public static RhinoScriptBuilder newChain() {
    return new RhinoScriptBuilder();
  }

  public static RhinoScriptBuilder newChain(final ScriptableObject scope) {
    return new RhinoScriptBuilder(scope);
  }


  /**
   * @return default {@link RhinoScriptBuilder} for script evaluation chaining.
   */
  public static RhinoScriptBuilder newClientSideAwareChain() {
    return new RhinoScriptBuilder().addClientSideEnvironment();
  }
}
