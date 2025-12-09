package com.huixun.lms.controller;

import com.huixun.lms.model.User;
import com.huixun.lms.repository.UserRepository;
import com.huixun.lms.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");
        String role = body.getOrDefault("role", "STUDENT");
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) return ResponseEntity.badRequest().body("用户名或密码不能为空");
        if (userRepository.findByUsername(username).isPresent()) return ResponseEntity.badRequest().body("用户名已存在");
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role);
        userRepository.save(u);
        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");
        var opt = userRepository.findByUsername(username);
        if (opt.isPresent() && passwordEncoder.matches(password, opt.get().getPasswordHash())) {
            Map<String, Object> m = new HashMap<>();
            var u = opt.get();
            m.put("token", jwtUtil.generate(u.getUsername(), u.getRole()));
            m.put("role", u.getRole());
            return ResponseEntity.ok(m);
        }
        return ResponseEntity.status(401).body("认证失败");
    }
}
