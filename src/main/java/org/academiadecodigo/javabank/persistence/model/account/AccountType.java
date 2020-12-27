package org.academiadecodigo.javabank.persistence.model.account;

import java.util.Arrays;
import java.util.List;

public enum AccountType {

    CHECKING,
    SAVINGS;

    public static List<AccountType> list() {
        return Arrays.asList(AccountType.values());
    }
}
