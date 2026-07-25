package com.heima.model.user.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ProfileUpdateDTO {

    private String username;

    private Date careerStartDate;

    private String careerDirection;

    private String position;

    private String company;

    private String website;

    private String bio;

    private List<Integer> tagIds;
}