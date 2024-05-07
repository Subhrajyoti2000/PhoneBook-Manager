package com.org.phonebook.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.org.phonebook.dao.ContactRepository;
import com.org.phonebook.dao.UserRepository;
import com.org.phonebook.entity.Contact;
import com.org.phonebook.entity.User;
import com.org.phonebook.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	/*
	 * I'm using @ModelAttribute here because I want to run this method for all
	 * bellow method and this method work for sending the user (method for adding
	 * common data to response)
	 */
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("User Name: " + userName);
		// get the user using userName(Email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("User: " + user);
		model.addAttribute("user", user);
	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile multipartFile, Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file
			if (multipartFile.isEmpty()) {
				// if the file is empty then try our message
				System.out.println("File is empty");
				contact.setImage("null.png");
			} else {
				// file the file to folder and update the name to contact
				contact.setImage(multipartFile.getOriginalFilename());
				File file = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("Added to data base");
			// message success....
			session.setAttribute("message", new Message("Your contact is added!! Add more...", "success"));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			// message error.....
			session.setAttribute("message", new Message("Somthing went wrong!! Try again...", "danger"));
		}
		return "normal/add_contact_form";
	}

	// show contact handler..
	// contact per page = 5[n]
	// current page = 0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Contacts List");

		/*
		 * This is one type to get contacts list User user =
		 * this.userRepository.getUserByUserName(userName); List<Contact> contacts
		 * =user.getContacts();
		 */
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		/*
		 * pageabel: currentPage:-page and Contact per page :- 5
		 */
		Pageable pageable = PageRequest.of(page, 7);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// specific contact details...
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println(cId);
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);

		// check...
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "normal/contact_detail";
	}

	// delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Principal principal, Model model,
			HttpSession session) {
		Optional<Contact> optional = this.contactRepository.findById(cId);
		Contact contact = optional.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		// check..
		// assignment:
		// remove, img, contact.getImage()
		if (user.getId() == contact.getUser().getId()) {
			user.getContacts().remove(contact);
			this.userRepository.save(user);
//			contact.setUser(null);
//			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact deleted succesfully", "success"));
		}
		return "redirect:/user/show-contacts/0";
	}

	// open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model model) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}

	// update contact handler
	@RequestMapping(value = "process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile multipartFile,
			Model model, HttpSession session, Principal principal) {
		try {
			// old contact details
			Contact oldContact = this.contactRepository.findById(contact.getcId()).get();
			// image
			if (!multipartFile.isEmpty()) {
				// file work...
				// rewrite
				// delete old photo
				File deleteFile = new File(new ClassPathResource("static/img").getFile(), oldContact.getImage());
				deleteFile.delete();
				// update new photo
				File file = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(multipartFile.getOriginalFilename());
			} else {
				contact.setImage(oldContact.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact is updated..!", "success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile page");
		return "normal/profile";
	}
}
