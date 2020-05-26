package com.juzi.v1;

import com.juzi.v1.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

public class DispatcherServlet extends HttpServlet {

    // 用于获取配置文件中的参数
    private Properties properties = new Properties();

    // 存储所有类的名称
    private List<String> classNames = new ArrayList<>();

    private Map<String,Object> ioc = new HashMap<>();

    private Map<String,Method> handlerMapping = new HashMap<>();


    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        // 加载配置文件
        loadContextConfig(servletConfig.getInitParameter("contextConfigLocation"));
        // 扫描包
        scanPackage(properties.getProperty("scanPackage"));
        // 将包中的类注入到ioc容器之中
        doInstance();
        // 给类中的属性赋值
        doGiveValue();
        // 建立url和controller的映射关系
        doHandlerMapping();

    }

    private void doHandlerMapping() {
        Set<Map.Entry<String, Object>> entries = ioc.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            // 获取class对象
            Class clazz = entry.getValue().getClass();
            // 判断类上是否有RequestMapping注解
            if (! clazz.isAnnotationPresent(RequestMapping.class)) continue; // 没有注解
            // 存在注解
            StringBuilder stringBuilder = new StringBuilder();
            RequestMapping requestMapping = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
            String url_1 = requestMapping.value();
            if(!"".equals(url_1)){
                // 判断是否以 / 开头
                stringBuilder.append(checkPrefix(url_1));
            }
            // 获取所有的方法
            Method[] methods = clazz.getMethods();
            if (methods == null || methods.length ==0) continue;
            for (Method method : methods) {
                // 判断method上是否有RequestMapping注解
                if(!method.isAnnotationPresent(RequestMapping.class)) continue;
                // 获取RequestMapping注解中的值
                String url_2 = method.getAnnotation(RequestMapping.class).value();
                if(!"".equals(url_2)){
                    stringBuilder.append(this.checkPrefix(url_2));
                }
                // 将url和method做一对一的关联关系
                handlerMapping.put(stringBuilder.toString(),method);
            }


        }
    }

    private String checkPrefix(String url_1) {
        if(url_1.startsWith("/") && url_1.charAt(1) != '/'){
            return url_1;
        }
        String s = ("/" + url_1).replaceAll("/+", "/");
        return s;
    }

    private void doGiveValue() {
        // 循环遍历ioc容器
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取对象中所有的属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                boolean flag = field.isAnnotationPresent(Autowired.class);
                if(! flag) continue;
                Autowired autowired = field.getAnnotation(Autowired.class);
                String beanId = autowired.value();
                if("".equals(beanId)){
                    // 使用属性的全类名作为bean的id
                    beanId = field.getType().getName();
                }
                Object fieldValue = ioc.get(beanId);
                field.setAccessible(true);  // 暴力反射
                try {
                    // 赋值
                    field.set(entry.getValue(),fieldValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    /**
     *  通过反射将符合规范的类添加到spring容器中
     */
    private void doInstance() {
        if(classNames.isEmpty()) return;


        for (String className : classNames) {
            try {
                // 通过反射获取字节码对象
                Class clazz = Class.forName(className);
                // 判断类上是否有JService, JController注解
                if(clazz.isAnnotationPresent(JController.class)){
                    // 有JController注解将其加入到ioc容器中
                    Object instance = clazz.newInstance();
                    // 获取注解中的value
                    JController jController = (JController) clazz.getAnnotation(JController.class);
                    // 获取注解中自定义的bean的名称
                    String beanName = jController.value();
                    // 没有自定义名称
                    if("".equals(beanName)){
                        beanName = this.toLowerCaseName(clazz.getSimpleName());
                    }
                    // 使用className首字母小写作为bean的名称
                    ioc.put(beanName,instance);
                    // 使用全类名再保存一份,用于根据类型自动注入
                    ioc.put(clazz.getName(),instance);
                }else if(clazz.isAnnotationPresent(JService.class)){
                    // 有JController注解将其加入到ioc容器中
                    Object instance = clazz.newInstance();
                    // 获取注解中的value
                    JService jService = (JService) clazz.getAnnotation(JService.class);
                    // 获取注解中自定义的bean的名称
                    String beanName = jService.value();
                    // 没有自定义名称
                    if("".equals(beanName)){
                        beanName = this.toLowerCaseName(clazz.getSimpleName());
                    }
                    // 使用className首字母小写作为bean的名称
                    ioc.put(beanName,instance);
                    // 使用全类名再保存一份,用于根据类型自动注入
                    Class[] anInterface = clazz.getInterfaces();
                    if(anInterface == null){
                        // 没有接口就按类的名称作为bean 的id
                        ioc.put(clazz.getName(),instance);
                    }else{
                        // 有接口则按接口的名称作为bean的id
                        ioc.put(anInterface[0].getName(),instance);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }

    private String toLowerCaseName(String beanName) {
        StringBuilder stringBuilder = new StringBuilder(beanName);
        String lowerCaseChar = String.valueOf(beanName.charAt(0)).toLowerCase();

        return lowerCaseChar+beanName.substring(1);
    }

    private void scanPackage(String classPath) {
        // 将配置文件中的包路径中的 . 全部替换称 \\
        classPath = classPath.replaceAll("\\.","//");
        URL url  = this.getClass().getClassLoader().getResource(classPath);
        // 类包路径
        File classPackage = new File(url.getFile());
        // 获取包下所有的类文件集合
        File[] listFiles = classPackage.listFiles();

        for (File classFile : listFiles) {
            // 判断是否是文件
            if(classFile.isFile()){
                // 获取文件名称
                String fileName = classFile.getName();
                // 判断后缀名是否是.java
                if(!fileName.endsWith(".class")) continue;

                String className = fileName.replace(".class", "");
                String fullClassName = (classPath + "." + className).replaceAll("//", "\\.");

                this.classNames.add(fullClassName);
            }else{
                // 递归扫描出包下的类
                this.scanPackage(classPath+"."+classFile.getName());
            }
        }


    }

    private void loadContextConfig(String configPath) {
        // 第一步: 从web.xml中根据contextConfigLocation属性名称获取配置文件路径,然后加载到内存中

        InputStream inputStream = null;
        if(configPath.startsWith("classpath")){
            String path = configPath.substring(configPath.trim().indexOf(":")+1);
            inputStream = this.getClass().getClassLoader().getResourceAsStream(path);
        }
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("加载配置文件出错");
            e.printStackTrace();
        }finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.dispatcher(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 SERVER ERROR");
        }
    }

    /**
     * 根据请求的url路径分发到对应的方法进行处理
     * @param req
     * @param resp
     */
    private void dispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ServletContext servletContext = req.getSession().getServletContext();
        String contextPath = servletContext.getContextPath();
        String url = req.getRequestURL().toString().split(contextPath)[1];
        Method method = handlerMapping.get(url);
        if(method == null){
            resp.getWriter().write("404  NOT FOUND");
        }

        Class<?>[] types = method.getParameterTypes();
        Parameter[] parameters = method.getParameters();
        String name = method.getName();

        String className = method.getDeclaringClass().getSimpleName();
        String beanId = this.toLowerCaseName(className);
        Object[] args =  new Object[parameters.length];
        Map<String,String[]> parameterMap = req.getParameterMap();
        if(types == null && types.length ==0){
            method.invoke(ioc.get(beanId),null);
            return;
        }else{

            for (int i = 0; i < types.length; i++) {
                Class parameterType = types[i];
                if(parameterType == HttpServletRequest.class){
                    args[i] = req;
                }else if(parameterType == HttpServletResponse.class){
                    args[i] = resp;
                }else if (parameterType == String.class){
                    RequestParam requestParam = (RequestParam) parameterType.getAnnotation(RequestParam.class);
                    //String value = requestParam.value();
                    args[i] = parameterMap.get("name")[0];
                }
            }
            method.invoke(ioc.get(beanId),args);
            return;
        }


    }


}
