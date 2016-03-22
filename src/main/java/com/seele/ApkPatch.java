package com.seele;

import com.seele.diff.DexDiffer;
import com.seele.diff.DiffInfo;
import com.seele.utils.Formater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.io.FileUtils;
import org.jf.baksmali.baksmali;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.util.SyntheticAccessorResolver;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.util.ClassFileNameHandler;

public class ApkPatch extends Build {
    private File from;
    private File to;
    private Set<String> classes;

    public ApkPatch(File from, File to, String name, File out, String keystore, String password, String alias, String entry) {
        super(name, out, keystore, password, alias, entry);
        this.from = from;
        this.to = to;
    }

    public void doPatch() {
        try {
            File smaliDir = new File(this.out, "smali");
            if (!smaliDir.exists())
                smaliDir.mkdir();
            try {
                FileUtils.cleanDirectory(smaliDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            File dexFile = new File(this.out, "diff.dex");
            if ((dexFile.exists()) && (!dexFile.delete())) {
                throw new RuntimeException("diff.dex can't be removed.");
            }
            File outFile = new File(this.out, "diff.apatch");
            if ((outFile.exists()) && (!outFile.delete())) {
                throw new RuntimeException("diff.apatch can't be removed.");
            }

            DiffInfo info = new DexDiffer().diff(this.from, this.to);

            this.classes = buildCode(smaliDir, dexFile, info);

            build(outFile, dexFile);

            release(this.out, dexFile, outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Set<String> buildCode(File smaliDir, File dexFile, DiffInfo info)
            throws IOException, RecognitionException, FileNotFoundException {
        Set classes = new HashSet();
        Set<DexBackedClassDef> list = new HashSet();
        list.addAll(info.getAddedClasses());
        list.addAll(info.getModifiedClasses());

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
            options.syntheticAccessorResolver = new SyntheticAccessorResolver(null,
                    list);
        }
        ClassFileNameHandler outFileNameHandler = new ClassFileNameHandler(
                smaliDir, ".smali");
        ClassFileNameHandler inFileNameHandler = new ClassFileNameHandler(
                smaliDir, ".smali");
        DexBuilder dexBuilder = DexBuilder.makeDexBuilder();

        for (DexBackedClassDef classDef : list) {
            String className = classDef.getType();
            try {
                Method method = baksmali.class.getDeclaredMethod("disassembleClass", ClassDef.class, ClassFileNameHandler.class, baksmaliOptions.class);
                method.setAccessible(true);
                method.invoke(null, classDef, outFileNameHandler, options);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            File smaliFile = inFileNameHandler.getUniqueFilenameForClass(className);
            classes.add(className.substring(1, className.length() - 1).replace('/', '.'));
            SmaliMod.assembleSmaliFile(smaliFile, dexBuilder, true, true);
        }

        dexBuilder.writeTo(new FileDataStore(dexFile));

        return classes;
    }

    protected Manifest getMeta() {
        Manifest manifest = new Manifest();
        Attributes main = manifest.getMainAttributes();
        main.putValue("Manifest-Version", "1.0");
        main.putValue("Created-By", "1.0 (ApkPatch)");
        main.putValue("Created-Time",
                new Date(System.currentTimeMillis()).toGMTString());
        main.putValue("From-File", this.from.getName());
        main.putValue("To-File", this.to.getName());
        main.putValue("Patch-Name", this.name);
        main.putValue("Patch-Classes", Formater.dotStringList(this.classes));
        return manifest;
    }
}
