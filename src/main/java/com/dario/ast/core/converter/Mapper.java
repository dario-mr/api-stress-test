package com.dario.ast.core.converter;

public interface Mapper<D, E> {

  D toDomain(E entity);

  E toEntity(D domain);

}
