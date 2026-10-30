package com.tech.emailvalidation.MODULE_API.Repo;

import com.tech.emailvalidation.MODULE_API.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepo extends JpaRepository<Project, Long> {
}
