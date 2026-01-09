package com.example.bankcards.dto;

import com.example.bankcards.security.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDto {

//    private int id;
    @NotNull
    @Size(min = 6, message = "username must be at least 3 characters long")
    private String username;
    @NotNull
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    @NotNull
    private Role role;

//    public String getName() {
//        return name;
//    }
//    public void setName(String name) {
//        this.name = name;
//    }
//    public int getId() {
//        return id;
//    }
//    public void setId(int id) {
//        this.id = id;
//    }
}
