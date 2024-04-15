package org.mifos.mobilewallet.core.domain.model.user;

import java.util.ArrayList;
import java.util.List;

import static org.mifos.mobilewallet.core.utils.Constants.NEW_USER_ROLE_IDS;


public class NewUser {

    public String username;
    public String firstname;
    public String lastname;
    public String email;
    public String officeId = "1";
    public List<Integer> roles = new ArrayList<>();
    public boolean sendPasswordToEmail = false;
    public boolean isSelfServiceUser = true;
    public String password;
    public String repeatPassword;

    public NewUser() {
    }

    public NewUser(String username, String firstname, String lastname, String email,
                   String password) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.repeatPassword = password;
        roles.addAll(NEW_USER_ROLE_IDS);
    }
}
