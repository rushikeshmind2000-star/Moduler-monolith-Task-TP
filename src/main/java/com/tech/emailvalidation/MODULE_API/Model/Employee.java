package com.tech.emailvalidation.MODULE_API.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Emp_id;

    private String Name;

    private String Email;

    private String Address;

    private double Package;

    @OneToMany( mappedBy = "employee" , cascade = CascadeType.ALL , orphanRemoval = true)
    @JsonManagedReference
    private List<EmployeeProject> projects;


    public Employee(int Emp_id, String Name , String Email, String Address, double Package){

        this.Emp_id=Emp_id;
        this.Name=Name;
        this.Address=Address;
        this.Email=Email;
        this.Package=Package;
    }


    public List<EmployeeProject> getProjects() {
        return projects;
    }

    public void setProjects(List<EmployeeProject> projects) {
        this.projects = projects;
    }

    public Employee(){}

    public int getEmp_id() {
        return Emp_id;
    }

    public void setEmp_id(int emp_id) {
        Emp_id = emp_id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public double getPackage() {
        return Package;
    }

    public void setPackage(double aPackage) {
        Package = aPackage;
    }
}
