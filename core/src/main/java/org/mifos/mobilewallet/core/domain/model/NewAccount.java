package org.mifos.mobilewallet.core.domain.model;

import java.util.Date;

/**
 * Created by ankur on 25/June/2018
 */

public class NewAccount {

    public int clientId;
    public String productId;
    public Date submittedOnDate;
    public String accountNo;
    public String locale;
    public String dateFormat;

    public NewAccount(int clientId, String accountNo) {
        this.clientId = clientId;
        this.accountNo = accountNo;
    }
}
