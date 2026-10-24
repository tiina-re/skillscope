package ee.testiplatvorm.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddResponse {
    // true, kui sama e-postiga kustutatud kasutaja taastati uue loomise asemel
    private Boolean restored;
}
