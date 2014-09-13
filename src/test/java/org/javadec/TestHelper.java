package org.javadec;

import com.google.common.io.ByteStreams;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class TestHelper {


    public static byte[] compile(String name, String content) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MemoryFileManager manager = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
        List<JavaFileObject> files = asList(new MemoryJavaFileObject(name, content));
        compiler.getTask(null, manager, null, null, null, files).call();
        return manager.objects.get(name).getBytes();
    }


    public static String disassemble(byte[] bytes) throws IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(printWriter);
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(traceClassVisitor, 0);
        return stringWriter.toString();
    }

    public static byte[] compileResource(String name, String resource) throws IOException {
        String content = new String(ByteStreams.toByteArray(TestHelper.class.getResourceAsStream(resource)), StandardCharsets.UTF_8);
        return compile(name, content);
    }


    private static class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, MemoryJavaClassObject> objects = new HashMap<>();

        public MemoryFileManager(StandardJavaFileManager manager) {
            super(manager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            MemoryJavaClassObject object = new MemoryJavaClassObject(name, kind);
            objects.put(name, object);
            return object;
        }

    }

    private static class MemoryJavaClassObject extends SimpleJavaFileObject {
        protected final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        public MemoryJavaClassObject(String name, Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
        }

        public byte[] getBytes() {
            return stream.toByteArray();
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return stream;
        }
    }

    private static class MemoryJavaFileObject extends SimpleJavaFileObject {
        private CharSequence content;

        protected MemoryJavaFileObject(String className, CharSequence content) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }

    }
}
