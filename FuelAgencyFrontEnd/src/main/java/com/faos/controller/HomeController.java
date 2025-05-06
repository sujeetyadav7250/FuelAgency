package com.faos.controller;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

//    @GetMapping("/")
//    public String home() {
//        return "index"; // Thymeleaf will render src/main/resources/templates/index.html
//    }
    
    @GetMapping("/suppliermanage/{id}")
    public String suppliermanage(Model model, @PathVariable Long id) {
        model.addAttribute("userId", id);
        return "suppliermanage"; 
    }
    
    @GetMapping("/cylindermanage/{id}")
    public String cylindermanage(Model model, @PathVariable Long id) {
        model.addAttribute("userId", id);
        return "cylindermanage";
    }
    
    @GetMapping("/reportmanage/{id}")
    public String reportmanage(Model model, @PathVariable Long id) {
        model.addAttribute("userId", id);
        return "reportmanage";
    }
    
    @GetMapping("/contact")
    public String contact(Model model, @PathVariable Long id) {
        return "contact";
    }
    
    
}

