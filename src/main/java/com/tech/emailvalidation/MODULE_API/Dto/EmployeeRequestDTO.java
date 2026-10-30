package com.tech.emailvalidation.MODULE_API.Dto;

public class EmployeeRequestDTO {

    private String Emp_id;
    private String name;
    private String email;
    private double Package;

    public EmployeeRequestDTO(String emp_id, String name, String email, double aPackage) {
        Emp_id = emp_id;
        this.name = name;
        this.email = email;
        Package = aPackage;
    }

    public String getEmp_id() {
        return Emp_id;
    }

    public void setEmp_id(String emp_id) {
        Emp_id = emp_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getPackage() {
        return Package;
    }

    public void setPackage(double aPackage) {
        Package = aPackage;
    }
}
