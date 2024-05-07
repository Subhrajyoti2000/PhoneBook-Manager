package com.org.phonebook.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.org.phonebook.dao.ContactRepository;
import com.org.phonebook.dao.UserRepository;
import com.org.phonebook.entity.Contact;
import com.org.phonebook.entity.User;

@RestController
public class SearchController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	// search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal){
		System.out.println(query);
		User user = this.userRepository.getUserByUserName(principal.getName());
		List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);
		return ResponseEntity.ok(contacts);
	}
}
