package com.tech.emailvalidation.MODULE_API.Service;

import com.tech.emailvalidation.MODULE_API.GlobalException.ResourceNotFoundException;
import com.tech.emailvalidation.MODULE_API.Model.Employee;
import com.tech.emailvalidation.MODULE_API.Model.EmployeeProject;
import com.tech.emailvalidation.MODULE_API.Repo.EmployeeProjectRepo;
import com.tech.emailvalidation.MODULE_API.Repo.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeProjectService {

    private final EmployeeProjectRepo employeeProjectRepo;
    private final EmployeeRepo employeeRepo;

    @Autowired
    public EmployeeProjectService(EmployeeProjectRepo employeeProjectRepo, EmployeeRepo employeeRepo) {
        this.employeeProjectRepo = employeeProjectRepo;
        this.employeeRepo = employeeRepo;
    }

    public EmployeeProject addEmployeeProject(EmployeeProject employeeProject) {
        if (employeeProject.getEmployee() != null && employeeProject.getEmployee().getEmp_id() > 0) {
            int empId = employeeProject.getEmployee().getEmp_id();
            Employee employee = employeeRepo.findById(empId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + empId));
            employeeProject.setEmployee(employee);
        }
        return employeeProjectRepo.save(employeeProject);
    }

    public List<EmployeeProject> getAllEmployeeProjects() {
        return employeeProjectRepo.findAll();
    }

    public EmployeeProject getEmployeeProjectById(int projectId) {
        return employeeProjectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeProject not found with ID: " + projectId));
    }

    public EmployeeProject updateEmployeeProject(int projectId, EmployeeProject updatedEmployeeProject) {
        EmployeeProject existing = getEmployeeProjectById(projectId);
        if (updatedEmployeeProject.getProjectName() != null) {
            existing.setProjectName(updatedEmployeeProject.getProjectName());
        }
        if (updatedEmployeeProject.getTechnology() != null) {
            existing.setTechnology(updatedEmployeeProject.getTechnology());
        }
        if (updatedEmployeeProject.getEmployee() != null && updatedEmployeeProject.getEmployee().getEmp_id() > 0) {
            int empId = updatedEmployeeProject.getEmployee().getEmp_id();
            Employee employee = employeeRepo.findById(empId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + empId));
            existing.setEmployee(employee);
        }
        return employeeProjectRepo.save(existing);
    }

    public String deleteEmployeeProjectById(int projectId) {
        EmployeeProject employeeProject = getEmployeeProjectById(projectId);
        employeeProjectRepo.delete(employeeProject);
        return "Deleted EmployeeProject with ID: " + projectId;
    }
}
