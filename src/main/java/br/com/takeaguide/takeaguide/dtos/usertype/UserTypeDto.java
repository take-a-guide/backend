package br.com.takeaguide.takeaguide.dtos.usertype;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "user_type"
)
public record UserTypeDto(
    
    long id,
    String name

){}
