package com.orkva.winter.core;

import com.orkva.winter.core.annotation.ComponentScan;

import java.io.File;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AnnotationApplicationContext
 *
 * @author Shepherd Xie
 * @version 2023/4/17
 */
public class AnnotationApplicationContext extends AbstractApplicationContext {

    private Class configClass;

    private final static ClassLoader CLASS_LOADER;

    static {
        CLASS_LOADER = AnnotationApplicationContext.class.getClassLoader();
    }

    public AnnotationApplicationContext(Class configClass) {
        this.configClass = configClass;

        ComponentScan componentScan = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        List<Class<?>> classList = componentClassScan(componentScan);

    }

    private List<Class<?>> componentClassScan(ComponentScan componentScan) {
        String basePackage = componentScan.value();
        String[] backPackagePaths = basePackage.split("\\.");
        URL resource = CLASS_LOADER.getResource(String.join("/", backPackagePaths));

        File basePackageDirectory = new File(resource.getFile());
        List<Class<?>> scanClassList = new ArrayList<>();
        if (basePackageDirectory.isDirectory()) {
            ArrayDeque<File> fileDeque = new ArrayDeque<>();
            fileDeque.add(basePackageDirectory);

            while (!fileDeque.isEmpty()) {
                File file = fileDeque.poll();
                if (file.isDirectory()) {
                    File[] subFiles = file.listFiles();
                    if (subFiles != null) {
                        fileDeque.addAll(Arrays.stream(subFiles).collect(Collectors.toList()));
                    }
                } else {
                    String absolutePath = file.getAbsolutePath();
                    if (absolutePath.endsWith(".class")) {
                        String fullyClassPath = absolutePath.substring(absolutePath.indexOf(String.join(File.separator, backPackagePaths)), absolutePath.indexOf(".class"));
                        String fullyClassName = fullyClassPath.replace(File.separator, ".");

                        try {
                            scanClassList.add(CLASS_LOADER.loadClass(fullyClassName));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return scanClassList;
    }
}
