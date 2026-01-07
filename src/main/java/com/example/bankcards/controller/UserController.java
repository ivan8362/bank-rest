package com.example.bankcards.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

//    @Autowired
    private UserService service;

//    @Autowired
    private JwtService jwtService;

//    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @GetMapping("/all")
//	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String getAllTheUserDto() {
        return "I am All";
//	        return service.getUserDto();
    }

    @PostMapping("/new")
    @CrossOrigin("http://localhost:3000/")
    public String addNewUser(@RequestBody UserInfo userInfo) {
        return service.addUser(userInfo);
    }

    @GetMapping("/user-new")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String getUserDtoById() {
        return "I am user";
    }

    @PostMapping("/authenticate")
//    @CrossOrigin("http://localhost:3000/")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        System.out.println("hello");
        if (authentication.isAuthenticated()) {
//            UserInfo user = new UserInfo();
//            user.setId(null);
//            user.setAuthorities();
//            user.setUsername(authRequest.getUsername());
            return jwtService.generateToken((UserDetails) authentication.getPrincipal());
        } else {
            throw new UsernameNotFoundException("invalid user request!");
        }
    }
}
