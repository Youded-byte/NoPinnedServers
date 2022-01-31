package me.onils.nopinnedservers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Transformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classfileBuffer == null || classfileBuffer.length == 0) {
            return new byte[0];
        }

        if (!className.startsWith("lunar/")) {
            return classfileBuffer;
        }

        ClassReader cr = new ClassReader(classfileBuffer);
        if(Arrays.asList(cr.getInterfaces()).contains("java/util/function/Consumer")){
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            for(MethodNode method : cn.methods){
                Set<String> stringsToMatch = new HashSet<>();
                stringsToMatch.add("versions");
                stringsToMatch.add("name");

                boolean matchesAllStrings = Arrays.stream(method.instructions.toArray())
                        .filter(LdcInsnNode.class::isInstance)
                        .map(LdcInsnNode.class::cast)
                        .map(ldc -> ldc.cst)
                        .filter(stringsToMatch::remove)
                        .anyMatch(__ -> stringsToMatch.isEmpty());
                if(matchesAllStrings){
                    method.instructions.clear();
                    method.localVariables.clear();
                    method.exceptions.clear();
                    method.tryCatchBlocks.clear();
                    method.instructions.add(new InsnNode(Opcodes.RETURN));
                    ClassWriter cw = new ClassWriter(cr, 0);
                    cn.accept(cw);
                    return cw.toByteArray();
                }
            }
        }

        return classfileBuffer;
    }
}
