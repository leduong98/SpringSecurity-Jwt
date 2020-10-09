package com.example.jwtdemo.domain.services;

import com.example.jwtdemo.app.dtos.LoginRequest;
import com.example.jwtdemo.app.dtos.LoginResponse;
import com.example.jwtdemo.app.dtos.RegisterRequest;
import com.example.jwtdemo.app.dtos.TokenRequest;
import com.example.jwtdemo.domain.configs.JwtTokenProvider;
import com.example.jwtdemo.domain.entities.CustomUserDetails;
import com.example.jwtdemo.domain.entities.User;
import com.example.jwtdemo.domain.entities.types.Role;
import com.example.jwtdemo.domain.entities.types.State;
import com.example.jwtdemo.domain.models.TokenInfo;
import com.example.jwtdemo.domain.utils.Const;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Log4j2
@Service
@Transactional
public class UserService extends BaseService implements UserDetailsService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private CacheManager cacheManager;

    //checkAuthen
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("can't find this email: "+ email);
        }
        return new CustomUserDetails(user);
    }

    //findById
    public UserDetails loadUserById(long id){
        User user = userRepository.findById(id).orElse(null);
       if(user == null){
           throw new UsernameNotFoundException("not found");
       }
       return new CustomUserDetails(user);
    }

    //send mail
    private void sendEmail(String email) {
        String token = generateToken();
        String url = Const.genMailKey(token);
        cacheManager.setValue(token, email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email active account!");
        message.setText("Click url below to active your account: " + url);
        this.emailSender.send(message);
        log.info("Email Sent!");
    }

    //logout
    public  ResponseEntity<?> logout (TokenRequest refreshToken){
        String refreshTokenValue = refreshToken.getRefreshToken();
        cacheManager.deleteTokenValue(refreshTokenValue);
        return new ResponseEntity<>("logout!", HttpStatus.OK);
    }

    //login
    public ResponseEntity<?> login(LoginRequest loginRequest)
    {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if(user.getState().equals(State.NON_ACTIVE)){
            return new ResponseEntity("your acc is not active", HttpStatus.BAD_REQUEST);
        }
        else{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken((CustomUserDetails) authentication.getPrincipal());
            String refreshToken = genRefreshToken();
            cacheManager.setTokenValue(refreshToken, loginRequest.getEmail(),loginRequest.getPassword());
            return ResponseEntity.ok(new LoginResponse(jwt, refreshToken));
        }
    }

    //register
    public ResponseEntity<?> register(RegisterRequest registerRequest){
        boolean exist = userRepository.existsByEmail(registerRequest.getEmail());
        if (exist){
            return new ResponseEntity<>("email have existed!", HttpStatus.BAD_REQUEST);
        }
        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .state(State.NON_ACTIVE).build();
        userRepository.save(user);
        sendEmail(registerRequest.getEmail());
        return new ResponseEntity<>("check your mail to active this acc!", HttpStatus.OK);
    }

    //active acc
    public ResponseEntity<?> changeState(String token){
        String email = cacheManager.getValue(token);
        User user = userRepository.findByEmail(email);
        if (user != null){
            user.setState(State.ACTIVE);
            userRepository.saveAndFlush(user);
        }
        return new ResponseEntity<>("Your acc was actived!", HttpStatus.OK);
    }

//    refreshToken
    public ResponseEntity<?> refreshToken(TokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        TokenInfo tokenInfo = cacheManager.getTokenValue(refreshToken);
        User user = userRepository.findByEmail(tokenInfo.getEmail());
        if (user == null) {
            throw new UsernameNotFoundException("Phien dang nhap het han!");
        } else {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                              tokenInfo.getEmail(),
                            tokenInfo.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken((CustomUserDetails) authentication.getPrincipal());
            cacheManager.deleteTokenValue(refreshToken);
            String newRefreshToken = genRefreshToken();
            cacheManager.setTokenValue(newRefreshToken, tokenInfo.getEmail(), tokenInfo.getPassword());
            return ResponseEntity.ok(new LoginResponse(jwt, newRefreshToken));
        }

    }
}