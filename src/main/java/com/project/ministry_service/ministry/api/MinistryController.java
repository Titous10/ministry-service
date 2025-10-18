package com.project.ministry_service.ministry.api;

import com.project.ministry_service.ministry.api.dto.CreateMinistryRequest;
import com.project.ministry_service.ministry.api.dto.MemberDto;
import com.project.ministry_service.ministry.api.dto.MinistryDto;
import com.project.ministry_service.ministry.application.HierarchyJdbcService;
import com.project.ministry_service.ministry.application.MinistryService;
import com.project.ministry_service.ministry.domain.model.Ministry;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ministries")
@CrossOrigin
public class MinistryController {

    private final MinistryService ministryService;
    private final HierarchyJdbcService hierarchyJdbcService;

    public MinistryController(MinistryService ministryService, HierarchyJdbcService hierarchyJdbcService) {
        this.ministryService = ministryService;
        this.hierarchyJdbcService = hierarchyJdbcService;
    }

    @PostMapping
    public ResponseEntity<Ministry> create(@Valid @RequestBody CreateMinistryRequest req) {
        System.out.println(req);
        Ministry m = ministryService.createMinistry(req);
        return ResponseEntity.ok(m);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ministry> update(@PathVariable UUID id, @RequestBody CreateMinistryRequest req) {
        Ministry m = ministryService.updateMinistry(id, req);
        return ResponseEntity.ok(m);
    }

    @GetMapping
    public ResponseEntity<List<MinistryDto>> search() {
        return ResponseEntity.ok(ministryService.getMinistries());
    }

    @GetMapping("/potential-members")
    public ResponseEntity<List<MemberDto>> potentialMembers(@RequestParam String ageGroup, @RequestParam String gender, @RequestParam String maritalStatus) {
        return ResponseEntity.ok(ministryService.getPotentialMembers(ageGroup, gender, maritalStatus));
    }

    @GetMapping("/{id}/potential-members")
    public ResponseEntity<List<MemberDto>> potentialMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(ministryService.getPotentialMembers(id));
    }

    @GetMapping("/{id}/rebuild-hierarchy")
    public ResponseEntity<String> rebuildHierarchy(@PathVariable UUID id) {
        hierarchyJdbcService.rebuildFullHierarchy(); // full rebuild
        return ResponseEntity.ok("Rebuild scheduled/done");
    }
}
