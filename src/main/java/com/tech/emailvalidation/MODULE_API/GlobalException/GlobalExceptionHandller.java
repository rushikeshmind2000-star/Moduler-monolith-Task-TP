    package com.tech.emailvalidation.MODULE_API.GlobalException;

    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.RestControllerAdvice;

    import java.time.LocalDateTime;
    import java.util.HashMap;
    import java.util.Map;

    @RestControllerAdvice
    public class GlobalExceptionHandller {



        @ExceptionHandler(EmployeeNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleEmployeeNotFound(EmployeeNotFoundException ex){

            Map<String , Object> reponse=new HashMap<>();

            reponse.put("timestamp", LocalDateTime.now());
            reponse.put("Status", HttpStatus.NOT_FOUND.value());
            reponse.put("error","Not found");
            reponse.put("message",ex.getMessage());

            return new ResponseEntity<>(reponse,HttpStatus.NOT_FOUND);
            }


            @ExceptionHandler(InvalidEmployeeDataException.class)
            public ResponseEntity<Map<String , Object>> InvalidEmployeeDataExc(InvalidEmployeeDataException ex){

            Map<String, Object> response=new HashMap<>();

            response.put("timestampe",LocalDateTime.now());
            response.put("Status" ,HttpStatus.BAD_REQUEST.value());
            response.put("error","Bad Request");
            response.put("Message" ,ex.getMessage());

            return new ResponseEntity<>(response , HttpStatus.BAD_REQUEST);
            }

            @ExceptionHandler(ResourceNotFoundException.class)
            public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
                Map<String, Object> response = new HashMap<>();
                response.put("timestamp", LocalDateTime.now());
                response.put("Status", HttpStatus.NOT_FOUND.value());
                response.put("error", "Not Found");
                response.put("message", ex.getMessage());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

    }

