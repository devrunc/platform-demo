package cn.running.demo.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


/**
 * TestController
 *
 * @Author hao cheng
 * @Date 2023/2/21 20:29
 */
@Slf4j
@Controller
@AllArgsConstructor
public class TestController {

    @GetMapping("/")
    public ModelAndView test() {
        ModelAndView model = new ModelAndView();
        // 返回给页面的数据
        model.addObject("data", "Welcome here.");
//        // 创建文件
//        File file = FileUtil.file("/tmp/test.txt");
//        // 向文件写入内容
//        FileUtil.writeUtf8String("测试123123123123123...", file);
        model.setViewName("index");
        return model;
    }
}
