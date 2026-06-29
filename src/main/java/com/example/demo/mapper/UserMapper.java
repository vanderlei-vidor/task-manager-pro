package com.example.demo.mapper;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "dataCadastro", source = "dataCriacao")
    @Mapping(target = "totalTasks", expression = "java(usuario.getTarefas() != null ? usuario.getTarefas().size() : 0)")
    UserProfileDTO toProfileDTO(Usuario usuario);
}