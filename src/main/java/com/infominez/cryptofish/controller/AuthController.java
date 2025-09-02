package com.infominez.cryptofish.controller;




import com.infominez.cryptofish.base.BaseResponse;
import com.infominez.cryptofish.config.AppProperties;
import com.infominez.cryptofish.model.User;
import com.infominez.cryptofish.security.TokenProvider;
import com.infominez.cryptofish.security.UserPrincipal;
import com.infominez.cryptofish.utils.JwtUtil;

import com.infominez.cryptofish.wrapperRequest.AuthResponse;
import com.infominez.cryptofish.wrapperRequest.UserRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
@AllArgsConstructor
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    private final AppProperties appProperties;

    @Autowired
    private JwtUtil jwtUtil;

    private final TokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse> login( @RequestBody UserRequest userRequest ) throws Exception{
        BaseResponse response = new BaseResponse();
        try {
            if(userRequest == null){
                response.setStatus(302);
                response.setMessage("userRequest not found");
                return ResponseEntity.status(HttpStatus.FOUND).body(response);
            }
            String username = userRequest.getUserName();
            String password = userRequest.getPassword();
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));

            if (authentication == null || authentication.getPrincipal() == null) {
                response.setUnauthorized().setMessage("Authentication failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }
//            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

            // Fetch the authenticated user
//            if (user == null || user.getId() == null) {
//                response.setUnauthorized().setMessage("UserPrincipal is null or invalid");
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(response);
//            }

            String token = tokenProvider.createToken(authentication);


            // Return the token as part of the BaseResponse
            response.set(HttpStatus.OK.value(), "Login successful", new AuthResponse(token));
            return ResponseEntity.ok(response);



        } catch (Exception e) {
            response.setUnauthorized().setMessage("Invalid login credentials");
            // Handle any authentication failure
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }
}