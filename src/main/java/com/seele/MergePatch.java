package com.seele;

import com.android.dx.io.DexBuffer;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class MergePatch extends Build {
    private File[] patchs;

    public MergePatch(File[] patchs, String name, File out, String keystore, String password, String alias, String entry) {
        super(name, out, keystore, password, alias, entry);
        this.patchs = patchs;
    }

    public void doMerge() {
        try {
            File dexFile = new File(this.out, "merge.dex");
            if ((dexFile.exists()) && (!dexFile.delete())) {
                throw new RuntimeException("merge.dex can't be removed.");
            }
            File outFile = new File(this.out, "merge.apatch");
            if ((dexFile.exists()) && (!dexFile.delete())) {
                throw new RuntimeException("merge.apatch can't be removed.");
            }

            mergeCode(dexFile);

            build(outFile, dexFile);

            release(this.out, dexFile, outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeCode(File dexFile) throws IOException {
        DexBuffer dexA = null;
        DexBuffer dexB = null;
        for (File file : this.patchs)
            if ((!dexFile.exists()) && (dexA == null)) {
                dexA = getDexFromJar(file);
            } else {
                if (dexFile.exists()) {
                    dexA = new DexBuffer(dexFile);
                }
                dexB = getDexFromJar(file);
                DexMerger dexMerger = new DexMerger(dexA, dexB,
                        CollisionPolicy.FAIL);
                DexBuffer dexM = dexMerger.merge();
                dexM.writeTo(dexFile);
            }
    }

    private DexBuffer getDexFromJar(File file) throws IOException {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);
            JarEntry dexEntry = jarFile.getJarEntry("classes.dex");
            return new DexBuffer(jarFile.getInputStream(dexEntry));
        } finally {
            if (jarFile != null)
                jarFile.close();
        }
    }

    protected Manifest getMeta() {
        Manifest retManifest = new Manifest();
        Attributes main = retManifest.getMainAttributes();
        main.putValue("Manifest-Version", "1.0");
        main.putValue("Created-By", "1.0 (ApkPatch)");
        main.putValue("Created-Time",
                new Date(System.currentTimeMillis()).toGMTString());
        main.putValue("Patch-Name", this.name);
        try {
            fillManifest(main);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return retManifest;
    }

    private void fillManifest(Attributes main)
            throws IOException {
        StringBuffer fromBuffer = new StringBuffer();
        StringBuffer toBuffer = new StringBuffer();

        for (File file : this.patchs) {
            JarFile jarFile = new JarFile(file);
            JarEntry dexEntry = jarFile.getJarEntry("META-INF/PATCH.MF");
            Manifest manifest = new Manifest(jarFile.getInputStream(dexEntry));
            Attributes attributes = manifest.getMainAttributes();

            String from = attributes.getValue("From-File");
            if (fromBuffer.length() > 0) {
                fromBuffer.append(',');
            }
            fromBuffer.append(from);
            String to = attributes.getValue("To-File");
            if (toBuffer.length() > 0) {
                toBuffer.append(',');
            }
            toBuffer.append(to);

            String name = attributes.getValue("Patch-Name");
            String classes = attributes.getValue("Patch-Classes");
            main.putValue(name + "-Classes", classes);
        }
        main.putValue("From-File", fromBuffer.toString());
        main.putValue("To-File", toBuffer.toString());
    }
}

