package com.juzi.v2;



import com.juzi.v1.annotation.Autowired;
import com.juzi.v1.annotation.JController;
import com.juzi.v1.annotation.JService;
import com.juzi.v1.annotation.RequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class DispatcherServlet extends HttpServlet {

    private static final String CONFIG_LOCATION = "contextConfigLocation";

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>(16);

    private Map<String,Object> ioc = new HashMap<>();

    private List<HandlerMapping> handlerMapping = new ArrayList<>();

    private Convert convert = new Convert();

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        // 加载配置文件
        this.loadConfig(servletConfig.getInitParameter(CONFIG_LOCATION));
        // 扫描包
        this.scanPackage(properties.getProperty("scanPackage"));
        // 进行赋值,并添加到ioc容器中
        this.giveValue();
        // 给类中所有添加了Autowired的属性赋值
        this.doAutowired();
        // 建立url和controller的关系映射
        this.handlerMapping();

    }

    private void handlerMapping() {
        Set<Map.Entry<String, Object>> entries = ioc.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            Class<?> clazz = entry.getValue().getClass();
            if(clazz.isAnnotationPresent(JController.class)){
                // 被JController注解修饰的类
                StringBuilder url = new StringBuilder();
                String baseUrl = null;
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                if(requestMapping != null && !"".equals(requestMapping.value())){
                    baseUrl = this.checnkPrefix(requestMapping.value());
                    url.append(baseUrl);
                }

                Method[] methods = clazz.getMethods();
                if (methods == null && methods.length ==0) continue;

                for (Method method : methods) {
                    if ( ! method.isAnnotationPresent(RequestMapping.class)) continue;
                    RequestMapping reqMapping = method.getAnnotation(RequestMapping.class);
                    if (reqMapping != null && !"".equals(reqMapping.value())){
                        url.append(this.checnkPrefix(reqMapping.value()));
                    }
                    // 添加到处理器映射器中
                    this.handlerMapping.add(new HandlerMapping(url.toString(),method,entry.getValue()));
                    url = new StringBuilder();
                    url.append(baseUrl);
                }

            }
        }
    }

    private String checnkPrefix(String value) {
        // 以/ 开头并且url的第二个位置不是/
        if(value.startsWith("/") && value.charAt(1) != '/'){
            return value;
        }
        // 将一个或者一个以上的 / 替换成一个 /
        String val = ("/"+value).replaceAll("/+","/");
        return val;
    }

    private void doAutowired() {
        Set<Map.Entry<String, Object>> entries = ioc.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            Object instance = entry.getValue();
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                // 判断是否存在Autowired注解
                if(field.isAnnotationPresent(Autowired.class)){
                    // 存在
                    String beanId = field.getType().getName();
                    field.setAccessible(true);  // 暴力反射
                    try {
                        field.set(instance,ioc.get(beanId));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private void giveValue() {
        if(classNames.isEmpty()) return;

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                // 判断类上是否存在Controller或者Service注解
                if(clazz.isAnnotationPresent(JController.class)){
                    // 创建对象
                    Object instance = clazz.newInstance();
                    String beanId = clazz.getAnnotation(JController.class).value();
                    // 自定义设置了bean的id则使用自定义的,没有则使用类名首字母小写作为bean的id
                    if("".equals(beanId)){
                        beanId = this.toLowerCase(clazz.getName());
                    }
                    ioc.put(beanId,instance);
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if(interfaces != null && interfaces.length != 0){
                        // 获取接口的名称作为bean的id
                        ioc.put(interfaces[0].getName(),instance);
                    }
                }else if(clazz.isAnnotationPresent(JService.class)){
                    Object instance = clazz.newInstance();
                    String beanId = clazz.getAnnotation(JService.class).value();
                    // 自定义设置了bean的id则使用自定义的,没有则使用类名首字母小写作为bean的id
                    if("".equals(beanId)){
                        beanId = this.toLowerCase(clazz.getName());
                    }
                     // 添加到ioc容器中
                    ioc.put(beanId,instance);
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if(interfaces != null && interfaces.length != 0){
                        // 获取接口的名称作为bean的id,添加到ioc容器中
                        ioc.put(interfaces[0].getName(),instance);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String toLowerCase(String clazzName) {
        String first = String.valueOf(clazzName.toCharArray()[0]).toLowerCase();
        String beanId = first + clazzName.substring(1);
        return beanId;
    }

    /**
     *  用于扫描包下所有的类
     * @param scanPackage
     */
    private void scanPackage(String scanPackage) {
        // 将. 替换成 /
        scanPackage = scanPackage.replaceAll("\\.","/");
        String classPath = this.getClass().getClassLoader().getResource(scanPackage).getFile();
        File file = new File(classPath);
        File[] listFiles = file.listFiles();
        for (File listFile : listFiles) {
            if(listFile.isFile() && listFile.getName().endsWith(".class")){
                // 如果是文件并且后缀名是.class
                classNames.add((scanPackage+"/"+listFile.getName().replace(".class",""))
                        .replaceAll("/","\\."));
            }else{
                this.scanPackage(scanPackage+"/"+listFile.getName());
            }
        }

    }

    private void loadConfig( String configLocation) {
        InputStream inputStream = null;
        if(configLocation.startsWith("classpath")){

            inputStream = this.getClass().getClassLoader().getResourceAsStream(configLocation.split(":")[1]);
        }else{
            try {
                inputStream = new FileInputStream(configLocation);
            } catch (FileNotFoundException e) {
                System.out.println("文件位置错误: "+configLocation);
                e.printStackTrace();
            }
        }
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.dispatcher(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 SERVER ERROR");
            e.printStackTrace();
            return;
        }
    }

    private void dispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception, IllegalAccessException {

        String requestUri = req.getRequestURI();
        String url = requestUri.split(req.getContextPath().toString())[1];
        for (HandlerMapping mapping : this.handlerMapping) {
            String mappingUrl = mapping.getUrl();
            if(! url.equals(mappingUrl)){
                continue;
            }
            Map<String,String[]> parameterMap = req.getParameterMap();
            Map<String, Integer> paramIndexMapping = mapping.getParamIndexMapping();
            Map<String, Class> paramTypeMapping = mapping.getParamTypeMapping();
            // 参数数组
            Object[] args = new Object[paramIndexMapping.size()];

            if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
                Integer index = paramIndexMapping.get(HttpServletRequest.class.getName());
                args[index] = req;
            }

            if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
                Integer index = paramIndexMapping.get(HttpServletResponse.class.getName());
                args[index] = resp;
            }

            Set<Map.Entry<String, Class>> entries = paramTypeMapping.entrySet();
            for (Map.Entry<String, Class> entry : entries) {
                String paramKey = entry.getKey();
                String[] paraValues = parameterMap.get(paramKey);
                Class type = paramTypeMapping.get(paramKey);
                Integer index = paramIndexMapping.get(paramKey);
                convert.convert(paraValues,type,index,args);
            }
            Object result = mapping.getMethod().invoke(mapping.getController(), args);
            resp.getWriter().write(result.toString());
            return;
        }

        resp.getWriter().write("404 NOT FOUND");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }
}
