package com.tech.emailvalidation.MODULE_API.Service;

import com.tech.emailvalidation.MODULE_API.GlobalException.EmployeeNotFoundException;
import com.tech.emailvalidation.MODULE_API.GlobalException.InvalidEmployeeDataException;
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

        if(emp.getName()==null || emp.getName().isBlank())
        {
            throw new InvalidEmployeeDataException("Employee name cannot be empty");
        }

        return Repo.save(emp);

    }

    public List<Employee> AllEmployee(){
        return Repo.findAll();
    }

    public Employee FindByID(int Emp_id){
       return Repo.findById(Emp_id).orElseThrow(()->new EmployeeNotFoundException(
               "Employee Not Found"+Emp_id
       ));
    }

    public Employee UpdateEmployee(int empId, Employee updatedEmployee) {
        Employee existing = FindByID(empId);
        if (updatedEmployee.getName() != null && !updatedEmployee.getName().isBlank()) {
            existing.setName(updatedEmployee.getName());
        }
        if (updatedEmployee.getEmail() != null) {
            existing.setEmail(updatedEmployee.getEmail());
        }
        if (updatedEmployee.getAddress() != null) {
            existing.setAddress(updatedEmployee.getAddress());
        }
        if (updatedEmployee.getPackage() > 0) {
            existing.setPackage(updatedEmployee.getPackage());
        }
        return Repo.save(existing);
    }

    public String DeleteById(int Emp_id) {
        Employee emp = FindByID(Emp_id);
        Repo.delete(emp);
        return "Deleted Employee with ID: " + Emp_id;
    }

}

