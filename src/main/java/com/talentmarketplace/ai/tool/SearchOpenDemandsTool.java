package com.talentmarketplace.ai.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.talentmarketplace.ai.ollama.OllamaTool;
import com.talentmarketplace.dto.response.DemandResponse;
import com.talentmarketplace.entity.DemandStatus;
import com.talentmarketplace.service.ProjectDemandService;

@Component
public class SearchOpenDemandsTool implements AiTool {

    private final ProjectDemandService projectDemandService;

    public SearchOpenDemandsTool(ProjectDemandService projectDemandService) {
        this.projectDemandService = projectDemandService;
    }

    @Override
    public String getName() {
        return "search_open_demands";
    }

    @Override
    public String getDescription() {
        return "Search open project demands by optional department, role, or location";
    }

    @Override
    public OllamaTool getOllamaTool() {
        Map<String, Object> props = new HashMap<>();

        Map<String, Object> deptProp = new HashMap<>();
        deptProp.put("type", "string");
        deptProp.put("description", "Optional department filter, e.g. Engineering, Finance, Analytics");
        props.put("department", deptProp);

        Map<String, Object> roleProp = new HashMap<>();
        roleProp.put("type", "string");
        roleProp.put("description", "Optional role keyword, e.g. Java, Python, Frontend, Architect");
        props.put("role", roleProp);

        Map<String, Object> locProp = new HashMap<>();
        locProp.put("type", "string");
        locProp.put("description", "Optional location, e.g. New York, London, Bangalore, Remote");
        props.put("location", locProp);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", props);

        return OllamaTool.function(getName(), getDescription(), parameters);
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        String department = arguments != null && arguments.get("department") != null ? arguments.get("department").toString() : null;
        String role = arguments != null && arguments.get("role") != null ? arguments.get("role").toString() : null;
        String location = arguments != null && arguments.get("location") != null ? arguments.get("location").toString() : null;

        List<DemandResponse> demands = projectDemandService.searchDemands(
                DemandStatus.OPEN, null, role, location, null, null, null
        );

        if (department != null && !department.isBlank()) {
            String deptFilter = department.trim().toLowerCase();
            demands = demands.stream()
                    .filter(d -> d.getDepartment() != null && d.getDepartment().toLowerCase().contains(deptFilter))
                    .collect(Collectors.toList());
        }

        return demands.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("demandId", d.getId());
            map.put("demandCode", d.getDemandCode());
            map.put("role", d.getRole());
            map.put("department", d.getDepartment());
            map.put("location", d.getLocation());
            map.put("allocationPercent", d.getAllocationPercent());
            map.put("startDate", d.getStartDate());
            map.put("endDate", d.getEndDate());
            map.put("status", d.getStatus());
            map.put("priority", d.getPriority());
            if (d.getRequiredSkills() != null) {
                map.put("requiredSkills", d.getRequiredSkills().stream()
                        .map(s -> s.getSkillName() + " (" + s.getMinimumProficiency() + (s.isMandatory() ? ", MANDATORY" : ", OPTIONAL") + ")")
                        .collect(Collectors.toList()));
            }
            return map;
        }).collect(Collectors.toList());
    }
}
