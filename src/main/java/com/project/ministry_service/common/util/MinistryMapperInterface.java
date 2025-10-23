package com.project.ministry_service.common.util;

//@Mapper(componentModel = "spring")
public interface MinistryMapperInterface {
    // ------------------ ENTITY → DTO ------------------

  /*  @Mapping(target = "id", expression = "java(ministry.getId() != null ? ministry.getId().toString() : null)")
    @Mapping(target = "info.name", expression = "java(ministry.getName())")
    @Mapping(target = "info.type", expression = "java(ministry.getType())")
    @Mapping(target = "info.startDate", source = "termStart")
    @Mapping(target = "info.endDate", source = "termEnd")
    @Mapping(target = "info.anniversaryDate", source = "establishedDate")
    @Mapping(target = "info.status", expression = "java(ministry.isActive() ? \"active\" : \"inactive\")")
    //@Mapping(target = "info.status", source = "active")
    @Mapping(target = "info.direction", source = "parentId")
    @Mapping(target = "configuration", source = "criteria")
    @Mapping(target = "ministryMember", ignore = true)// optional: fill later
    MinistryDto toDto(Ministry ministry);

    List<MinistryDto> toDtoList(List<Ministry> ministries);


    // ------------------ DTO → ENTITY ------------------

    @Mapping(target = "id", ignore = true) // ignore for create
    @Mapping(target = "name", source = "info.name")
    @Mapping(target = "type", source = "info.type")
    @Mapping(target = "establishedDate", source = "info.anniversaryDate")
    @Mapping(target = "termStart", source = "info.startDate")
    @Mapping(target = "termEnd", source = "info.endDate")
    @Mapping(target = "criteria", source = "configuration")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "active", constant = "true")
    Ministry toEntity(MinistryDto dto);

    // same but for update, we keep id
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "id", expression = "java(dto.getId() != null ? java.util.UUID.fromString(dto.getId()) : null)")
    Ministry toEntityWithId(MinistryDto dto);*/
}