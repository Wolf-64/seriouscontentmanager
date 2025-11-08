package com.wlf.app.main.data;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(uses = PropertyMapper.class)
public interface ContentMapper {
    ContentMapper INSTANCE = new ContentMapperImpl();
    ContentModel toGui(ContentEntity entity);
    ContentEntity toEntity(ContentModel guiUser);
    void updateEntityFromGui(ContentModel guiUser, @MappingTarget ContentEntity entity);
}
