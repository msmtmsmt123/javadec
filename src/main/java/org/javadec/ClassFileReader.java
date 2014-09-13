package org.javadec;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;

public class ClassFileReader {

    public static ClassNode readClass(InputStream is) throws IOException {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(is);
        classReader.accept(classNode, 0);
        return classNode;
    }

    public static ClassNode readClass(byte[] bytes) throws IOException {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        return classNode;
    }

}
