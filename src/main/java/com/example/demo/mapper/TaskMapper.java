package com.example.demo.mapper;

import com.example.demo.dto.TaskDTO;
import com.example.demo.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para conversão Task ↔ TaskDTO
 * Gera implementação automaticamente em tempo de compilação
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TaskMapper {

    @Mapping(target = "tagIds", expression = "java(mapTagsToIds(task.getTags()))")
    @Mapping(target = "tagNames", expression = "java(mapTagsToNames(task.getTags()))")
    @Mapping(target = "atrasada", expression = "java(task.isAtrasada())")
    @Mapping(target = "concluida", expression = "java(task.isConcluida())")
    @Mapping(target = "venceHoje", expression = "java(task.isVenceHoje())")
    @Mapping(target = "venceEmBreve", expression = "java(task.isVenceEmBreve())")
    @Mapping(target = "diasRestantes", expression = "java(task.getDiasRestantes())")
    TaskDTO toDTO(Task task);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    Task toEntity(TaskDTO dto);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    void updateEntityFromDTO(TaskDTO dto, @MappingTarget Task task);

    List<TaskDTO> toDTOList(List<Task> tasks);

    // Métodos auxiliares
    default Set<Long> mapTagsToIds(Set<com.example.demo.model.Tag> tags) {
        if (tags == null) return null;
        return tags.stream().map(com.example.demo.model.Tag::getId).collect(Collectors.toSet());
    }

    default Set<String> mapTagsToNames(Set<com.example.demo.model.Tag> tags) {
        if (tags == null) return null;
        return tags.stream().map(com.example.demo.model.Tag::getName).collect(Collectors.toSet());
    }
}