package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.bankcards.entity.UserInfo;
import com.example.bankcards.repository.UserInfoRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    @Autowired
    private UserInfoRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

//    @Autowired
//    private UserInfoRepository userInfoRepository;


//	 List<UserDto> userDtoList = new ArrayList<>();

//	    @PostConstruct
//	    public void loadUsersFromDB() {
//	    	for (int i = 1; i <= 100; i++) {
//	    	    UserDto userDto = new UserDto();
//	    	    userDto.setId(i);
//	    	    userDto.setName("user " + i);
//	    	    userDtoList.add(userDto);
//	    	}
//	    }


//	    public List<UserDto> getUserDto() { return userDtoList; }

//	    public UserDto getUserDto(int id) {
//	        return userDtoList.stream()
//	                .filter(userDto -> userDto.getId() == id)
//	                .findAny()
//	                .orElseThrow(() -> new RuntimeException("user " + id + " not found"));
//	    }

    public String addUser(UserInfo userInfo) {
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "user added to system ";
    }

    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        return repository.findByName(name)
            .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
}


