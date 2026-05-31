package com.ienrique.ressourceRelationnelle.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ienrique.ressourceRelationnelle.dto.CommentDto;
import com.ienrique.ressourceRelationnelle.entity.Comments;

@Mapper(componentModel = "spring")
public interface CommentsMapper {
  @Mapping(target = "resourceId", source = "resource.resourceId")
  CommentDto toDto(Comments comments);

  List<CommentDto> toDtos(List<Comments> comments);

  @Mapping(target = "parentComment", ignore = true)
  @Mapping(target = "resource", ignore = true)
  @Mapping(target = "moderationReason", ignore = true)
  Comments toEntity(CommentDto commentDto);
}
