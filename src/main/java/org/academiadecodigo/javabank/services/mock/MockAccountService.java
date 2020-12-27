package org.academiadecodigo.javabank.services.mock;

import org.academiadecodigo.javabank.exceptions.AccountNotFoundException;
import org.academiadecodigo.javabank.exceptions.CustomerNotFoundException;
import org.academiadecodigo.javabank.exceptions.TransactionInvalidException;
import org.academiadecodigo.javabank.persistence.model.Customer;
import org.academiadecodigo.javabank.persistence.model.account.Account;
import org.academiadecodigo.javabank.persistence.model.account.SavingsAccount;
import org.academiadecodigo.javabank.services.AccountService;
import org.academiadecodigo.javabank.services.CustomerService;

/**
 * A mock {@link AccountService} implementation
 */
public class MockAccountService extends AbstractMockService<Account> implements AccountService {

    private CustomerService customerService;

    /**
     * Sets the customer service
     *
     * @param customerService the customer service to set
     */
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * @see AccountService#get(Integer)
     */
    @Override
    public Account get(Integer id) {
        return modelMap.get(id);
    }

    /**
     * @see AccountService#deposit(Integer, Integer, double)
     */
    @Override
    public void deposit(Integer id, Integer customerId, double amount)
            throws CustomerNotFoundException, AccountNotFoundException, TransactionInvalidException {

        Customer customer = customerService.get(customerId);
        Account account = modelMap.get(id);

        if (customer == null) {
            throw new CustomerNotFoundException();
        }

        if (account == null) {
            throw new AccountNotFoundException();
        }

        if (!account.getCustomer().getId().equals(customerId)) {
            throw new AccountNotFoundException();
        }

        if (!account.canCredit(amount)) {
            throw new TransactionInvalidException();
        }

        for (Account a : customer.getAccounts()) {
            if (a.getId().equals(id)) {
                a.credit(amount);
            }
        }

        account.credit(amount);
    }

    /**
     * @see AccountService#withdraw(Integer, Integer, double)
     */
    @Override
    public void withdraw(Integer id, Integer customerId, double amount)
            throws CustomerNotFoundException, AccountNotFoundException, TransactionInvalidException {

        Customer customer = customerService.get(customerId);
        Account account = get(id);

        if (customer == null) {
            throw new CustomerNotFoundException();
        }

        if (account == null || !account.getCustomer().getId().equals(customerId)) {
            throw new AccountNotFoundException();
        }

        // in UI the user cannot click on Withdraw so this is here just for safety
        if (account instanceof SavingsAccount) {
            throw new TransactionInvalidException();
        }

        // make sure transaction can be performed
        if (!account.canDebit(amount)) {
            throw new TransactionInvalidException();
        }

        for (Account a : customer.getAccounts()) {
            if (a.getId().equals(id)) {
                a.debit(amount);
            }
        }

        account.debit(amount);
    }
}
