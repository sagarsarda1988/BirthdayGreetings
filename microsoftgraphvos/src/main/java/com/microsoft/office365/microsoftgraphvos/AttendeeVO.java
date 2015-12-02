package com.microsoft.office365.microsoftgraphvos;

public class AttendeeVO {

    public static final String TYPE_REQUIRED = "Required";
    public static final String TYPE_OPTIONAL = "Optional";
    public static final String TYPE_RESOURCE = "Resource";

    public String type;
    public EmailAddressVO emailAddress;

}
