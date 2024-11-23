// package com.CodeEvalCrew.AutoScore.exceptions;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import org.springframework.security.core.userdetails.UsernameNotFoundException;

// @ControllerAdvice
// public class GlobalExceptionHandler {

//     @ExceptionHandler(UsernameNotFoundException.class)
//     public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
//         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
//     }

//     @ExceptionHandler(IllegalStateException.class)
//     public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex) {
//         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
//     }

//     // Thêm các handler cho các loại ngoại lệ khác nếu cần
// }
