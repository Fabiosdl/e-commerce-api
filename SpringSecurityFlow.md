Spring Security Login Flow Explained 🚀
Your Spring Security setup follows a session-based authentication approach, where Spring Security manages user authentication and session persistence. Below is the step-by-step flow of how login works in your application.

1️⃣ React Frontend Sends Login Request
Your React frontend collects email and password, then sends a POST request with the form-urlencoded format.

React Code

const formData = new URLSearchParams();
formData.append('username', credentials.email); // "username" even though it's an email
formData.append('password', credentials.password);

const response = await fetch('http://localhost:8080/login', {
method: 'POST',
headers: {
'Content-Type': 'application/x-www-form-urlencoded',
'X-CSRF-TOKEN': getCookie('XSRF-TOKEN') // CSRF token handling
},
body: formData.toString()
});
💡 Important Notes:

username is used instead of email because Spring Security's default UsernamePasswordAuthenticationFilter expects "username".
The request is sent to /login (handled by Spring Security).
CSRF token is included for security.

2️⃣ Spring Security Handles Authentication (/login)
Your Spring Security filter chain intercepts the request. The AuthenticationManager and CustomUserDetailsService process the credentials.

🔹 LoginController (Entry Point)

@PostMapping
public ResponseEntity<String> loginUser(@RequestParam String email,
@RequestParam String password) {
try {
Authentication authentication = authenticationManager.authenticate(
new UsernamePasswordAuthenticationToken(email, password)
);
return ResponseEntity.ok("User logged in successfully.");
} catch (Exception e) {
return ResponseEntity.status(401).body("Invalid credentials.");
}
}
💡 Why is it working with email?

Spring Security automatically maps username to email if your database and UserDetailsService expect email as the unique identifier.
3️⃣ AuthenticationManager Calls Custom User Details Service
The AuthenticationManager calls the CustomUserDetailsService to retrieve user details.

🔹 CustomUserDetailsService

@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
Optional<User> optional = userRepository.findByEmail(email);
if (optional.isEmpty())
throw new NotFoundException(String.format("User with email %s not found.", email));

    User user = optional.get();

    return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                    .toList()
    );
}
✅ This loads the user from the database using email as the username.
✅ If the user exists, Spring Security compares passwords.

4️⃣ Password Verification & Authentication
Spring Security retrieves the stored hashed password from the database.
It compares the hashed password with the one entered (using BCryptPasswordEncoder).
If the passwords match, authentication is successful!
If authentication fails, an error message is returned (401 Unauthorized).
🔹 AppSecurity (Password Encoder)

@Bean
public BCryptPasswordEncoder passwordEncoder() {
return new BCryptPasswordEncoder();
}
5️⃣ Success Handling & Session Management
After successful authentication, the CustomAuthenticationSuccessHandler takes over.

🔹 CustomAuthenticationSuccessHandler

@Override
@Transactional
public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
throws IOException, ServletException {

    String userName = authentication.getName();
    log.info("User {} has been successfully authenticated", userName);

    Optional<User> optional = userRepository.findByEmail(userName);
    if (optional.isEmpty()) {
        throw new NotFoundException(String.format("User with email %s not found.", userName));
    }
    User theUser = optional.get();
    Long userId = theUser.getId();

    // Store user in session
    HttpSession session = request.getSession();
    session.setAttribute("user", theUser);

    // Create a basket for customers
    boolean isCustomer = theUser.getRoles().stream()
            .anyMatch(role -> role.getName().toString().equalsIgnoreCase("ROLE_CUSTOMER"));
    if (isCustomer) {
        basketService.createBasketAndAddToUser(theUser);
    }

    // Return response to frontend
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);

    String jsonResponse = String.format(
            "{\"message\": \"Login successful\", \"userId\": %d, \"role\": \"%s\"}",
            userId,
            isCustomer ? "CUSTOMER" : "ADMIN"
    );

    response.getWriter().write(jsonResponse);
    response.getWriter().flush();
}
✅ Stores user in session (session.setAttribute("user", theUser))
✅ Redirects or sends a JSON response back to React
✅ Creates a shopping basket for customers

6️⃣ React Receives Response & Redirects User
React gets the JSON response from the server and stores user details:

if (response.ok) {
const data = await response.json();
const { role, userId } = data;

    // Store user details
    localStorage.setItem('user', JSON.stringify({ userId, role }));

    // Redirect based on role
    if (role === 'CUSTOMER') {
        navigate('/customer-dashboard');
    } else {
        navigate('/admin-dashboard');
    }
}
✅ User is redirected to the appropriate dashboard (CUSTOMER or ADMIN).
✅ Session is maintained by Spring Security.

7️⃣ Role-Based Authorization & Protected Routes
After login, users can only access pages based on their roles.

🔹 AppSecurity (Role-based Access Control)
java
Copy
Edit
.authorizeHttpRequests(authorizeRequests ->
authorizeRequests
.requestMatchers("/swagger-ui/**","/v3/api-docs/**").permitAll()
.requestMatchers("/user/**").hasRole("CUSTOMER")
.requestMatchers("/admin/**").hasRole("ADMIN")
.requestMatchers("/register/**", "/login", "/authenticate").permitAll()
.anyRequest().authenticated()
)
✅ CUSTOMER can access /user/**
✅ ADMIN can access /admin/**
✅ Unauthenticated users are redirected to /login

🔎 Summary of Spring Security Login Flow
1️⃣ React sends credentials (username=email).
2️⃣ Spring Security intercepts /login and calls AuthenticationManager.
3️⃣ CustomUserDetailsService loads user by email.
4️⃣ Password is verified with BCryptPasswordEncoder.
5️⃣ On success, CustomAuthenticationSuccessHandler sends JSON response.
6️⃣ React stores user info & redirects based on role.
7️⃣ Session-based authentication ensures protected routes.