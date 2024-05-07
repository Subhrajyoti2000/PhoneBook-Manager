package com.org.phonebook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.org.phonebook.dao.UserRepository;
import com.org.phonebook.entity.User;
import com.org.phonebook.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class MainController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	// Home page handler
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - PhoneBook Manager");
		return "home";
	}

	// About page handler
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - PhoneBook Manager");
		return "about";
	}

	// Registration page handler
	@RequestMapping("/signup/")
	public String signup(Model model) {
		model.addAttribute("title", "Signup - PhoneBook Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handler for registering user
	@PostMapping("/do_register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {
		try {
			if (!agreement) {
				throw new Exception("You have not agreed the terms and conditions");
			}
			if (bindingResult.hasErrors()) {
				System.out.println("Error :" + bindingResult.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			
			User result = this.userRepository.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully registered!!", "alert-success"));
			return "signup";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong!!" + e.getMessage(), "alert-danger"));
			return "signup";
		}
	}
	
	//handler for custom login
	@RequestMapping("/login")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
	}
}
