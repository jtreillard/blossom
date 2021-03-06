package fr.blossom.core.common.mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import com.google.common.reflect.TypeToken;

import fr.blossom.core.common.dto.AbstractDTO;
import fr.blossom.core.common.entity.AbstractEntity;

public abstract class AbstractDTOMapper<E extends AbstractEntity, D extends AbstractDTO> implements DTOMapper<E, D> {
  private final static Logger LOGGER = LoggerFactory.getLogger(AbstractDTOMapper.class);
  private final TypeToken<E> typeToken = new TypeToken<E>(getClass()) {
  };

  public AbstractDTOMapper() {
    LOGGER.info("Creating AbstractDTOMapper for entity class {}", typeToken.getRawType());
  }

  @Override
  public List<D> mapEntities(Collection<E> entities) {
    if (entities == null) {
      return null;
    }
    return entities.stream().map(this::mapEntity).collect(Collectors.toList());
  }

  @Override
  public Page<D> mapEntitiesPage(Page<E> entities) {
    if (entities == null) {
      return null;
    }
    return entities.map(this::mapEntity);
  }

  @Override
  public List<E> mapDtos(Collection<D> dtos) {
    if (dtos == null) {
      return null;
    }
    return dtos.stream().map(this::mapDto).collect(Collectors.toList());
  }

  @Override
  public Page<E> mapDtosPage(Page<D> dtos) {
    if (dtos == null) {
      return null;
    }
    return dtos.map(this::mapDto);
  }

  protected void mapEntityCommonFields(D dto, E entity) {
    dto.setId(entity.getId());
    dto.setCreationDate(entity.getCreationDate());
    dto.setModificationDate(entity.getModificationDate());
    dto.setCreationUser(entity.getCreationUser());
    dto.setModificationUser(entity.getModificationUser());
  }

  protected void mapDtoCommonFields(E entity, D dto) {
    entity.setId(dto.getId());
    entity.setCreationDate(dto.getCreationDate());
    entity.setModificationDate(dto.getModificationDate());
    entity.setCreationUser(dto.getCreationUser());
    entity.setModificationUser(dto.getModificationUser());
  }

  @Override
  public boolean supports(Class<? extends AbstractEntity> delimiter) {
    return delimiter.isAssignableFrom(typeToken.getRawType());
  }
}
