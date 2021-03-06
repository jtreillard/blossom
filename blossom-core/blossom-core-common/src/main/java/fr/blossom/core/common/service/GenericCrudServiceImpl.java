package fr.blossom.core.common.service;

import fr.blossom.core.common.dao.CrudDao;
import fr.blossom.core.common.dto.AbstractDTO;
import fr.blossom.core.common.entity.AbstractEntity;
import fr.blossom.core.common.event.BeforeDeletedEvent;
import fr.blossom.core.common.event.CreatedEvent;
import fr.blossom.core.common.event.DeletedEvent;
import fr.blossom.core.common.event.UpdatedEvent;
import fr.blossom.core.common.mapper.DTOMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GenericCrudServiceImpl<DTO extends AbstractDTO, ENTITY extends AbstractEntity> extends GenericReadOnlyServiceImpl<DTO, ENTITY> implements CrudService<DTO> {
  protected final CrudDao<ENTITY> dao;
  protected final ApplicationEventPublisher publisher;

  protected GenericCrudServiceImpl(CrudDao<ENTITY> dao, DTOMapper<ENTITY, DTO> mapper, ApplicationEventPublisher publisher) {
    super(dao, mapper);
    this.dao = dao;
    this.publisher = publisher;
  }

  @Override
  @Transactional
  public DTO create(DTO toCreate) {
    ENTITY entity = this.mapper.mapDto(toCreate);
    DTO dto = this.mapper.mapEntity(this.dao.create(entity));
    this.publisher.publishEvent(new CreatedEvent<DTO>(this, dto));
    return dto;
  }

  @Override
  @Transactional
  public void delete(DTO toDelete) {
    this.publisher.publishEvent(new BeforeDeletedEvent<DTO>(this, toDelete));
    this.dao.delete(this.mapper.mapDto(toDelete));
    this.publisher.publishEvent(new DeletedEvent<DTO>(this, toDelete));
  }

  @Override
  @Transactional
  public DTO update(long id, DTO toUpdate) {

    ENTITY modifiedEntity = this.mapper.mapDto(toUpdate);

    ENTITY entity = this.dao.update(id, modifiedEntity);

    DTO dto = this.mapper.mapEntity(entity);

    this.publisher.publishEvent(new UpdatedEvent<DTO>(this, dto));

    return dto;
  }

  @Override
  @Transactional
  public List<DTO> create(Collection<DTO> toCreates) {
    List<DTO> dtos = this.mapper.mapEntities(this.dao.create(this.mapper.mapDtos(toCreates)));
    dtos.forEach(dto -> this.publisher.publishEvent(new CreatedEvent<DTO>(this, dto)));
    return dtos;
  }

  @Override
  @Transactional
  public List<DTO> update(Collection<DTO> toUpdates) {
    Map<Long, ENTITY> toUpdatesEntities = this.mapper.mapDtos(toUpdates).stream().collect(Collectors.toMap(entity -> entity.getId(), Function.identity()));

    List<DTO> dtos = this.mapper.mapEntities(this.dao.update(toUpdatesEntities));
    dtos.forEach(dto -> this.publisher.publishEvent(new UpdatedEvent<DTO>(this, dto)));
    return dtos;
  }
}
