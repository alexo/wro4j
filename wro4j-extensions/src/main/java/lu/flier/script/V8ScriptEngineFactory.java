package lu.flier.script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

@SuppressWarnings("serial")
public class V8ScriptEngineFactory implements ScriptEngineFactory
{
    static
    {
        try
        {
            loadLibrary("jav8");
        }
        catch (IOException e)
        {
            throw new UnsatisfiedLinkError(e.getMessage());
        }
    }

    private static List<String> names;
    private static List<String> mimeTypes;
    private static List<String> extensions;

    static
    {
        names = new ArrayList<String>()
            {
                {
                    add("js");
                    add("v8");
                    add("jav8");
                    add("JavaScript");
                    add("javascript");
                    add("ECMAScript");
                    add("ecmascript");
                }
            };

        mimeTypes = new ArrayList<String>()
            {
                {
                    add("application/javascript");
                    add("application/ecmascript");
                    add("text/javascript");
                    add("text/ecmascript");
                }
            };

        extensions = new ArrayList<String>()
            {
                {
                    add("js");
                }
            };
    }

    private static void loadLibrary(String name) throws IOException
    {
        try
        {
            System.loadLibrary(name);

            return;
        }
        catch (UnsatisfiedLinkError e)
        {
            // Ignore the error and try to extract the JNI module from .jar
        }

        String filename = System.mapLibraryName(name);

        InputStream in = V8ScriptEngineFactory.class.getClassLoader().getResourceAsStream(filename);

        int pos = filename.lastIndexOf('.');

        File file = File.createTempFile(filename.substring(0, pos), filename.substring(pos));

        file.deleteOnExit();

        try
        {
            byte[] buf = new byte[4096];
            OutputStream out = new FileOutputStream(file);

            try
            {
                while (in.available() > 0)
                {
                    int len = in.read(buf);

                    if (len >= 0)
                    {
                        out.write(buf, 0, len);
                    }
                }
            }
            finally
            {
                out.close();
            }
        }
        finally
        {
            in.close();
        }

        System.load(file.getAbsolutePath());
    }

    public String getName()
    {
        return (String) getParameter(ScriptEngine.NAME);
    }

    @Override
    public String getEngineName()
    {
        return (String) getParameter(ScriptEngine.ENGINE);
    }

    @Override
    public String getEngineVersion()
    {
        return (String) getParameter(ScriptEngine.ENGINE_VERSION);
    }

    @Override
    public List<String> getExtensions()
    {
        return extensions;
    }

    @Override
    public List<String> getMimeTypes()
    {
        return mimeTypes;
    }

    @Override
    public List<String> getNames()
    {
        return names;
    }

    @Override
    public String getLanguageName()
    {
        return (String) getParameter(ScriptEngine.LANGUAGE);
    }

    @Override
    public String getLanguageVersion()
    {
        return (String) getParameter(ScriptEngine.LANGUAGE_VERSION);
    }

    @Override
    public native Object getParameter(String key);

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args)
    {
        StringBuilder sb = new StringBuilder(obj).append('.').append(m).append('(');

        for (String arg : args)
        {
            if (sb.charAt(sb.length() - 1) != '(')
            {
                sb.append(',');
            }

            sb.append(arg);
        }

        return sb.append(')').toString();
    }

    @Override
    public String getOutputStatement(String toDisplay)
    {
        StringBuilder sb = new StringBuilder("print(\"");

        sb.append(toDisplay.replace("\\", "\\\\").replace("\"", "\\\""));

        return sb.append("\")").toString();
    }

    @Override
    public String getProgram(String... statements)
    {
        StringBuilder sb = new StringBuilder();

        for (String stmt : statements)
        {
            sb.append(stmt).append(';');
        }

        return sb.toString();
    }

    @Override
    public ScriptEngine getScriptEngine()
    {
        return new V8ScriptEngine(this);
    }
}
