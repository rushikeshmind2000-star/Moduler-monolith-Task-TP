package com.tech.emailvalidation.MODULE_API.Service;

import com.tech.emailvalidation.MODULE_API.Model.Employee;
import com.tech.emailvalidation.MODULE_API.Repo.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepo Repo;

    public EmployeeService(EmployeeRepo Repo){
        this.Repo=Repo;
    }

    public Employee AddEmployee(Employee emp){
       return Repo.save(emp);
    }

    public List<Employee> AllEmployee(){
        return Repo.findAll();
    }

    public Employee FindByID(int Emp_id){
       return Repo.findById(Emp_id).get();
    }

    public String DeleteById(int Emp_id){
        Employee emp=Repo.findById(Emp_id).get();
        Repo.delete(emp);
        return "Delete  ID ";
    }



}
