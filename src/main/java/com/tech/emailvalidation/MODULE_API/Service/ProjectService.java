package com.tech.emailvalidation.MODULE_API.Service;

import com.tech.emailvalidation.MODULE_API.GlobalException.ResourceNotFoundException;
import com.tech.emailvalidation.MODULE_API.Model.Project;
import com.tech.emailvalidation.MODULE_API.Repo.ProjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepo projectRepo;

    @Autowired
    public ProjectService(ProjectRepo projectRepo) {
        this.projectRepo = projectRepo;
    }

    public Project addProject(Project project) {
        return projectRepo.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepo.findAll();
    }

    public Project getProjectById(Long projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
    }

    public Project updateProject(Long projectId, Project updatedProject) {
        Project existingProject = getProjectById(projectId);
        if (updatedProject.getProjectName() != null) {
            existingProject.setProjectName(updatedProject.getProjectName());
        }
        if (updatedProject.getTechnology() != null) {
            existingProject.setTechnology(updatedProject.getTechnology());
        }
        if (updatedProject.getRole() != null) {
            existingProject.setRole(updatedProject.getRole());
        }
        return projectRepo.save(existingProject);
    }

    public String deleteProjectById(Long projectId) {
        Project project = getProjectById(projectId);
        projectRepo.delete(project);
        return "Deleted Project with ID: " + projectId;
    }
}
