package com.tech.emailvalidation.MODULE_API.Dto;

public class ProjectDTO {

    private Long projectId;
    private String projectName;
    private String technology;
    private String role;

    public ProjectDTO() {
    }

    public ProjectDTO(Long projectId, String projectName, String technology, String role) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.technology = technology;
        this.role = role;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
