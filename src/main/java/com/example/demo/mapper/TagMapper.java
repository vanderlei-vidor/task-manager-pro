package com.example.demo.mapper;

import com.example.demo.dto.TagDTO;
import com.example.demo.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TagMapper {

    @Mapping(target = "totalTasks", expression = "java(tag.getTasks() != null ? tag.getTasks().size() : 0)")
    TagDTO toDTO(Tag tag);

    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "id", ignore = true)
    Tag toEntity(TagDTO dto);

    List<TagDTO> toDTOList(List<Tag> tags);
}