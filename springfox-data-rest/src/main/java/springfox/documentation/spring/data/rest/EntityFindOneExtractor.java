/*
 *
 *  Copyright 2017-2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package springfox.documentation.spring.data.rest;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.core.CrudMethods;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.RequestHandler;
import springfox.documentation.service.ResolvedMethodParameter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Collections.*;
import static springfox.documentation.spring.data.rest.RequestExtractionUtils.*;

class EntityFindOneExtractor implements EntityOperationsExtractor {
  @Override
  public List<RequestHandler> extract(EntityContext context) {
    final List<RequestHandler> handlers = new ArrayList<>();
    final PersistentEntity<?, ?> entity = context.entity();
    CrudMethods crudMethods = context.crudMethods();
    TypeResolver resolver = context.getTypeResolver();
    RepositoryMetadata repository = context.getRepositoryMetadata();
    Object getFindOneMethod = crudMethods.getFindOneMethod();
    if (crudMethods.hasFindOneMethod()) {
      OptionalDeferencer<Method> converter = new OptionalDeferencer<>();
      Method actualFindOneMethod = converter.convert(getFindOneMethod);
      HandlerMethod handler = new HandlerMethod(
          context.getRepositoryInstance(),
          actualFindOneMethod);
      ActionSpecification spec = new ActionSpecification(
          actionName(entity, actualFindOneMethod),
          String.format("%s%s/{id}",
              context.basePath(),
              context.resourcePath()),
          singleton(RequestMethod.GET),
          new HashSet<>(),
          new HashSet<>(),
          handler,
          singletonList(new ResolvedMethodParameter(
              0,
              "id",
              pathAnnotations("id", handler),
              resolver.resolve(repository.getIdType()))),
          resolver.resolve(Resource.class, repository.getReturnedDomainClass(handler.getMethod())));
      handlers.add(new SpringDataRestRequestHandler(context, spec));
    }
    return handlers;
  }
}
