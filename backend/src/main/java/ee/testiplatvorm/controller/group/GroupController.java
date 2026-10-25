package ee.testiplatvorm.controller.group;

import ee.testiplatvorm.controller.group.dto.GroupResponse;
import ee.testiplatvorm.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/api/groups")
    @Operation(summary = "Tagastab kõik aktiivsed grupid (nt kasutaja lisamise vormi valikute jaoks).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<GroupResponse> findActiveGroups() {
        return groupService.findActiveGroups();
    }

    @GetMapping("/api/groups/managed")
    @Operation(summary = "Tagastab sisseloginud kasutaja hallatavate gruppide id-d (tabelist group_manager).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public List<Integer> findCurrentUserManagedGroupIds() {
        return groupService.findCurrentUserManagedGroupIds();
    }
}
