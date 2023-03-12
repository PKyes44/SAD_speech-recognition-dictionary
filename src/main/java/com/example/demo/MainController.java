package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    @Autowired
    MainService mainService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("result", new ResponseEntity());
        return "home";
    }
    @GetMapping("/home")
    public String base(Model model) {
        model.addAttribute("result", new ResponseEntity());
        return "home";
    }
    @RequestMapping("/home")
    @ResponseBody
    public ResponseEntity searchCon(Model model, @ModelAttribute WordVO wordDTO) {
        System.out.println("wordDTO.getWord() = " + wordDTO.getWord());
        String keyword = wordDTO.getWord();
        keyword = keyword.trim();
        ResponseEntity result = null;
        if (keyword != null || keyword.isEmpty()) {
            result = mainService.searchMain(keyword);
        }
        System.out.println("result = " + result);
        return result;
    }
}