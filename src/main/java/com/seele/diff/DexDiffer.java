package com.seele.diff;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.util.SyntheticAccessorResolver;
import org.jf.util.IndentingWriter;

public class DexDiffer {
    public DiffInfo diff(File newFile, File oldFile)
            throws IOException {
        DexBackedDexFile newDexFile = DexFileFactory.loadDexFile(newFile, 19,
                true);
        DexBackedDexFile oldDexFile = DexFileFactory.loadDexFile(oldFile, 19,
                true);

        DiffInfo info = DiffInfo.getInstance();


        for (DexBackedClassDef newClazz : newDexFile.getClasses()) {
            Set<DexBackedClassDef> oldclasses = (Set<DexBackedClassDef>) oldDexFile.getClasses();
            boolean contains = false;
            for (DexBackedClassDef oldClazz : oldclasses) {
                if (newClazz.equals(oldClazz)) {

                    String oldClassString = genClassString(oldClazz);
                    String newClassString = genClassString(newClazz);
                    if(!oldClassString.equals(newClassString)){
                        info.addModifiedClasses(newClazz);
                    }

//                    compareField(newClazz, oldClazz, info);
//                    compareMethod(newClazz, oldClazz, info);
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                info.addAddedClasses(newClazz);
            }
        }
        return info;
    }

    public String genClassString(DexBackedClassDef dexDef){
        baksmaliOptions options = new baksmaliOptions();
        options.deodex = false;
        options.noParameterRegisters = false;
        options.useLocalsDirective = true;
        options.useSequentialLabels = true;
        options.outputDebugInfo = true;
        options.addCodeOffsets = false;
        options.jobs = -1;
        options.noAccessorComments = false;
        options.registerInfo = 0;
        options.ignoreErrors = false;
        options.inlineResolver = null;
        options.checkPackagePrivateAccess = false;
        if (!options.noAccessorComments) {
            Set<DexBackedClassDef> list = new HashSet();
            list.add(dexDef);
            options.syntheticAccessorResolver = new SyntheticAccessorResolver(null,
                    list);
        }
        ClassDefinition classDefinition = new ClassDefinition(options, dexDef);
        OutputStream outputStream = new ByteArrayOutputStream();
        BufferedWriter bufWriter = null;
        try {
            bufWriter = new BufferedWriter(new OutputStreamWriter(
                    outputStream, "UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        IndentingWriter writer = new IndentingWriter(bufWriter);
        try {
            classDefinition.writeTo(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(writer!=null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String classString = outputStream.toString();


        return  classString;
    }

    public void compareMethod(DexBackedClassDef newClazz, DexBackedClassDef oldClazz, DiffInfo info) {
        compareMethod(newClazz.getMethods(), oldClazz.getMethods(), info);
    }

    public void compareMethod(Iterable<? extends DexBackedMethod> news, Iterable<? extends DexBackedMethod> olds, DiffInfo info) {
        for (DexBackedMethod reference : news)
            if (!reference.getName().equals("<clinit>")) {
                compareMethod(reference, olds, info);
            }
    }

    public void compareMethod(DexBackedMethod object, Iterable<? extends DexBackedMethod> olds, DiffInfo info) {
        for (DexBackedMethod reference : olds) {
            if (reference.equals(object)) {
                if ((reference.getImplementation() == null) &&
                        (object.getImplementation() != null)) {
                    info.addModifiedMethods(object);
                    return;
                }
                if ((reference.getImplementation() != null) &&
                        (object.getImplementation() == null)) {
                    info.addModifiedMethods(object);
                    return;
                }
                if ((reference.getImplementation() == null) &&
                        (object.getImplementation() == null)) {
                    return;
                }

                if (!reference.equals(object)) {
                    info.addModifiedMethods(object);
                    return;
                }
                return;
            }
        }

        info.addAddedMethods(object);
    }

    public void compareField(DexBackedClassDef newClazz, DexBackedClassDef oldClazz, DiffInfo info) {
        compareField(Iterables.concat(newClazz.getStaticFields(), oldClazz.getInstanceFields()), Iterables.concat(oldClazz.getStaticFields(), oldClazz.getInstanceFields()), info);
    }

    public void compareField(Iterable<? extends DexBackedField> news, Iterable<? extends DexBackedField> olds, DiffInfo info) {
        for (DexBackedField reference : news)
            compareField(reference, olds, info);
    }

    public void compareField(DexBackedField object, Iterable<? extends DexBackedField> olds, DiffInfo info) {
        for (DexBackedField reference : olds) {
            if (reference.equals(object)) {
                if ((reference.getInitialValue() == null) &&
                        (object.getInitialValue() != null)) {
                    info.addModifiedFields(object);
                    return;
                }
                if ((reference.getInitialValue() != null) &&
                        (object.getInitialValue() == null)) {
                    info.addModifiedFields(object);
                    return;
                }
                if ((reference.getInitialValue() == null) &&
                        (object.getInitialValue() == null)) {
                    return;
                }
                if (reference.getInitialValue().compareTo(
                        object.getInitialValue()) != 0) {
                    info.addModifiedFields(object);
                    return;
                }
                return;
            }
        }

        info.addAddedFields(object);
    }
}

