package com.heima.model.user.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserProfileVO {

    private Long userId;

    private String username;

    private String avatarUrl;

    private Date careerStartDate;

    private String careerDirection;

    private String position;

    private String company;

    private String website;

    private String bio;

    private List<Integer> selectedTagIds;

    private List<TagGroupVO> tagGroups;
}