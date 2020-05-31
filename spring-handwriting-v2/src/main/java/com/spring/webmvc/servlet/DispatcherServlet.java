package com.spring.webmvc.servlet;

import com.spring.annotation.Controller;
import com.spring.annotation.RequestMapping;
import com.spring.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {

    private final String contextConfigLocation = "contextConfigLocation";

    private final String templateRoot = "templateRoot";

    private List<HandlerMapping> handlerMappings = new ArrayList<>(16);

    private List<ViewResolver> viewResolvers = new ArrayList<>(16);


    /**
     *  spring 中是List,此处为了方便使用Map,直接映射处理器映射器
     */
    private Map<HandlerMapping,HandlerAdapter> handlerAdapters = new ConcurrentHashMap<>(16);


    @Override
    public void init(ServletConfig config) throws ServletException {
        // 初始化applicationContext
        ApplicationContext applicationContext = new ApplicationContext(config.getInitParameter("contextConfigLocation"));
        // 初始化容器后会触发onRefresh方法,进行初始化spring mvc的9大组件
        onRefresh(applicationContext);
    }

    private void onRefresh(ApplicationContext applicationContext) {
        initStrategies(applicationContext);
    }


    private void initStrategies(ApplicationContext context) {

        // 多文件上传的组件
        initMultipartResolver(context);
        // 初始化本地语言环境
        initLocaleResolver(context);
        // 初始化模板处理器
        initThemeResolver(context);

        // 初始化处理器映射器
        initHandlerMappings(context);
        // 初始化参数适配器
        initHandlerAdapters(context);
        // 初始化异常拦截器
        initHandlerExceptionResolvers(context);
        // 初始化视图预处理器
        initRequestToViewNameTranslator(context);
        // 初始化视图解析器
        initViewResolvers(context);
        // 初始化参数缓存管理对象,用于请求重定向时缓存请求中的参数
        initFlashMapManager(context);
    }



    /**
     *  初始化视图解析器
     *          作用: 加载视图解析器中的视图存放目录,用于后面的解析ModelAndView中的视图名称
     * @param context
     */
    private void initViewResolvers(ApplicationContext context) {
        // 初始化视图解析器
        String templateRoot = context.getConfig().getProperty(this.templateRoot);
        String templateRootDir = getDefaultClassLoader().getResource(templateRoot).getFile();

        File[] files = new File(templateRootDir).listFiles();
        for (File file1 : files) {
            viewResolvers.add(new ViewResolver(templateRoot));
        }


    }

    /**
     *  建立处理器映射器与处理器适配器的一对一关系
     * @param context
     */
    private void initHandlerAdapters(ApplicationContext context) {

        for (HandlerMapping handlerMapping : handlerMappings) {

            handlerAdapters.put(handlerMapping,new HandlerAdapter());
        }
    }

    private void initHandlerMappings(ApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            // 类上没有Controller注解
            if(!beanClass.isAnnotationPresent(Controller.class)) continue;

            StringBuilder url = new StringBuilder();
            String baseUrl = beanClass.isAnnotationPresent(RequestMapping.class) ? beanClass.getAnnotation(RequestMapping.class).value() : "";
            if(! "".equals(baseUrl)){
                url.append(checkUrl(baseUrl));
            }

            Method[] methods = beanClass.getMethods();
            if (methods != null && methods.length != 0 ){
                for (Method method : methods) {
                    if( method.isAnnotationPresent(RequestMapping.class)){
                        String value = method.getAnnotation(RequestMapping.class).value();
                        url.append(checkUrl(value));
                        // 添加处理器映射器
                        handlerMappings.add(new HandlerMapping(Pattern.compile(url.toString()),bean,method));
                    }
                }
            }

        }

    }

    private void initRequestToViewNameTranslator(ApplicationContext context) {

    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {

    }

    private void initFlashMapManager(ApplicationContext context) {

    }



    private String checkUrl(String baseUrl) {
        if(baseUrl.startsWith("/") && baseUrl.charAt(1) != '/'){
            return baseUrl;
        }
        baseUrl = ( "/" + baseUrl ) .replaceAll("/+","/").replaceAll("\\*",".*");
        return baseUrl;
    }

    private void initThemeResolver(ApplicationContext context) {


    }

    private void initLocaleResolver(ApplicationContext context) {


    }

    private void initMultipartResolver(ApplicationContext context) {

    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        // 获取处理器映射器
        HandlerMapping handler = getHandler(req);
        if(handler == null){
            resp.getWriter().write("404 NOT FOUND");
            return;
        }

        // 根据处理器映射器获取处理器适配器
        HandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        // 执行方法
        ModelAndView modelAndView = handlerAdapter.handle(req, resp, handler);
        if (modelAndView == null){
            return;
        }

        // 处理结果
        processDispatcherResult(req,resp,modelAndView);

    }


    private void processDispatcherResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView modelAndView) throws Exception {
        String viewName = modelAndView.getView();

        ViewResolver viewResolver = getViewResolver(viewName);
        View view = viewResolver.resolveViewName(modelAndView.getView());
        view.render(modelAndView.getModel(),req,resp);

    }

    private ViewResolver getViewResolver(String viewName) throws Exception {

        return viewResolvers.get(0);
    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {

        return handlerAdapters.get(handler);
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        String url = req.getRequestURI();

        for (HandlerMapping handlerMapping : handlerMappings) {
            // 如果匹配
            if(handlerMapping.getUrl().matcher(url).matches()){
                return handlerMapping;
            }
        }

        return null;
    }


    public ClassLoader getDefaultClassLoader(){
        return this.getClass().getClassLoader();
    }
}
