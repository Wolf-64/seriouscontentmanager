package com.wlf.app.main.data;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(uses = PropertyMapper.class)
public interface ContentMapper {
    ContentMapper INSTANCE = Mappers.getMapper(ContentMapper.class);
    ContentModel toGui(ContentEntity entity);
    ContentEntity toEntity(ContentModel guiUser);
    void updateEntityFromGui(ContentModel guiUser, @MappingTarget ContentEntity entity);
}
