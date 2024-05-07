package com.org.phonebook.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.org.phonebook.entity.Contact;
import com.org.phonebook.entity.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	// pagination...
	/*
	 * pageabel: currentPage:-page and Contact per page :- 5
	 */
	@Query("FROM Contact as c WHERE c.user.id = :userId")
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);
	
	// search
	// it's implementation class is given by spring automatically
	public List<Contact> findByNameContainingAndUser(String name, User user);
}
