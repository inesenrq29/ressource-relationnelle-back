package com.ienrique.ressourceRelationnelle.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ienrique.ressourceRelationnelle.dto.ResourceDto;
import com.ienrique.ressourceRelationnelle.entity.Resource;
import com.ienrique.ressourceRelationnelle.entity.Tag;

@Mapper(componentModel = "spring")
public interface ResourceMapper {
  @Mapping(source = "category.categoryId", target = "categoryId")
  @Mapping(source = "category.name", target = "categoryName")
  ResourceDto toDto(Resource resource);

  List<ResourceDto> toDtos(List<Resource> resource);

  default List<String> map(Set<Tag> tags) {
    if (tags == null) return null;
    return tags.stream().map(Tag::getWording).toList();
  }
}
