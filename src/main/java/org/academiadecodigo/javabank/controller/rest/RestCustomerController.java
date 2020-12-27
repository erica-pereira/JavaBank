package org.academiadecodigo.javabank.controller.rest;

import org.academiadecodigo.javabank.command.AccountDto;
import org.academiadecodigo.javabank.command.CustomerDto;
import org.academiadecodigo.javabank.converters.AccountToAccountDto;
import org.academiadecodigo.javabank.converters.CustomerDtoToCustomer;
import org.academiadecodigo.javabank.converters.CustomerToCustomerDto;
import org.academiadecodigo.javabank.exceptions.AssociationExistsException;
import org.academiadecodigo.javabank.exceptions.CustomerNotFoundException;
import org.academiadecodigo.javabank.persistence.model.Customer;
import org.academiadecodigo.javabank.persistence.model.account.Account;
import org.academiadecodigo.javabank.services.AccountService;
import org.academiadecodigo.javabank.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller responsible for {@link Customer} related CRUD operations
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/customer")
public class RestCustomerController {

    private CustomerService customerService;
    private AccountService accountService;
    private CustomerDtoToCustomer customerDtoToCustomer;
    private CustomerToCustomerDto customerToCustomerDto;
    private AccountToAccountDto accountToAccountDto;

    /**
     * Sets the customer service
     *
     * @param customerService the customer service to set
     */
    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Sets the account service
     *
     * @param accountService the account service to set
     */
    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Sets the converter for converting between customer DTO and customer model objects
     *
     * @param customerDtoToCustomer the customer DTO to customer converter to set
     */
    @Autowired
    public void setCustomerDtoToCustomer(CustomerDtoToCustomer customerDtoToCustomer) {
        this.customerDtoToCustomer = customerDtoToCustomer;
    }

    /**
     * Sets the converter for converting between customer model objects and customer DTO
     *
     * @param customerToCustomerDto the customer to customer DTO converter to set
     */
    @Autowired
    public void setCustomerToCustomerDto(CustomerToCustomerDto customerToCustomerDto) {
        this.customerToCustomerDto = customerToCustomerDto;
    }

    /**
     * Sets the converter for converting between account customer model and account DTO
     *
     * @param accountToAccountDto the customer to customer DTO converter to set
     */
    @Autowired
    public void setAccountToAccountDto(AccountToAccountDto accountToAccountDto) {
        this.accountToAccountDto = accountToAccountDto;
    }

    /**
     * Retrieves a representation of the list of customers
     *
     * @return the response entity
     */
    @RequestMapping(method = RequestMethod.GET, path = {"/", ""})
    public List<CustomerDto> listCustomers() {
        List<CustomerDto> customerDtos = new ArrayList<>();
        for (Customer customer : customerService.list()) {
            customerDtos.add(customerToCustomerDto.convert(customer));
        }
        return customerDtos;
    }

    /**
     * Retrieves a representation of the given customer
     *
     * @param id the customer id
     * @return the response entity
     */
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<CustomerDto> showCustomer(@PathVariable Integer id) {

        Customer customer = customerService.get(id);

        if (customer == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(customerToCustomerDto.convert(customer), HttpStatus.OK);
    }

    /**
     * Adds a customer
     *
     * @param customerDto          the customer DTO
     * @param bindingResult        the binding result object
     * @param uriComponentsBuilder the uri components builder
     * @return the response entity
     */
    @RequestMapping(method = RequestMethod.POST, path = {"/", ""})
    public ResponseEntity<?> addCustomer(@Valid @RequestBody CustomerDto customerDto, BindingResult bindingResult, UriComponentsBuilder uriComponentsBuilder) {

        if (bindingResult.hasErrors() || customerDto.getId() != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Customer savedCustomer = customerService.save(customerDtoToCustomer.convert(customerDto));

        // get help from the framework building the path for the newly created resource
        UriComponents uriComponents = uriComponentsBuilder.path("/api/customer/" + savedCustomer.getId()).build();

        // set headers with the created path
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * Edits a customer
     *
     * @param customerDto   the customer DTO
     * @param bindingResult the binding result
     * @param id            the customer id
     * @return the response entity
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<CustomerDto> editCustomer(@Valid @RequestBody CustomerDto customerDto, BindingResult bindingResult, @PathVariable Integer id) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (customerDto.getId() != null && customerDto.getId() != id) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (customerService.get(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        customerDto.setId(id);

        Customer savedCustomer = customerService.save(customerDtoToCustomer.convert(customerDto));
        return new ResponseEntity<>(customerToCustomerDto.convert(savedCustomer), HttpStatus.OK);
    }

    /**
     * Deletes a customer
     *
     * @param id the customer id
     * @return the response entity
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<CustomerDto> deleteCustomer(@PathVariable Integer id) {

        try {

            customerService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (AssociationExistsException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        } catch (CustomerNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves a representation of the given customer accounts
     *
     * @param id the customer id
     * @return the response entity
     */
    @RequestMapping(method = RequestMethod.GET, path = "/{id}/account")
    public ResponseEntity<List<AccountDto>> listCustomerAccounts(@PathVariable Integer id) {

        Customer customer = customerService.get(id);

        if (customer == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<AccountDto> accountDtos = new ArrayList<>();
        for (Account account : customer.getAccounts()) {
            accountDtos.add(accountToAccountDto.convert(account));
        }

        return new ResponseEntity<>(accountDtos, HttpStatus.OK);
    }

    /**
     * Retrieves a representation of the customer account
     *
     * @param id  the customer id
     * @param aid the account id
     * @return the response entity
     */
    @RequestMapping(method = RequestMethod.GET, path = "/{id}/account/{aid}")
    public ResponseEntity<AccountDto> showCustomerAccount(@PathVariable Integer id, @PathVariable Integer aid) {

        Account account = accountService.get(aid);
        if (account == null || account.getCustomer() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (account.getCustomer().getId() != id) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(accountToAccountDto.convert(account), HttpStatus.OK);
    }

    /**
     * Deletes an account
     *
     * @param id  the customer id
     * @param aid the account id
     * @return the response entity
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}/account/{aid}")
    public ResponseEntity<AccountDto> deleteAccount(@PathVariable Integer id, @PathVariable Integer aid) {

        Customer customer = customerService.get(id);

        if (customer == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Account account = accountService.get(aid);
        if (account == null || !customer.getAccounts().contains(account)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        customer.removeAccount(account);
        customerService.save(customer);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
