package com.xm;

import com.spring.annotation.*;
import com.spring.annotation.config.ScopeType;
import com.spring.webmvc.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
//@LazyInit(true)
@RequestMapping("/hello")
public class HelloWorldController {


    @Autowired
    private TestServiceImpl testService;

    @RequestMapping("test")
    public ModelAndView test(@RequestParam("message")String message, HttpServletRequest request, HttpServletResponse response) {
        testService.hello();
        Map<String,String> map = new HashMap<>();
        map.put("message",message);
        ModelAndView modelAndView = new ModelAndView("test",map);

        return modelAndView;
    }


}
