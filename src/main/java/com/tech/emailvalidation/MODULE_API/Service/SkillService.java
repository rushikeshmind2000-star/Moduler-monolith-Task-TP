package com.tech.emailvalidation.MODULE_API.Service;

import com.tech.emailvalidation.MODULE_API.GlobalException.ResourceNotFoundException;
import com.tech.emailvalidation.MODULE_API.Model.Skill;
import com.tech.emailvalidation.MODULE_API.Repo.SkillRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepo skillRepo;

    @Autowired
    public SkillService(SkillRepo skillRepo) {
        this.skillRepo = skillRepo;
    }

    public Skill addSkill(Skill skill) {
        return skillRepo.save(skill);
    }

    public List<Skill> getAllSkills() {
        return skillRepo.findAll();
    }

    public Skill getSkillById(int skillId) {
        return skillRepo.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillId));
    }

    public Skill updateSkill(int skillId, Skill updatedSkill) {
        Skill existingSkill = getSkillById(skillId);
        if (updatedSkill.getSkillName() != null) {
            existingSkill.setSkillName(updatedSkill.getSkillName());
        }
        if (updatedSkill.getExperienceYears() > 0) {
            existingSkill.setExperienceYears(updatedSkill.getExperienceYears());
        }
        return skillRepo.save(existingSkill);
    }

    public String deleteSkillById(int skillId) {
        Skill skill = getSkillById(skillId);
        skillRepo.delete(skill);
        return "Deleted Skill with ID: " + skillId;
    }
}
