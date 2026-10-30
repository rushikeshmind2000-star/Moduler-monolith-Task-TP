package com.tech.emailvalidation.MODULE_API.Controller;

import com.tech.emailvalidation.MODULE_API.Model.EmployeeProject;
import com.tech.emailvalidation.MODULE_API.Service.EmployeeProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee-projects")
public class EmployeeProjectController {

    private final EmployeeProjectService employeeProjectService;

    @Autowired
    public EmployeeProjectController(EmployeeProjectService employeeProjectService) {
        this.employeeProjectService = employeeProjectService;
    }

    @PostMapping
    public ResponseEntity<EmployeeProject> createEmployeeProject(@RequestBody EmployeeProject employeeProject) {
        EmployeeProject saved = employeeProjectService.addEmployeeProject(employeeProject);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeProject>> getAllEmployeeProjects() {
        return ResponseEntity.ok(employeeProjectService.getAllEmployeeProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeProject> getEmployeeProjectById(@PathVariable("id") int id) {
        return ResponseEntity.ok(employeeProjectService.getEmployeeProjectById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeProject> updateEmployeeProject(@PathVariable("id") int id,
                                                                 @RequestBody EmployeeProject employeeProject) {
        return ResponseEntity.ok(employeeProjectService.updateEmployeeProject(id, employeeProject));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeProject(@PathVariable("id") int id) {
        return ResponseEntity.ok(employeeProjectService.deleteEmployeeProjectById(id));
    }
}
