package ee.testiplatvorm.controller.user;


import ee.testiplatvorm.controller.user.dto.NewUserRequest;
import ee.testiplatvorm.controller.user.dto.UserAddResponse;
import ee.testiplatvorm.controller.user.dto.UserResponse;
import ee.testiplatvorm.controller.user.dto.UserUpdateRequest;
import ee.testiplatvorm.infrastructure.error.ApiError;
import ee.testiplatvorm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/users")
    public List<UserResponse> findAllUsers() {

        List<UserResponse> allUsersResponses = userService.findAllUsers();
        return allUsersResponses;
    }

    @PostMapping("/api/users")
    @Operation(summary = "Lisab uue kasutaja koos parooli ja profiiliga (ees- ja perekonnanimi), valitud rolliga ja aktiivse staatusega (A) ning lisab ta valitud gruppidesse (groupIds, vähemalt üks). ADMIN lisab kõiki, HALDUR ainult KASUTAJA rolliga ja oma hallatavatesse gruppidesse. Kui e-post kuulub kustutatud (I) kasutajale, taastatakse see kasutaja vormi andmetega ja vastuses on restored = true.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Vigane sisend (nt puudub e-post, parool, nimi või roll, e-post on vales vormingus, parool on lühem kui 6 märki), 'errorCode': INCORRECT_INPUT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Selle e-postiga kasutaja on juba olemas, 'message': Selle e-posti aadressiga kasutaja on juba olemas, 'errorCode': EMAIL_ALREADY_EXISTS",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ei leidnud primary keyd 'roleId' või 'groupId' väärtusega ?",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public UserAddResponse addUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        return userService.addUser(newUserRequest);
    }

    @PutMapping("/api/users/{userId}")
    @Operation(summary = "Muudab aktiivse kasutaja andmeid. ADMIN muudab nime, rolli ja gruppe (v.a enda rolli). HALDUR muudab ainult oma hallatavate gruppide liikmesust ning enda ja oma gruppide liikmete nime, roll jäetakse vahele. E-posti ja staatust muuta ei saa.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Vigane sisend (nt puudub nimi või roll, grupid on tühjad), 'errorCode': INCORRECT_INPUT; kasutaja pole aktiivne, 'errorCode': USER_NOT_ACTIVE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Puudub õigus, 'errorCode': NO_PERMISSION_TO_MANAGE_USERS, NO_PERMISSION_TO_MANAGE_GROUP või CANNOT_CHANGE_OWN_ROLE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ei leidnud primary keyd 'userId', 'roleId' või 'groupId' väärtusega ?",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void updateUser(@PathVariable Integer userId, @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        userService.updateUser(userId, userUpdateRequest);
    }

    @PatchMapping("/api/users/{userId}/activate")
    @Operation(summary = "Aktiveerib kustutatud (I) kasutaja: staatus A (registreerimata kasutajal P). Parool, roll ja grupid jäävad samaks. Lubatud ainult ADMIN-ile.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Kasutaja pole kustutatud, 'errorCode': USER_NOT_DELETED",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Puudub õigus, 'errorCode': NO_PERMISSION_TO_MANAGE_USERS",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ei leidnud primary keyd 'userId' väärtusega ?",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void activateUser(@PathVariable Integer userId) {
        userService.activateUser(userId);
    }

    @DeleteMapping("/api/users/{userId}")
    @Operation(summary = "Kustutab kasutaja ja jätab staatusesse inactive tähisega I. Lubatud ainult ADMIN-ile, iseennast kustutada ei saa.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "OK"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Puudub õigus, 'errorCode': NO_PERMISSION_TO_MANAGE_USERS või CANNOT_DELETE_YOURSELF",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ei leidnud primary keyd 'userId' väärtusega ?",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public void deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }
}


