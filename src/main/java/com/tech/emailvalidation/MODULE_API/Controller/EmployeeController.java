package com.tech.emailvalidation.MODULE_API.Controller;

import com.tech.emailvalidation.MODULE_API.Model.Employee;
import com.tech.emailvalidation.MODULE_API.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    public EmployeeController(EmployeeService service){
        this.service=service;
    }


    @PostMapping("/add")
    public Employee AddEmp(Employee emp){
        return service.AddEmployee(emp);
    }


    @GetMapping("/allEmployee")
    public List<Employee> All(){
        return service.AllEmployee();
    }

    @GetMapping("/{id}")
    public Employee FindId(@RequestParam("Emp_id")int Emp_id){
        return service.FindByID(Emp_id);
    }

    @DeleteMapping("/{ID}")
    public String DeleteEmployees(@RequestParam("Emp_id")int Emp_id){
        return service.DeleteById(Emp_id);
    }








}
