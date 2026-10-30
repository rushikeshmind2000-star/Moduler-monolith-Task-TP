package com.tech.emailvalidation.MODULE_API.GlobalException;

public class InvalidEmployeeDataException  extends RuntimeException{

    public InvalidEmployeeDataException(String msg){
        super(msg);
    }

}
