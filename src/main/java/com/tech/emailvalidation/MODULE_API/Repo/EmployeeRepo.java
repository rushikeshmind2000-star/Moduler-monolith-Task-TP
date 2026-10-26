package com.tech.emailvalidation.MODULE_API.Repo;

import com.tech.emailvalidation.MODULE_API.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepo extends JpaRepository<Employee,Integer> {


}
