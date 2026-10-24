package ee.testiplatvorm.controller.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @NotBlank
    @Size(max = 255)
    private String firstName;

    @NotBlank
    @Size(max = 255)
    private String lastName;

    @NotNull
    private Integer roleId;

    @NotEmpty
    private List<@NotNull Integer> groupIds = new ArrayList<>();
}
