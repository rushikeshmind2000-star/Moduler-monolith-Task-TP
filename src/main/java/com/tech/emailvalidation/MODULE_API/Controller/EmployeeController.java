package com.tech.emailvalidation.MODULE_API.Controller;

import com.tech.emailvalidation.MODULE_API.Model.Employee;
import com.tech.emailvalidation.MODULE_API.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    @Autowired
    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping({"", "/add"})
    public ResponseEntity<Employee> AddEmp(@RequestBody Employee emp) {
        Employee saved = service.AddEmployee(emp);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping({"", "/allEmployee"})
    public ResponseEntity<List<Employee>> All() {
        return ResponseEntity.ok(service.AllEmployee());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> FindId(@PathVariable(value = "id", required = false) Integer id,
                                           @RequestParam(value = "Emp_id", required = false) Integer empIdParam) {
        int empId = (id != null) ? id : (empIdParam != null ? empIdParam : 0);
        return ResponseEntity.ok(service.FindByID(empId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> UpdateEmp(@PathVariable("id") int id, @RequestBody Employee emp) {
        return ResponseEntity.ok(service.UpdateEmployee(id, emp));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> DeleteEmployees(@PathVariable(value = "id", required = false) Integer id,
                                                  @RequestParam(value = "Emp_id", required = false) Integer empIdParam) {
        int empId = (id != null) ? id : (empIdParam != null ? empIdParam : 0);
        return ResponseEntity.ok(service.DeleteById(empId));
    }
}

