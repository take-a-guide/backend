package br.com.takeaguide.takeaguide.controller;

import static br.com.takeaguide.takeaguide.utils.ResponseUtils.formatResponse;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.takeaguide.takeaguide.dtos.ResponseObject;
import br.com.takeaguide.takeaguide.dtos.account.ChangeUserRequest;
import br.com.takeaguide.takeaguide.dtos.account.ChangeUserResponse;
import br.com.takeaguide.takeaguide.dtos.account.RetrieveUserRequest;
import br.com.takeaguide.takeaguide.dtos.account.RetrieveUsersResponse;
import br.com.takeaguide.takeaguide.dtos.account.CreateUserRequest;
import br.com.takeaguide.takeaguide.dtos.account.CreateUserResponse;
import br.com.takeaguide.takeaguide.dtos.account.DeleteUserRequest;
import br.com.takeaguide.takeaguide.dtos.account.LoginRequest;
import br.com.takeaguide.takeaguide.dtos.account.LoginResponse;
import br.com.takeaguide.takeaguide.dtos.account.UserDto;
import br.com.takeaguide.takeaguide.repositories.mysql.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.GET, RequestMethod.DELETE})
@RequestMapping("/api/v1/take_a_guide/user")
@Tag(
    name = "APIs-TAKE-A-GUIDE: USER",
    description = "CONTAINS ALL USER-ACCOUNT-RELATED ENDPOINTS"
)
public class UserController {

    @Autowired
    private UserRepository UserRepository;

    

@PostMapping("/login")
@Operation(
    summary = "API USED FOR USER LOGIN",
    responses = {
        @ApiResponse(
            responseCode = "200",
            description = "USER SUCCESSFULLY LOGGED IN",
            content = @Content(
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "SOME OF THE REQUEST ITEMS ARE INVALID",
            content = @Content(
                schema = @Schema(implementation = ResponseObject.class)   
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "AN ISSUE OCCURRED ON THE SERVER",
            content = @Content(
                schema = @Schema(implementation = ResponseObject.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "USER NOT FOUND",
            content = @Content(
                schema = @Schema(implementation = ResponseObject.class)
            )
        )
    }
)
public ResponseEntity<ResponseObject> login(
    @io.swagger.v3.oas.annotations.parameters.RequestBody
    @RequestBody LoginRequest request){

    ResponseEntity<ResponseObject> validate = request.validate();

    if(validate != null){
        return validate;
    }

    UserDto userDto = UserRepository.login(request.email(), request.password());

    if(userDto == null){
        return formatResponse(
            HttpStatus.FORBIDDEN, 
            ResponseObject.builder().error("User not found").build()
        );
    }

    String sessionId = "some-generated-session-id";

    ResponseCookie cookie = ResponseCookie.from("user-session", sessionId)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(3600)
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new LoginResponse(userDto, "User successfully logged in"));
}


    @PostMapping("/create")
    @Operation(
        summary = "API USED TO CREATE A NEW USER ACCOUNT",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "USER CREATED SUCCESSFULLY",
                content = @Content(
                    schema = @Schema(implementation = CreateUserResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "409",
                description = "SOME OF THE ITEMS IN THE REQUEST HAVE CONFLICTS",
                content = @Content(
                    schema = @Schema(implementation = ResponseObject.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "SOME OF THE ITEMS IN THE REQUEST ARE INVALID",
                content = @Content(
                    schema = @Schema(implementation = ResponseObject.class)   
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "SOME PROBLEM OCCURRED ON THE SERVER",
                content = @Content(
                    schema = @Schema(implementation = ResponseObject.class)
                )
            )
        }
    )
    public ResponseEntity<ResponseObject> Createaccount(
        @io.swagger.v3.oas.annotations.parameters.RequestBody
        @RequestBody CreateUserRequest request){
        ResponseEntity<ResponseObject> validate = request.validate();

        if(validate != null){

            return validate;

        }

        Integer numberOfOccurrences = UserRepository.checkIfUserIsAllowed(
            request.email(), 
            request.name()
        );

        if(numberOfOccurrences == null){

            return formatResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                ResponseObject.builder().error("Problems").build()
            );

        }

        if(numberOfOccurrences > 0){

            return formatResponse(
                HttpStatus.CONFLICT, 
                ResponseObject.builder().error("User or email already in use").build()
            );

        }

        BigInteger userCpf = UserRepository.insertUser(request);


        return formatResponse(
            HttpStatus.OK, 
            new CreateUserResponse(userCpf, "Success, boss!")
        );

    }

    @PutMapping("/change")
    @Operation(
        summary = "API USED TO CHANGE A USER ACCOUNT",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "USER SUCCESSFULLY CHANGED",
                content = @Content(
                    schema = @Schema(implementation = ChangeUserResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "409",
                description = "SOME OF THE ITEMS DESIRED BY THE USER HAVE A CONFLICT",
                content = @Content(
                    schema = @Schema(implementation = ResponseObject.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "SOME OF THE REQUEST ITEMS ARE INVALID",
                content = @Content(
                    schema = @Schema(implementation = ResponseObject.class)   
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "AN ISSUE OCCURRED ON THE SERVER",
                content = @Content(
                    schema = @Schema(implementation = ResponseObject.class)
                )
            )
        }
    )
    public ResponseEntity<ResponseObject> Changeuser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody
        @RequestBody ChangeUserRequest request){

        ResponseEntity<ResponseObject> validate = request.validate();

        if(validate != null){

            return validate;

        }

        Integer numberOfOccurrences = UserRepository.checkIfUserIsAllowed(
            request.email(), 
            request.name()
        );

        if(numberOfOccurrences == null){

            return formatResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                ResponseObject.builder().error("Problem By MySQL connection").build()
            );

        }

        if(numberOfOccurrences > 0){

            return formatResponse(
                HttpStatus.CONFLICT, 
                ResponseObject.builder().error("User or email already in use").build()
            );

        }
            UserRepository.updateUser(request);

        return formatResponse(
            HttpStatus.OK, 
            new ChangeUserResponse((request.cpf() + ""), "User successfully changed")
        );

    }

    @DeleteMapping("/remove")
    @Operation(
        summary = "API USED TO REMOVE A USER ACCOUNT",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "USER SUCCESSFULLY REMOVED",
                content = @Content(
                    schema = @Schema(implementation = ResponseObject.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "SOME OF THE REQUEST ITEMS ARE INVALID",
                content = @Content(
                    schema = @Schema(implementation = ResponseObject.class)
                )
            )
        }
    )
    public ResponseEntity<ResponseObject> removeAccount(
        @io.swagger.v3.oas.annotations.parameters.RequestBody
        @RequestBody DeleteUserRequest request){

        ResponseEntity<ResponseObject> validate = request.validate();

        if(validate != null){

            return validate;

        }
        
        UserRepository.removeUser(String.valueOf(request.cpf()));

        return formatResponse(
            HttpStatus.OK, 
            ResponseObject.builder().success("User successfully removed").build()
        );

    }

    @PostMapping("/retrieve")
@Operation(
    summary = "API USED TO RETRIEVE USERS",
    responses = {
        @ApiResponse(
            responseCode = "200",
            description = "USER SUCCESSFULLY RETRIEVED",
            content = @Content(
                schema = @Schema(implementation = RetrieveUsersResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "SOME ERROR IN THE REQUEST ITEMS",
            content = @Content(
                schema = @Schema(implementation = ResponseObject.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "NO USER FOUND",
            content = @Content(
                schema = @Schema(implementation = ResponseObject.class)
            )
        )
    }
)

 private boolean validateSession(String sessionId) {
        return UserRepository.isSessionValid(sessionId);
    }

public ResponseEntity<ResponseObject> retrieveUser(
    @CookieValue("user-session") String sessionId,
    @RequestBody RetrieveUserRequest request){

    boolean isSessionValid = validateSession(sessionId);

    if (!isSessionValid) {
        return formatResponse(
            HttpStatus.UNAUTHORIZED, 
            ResponseObject.builder().error("Session expired or invalid").build()
        );
    }

    ResponseEntity<ResponseObject> validate = request.validate();

    if(validate != null){
        return validate;
    }

    List<UserDto> userDtos = null;

    if(request.cpf() != null){
        userDtos = UserRepository.retrieveUserByCpf(request.cpf());
    }

    if(request.email() != null){
        userDtos = UserRepository.retrieveUserByEmail(request.email());
    }

    if(request.name() != null){
        userDtos = UserRepository.retrieveUserByName(request.name());
    }

    if(userDtos == null || userDtos.size() < 1){
        return formatResponse(
            HttpStatus.NOT_FOUND, 
            ResponseObject.builder().error("No user found").build()
        );
    }

    return formatResponse(
        HttpStatus.OK, 
        new RetrieveUsersResponse(userDtos, "User(s) found")
    );
}

 

    
}


