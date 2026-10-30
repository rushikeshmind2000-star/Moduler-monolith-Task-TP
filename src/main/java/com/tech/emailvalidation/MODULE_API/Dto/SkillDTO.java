package com.tech.emailvalidation.MODULE_API.Dto;

public class SkillDTO {

    private String skillName;
    private int experienceYears;

    public SkillDTO() {
    }

    public SkillDTO(String skillName, int experienceYears) {
        this.skillName = skillName;
        this.experienceYears = experienceYears;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }
}
