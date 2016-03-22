package com.seele.diff;

import com.seele.utils.Formater;

import java.util.HashSet;
import java.util.Set;

import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

public class DiffInfo {
    private static DiffInfo info = new DiffInfo();

    private Set<DexBackedClassDef> addedClasses = new HashSet();
    private Set<DexBackedClassDef> modifiedClasses = new HashSet();

    private Set<DexBackedField> addedFields = new HashSet();
    private Set<DexBackedField> modifiedFields = new HashSet();

    private Set<DexBackedMethod> addedMethods = new HashSet();
    private Set<DexBackedMethod> modifiedMethods = new HashSet();

    public static synchronized DiffInfo getInstance() {
        return info;
    }

    public Set<DexBackedClassDef> getAddedClasses() {
        return this.addedClasses;
    }

    public DexBackedClassDef getAddedClasses(String clazz) {
        for (DexBackedClassDef classDef : this.addedClasses) {
            if (classDef.getType().equals(clazz)) {
                return classDef;
            }
        }
        return null;
    }

    public void addAddedClasses(DexBackedClassDef clazz) {
        System.out.println("add new Class:" + clazz.getType());
        this.addedClasses.add(clazz);
    }

    public Set<DexBackedClassDef> getModifiedClasses() {
        return this.modifiedClasses;
    }

    public DexBackedClassDef getModifiedClasses(String clazz) {
        for (DexBackedClassDef classDef : this.modifiedClasses) {
            if (classDef.getType().equals(clazz)) {
                return classDef;
            }
        }
        return null;
    }

    public void addModifiedClasses(DexBackedClassDef clazz) {
        System.out.println("add modified Class:" + clazz.getType());
        this.modifiedClasses.add(clazz);
    }

    public Set<DexBackedField> getAddedFields() {
        return this.addedFields;
    }

    public void addAddedFields(DexBackedField field) {
        this.addedFields.add(field);
        throw new RuntimeException("can,t add new Field:" +
                field.getName() + "(" + field.getType() + "), " + "in class :" +
                field.getDefiningClass());
    }

    public Set<DexBackedField> getModifiedFields() {
        return this.modifiedFields;
    }

    public void addModifiedFields(DexBackedField field) {
        this.modifiedFields.add(field);
        throw new RuntimeException("can,t modified Field:" +
                field.getName() + "(" + field.getType() + "), " + "in class :" +
                field.getDefiningClass());
    }

    public Set<DexBackedMethod> getAddedMethods() {
        return this.addedMethods;
    }

    public void addAddedMethods(DexBackedMethod method) {
        System.out.println("add new Method:" + method.getReturnType() +
                "  " + method.getName() + "(" +
                Formater.formatStringList(method.getParameterTypes()) +
                ")  in Class:" + method.getDefiningClass());
        this.addedMethods.add(method);

        if (!this.modifiedClasses.contains(method.classDef))
            this.modifiedClasses.add(method.classDef);
    }

    public Set<DexBackedMethod> getModifiedMethods() {
        return this.modifiedMethods;
    }

    public void addModifiedMethods(DexBackedMethod method) {
        System.out.println("add modified Method:" + method.getReturnType() +
                "  " + method.getName() + "(" +
                Formater.formatStringList(method.getParameterTypes()) +
                ")  in Class:" + method.getDefiningClass());
        this.modifiedMethods.add(method);

        if (!this.modifiedClasses.contains(method.classDef))
            this.modifiedClasses.add(method.classDef);
    }
}

