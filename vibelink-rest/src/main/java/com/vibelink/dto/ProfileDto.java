package com.vibelink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private String id;
    private String displayName;
    private String email;
    private String country;
    private Integer followers;
    private String imageUrl;
}
