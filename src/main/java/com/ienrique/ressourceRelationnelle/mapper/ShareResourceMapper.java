package com.ienrique.ressourceRelationnelle.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;

import com.ienrique.ressourceRelationnelle.dto.SharedResourceDto;
import com.ienrique.ressourceRelationnelle.entity.ShareResource;
import com.ienrique.ressourceRelationnelle.entity.Tag;

@Mapper(
    componentModel = "spring",
    uses = {ResourceMapper.class, UserMapper.class})
public interface ShareResourceMapper {
  SharedResourceDto toDto(ShareResource shareResource);

  default List<String> map(Set<Tag> tags) {
    if (tags == null) return null;
    return tags.stream().map(Tag::getWording).toList();
  }
}
