package mcom.kernel.processor;

/**
 * Extracts all class files from a mCom bundle (.jar file)
 *
 * @Author: Inah Omoronyia
 */

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class BundleJarProcessor {

    private String[] classFiles;

    public BundleJarProcessor(File file) {
        classFiles = new String[0];
        findClassesInJar(file);
    }

    public String[] getClassFiles() {
        return classFiles;
    }

    private void findClassesInJar(File file) {
        String path = file.getAbsolutePath();

        try {
            JarFile jarFile = new JarFile(path);
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String entryName = entry.getName();
                if (entryName.endsWith(".class")) {
                    String[] parts = entryName.split(".class");
                    String entryName1 = parts[0];
                    entryName1 = entryName1.replace('/', '.');
                    addClassFile(entryName1);
                }
            }
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addClassFile(String classFile) {
        String[] c_temp = new String[classFiles.length + 1];
        List<String> c_t = new LinkedList<String>();

        for (String ct : classFiles) {
            if (ct != null) {
                c_t.add(ct);
            }
        }

        int i = 0;
        for (String ct : c_t) {
            c_temp[i] = ct;
            i = i + 1;
        }

        c_temp[i] = classFile;
        classFiles = c_temp;
    }

}
