package org.academiadecodigo.javabank.services.mock;

import org.academiadecodigo.javabank.exceptions.CustomerNotFoundException;
import org.academiadecodigo.javabank.exceptions.RecipientNotFoundException;
import org.academiadecodigo.javabank.persistence.model.Customer;
import org.academiadecodigo.javabank.persistence.model.Recipient;
import org.academiadecodigo.javabank.persistence.model.account.Account;
import org.academiadecodigo.javabank.services.AccountService;
import org.academiadecodigo.javabank.services.CustomerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A mock {@link CustomerService} implementation
 */
public class MockCustomerService extends AbstractMockService<Customer> implements CustomerService {

    private AccountService accountService;

    /**
     * Sets the customer service
     *
     * @param accountService the customer service to set
     */
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * @see CustomerService#get(Integer)
     */
    @Override
    public Customer get(Integer id) {
        return modelMap.get(id);
    }

    /**
     * @see CustomerService#getBalance(Integer)
     */
    @Override
    public double getBalance(Integer id) throws CustomerNotFoundException {

        Customer customer = get(id);

        if (customer == null) {
            throw new CustomerNotFoundException();
        }

        List<Account> accounts = customer.getAccounts();

        double balance = 0;

        for (Account account : accounts) {
            balance += account.getBalance();
        }

        return balance;
    }

    /**
     * @see CustomerService#save(Customer)
     */
    @Override
    public Customer save(Customer customer) {

        if (customer.getId() == null) {
            customer.setId(getNextId());
        }

        modelMap.put(customer.getId(), customer);
        return customer;
    }

    /**
     * @see CustomerService#delete(Integer)
     */
    @Override
    public void delete(Integer id) {
        modelMap.remove(id);
    }

    /**
     * @see CustomerService#list()
     */
    @Override
    public List<Customer> list() {
        return new ArrayList<>(modelMap.values());
    }

    /**
     * @see CustomerService#listRecipients(Integer)
     */
    @Override
    public List<Recipient> listRecipients(Integer id) throws CustomerNotFoundException {

        Customer customer = get(id);

        if (customer == null) {
            throw new CustomerNotFoundException();
        }

        return customer.getRecipients();
    }

    /**
     * @see CustomerService#addRecipient(Integer, Recipient)
     */
    @Override
    public void addRecipient(Integer id, Recipient recipient) {

        Customer customer = modelMap.get(id);

        if (accountService.get(recipient.getAccountNumber()) == null ||
                getAccountIds(customer).contains(recipient.getAccountNumber())) {
            return;
        }

        if (recipient.getId() == null) {
            recipient.setId(getNextId());
        }

        customer.addRecipient(recipient);

    }

    /**
     * @see CustomerService#removeRecipient(Integer, Integer)
     */
    @Override
    public void removeRecipient(Integer id, Integer recipientId)
            throws CustomerNotFoundException, RecipientNotFoundException {

        Customer customer = modelMap.get(id);

        if (customer == null) {
            throw new CustomerNotFoundException();
        }

        Recipient recipient = null;

        for (Recipient rcpt : customer.getRecipients()) {
            if (rcpt.getId().equals(recipientId)) {
                recipient = rcpt;
            }
        }

        if (recipient == null || !recipient.getCustomer().getId().equals(id)) {
            throw new RecipientNotFoundException();
        }

        customer.removeRecipient(recipient);
    }

    /**
     * @see CustomerService#addAccount(Integer, Account)
     */
    @Override
    public void addAccount(Integer id, Account account) {
        Customer customer = get(id);
        customer.addAccount(account);
    }

    /**
     * @see CustomerService#closeAccount(Integer, Integer)
     */
    @Override
    public void closeAccount(Integer cid, Integer accountId) {
        Customer customer = modelMap.get(cid);
        Account accountToRemove = null;

        for (Account account : customer.getAccounts()) {
            if (account.getId().equals(accountId)) {
                accountToRemove = account;
            }
        }

        if (accountToRemove != null) {
            customer.removeAccount(accountToRemove);
        }
    }

    private Set<Integer> getAccountIds(Customer customer) {
        Set<Integer> accountIds = new HashSet<>();
        List<Account> accounts = customer.getAccounts();

        for (Account account : accounts) {
            accountIds.add(account.getId());
        }
        return accountIds;
    }

}
