package com.tech.emailvalidation.MODULE_API.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;

@Entity
public class EmployeeProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int projectId;

    private String projectName;

    private String technology;



    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "emp_id" , nullable = false)
    @JsonBackReference
    private Employee employee;


    public EmployeeProject(int projectId,String projectName,String technology){
        this.projectId=projectId;
        this.projectName=projectName;
        this.technology=technology;
    }


    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public EmployeeProject(){}

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }
}
