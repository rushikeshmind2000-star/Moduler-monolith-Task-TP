    package com.tech.emailvalidation.MODULE_API.GlobalException;

    import com.tech.emailvalidation.MODULE_API.Model.Employee;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.HttpStatusCode;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.RestControllerAdvice;

    import java.time.LocalDateTime;
    import java.util.HashMap;
    import java.util.Map;

    @RestControllerAdvice
    public class GlobalExceptionHandler {



        @ExceptionHandler(EmployeeNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleEmployeeNotFound(EmployeeNotFoundException ex){

            Map<String , Object> reponse=new HashMap<>();

            reponse.put("timestamp", LocalDateTime.now());
            reponse.put("Status", HttpStatus.NOT_FOUND.value());
            reponse.put("error","Not found");
            reponse.put("message",ex.getMessage());

            return new ResponseEntity<>(reponse,HttpStatus.NOT_FOUND);
            }

    }
