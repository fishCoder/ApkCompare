package com.seele.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

public class PatchBuilder {
    private SignedJarBuilder mBuilder;

    public PatchBuilder(File outFile, File dexFile, KeyStore.PrivateKeyEntry key, PrintStream verboseStream) {
        try {
            this.mBuilder = new SignedJarBuilder(new FileOutputStream(outFile, false), key.getPrivateKey(),
                    (X509Certificate) key.getCertificate());
            this.mBuilder.writeFile(dexFile, "classes.dex");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeMeta(Manifest manifest) {
        try {
            this.mBuilder.getOutputStream().putNextEntry(
                    new JarEntry("META-INF/PATCH.MF"));
            manifest.write(this.mBuilder.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sealPatch() {
        try {
            this.mBuilder.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

