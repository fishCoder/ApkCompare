package com.seele;

import com.seele.build.PatchBuilder;
import com.seele.utils.HexUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.jar.Manifest;

public abstract class Build {
    protected static final String SUFFIX = ".apatch";
    protected String name;
    private String keystore;
    private String password;
    private String alias;
    private String entry;
    protected File out;

    public Build(String name, File out, String keystore, String password, String alias, String entry) {
        this.name = name;
        this.out = out;
        this.keystore = keystore;
        this.password = password;
        this.alias = alias;
        this.entry = entry;
        if (!out.exists())
            out.mkdirs();
        else if (!out.isDirectory())
            throw new RuntimeException("output path must be directory.");
    }

    protected void release(File outDir, File dexFile, File outFile)
            throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("md5");
        FileInputStream fileInputStream = new FileInputStream(dexFile);
        byte[] buffer = new byte[8192];
        int len = 0;
        while ((len = fileInputStream.read(buffer)) > 0) {
            messageDigest.update(buffer, 0, len);
        }

        String md5 = HexUtil.hex(messageDigest.digest());
        fileInputStream.close();
        outFile.renameTo(new File(outDir, this.name + "-" + md5 + ".apatch"));
    }

    protected void build(File outFile, File dexFile)
            throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        KeyStore.PrivateKeyEntry privateKeyEntry = null;
        InputStream is = new FileInputStream(this.keystore);
        keyStore.load(is, this.password.toCharArray());
        privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(this.alias,
                new KeyStore.PasswordProtection(this.entry.toCharArray()));

        PatchBuilder builder = new PatchBuilder(outFile, dexFile, privateKeyEntry, System.out);
        builder.writeMeta(getMeta());
        builder.sealPatch();
    }

    protected abstract Manifest getMeta();
}

