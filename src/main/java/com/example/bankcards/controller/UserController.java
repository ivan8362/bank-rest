package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure\n";
    }

/*    @GetMapping("/all")
//	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String getAllTheUserDto() {
        return "I am All";
//	        return service.getUserDto();
    }
 */

    @PostMapping("/new")
//    @CrossOrigin("http://localhost:3000/")
//    public String addNewUser(@RequestBody UserInfo userInfo) {
    public String addNewUser(@Valid @RequestBody UserDto user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(user.getUsername());
        userInfo.setPassword(user.getPassword());
        userInfo.setRole(user.getRole());
        return userService.addUser(userInfo);
    }
/*
    @GetMapping("/user-new")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String getUserDtoById() {
        return "I am user";
    }
*/
    @PostMapping("/authenticate")
//    @CrossOrigin("http://localhost:3000/")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        System.out.println("hello"); // todo
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

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        LOGGER.info("Called API DELETE /users/{id}");
        userService.deleteUser(userId);
    }
}
