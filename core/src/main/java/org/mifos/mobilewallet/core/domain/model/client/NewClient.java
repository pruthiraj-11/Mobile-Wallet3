package org.mifos.mobilewallet.core.domain.model.client;

import org.mifos.mobilewallet.core.utils.DateHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by naman on 20/8/17.
 */

public class NewClient {

    public String fullname;
    public String externalId;
    public String officeId = "1";
    public boolean active = true;
    public String activationDate;
    public List<Address> address = new ArrayList<>();
    public String mobileNo;
    public String dateFormat = "dd MMMM yyyy";
    public String locale = "en";
    public String submittedOnDate;
    public int savingsProductId;
//    List<CustomDataTable> datatables = new ArrayList<>();

    public NewClient(String fullname, String externalId, String addressLine1,
            String addressLine2, String city, String postalCode, String stateProvinceId,
            String countryId, String mobileNo, int mifosSavingsProductId) {
        this.fullname = fullname;
        this.externalId = externalId + "@moneypay";

        address.add(new Address(addressLine1, addressLine2, city, postalCode, stateProvinceId,
                countryId));
        this.mobileNo = mobileNo;

        activationDate = DateHelper.getDateAsStringFromLong(System.currentTimeMillis());
        submittedOnDate = activationDate;
        savingsProductId = mifosSavingsProductId;

//        CustomDataTable dataTable = new CustomDataTable();
//        datatables.add(dataTable);
    }
}

class Address {

    public String addressTypeId = "1"; // office
    public boolean isActive = true;
    public String addressLine1;
    public String addressLine2;
    public String street;
    public String postalCode;
    public String stateProvinceId;
    public String countryId;

    public Address(String addressLine1, String addressLine2, String street, String postalCode,
            String stateProvinceId, String countryId) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.postalCode = postalCode;
        this.stateProvinceId = stateProvinceId;
        this.countryId = countryId;
        this.street = street;
    }
}

class CustomDataTable {
    public String registeredTableName = "client_info";
    public HashMap<String, Object> data;

    public CustomDataTable() {
        data = new HashMap<>();
        data.put("locale", "en");
        data.put("info_id", 1);
    }
}