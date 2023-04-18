package com.orkva.winter.core;

import com.orkva.winter.core.annotation.Autowired;
import com.orkva.winter.core.annotation.Component;
import com.orkva.winter.core.annotation.ComponentScan;
import com.orkva.winter.core.annotation.Scope;
import com.orkva.winter.core.aware.BeanNameAware;
import com.orkva.winter.core.exception.NoBeanDefinitionException;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AnnotationApplicationContext
 *
 * @author Shepherd Xie
 * @version 2023/4/17
 */
public class AnnotationApplicationContext extends AbstractApplicationContext {

    private Class<?> configClass;

    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private final static ClassLoader CLASS_LOADER;

    static {
        CLASS_LOADER = AnnotationApplicationContext.class.getClassLoader();
    }

    public AnnotationApplicationContext(Class<?> configClass) {
        this.configClass = configClass;

        ComponentScan componentScan = configClass.getDeclaredAnnotation(ComponentScan.class);
        componentClassScan(componentScan);

        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            BeanDefinition beanDefinition = beanDefinitionEntry.getValue();
            if ("singleton".equals(beanDefinition.getScope())) {
                singletonObjects.put(beanName, createBean(beanName, beanDefinition));
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }

            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void componentClassScan(ComponentScan componentScan) {
        String basePackage = componentScan.value();
        String[] backPackagePaths = basePackage.split("\\.");
        URL resource = CLASS_LOADER.getResource(String.join("/", backPackagePaths));

        File basePackageDirectory = new File(resource.getFile());
        if (!basePackageDirectory.isDirectory()) {
            return;
        }

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
                        Class<?> clazz = CLASS_LOADER.loadClass(fullyClassName);
                        if (!clazz.isAnnotationPresent(Component.class)) {
                            continue;
                        }
                        Component component = clazz.getDeclaredAnnotation(Component.class);
                        String beanName = component.value();

                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(clazz);

                        if (clazz.isAnnotationPresent(Scope.class)) {
                            Scope scope = clazz.getDeclaredAnnotation(Scope.class);
                            beanDefinition.setScope(scope.value());
                        } else {
                            beanDefinition.setScope("singleton");
                        }

                        beanDefinitionMap.put(beanName, beanDefinition);

                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public Object getBean(String beanName) {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NoBeanDefinitionException();
        }

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if ("singleton".equals(beanDefinition.getScope())) {
            return singletonObjects.get(beanName);
        }

        return createBean(beanName, beanDefinition);
    }
}
