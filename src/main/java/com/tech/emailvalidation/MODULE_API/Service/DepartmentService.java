package com.tech.emailvalidation.MODULE_API.Service;

import com.tech.emailvalidation.MODULE_API.GlobalException.ResourceNotFoundException;
import com.tech.emailvalidation.MODULE_API.Model.Department;
import com.tech.emailvalidation.MODULE_API.Repo.DepartmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepo departmentRepo;

    @Autowired
    public DepartmentService(DepartmentRepo departmentRepo) {
        this.departmentRepo = departmentRepo;
    }

    public Department addDepartment(Department department) {
        return departmentRepo.save(department);
    }

    public List<Department> getAllDepartments() {
        return departmentRepo.findAll();
    }

    public Department getDepartmentById(int departmentId) {
        return departmentRepo.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
    }

    public Department updateDepartment(int departmentId, Department updatedDepartment) {
        Department existingDepartment = getDepartmentById(departmentId);
        if (updatedDepartment.getDepartmentName() != null) {
            existingDepartment.setDepartmentName(updatedDepartment.getDepartmentName());
        }
        return departmentRepo.save(existingDepartment);
    }

    public String deleteDepartmentById(int departmentId) {
        Department department = getDepartmentById(departmentId);
        departmentRepo.delete(department);
        return "Deleted Department with ID: " + departmentId;
    }
}
