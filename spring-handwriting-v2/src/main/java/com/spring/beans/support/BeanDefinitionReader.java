package com.spring.beans.support;

import com.spring.annotation.LazyInit;
import com.spring.annotation.Scope;
import com.spring.annotation.config.ScopeType;
import com.spring.beans.config.BeanDefinition;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class BeanDefinitionReader {

    private String[] configLocations;

    private final String SCAN_PACKAGE = "scanPackage";


    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>(16);

    public BeanDefinitionReader(String[] configLocations) {
        this.configLocations = configLocations;
        // 定位配置文件,加载配置文件到内存中
        loadConfig();

        scanPackage(properties.getProperty(SCAN_PACKAGE));

    }

    /**
     * 在解析的时候就将bean的定义信息设置好了
     */
    public List<BeanDefinition> loadBeanDefinitions() {
        if (classNames.isEmpty()) return null;
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        for (String className : classNames) {
            try {
                Class<?> beanClazz = Class.forName(className);
                if(beanClazz.isInterface()){continue;}

                BeanDefinition beanDefinition = this.loadBeanDefinition(beanClazz,className,beanClazz.getSimpleName());
                beanDefinitions.add(beanDefinition);

                Class<?>[] interfaces = beanClazz.getInterfaces();
                if(interfaces != null && interfaces.length != 0){
                    for (Class<?> anInterface : interfaces) {
                        beanDefinitions.add(this.loadBeanDefinition(beanClazz,beanClazz.getName(),anInterface.getName()));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return beanDefinitions;
    }

    private BeanDefinition loadBeanDefinition(Class beanClazz,String className,String factoryBeanName) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setClassName(className);
            beanDefinition.setFactoryBeanName(toLowerCaseBeanName(factoryBeanName));
            beanDefinition.setSingleton(checkSingleton(beanClazz));
            beanDefinition.setLazyInit(checkLazyInit(beanClazz));

            return beanDefinition;
    }

    private boolean checkLazyInit(Class<?> beanClazz) {
        if (beanClazz.isAnnotationPresent(LazyInit.class)) {
            boolean isLazyInit = beanClazz.getAnnotation(LazyInit.class).value();

            return isLazyInit;
        }

        return false;
    }

    private boolean checkSingleton(Class<?> beanClazz) {
        if (beanClazz.isAnnotationPresent(Scope.class)) {
            ScopeType scopeType = beanClazz.getAnnotation(Scope.class).scope();
            boolean isSingleton = true;
            switch (scopeType) {
                case SINGLETON:
                    isSingleton = true;
                    break;
                case PROTOTYPE:
                    isSingleton = false;
                    break;
            }

            return isSingleton;

        }
        return true;
    }

    private String toLowerCaseBeanName(String name) {
        String first = String.valueOf(name.toCharArray()[0]).toLowerCase();
        name = name.substring(1);
        return first + name;
    }


    private void scanPackage(String scanPackage) {
        scanPackage = scanPackage.replaceAll("\\.", "/");

        String basePackage = getDefaultClassLoader().getResource(scanPackage).getFile();

        File[] files = new File(basePackage).listFiles();

        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String base = scanPackage.replaceAll("/", "\\.") + ".";
                    String className = base + file.getName().replace(".class", "");
                    classNames.add(className);
                } else {
                    scanPackage(basePackage + "/" + file.getName());
                }
            }
        }


    }


    private void loadConfig() {
        String configLocation = configLocations[0];
        InputStream inputStream = null;
        if (configLocation.startsWith("classpath")) {
            inputStream = getDefaultClassLoader().getResourceAsStream(configLocation.split(":")[1]);
        } else {
            inputStream = getDefaultClassLoader().getResourceAsStream(configLocation);
            if (inputStream == null) {
                try {
                    inputStream = new FileInputStream(configLocation);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private ClassLoader getDefaultClassLoader() {
        return this.getClass().getClassLoader();
    }


    public Properties getConfig(){return this.properties;}
}
