package org.nenita.organization;

import java.util.UUID;

import javax.validation.Valid;

import org.nenita.organization.domain.Company;
import org.nenita.organization.follow.FollowCompanyInput;
import org.nenita.organization.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationController {

	@Autowired
	private CompanyRepository repo;

	/**
	 * Follow a company
	 * 
	 * @param companyUuid
	 * @param input
	 * @return
	 */
	@RequestMapping(path = "/api/organization/{uuid}/follow", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> follow(@PathVariable String companyUuid, 
			@RequestBody @Valid FollowCompanyInput input) {

		HttpHeaders httpHeaders = new HttpHeaders();
		Company toFollow = repo.findByUuid(companyUuid);
		if (toFollow == null) {
			// How to create common body?
			return new ResponseEntity<>(null, httpHeaders, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
	}
	
	@RequestMapping(path = "/api/organization", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> add(@RequestBody @Valid CompanyInput input) {

		HttpHeaders httpHeaders = new HttpHeaders();
		//httpHeaders.setLocation(ServletUriComponentsBuilder
		//		.fromCurrentRequest().path("/{id}")
		//		.buildAndExpand(result.getId()).toUri());
		//stripePaymentSvc.pay(input.getStripeToken(), input.getAmount());
		Company co = repo.findByName(input.getName());
		System.out.println("Company: " + co);
		return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
	}
}
