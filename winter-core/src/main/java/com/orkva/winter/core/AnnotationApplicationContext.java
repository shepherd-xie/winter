package com.orkva.winter.core;

import com.orkva.winter.core.annotation.Autowired;
import com.orkva.winter.core.annotation.Component;
import com.orkva.winter.core.annotation.ComponentScan;
import com.orkva.winter.core.annotation.Scope;
import com.orkva.winter.core.exception.NoBeanDefinitionException;
import com.orkva.winter.core.factory.BeanNameAware;
import com.orkva.winter.core.factory.BeanPostProcessor;
import com.orkva.winter.core.factory.InitializingBean;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
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
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

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
        Object instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();

            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        if (instance instanceof BeanNameAware) {
            ((BeanNameAware) instance).setBeanName(beanName);
        }

        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
        }

        if (instance instanceof InitializingBean) {
            try {
                ((InitializingBean) instance).afterPropertiesSet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
        }

        return instance;
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

                    Class<?> beanClass;
                    try {
                        beanClass = CLASS_LOADER.loadClass(fullyClassName);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    if (!beanClass.isAnnotationPresent(Component.class)) {
                        continue;
                    }

                    if (BeanPostProcessor.class.isAssignableFrom(beanClass)) {
                        try {
                            BeanPostProcessor beanPostProcessor = (BeanPostProcessor) beanClass.getDeclaredConstructor().newInstance();
                            beanPostProcessors.add(beanPostProcessor);
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Component component = beanClass.getDeclaredAnnotation(Component.class);
                        String beanName = component.value();

                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(beanClass);

                        if (beanClass.isAnnotationPresent(Scope.class)) {
                            Scope scope = beanClass.getDeclaredAnnotation(Scope.class);
                            beanDefinition.setScope(scope.value());
                        } else {
                            beanDefinition.setScope("singleton");
                        }

                        beanDefinitionMap.put(beanName, beanDefinition);
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
