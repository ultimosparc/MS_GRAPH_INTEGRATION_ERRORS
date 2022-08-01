import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.Contact;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.SingleValueLegacyExtendedProperty;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.SingleValueLegacyExtendedPropertyCollectionPage;
import com.microsoft.graph.requests.SingleValueLegacyExtendedPropertyCollectionRequestBuilder;
import okhttp3.Request;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AddFaxNumbersSample implements SampleClass {

    private static final String PRIVATE_FAX_NUMBER = "privateFaxNumber";
    private static final String BUSINESS_FAX_NUMBER = "businessFaxNumber";
    private static final String ACADEMIC_TITLE = "academicTitle";
    private static final String PUBLIC_GUID_STRING = "00020329-0000-0000-c000-000000000046";
    private static final String NAME = "} Name ";

    @Override
    public void run() {
        final String primaryUser = "office365userEmail";
        final String REQUEST_URI = "https://graph.microsoft.com/v1.0/users/"
                + primaryUser
                + "/contacts"
                + "/";

        final GraphServiceClient<okhttp3.Request> graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(getAuthenticationProvider())
                        .buildClient();

        Contact contact = new Contact();
        List<SingleValueLegacyExtendedProperty> pageContents = new ArrayList<>();
        pageContents.add(setProperty(BUSINESS_FAX_NUMBER, "001"));
        pageContents.add(setProperty(PRIVATE_FAX_NUMBER, "002"));
        if (!pageContents.isEmpty()) {
            contact.singleValueExtendedProperties =
                    new SingleValueLegacyExtendedPropertyCollectionPage(pageContents,
                    new SingleValueLegacyExtendedPropertyCollectionRequestBuilder(
                            REQUEST_URI, graphClient, new ArrayList<>()));
        }

        contact.givenName = "Pavel";
        contact.surname = "Bansky";
        LinkedList<EmailAddress> emailAddressesList = new LinkedList<EmailAddress>();
        EmailAddress emailAddresses = new EmailAddress();
        emailAddresses.address = "pavelb@fabrikam.onmicrosoft.com";
        emailAddresses.name = "Pavel Bansky";
        emailAddressesList.add(emailAddresses);
        contact.emailAddresses = emailAddressesList;
        LinkedList<String> businessPhonesList = new LinkedList<String>();
        businessPhonesList.add("+1 732 555 0102");
        contact.businessPhones = businessPhonesList;

        //response to the POST request doesn't contain the extra properties
        contact = graphClient
                .users(primaryUser)
                .contacts()
                .buildRequest()
                .post(contact);

        //to get all properties we need to expand the GET request
        contact = graphClient
                .users(primaryUser)
                .contacts(contact.id)
                .buildRequest()
                .expand(new StringBuilder()
                        .append("singleValueExtendedProperties($filter=id eq 'String {")
                        .append(PUBLIC_GUID_STRING)
                        .append(NAME)
                        .append(PRIVATE_FAX_NUMBER)
                        .append("'")
                        .append(" or id eq 'String {")
                        .append(PUBLIC_GUID_STRING)
                        .append(NAME)
                        .append(BUSINESS_FAX_NUMBER)
                        .append("'")
                        .append(" or id eq 'String {")
                        .append(PUBLIC_GUID_STRING)
                        .append(NAME)
                        .append(ACADEMIC_TITLE)
                        .append("')").toString())
                .get();

    }

    private SingleValueLegacyExtendedProperty setProperty(String propertyName, String propertyValue) {
        final SingleValueLegacyExtendedProperty property = new SingleValueLegacyExtendedProperty();
        property.id = new StringBuilder()
                .append("String {")
                .append(PUBLIC_GUID_STRING)
                .append(NAME)
                .append(propertyName).toString();
        property.value = propertyValue;
        return property;
    }

    @Override
    public IAuthenticationProvider getAuthenticationProvider() {
        return new IAuthenticationProvider() {
            private String hostNameToCheck = "graph";
            @Override
            public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
                CompletableFuture<String> future = new CompletableFuture<>();
                if(requestUrl.getHost().toLowerCase().contains(hostNameToCheck)){
                    future.complete(getToken());
                } else{
                    future.complete(null);
                }
                return future;
            }
        };
    }

    @Override
    public String getToken() {
        return null;
    }

}