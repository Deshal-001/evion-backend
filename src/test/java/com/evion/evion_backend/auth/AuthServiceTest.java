// package com.evion.evion_backend.auth;

// import com.evion.evion_backend.auth.dto.LoginRequest;
// import com.evion.evion_backend.auth.dto.RegisterRequest;
// import com.evion.evion_backend.auth.model.Role;
// import com.evion.evion_backend.auth.model.User;
// import com.evion.evion_backend.auth.repository.UserRepository;
// import com.evion.evion_backend.auth.security.JwtService;
// import com.evion.evion_backend.auth.service.AuthService;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.mockito.Mockito;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// import java.util.Optional;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// class AuthServiceTest {

//     private UserRepository userRepository = mock(UserRepository.class);
//     private JwtService jwtService = mock(JwtService.class);
//     private BCryptPasswordEncoder passwordEncoder = mock(BCryptPasswordEncoder.class);

//     private AuthService authService;

//     @BeforeEach
//     void setUp() {
//         authService = new AuthService(userRepository, jwtService, passwordEncoder);
//     }

//     @Test
//     void testRegisterSuccess() {
//         RegisterRequest request = new RegisterRequest("test@ev.com", "pass", Role.USER, "John", "Doe");
//         when(userRepository.findByEmail("test@ev.com")).thenReturn(Optional.empty());
//         when(passwordEncoder.encode("pass")).thenReturn("hashed");
//         when(jwtService.generateToken(anyString(), anyList())).thenReturn("jwt-token");

//         String token = authService.register(request);

//         assertEquals("jwt-token", token);
//         verify(userRepository).save(any(User.class));
//     }

//     @Test
//     void testRegisterEmailTaken() {
//         RegisterRequest request = new RegisterRequest("test@ev.com", "pass", Role.USER, "John", "Doe");
//         when(userRepository.findByEmail("test@ev.com")).thenReturn(Optional.of(new User()));

//         assertThrows(IllegalStateException.class, () -> authService.register(request));
//     }

//     @Test
//     void testLoginSuccess() {
//         LoginRequest request = new LoginRequest("test@ev.com", "pass");
//         User user = User.builder().email("test@ev.com").password("hashed").role(Role.USER).build();
//         when(userRepository.findByEmail("test@ev.com")).thenReturn(Optional.of(user));
//         when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
//         when(jwtService.generateToken(anyString(), anyList())).thenReturn("jwt-token");

//         String token = authService.login(request);

//         assertEquals("jwt-token", token);
//     }

//     @Test
//     void testLoginWrongPassword() {
//         LoginRequest request = new LoginRequest("test@ev.com", "wrong");
//         User user = User.builder().email("test@ev.com").password("hashed").role(Role.USER).build();
//         when(userRepository.findByEmail("test@ev.com")).thenReturn(Optional.of(user));
//         when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

//         assertThrows(IllegalStateException.class, () -> authService.login(request));
//     }

//     @Test
//     void testGetUserByEmail() {
//         User user = User.builder().email("test@ev.com").build();
//         when(userRepository.findByEmail("test@ev.com")).thenReturn(Optional.of(user));

//         User found = authService.getUserByEmail("test@ev.com");
//         assertEquals("test@ev.com", found.getEmail());
//     }

//     @Test
//     void testGetAllUsers() {
//         when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
//         List<User> users = authService.getAllUsers();
//         assertEquals(2, users.size());
//     }

//     @Test
//     void testUpdateUser() {
//         User user = User.builder().id(1L).firstName("Old").lastName("Name").role(Role.USER).build();
//         User updated = User.builder().firstName("New").lastName("Name").role(Role.ADMIN).build();
//         when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//         when(userRepository.save(any(User.class))).thenReturn(user);

//         User result = authService.updateUser(1L, updated);
//         assertEquals("New", result.getFirstName());
//         assertEquals(Role.ADMIN, result.getRole());
//     }

//     @Test
//     void testDeleteUser() {
//         when(userRepository.existsById(1L)).thenReturn(true);
//         authService.deleteUser(1L);
//         verify(userRepository).deleteById(1L);
//     }

//     @Test
//     void testDeleteUserNotFound() {
//         when(userRepository.existsById(2L)).thenReturn(false);
//         assertThrows(IllegalStateException.class, () -> authService.deleteUser(2L));
//     }
// }
